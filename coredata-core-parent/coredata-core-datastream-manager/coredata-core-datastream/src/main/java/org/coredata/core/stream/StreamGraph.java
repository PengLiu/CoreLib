package org.coredata.core.stream;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.coredata.core.metric.documents.Metric;
import org.coredata.core.stream.decision.SysDecisionModelFlow;
import org.coredata.core.stream.mining.MiningDataFlow;
import org.coredata.core.stream.mining.PrepareMiningDataFlow;
import org.coredata.core.stream.mining.SysMiningModelFlow;
import org.coredata.core.stream.mining.entity.MiningData;
import org.coredata.core.stream.output.SaveSourceDataSink;
import org.coredata.core.stream.transform.FilterFlow;
import org.coredata.core.stream.transform.SaveDsTransformFlow;
import org.coredata.core.stream.transform.SysTransformModelFlow;
import org.coredata.core.stream.vo.GraphVO;
import org.springframework.stereotype.Component;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.kafka.ConsumerMessage.CommittableMessage;
import akka.kafka.ProducerSettings;
import akka.kafka.javadsl.Consumer.Control;
import akka.kafka.javadsl.Producer;
import akka.stream.ClosedShape;
import akka.stream.Graph;
import akka.stream.Outlet;
import akka.stream.SinkShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Balance;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

/**
 * 流程图构建类
 * @author sue
 *
 */
@Component
public class StreamGraph {

	private Source<CommittableMessage<String, String>, Control> source;

	private SysTransformModelFlow<String, String> transformModelFlow;

	private FilterFlow<String, String> filterFlow;

	private SysMiningModelFlow<String, String> miningModelFlow;

	private PrepareMiningDataFlow<String, List<MiningData>> prepareFlow;

	private MiningDataFlow<List<MiningData>, List<Metric>> miningFlow;

	private SysDecisionModelFlow<List<Metric>, List<Metric>> decisionModelFlow;

	@SuppressWarnings("rawtypes")
	private Graph saveMetricSink;

	@SuppressWarnings("rawtypes")
	private Graph decisionSink;

	@SuppressWarnings({ "rawtypes" })
	public RunnableGraph spliceGraph(GraphVO graphVo, ActorSystem system) {
		//首先拼接并行flow图
		Sink<Object, NotUsed> sinks = joinParallelFlows(graphVo, system);
		RunnableGraph graph = RunnableGraph.fromGraph(GraphDSL.create(b -> {
			Source<String, Control> source = this.source.map(f -> f.record().value());
			Outlet<String> out = b.add(source).out();
			/**
			 * 并行+串行写法
			 */
			b.from(out).to(b.add(sinks));
			return ClosedShape.getInstance();
		}));
		return graph;
	}

	public void setTransformFlows(SysTransformModelFlow<String, String> transformModelFlow, FilterFlow<String, String> filterFlow) {
		this.transformModelFlow = transformModelFlow;
		this.filterFlow = filterFlow;
	}

	public void setMiningFlows(SysMiningModelFlow<String, String> miningModelFlow, PrepareMiningDataFlow<String, List<MiningData>> prepareFlow,
			MiningDataFlow<List<MiningData>, List<Metric>> miningFlow) {
		this.miningModelFlow = miningModelFlow;
		this.prepareFlow = prepareFlow;
		this.miningFlow = miningFlow;
	}

