package org.coredata.core.datastream.blueprint.actor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.coredata.core.datastream.blueprint.vo.BluePrint;
import org.coredata.core.datastream.blueprint.vo.DataWarehouseFragment;
import org.coredata.core.datastream.blueprint.vo.EntityFragment;
import org.coredata.core.util.actor.config.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.Done;
import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.kafka.ConsumerMessage.CommittableMessage;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.kafka.javadsl.Consumer.Control;
import akka.pattern.PatternsCS;
import akka.routing.RoundRobinPool;
import akka.stream.ActorMaterializer;
import akka.stream.ClosedShape;
import akka.stream.Outlet;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.Timeout;
import scala.concurrent.duration.Duration;

@Component
@Scope("prototype")
public class BluePrintActor extends AbstractActor {

	public enum Cmd {
		Init, Cleanup, ShowChildren
	}

	@Value("${spring.kafka.bootstrap-servers}")
	private String kafkaAddr;

	@Value("${spring.kafka.consumer.group-id}")
	private String groupId;

	@Value("${spring.kafka.consumer.auto-offset-reset}")
	private String offsetRest;

	@Value("${spring.kafka.consumer.enable-auto-commit}")
	private String autoCommit;

	@Value("${spring.kafka.topics.data_import}")
	private String topic;

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private SpringExtension springExtension;

	private int parallelism = 5;

	private BluePrint bluePrint;

	private Timeout askTimeout = Timeout.apply(10, TimeUnit.SECONDS);

	private ObjectMapper mapper = new ObjectMapper();

	private SupervisorStrategy strategy = new OneForOneStrategy(5, Duration.create(30, TimeUnit.SECONDS),
			Collections.<Class<? extends Throwable>> singletonList(Exception.class));

	public BluePrintActor(BluePrint bluePrint) {
		this.bluePrint = bluePrint;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(Cmd.class, cmd -> {

			switch (cmd) {
			case Init:
				init();
				break;
			case Cleanup:
				getContext().stop(getSelf());
				break;
			case ShowChildren:
				List<String> paths = new ArrayList<>();
				getContext().getChildren().forEach(child -> {
					paths.add(child.path().toString());
				});
				getSender().tell(paths, getSelf());
				return;
			}

			getSender().tell(Done.getInstance(), getSelf());

		}).build();
	}

	private boolean jobIdFilter(String jobId, String record) {
		try {
			JsonNode json = mapper.readTree(record);
			if (jobId.equals(json.get("jobId").asText())) {
				return true;
			}
		} catch (IOException e) {
			;
		}
		return false;
	}

	private void init() {

		ActorMaterializer materializer = ActorMaterializer.create(getContext());

		bluePrint.setMaterializer(materializer);

		ConsumerSettings<String, String> consumerSettings = ConsumerSettings.create(actorSystem, new StringDeserializer(), new StringDeserializer())
				.withBootstrapServers(kafkaAddr).withGroupId(groupId).withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit)
				.withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "3000").withProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
				.withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetRest);

		Source<CommittableMessage<String, String>, Control> in = Consumer.committableSource(consumerSettings, Subscriptions.topics(topic));

		Sink<Object, CompletionStage<Done>> sink = Sink.ignore();

		bluePrint.setGraph(RunnableGraph.fromGraph(GraphDSL.create(in.map(msg -> msg.record().value()).filter(msg -> {
			return isValidJSON(msg);
		}), (builder, src) -> {

			final UniformFanOutShape<String, String> bcast = builder.add(Broadcast.create(bluePrint.getFlows()));

			final Outlet<String> source = src.out();

			boolean firstPath = true;

			for (DataWarehouseFragment fragment : bluePrint.getDwFragments()) {

				ActorRef dwGenerator = getContext().actorOf(new RoundRobinPool(parallelism).withSupervisorStrategy(strategy)
						.props(springExtension.props("dataWarehouseGenerator", fragment, bluePrint.getToken())));
				getContext().watch(dwGenerator);

				Flow<String, Object, NotUsed> dwGen = Flow.of(String.class).filter(msg -> jobIdFilter(fragment.getModel().getJobId(), msg))
						.mapAsyncUnordered(parallelism, msg -> PatternsCS.ask(dwGenerator, msg, askTimeout));

				if (firstPath) {
					builder.from(source).viaFanOut(bcast).via(builder.add(dwGen)).to(builder.add(sink));
					firstPath = false;
				} else {
					builder.from(bcast).via(builder.add(dwGen)).to(builder.add(sink));
				}

			}

			for (EntityFragment fragment : bluePrint.getEntityFragments()) {

				ActorRef entityGenerator = getContext().actorOf(new RoundRobinPool(parallelism).withSupervisorStrategy(strategy)
						.props(springExtension.props("entityGenerator", fragment, bluePrint.getToken())));
				getContext().watch(entityGenerator);

				Flow<String, Object, NotUsed> entityGen = null;

				switch (fragment.getBatchType()) {
				case Sliding:
					entityGen = Flow.of(String.class).sliding(fragment.getBatchSize(), fragment.getStep()).mapAsyncUnordered(parallelism,
							msg -> PatternsCS.ask(entityGenerator, msg, askTimeout));
					break;
				case Group:
					entityGen = Flow.of(String.class).grouped(fragment.getBatchSize()).mapAsyncUnordered(parallelism,
							msg -> PatternsCS.ask(entityGenerator, msg, askTimeout));
					break;
				case GroupWithIn:
					entityGen = Flow.of(String.class).groupedWithin(fragment.getBatchSize(), fragment.getTimeout()).mapAsyncUnordered(parallelism,
							msg -> PatternsCS.ask(entityGenerator, msg, askTimeout));
					break;
				case Time:
					entityGen = Flow.of(String.class).groupedWithin(Integer.MAX_VALUE, fragment.getTimeout()).mapAsyncUnordered(parallelism,
							msg -> PatternsCS.ask(entityGenerator, msg, askTimeout));
					break;
				case None:
					entityGen = Flow.of(String.class).mapAsyncUnordered(parallelism, msg -> PatternsCS.ask(entityGenerator, msg, askTimeout));
					break;
				}

				if (firstPath) {
					builder.from(source).viaFanOut(bcast).via(builder.add(entityGen)).to(builder.add(sink));
					firstPath = false;
				} else {
					builder.from(bcast).via(builder.add(entityGen)).to(builder.add(sink));
				}

			}

			return ClosedShape.getInstance();
		})));
	}

	private boolean isValidJSON(final String json) {
		boolean valid = false;
		try {
			JsonNode node = mapper.readTree(json);
			if (node.has("jobId")) {
				valid = true;
			}
		} catch (IOException ioe) {
			;
		}
		return valid;
	}

}