package org.coredata.core.stream.alarm;

import java.util.ArrayList;
import java.util.List;

import org.coredata.core.entities.ResEntity;
import org.coredata.core.stream.alarm.consumer.QueueManager;
import org.coredata.core.util.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * 改变资源状态的Action
 * @author Ammen
 *
 */
public class StateChange {

	private Logger logger = LoggerFactory.getLogger(StateChange.class);

	private static final StateChange change = new StateChange();

	private static final String OK = "green";

	private static final String ERROR = "red";

	private StateChange() {

	}

	public static StateChange instance() {
		return change;
	}

	public final void stateChange(QueueManager queueManager, boolean expResult, List<String> instIds, RedisService redisService) {
		if (CollectionUtils.isEmpty(instIds))
			return;
		String state = OK;
		if (expResult)
			state = ERROR;
		List<String> results = new ArrayList<>();
		try {
			for (String instId : instIds) {
				ResEntity instance = (ResEntity) redisService.loadDataByTableAndKey(RedisService.INSTANCE, instId);
				//此处调整逻辑
				if (instance == null) {
					results.add(instId);
					continue;
				}
				if (instance != null && !state.equals(instance.getStatus()) && (OK.equals(state) || ERROR.equals(state)))//满足此条件才放入结果更新状态
					results.add(instId);
			}
			if (results.size() <= 0)
				return;
			StateChangeDto dto = new StateChangeDto();
			dto.setState(state);
			dto.setInstIds(results);
			//修改相关流程，将告警状态及生成告警转移到告警中心处理
			long first = System.currentTimeMillis();
			queueManager.put(dto);
			long second = System.currentTimeMillis();
			if (logger.isDebugEnabled())
				logger.debug("+++++++++++++++++++++++Put queue 耗时:" + (second - first));
		} catch (Exception e) {
			logger.error("Change instance state error.", e);
		}
	}

}