	public void setSaveMetricSink(Sink<List<Metric>, NotUsed> saveMetricSink) {
		this.saveMetricSink = saveMetricSink;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <In> Sink<In, NotUsed> joinParallelFlows(GraphVO vo, ActorSystem system) {
		int parallelNum = vo.getParallelNum();
		boolean saveTransform = vo.isSaveTransform();//是否保存清洗后的原数据到hdfs
		boolean saveMetric = vo.isSaveMetric();//是否保存挖掘后的数据到hdfs
		Sink<In, NotUsed> sinkGraph = (Sink<In, NotUsed>) Sink.fromGraph(GraphDSL.create(b -> {
			final UniformFanOutShape<String, String> fanIn = b.add(Balance.create(parallelNum));
			if (saveTransform) {
				Graph saveDataSourceSink = joinSaveDataSourceSink(vo.getKafkaAddr(), system, parallelNum);
				SaveDsTransformFlow<String, ProducerRecord<String, String>> transforKafka = new SaveDsTransformFlow<>(vo.getDstransformTopic());
				for (int i = 0; i < parallelNum; i++) {
					UniformFanOutShape<List<Metric>, List<Metric>> bcast;
					if (saveMetric)
						bcast = b.add(Broadcast.create(3));
					else
						bcast = b.add(Broadcast.create(2));
					final UniformFanOutShape<String, String> transformbatch = b.add(Broadcast.create(2));
					b.from(fanIn.out(i)).via(b.add(transformModelFlow.async())).via(b.add(filterFlow.async())).viaFanOut(transformbatch)
							.via(b.add(transforKafka.async())).to((SinkShape<? super ProducerRecord<String, String>>) b.add(saveDataSourceSink));
					b.from(transformbatch).via(b.add(miningModelFlow.async())).via(b.add(prepareFlow.async())).via(b.add(miningFlow.async())).viaFanOut(bcast)
							.to((SinkShape<? super List<Metric>>) b.add(saveMetricSink));
					b.from(bcast).via(b.add(decisionModelFlow.async())).to((SinkShape<? super List<Metric>>) b.add(decisionSink));
					if (saveMetric) {
						Graph saveSourceDataSink = new SaveSourceDataSink<>(vo.getKafkaAddr(), vo.getDsmetricTopic(), system, parallelNum);
						b.from(bcast).to((SinkShape<? super List<Metric>>) b.add(saveSourceDataSink));
					}
				}
			} else {//不保存清洗数据的情况
				for (int i = 0; i < parallelNum; i++) {
					UniformFanOutShape<List<Metric>, List<Metric>> bcast;
					if (saveMetric)
						bcast = b.add(Broadcast.create(3));
					else
						bcast = b.add(Broadcast.create(2));
					b.from(fanIn.out(i)).via(b.add(transformModelFlow.async())).via(b.add(filterFlow.async())).via(b.add(miningModelFlow.async()))
							.via(b.add(prepareFlow.async())).via(b.add(miningFlow.async())).viaFanOut(bcast)
							.to((SinkShape<? super List<Metric>>) b.add(saveMetricSink));//to(b.add(Sink.ignore()));//
					b.from(bcast).via(b.add(decisionModelFlow.async())).to((SinkShape<? super List<Metric>>) b.add(decisionSink));
					if (saveMetric) {
						Graph saveSourceDataSink = new SaveSourceDataSink<>(vo.getKafkaAddr(), vo.getDsmetricTopic(), system, parallelNum);
						b.from(bcast).to((SinkShape<? super List<Metric>>) b.add(saveSourceDataSink));
					}
				}
			}
			return SinkShape.of(fanIn.in());
		}));
		return sinkGraph;
	}

	@SuppressWarnings("rawtypes")
	private Graph joinSaveDataSourceSink(String kafkaAddr, ActorSystem system, int parallelNum) {
		ProducerSettings<String, String> producerSettings = ProducerSettings.create(system, new StringSerializer(), new StringSerializer())
				.withBootstrapServers(kafkaAddr).withParallelism(parallelNum);
		Sink<ProducerRecord<String, String>, CompletionStage<Done>> plainSink = Producer.plainSink(producerSettings);
		return plainSink;
	}

	public void setDecisionModelFlow(SysDecisionModelFlow<List<Metric>, List<Metric>> decisionModelFlow) {
		this.decisionModelFlow = decisionModelFlow;
	}

	public void setDecisionSink(Sink<List<Metric>, NotUsed> decisionSink) {
		this.decisionSink = decisionSink;
	}

	public void setSource(Source<CommittableMessage<String, String>, Control> source) {
		this.source = source;
	}

}
