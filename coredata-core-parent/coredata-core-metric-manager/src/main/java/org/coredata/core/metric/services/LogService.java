package org.coredata.core.metric.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.coredata.core.metric.documents.LogFile;
import org.coredata.core.metric.repositories.LogResp;
import org.coredata.core.util.nlp.service.NLPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.Done;
import akka.actor.ActorSystem;
import akka.kafka.ConsumerMessage;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;

@Service
@Transactional(readOnly = true)
public class LogService {

	private Logger logger = LoggerFactory.getLogger(LogService.class);

	private ObjectMapper mapper = new ObjectMapper();

	@Value("${spring.kafka.bootstrap-servers}")
	private String kafkaAddr;

	@Value("${spring.kafka.consumer.group-id}")
	private String groupId;

	@Value("${spring.kafka.consumer.auto-offset-reset}")
	private String offsetRest;

	@Value("${spring.kafka.consumer.enable-auto-commit}")
	private String autoCommit;

	@Value("${spring.kafka.topics.log}")
	private String topic;

	@Autowired
	private LogResp logResp;

	@Autowired
	private NLPService nlpService;

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	private int parallelism = 500;

	private BlockingQueue<LogFile> data = new LinkedBlockingQueue<>();

	@PostConstruct
	public void init() {

		elasticsearchTemplate.createIndex(LogFile.class);

		ConsumerSettings<String, String> consumerSettings = ConsumerSettings.create(actorSystem, new StringDeserializer(), new StringDeserializer())
				.withBootstrapServers(kafkaAddr).withGroupId(groupId).withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit)
				.withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "3000").withProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
				.withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetRest);

		Materializer materializer = ActorMaterializer.create(actorSystem);

		Consumer.committableSource(consumerSettings, Subscriptions.topics(topic))
				.mapAsync(parallelism, msg -> cacheLog(msg.record().value()).thenApply(done -> msg.committableOffset()))
				.batch(100, first -> ConsumerMessage.emptyCommittableOffsetBatch().updated(first), (batch, elem) -> batch.updated(elem))
				.mapAsync(10, c -> c.commitJavadsl()).runWith(Sink.ignore(), materializer);
		
	}

	@Scheduled(fixedDelay = 1000)
	public void saveLogs() {	
		
		List<LogFile> tmp = new ArrayList<>();
		data.drainTo(tmp);
		if (!CollectionUtils.isEmpty(tmp)) {
			logResp.saveAll(tmp);
			if (logger.isDebugEnabled()) {
				logger.debug("saved " + tmp.size() + " logs.");
			}
		}
	}

	public CompletionStage<Done> cacheLog(String logStr) {

		try {
			LogFile log = new LogFile();
			JsonNode logJson = mapper.readTree(logStr);
			if (logJson == null) {
				return CompletableFuture.completedFuture(Done.getInstance());
			}
			JsonNode msgJson = mapper.readTree(logJson.get("message").asText());
			if (msgJson == null || msgJson.get("Msg") == null) {
				return CompletableFuture.completedFuture(Done.getInstance());
			}
			JsonNode logContent = mapper.readTree(msgJson.get("Msg").asText());
			String filePath = msgJson.get("FilePath").asText();
			String clientIp = logJson.get("client_ip").asText();
			log.setCreatedTime(logJson.get("time").asLong());
			log.setFilePath(filePath);
			log.setLog(logContent.get("log").asText());
			log.setServerIp(clientIp);
			List<String> keywords = nlpService.extractKeyword(log.getLog(), 3);
			if (!CollectionUtils.isEmpty(keywords)) {
				StringBuilder keywordBuilder = new StringBuilder();
				for (String keyword : keywords) {
					keywordBuilder.append(keyword).append(" ");
				}
				log.setKeywords(keywordBuilder.toString());
			}
			data.add(log);
		} catch (IOException e) {
			logger.error("Parse log entity from json error.", e);
		}

		return CompletableFuture.completedFuture(Done.getInstance());
	}

	@Transactional
	public void save(List<LogFile> logs) {
		if (CollectionUtils.isEmpty(logs)) {
			return;
		}
		logResp.saveAll(logs);
	}

	@Transactional
	public LogFile save(LogFile log) {
		return logResp.save(log);
	}

	public long count() {
		return logResp.count();
	}

}