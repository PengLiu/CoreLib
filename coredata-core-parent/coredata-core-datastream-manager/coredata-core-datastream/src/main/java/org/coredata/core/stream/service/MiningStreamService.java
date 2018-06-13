package org.coredata.core.stream.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.coredata.core.entities.ResEntity;
import org.coredata.core.model.common.Metric;
import org.coredata.core.model.common.Metric.MetricDataType;
import org.coredata.core.model.mining.Datamining;
import org.coredata.core.model.mining.Expression;
import org.coredata.core.model.mining.Param;
import org.coredata.core.stream.mining.entity.MetricInfo;
import org.coredata.core.stream.mining.entity.MiningData;
import org.coredata.core.stream.mining.functions.MiningFunctions;
import org.coredata.core.stream.util.ModelExpHelper;
import org.coredata.core.stream.vo.CMDInfo.SourceType;
import org.coredata.core.stream.vo.DSInfo;
import org.coredata.core.stream.vo.PrepareMiningData;
import org.coredata.core.stream.vo.TransformData;
import org.coredata.core.util.common.CloneUtil;
import org.coredata.core.util.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MiningStreamService extends AbstractStreamService {

	private static final Logger logger = LoggerFactory.getLogger(MiningStreamService.class);

	@Autowired
	private RedisService redisService;

	private ObjectMapper mapper = new ObjectMapper();

	private ConcurrentMap<String, Metric> metricCache = new ConcurrentHashMap<>();

	@Override
	public List<MiningData> process(List<PrepareMiningData> datas, TransformData transform) {
		if (CollectionUtils.isEmpty(datas))
			return null;
		List<MiningData> minings = new ArrayList<>();
		for (PrepareMiningData data : datas) {
			Map<String, String> alias = data.getAlias();
			String aid = alias.get(transform.getName());
			if (StringUtils.isEmpty(aid))
				continue;
			//此处过滤不支持的命令Alias集合
			Set<String> notSupportAlias = data.getNotSupportAlias();
			String instanceId = transform.getInstanceId();
			String notSupportCmds = getNotSupportCmds(instanceId);
			if (!StringUtils.isEmpty(notSupportCmds)) {
				String[] cmds = notSupportCmds.split(",");
				for (String cmd : cmds) {
					alias.forEach((k, v) -> {
						if (k.equals(cmd))
							notSupportAlias.add(v);
					});
				}
			}
			process(data, aid, transform, notSupportAlias, minings);
		}
		return minings;
	}

	private void process(PrepareMiningData data, String aid, TransformData resp, Set<String> notSupportAlias, List<MiningData> results) {
		Datamining mining = data.getDatamining();
		boolean needInstance = data.getNeedInstance();
		List<Expression> exps = mining.getType().getExp();
		for (Expression exp : exps) {
			Expression cloneExp = CloneUtil.createCloneObj(exp);
			cloneExp = processExp(cloneExp, resp);
			MetricInfo info = getMetricInfoFromCache(resp.getInstanceId(), resp.getModelid(), cloneExp.getMetric(), mining.getId());
			if (info == null)
				info = ModelExpHelper.process(resp.getInstanceId(), cloneExp, mining.getType().getMethod(), notSupportAlias);
			else {//判定是否包含不存在的指令
				DSInfo[] dsinfo = info.getDsinfo();
				List<DSInfo> newDsInfos = new ArrayList<>();
				for (DSInfo ds : dsinfo) {
					if (notSupportAlias != null && notSupportAlias.contains(ds.getAlias()))
						continue;
					newDsInfos.add(ds);
				}
				if (newDsInfos.size() < dsinfo.length)
					info.reProcessDsInfo(newDsInfos);
			}
			if (needInstance) {
				//绑定实例属性信息
				//TODO
				ResEntity instance = sysInstancePro(resp);
				process(info, instance, data.getInstAlias());
			}
			MiningData md = process(info, aid, resp, cloneExp, data.getDatamining());
			if (md != null)
				results.add(md);
		}
	}

	private MetricInfo getMetricInfoFromCache(String instanceId, String modelid, String exp, String miningId) {
		MetricInfo metricInfo = (MetricInfo) redisService.loadDataByTableAndKey(RedisService.METRIC_INFO, instanceId + modelid + miningId + exp);
		return metricInfo;
	}

	/**
	 * 根据采集数据中实例id获取对应instance实例
	 * @param resp
	 */
	//TODO
	private ResEntity sysInstancePro(TransformData resp) {
		String instanceId = resp.getInstanceId();//获取实例id
		return sysInstance(instanceId);
	}

	//TODO
	private void process(MetricInfo info, ResEntity instance, Map<String, String> instAlias) {
		if (instance == null)
			return;
		String uniqueIdent = instance.getEntityId();
		Map<String, Object> properties = instance.getProps();
		Map<String, Object> connections = instance.getConn();
		for (Map.Entry<String, String> entry : instAlias.entrySet()) {
			String aid = entry.getValue();
			JsonNode value = null;
			if (info.needBinding(aid, uniqueIdent)) {
				TransformData resp = new TransformData();
				resp.setInstanceId(uniqueIdent);
				try {
					if (SourceType.property.toString().equals(entry.getKey())) {
						String propStr = mapper.writeValueAsString(properties);
						value = mapper.readTree(propStr);
					} else if (SourceType.conninfo.toString().equals(entry.getKey())) {
						String con = mapper.writeValueAsString(connections);
						value = mapper.readTree(con);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					resp.setResult(mapper.writeValueAsString(value));
				} catch (Exception e) {
					e.printStackTrace();
				}
				info.cacheData(aid, resp);
				MiningFunctions.binding(info, resp, aid);
			}
		}
	}

	private MiningData process(MetricInfo info, String aid, TransformData resp, Expression exp, Datamining mining) {
		MiningData md = null;
		if (resp.isError()) {
			String metric = getMetricById(info.getId());
			if ("avail".equals(metric)) {
				String result = resp.getResult();
				Map<String, Object> rs = null;
				if (StringUtils.isEmpty(result))
					rs = new HashMap<>();//特殊处理 将可用性指标拼接后直接后续逻辑
				else
					rs = JSON.parseObject(result, new TypeReference<HashMap<String, Object>>() {
					});
				DSInfo[] dsinfo = info.getDsinfo();
				for (DSInfo di : dsinfo)
					rs.put(di.getKey(), "error");
				resp.setResult(JSON.toJSONString(rs));
				md = new MiningData(info, resp, aid, exp, mining.getId());
			}
		} else
			md = new MiningData(info, resp, aid, exp, mining.getId());
		return md;
	}

	/**
	 * 该方法用于替换表达式中变量，包含采集回来的数据和实例对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Expression processExp(Expression exp, TransformData resp) {
		String params = resp.getParams();//获取对应参数集合
		try {
			if (!StringUtils.isEmpty(params) && !"null".equals(params)) {//替换变量参数
				Map<String, Object> param = mapper.readValue(params, Map.class);
				Set<Entry<String, Object>> entrySet = param.entrySet();
				List<Param> cmdParams = exp.getParam();
				for (Entry<String, Object> entry : entrySet) {
					for (Param p : cmdParams) {
						String value = p.getValue();
						value = value.replaceAll("\\$\\{(?i)" + entry.getKey() + "\\}", entry.getValue().toString());
						p.setValue(value);
					}
				}
			}
		} catch (IOException e) {
			logger.error("Transform Exp Error.", e);
		}
		return exp;
	}

	/**
	 * 该方法根据instId获取对应全部不支持命令名称
	 * @param instId
	 * @return
	 */
	private String getNotSupportCmds(String instId) {
		Object data = redisService.loadDataByTableAndKey(RedisService.NOTSUP, instId);
		String cmds = data == null ? null : data.toString();
		return cmds;
	}

	//TODO
	private ResEntity sysInstance(String insId) {
		return (ResEntity) redisService.loadDataByTableAndKey(RedisService.INSTANCE, insId);
	}

	private String getMetricById(String id) {
		Metric metric = metricCache.get(id);
		if (metric == null)
			metric = (Metric) redisService.loadDataByTableAndKey(RedisService.METRIC, id);
		if (metric != null) {
			metricCache.put(id, metric);
			return metric.getMetrictype();
		}
		return "";
	}

	/**
	 * 该方法根据metricId获取对应信息
	 * @param info
	 */
	@Override
	public void setScale(MetricInfo info) {
		Metric m = metricCache.get(info.getId());
		if (m == null)
			m = (Metric) redisService.loadDataByTableAndKey(RedisService.METRIC, info.getId());// mapper.readValue(metric, Metric.class);
		if (m == null)
			return;
		metricCache.put(info.getId(), m);
		String datatype = m.getDatatype();//获取对应指标的datatype，判断该datatype是否可以进行四舍五入
		MetricDataType mdatatype = MetricDataType.valueOf(datatype);
		info.setNeedScale(m.needScale(mdatatype));
	}

}
