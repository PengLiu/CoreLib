package org.coredata.core.util.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

	public static final String METRIC_INFO = "metricInfo";

	public static final String INSTANCE = "instance";

	public static final String METRIC_VAL = "metric_val";

	public static final String POLICY = "policy";

	public static final String BUSINESS = "business";

	public static final String NOTSUP = "notsup";

	public static final String METRIC = "metric";

	public static final String MINING = "mining";

	public static final String DECISION = "decision";

	public static final String TRANSFORM = "transform";

	public static final String FUNCTIONS = "functions";

	//缓存采集数据
	public static final String CMDCACHE = "cmdcache";

	//挖掘数据前关联信息
	public static final String MININGMETA = "miningmeta";

	public static final String MACIP = "macIp";

	//过滤器链信息
	public static final String FILTERS = "filter";

	//资产属性缓存信息
	public static final String INSTANCE_PRO = "instancePro";

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private RedisMessageListenerContainer redisMessageListenerContainer;

	@SuppressWarnings("unchecked")
	public Object loadDataByTableAndKey(String table, String key) {
		return redisTemplate.opsForHash().get(table, key);
	}

	@SuppressWarnings("unchecked")
	public void saveData(String table, String key, Object metric) {
		redisTemplate.opsForHash().put(table, key, metric);
	}

	@SuppressWarnings("unchecked")
	public void deleteDataByTableAndKey(String table, String key) {
		redisTemplate.opsForHash().delete(table, key);
	}

	// Pub/Sub 消息订阅
	public void publish(ChannelTopic topic, String message) {
		stringRedisTemplate.convertAndSend(topic.getTopic(), message);
	}

	public void registSubscriber(MessageListener listener, ChannelTopic topic) {
		redisMessageListenerContainer.addMessageListener(listener, topic);
	}

}
