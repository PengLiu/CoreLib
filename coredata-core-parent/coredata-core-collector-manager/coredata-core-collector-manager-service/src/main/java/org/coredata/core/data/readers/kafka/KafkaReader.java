package org.coredata.core.data.readers.kafka;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.coredata.core.data.DefaultRecord;
import org.coredata.core.data.Fields;
import org.coredata.core.data.OutputFieldsDeclarer;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Reader;
import org.coredata.core.data.Record;
import org.coredata.core.data.RecordCollector;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.debugger.JobStatus;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service(value = "kafkaReader")
@Scope("prototype")
public class KafkaReader extends Reader {

	private Fields fields;

	private int timeout = 100;

	private boolean running = true;

	private Properties props = new Properties();

	private String topic;

	@Override
	public void prepare(PluginConfig readerConfig) {

		super.prepare(readerConfig);

		String kafkaType = readerConfig.getString(KafkaReaderProperties.KAFKA_TYPE);
		String zkAddr = readerConfig.getString(KafkaReaderProperties.ZOOKEEPER_CONNECT);

		String addr = KafkaUtils.getBrokers(kafkaType, zkAddr);

		topic = readerConfig.getString(KafkaReaderProperties.TOPIC);

		props.put("bootstrap.servers", addr);
		props.put("group.id", readerConfig.getString(KafkaReaderProperties.GROUP_ID));
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("max.poll.records", "1000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		if (readerConfig.containsKey("schema")) {
			fields = new Fields();
			String[] tokens = readerConfig.getString("schema").split("\\s*,\\s*");
			for (String field : tokens) {
				fields.add(field);
			}
		}
	}

	@Async
	@Override
	public CompletableFuture<JobDetail> execute(RecordCollector recordCollector) {

		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
			consumer.subscribe(Arrays.asList(topic.split(",")));
			for (;;) {
				ConsumerRecords<String, String> records = consumer.poll(timeout);
				if (records.count() > 0) {
					for (ConsumerRecord<String, String> record : records) {
						Record r = new DefaultRecord(1);
						r.add(record.value());
						doFilter(r);
						//get selected columns
						doSelect(r);
						recordCollector.send(r);
					}
				}
				if (!running) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		jobDetail.setStatus(JobStatus.Success);
		return CompletableFuture.completedFuture(jobDetail);

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(fields);
	}

	@Override
	public void close() {
		running = false;
	}

}
