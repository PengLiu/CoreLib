package org.coredata.core.model.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.rits.cloning.Cloner;
import org.coredata.core.entities.EntityPropKey;
import org.coredata.core.entities.ResEntity;
import org.coredata.core.entities.services.EntityService;
import org.coredata.core.model.collection.CollectionModel;
import org.coredata.core.model.collection.Collector;
import org.coredata.core.model.collection.Param;
import org.coredata.core.model.constants.ClientConstant;
import org.coredata.core.model.entities.CollectionEntity;
import org.coredata.core.model.mining.DataminingModel;
import org.coredata.core.model.repositories.CollectionModelRepository;
import org.coredata.core.model.util.EntityUtil;
import org.coredata.core.util.common.DateUtil;
import org.coredata.core.util.common.MethodUtil;
import org.coredata.core.util.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

@Service
@Transactional
public class CollectionService {

	private static Logger logger = LoggerFactory.getLogger(CollectionService.class);

	private static final String COMMON = "[,]";

	private static final String POINT = "[.]";

	private static String cmd = "cmd\\.\\$\\{(.+?)\\}";

	private static Pattern cmdPattern = Pattern.compile(cmd);

	private static String metricExp = "^(.*?):(.*?)$";

	private static Pattern metricPattern = Pattern.compile(metricExp);

	private static final String COLLECT_MODEL = "collect";

	private static final String DATAMINING_MODEL = "datamining";

	private Cloner cloner = new Cloner();

	@Autowired
	private CollectionModelRepository collectionModelRepository;

	@Autowired
	private EntityService entityService;

	@Autowired
	private RedisService redisService;

	public CollectionModel findById(String id) {
		CollectionEntity colEntity = collectionModelRepository.findById(id);
		if (colEntity == null) {
			return null;
		}
		return colEntity.getDecryptModel();
	}

	public void save(CollectionModel model) {
		List<Collector> collector = model.getCollector();
		if (!CollectionUtils.isEmpty(collector)) {
			collector.forEach(c -> {
				String datatype = c.getDatatype();
				String period = DateUtil.translateDatatypeForPeriod(datatype);
				c.setPeriod(period);
			});
		}
		String id = model.getId();
		CollectionEntity colEntity = collectionModelRepository.findById(id);
		if (colEntity != null) {
			collectionModelRepository.delete(colEntity);
		}
		colEntity = new CollectionEntity();
		colEntity.setColModel(model);
		collectionModelRepository.save(colEntity);
	}

	public void updateModel(CollectionModel model) {
		String id = model.getId();
		CollectionEntity colEntity = collectionModelRepository.findById(id);
		if (colEntity != null) {
			collectionModelRepository.delete(colEntity);
		}
		colEntity = new CollectionEntity();
		colEntity.setColModel(model);
		collectionModelRepository.save(colEntity);
	}

	public void deleteAll() {
		collectionModelRepository.deleteAll();
	}

	public void delete(String id) {
		CollectionEntity colEntity = collectionModelRepository.findById(id);
		if (colEntity != null) {
			collectionModelRepository.delete(colEntity);
		}
	}

	public String findCollectionModelByInstanceId(String instanceId) {
		// 首先根据实例化id获取对应资源
		ResEntity resEntity = (ResEntity) entityService.findByEntityId(instanceId);
		String modelId = resEntity == null ? "" : (String) resEntity.getProp(EntityPropKey.modelId);
		// String modelId = instanceClient.findResEntity(instanceId);
		CollectionModel collectionModel = findById(modelId);
		return JSON.toJSONString(collectionModel);
	}

	public String findAllCollectionModels() {
		Iterable<CollectionEntity> collections = collectionModelRepository.findAll();
		List<CollectionModel> models = new ArrayList<>();
		if (collections == null) {
			return JSON.toJSONString(models);
		}
		collections.forEach(collect -> models.add(collect.getDecryptModel()));
		return JSON.toJSONString(models);
	}

	public void processCollectModel(String instanceIds, Map<String, List<String>> collectors) {
		String[] instances = instanceIds.split(COMMON);
		for (String ins : instances) {

			String instanceJson = null;
			String[] ids = ins.split(",");
			if (ids.length == 1) {
				ResEntity resEntity = (ResEntity) entityService.findByEntityId(ids[0]);
				instanceJson = JSON.toJSONString(resEntity);
			} else {
				List<ResEntity> result = new ArrayList<>();
				for (String id : ids) {
					ResEntity resEntity = (ResEntity) entityService.findByEntityId(id);
					result.add(resEntity);
				}
				instanceJson = JSON.toJSONString(result);
			}

			// String instanceJson =
			// instanceClient.findInstanceByUniqueIdent(ins);//转换JSON字符串
			if (null == instanceJson || "null".equals(instanceJson)) {
				continue;
			}
			JSONObject json = JSON.parseObject(instanceJson);
			String type = json.get(ClientConstant.NODELEVEL).toString();// 获取对应资源类型
			String collectorKey = ins;
			if (!ClientConstant.TYPE_ROOT.equals(type)) {// 如果不是根资源，表明需要多次处理相关记录
				String rootUnique = json.get(ClientConstant.ROOT_INSTID).toString();
				collectorKey = rootUnique;
			}
			List<String> collector = collectors.get(collectorKey);
			if (collector == null) {
				collector = new ArrayList<>();
			}
			collector.add(instanceJson);
			collectors.put(collectorKey, collector);
		}
	}

	@SuppressWarnings("unchecked")
	public void processCollectParams(Map<String, Object> params, List<String> instances, String needProtocol) {
		params.put(ClientConstant.SERVER_REQUEST_ACTION, ClientConstant.SERVER_REQUEST_ACTION_COLLECT);
		String resultCollectId = "";
		Map<String, Object> hasConnections = new HashMap<>();
		Map<String, List<Map<String, Object>>> hasCollector = new HashMap<>();// 已经存在的采集命令
		for (String ins : instances) {
			// 加入监控的子资源信息
			List<Map<String, String>> subInstanceInfo = new ArrayList<Map<String, String>>();
			// 此处重新分割相关实例化id
			List<Map<String, Object>> collector = new ArrayList<>();
			JSONObject json = JSON.parseObject(ins);
			String type = json.get(ClientConstant.NODELEVEL).toString();// 获取对应资源类型
			String resultIns = json.get(ClientConstant.INST_ID).toString();
			if (ClientConstant.TYPE_ROOT.equals(type)) {// 如果是根资源，表明需要多次处理相关记录
				// TODO
				resultCollectId = resultIns;

				List<ResEntity> tmp = null;

				//              tmp =  entityService.findMonitorInstancesByRootUnique(resultIns);
				String allInstances = JSON.toJSONString(tmp);
				// String allInstances =
				// instanceClient.findMonitorInstancesByRootUnique(resultIns);
				if (!StringUtils.isEmpty(allInstances) && !"null".equalsIgnoreCase(allInstances)) {
					List<JSONObject> subinstances = JSON.parseObject(allInstances, List.class);
					subinstances.forEach(j -> {
						Map<String, String> subInstanceMap = new HashMap<String, String>();
						subInstanceMap.put(ClientConstant.INSTANCE_ID, j.get(ClientConstant.INST_ID).toString());
						subInstanceMap.put(ClientConstant.MODEL_INS_ID, j.get(ClientConstant.MODEL_INS_ID_ST).toString());
						subInstanceMap.put(ClientConstant.INSTANCE_PROPERTIES_INDEX, j.get(ClientConstant.INSTANCE_PROPERTIES_INDEX).toString());
						subInstanceInfo.add(subInstanceMap);
					});
				}
			}
			Object object = json.get(ClientConstant.SERVER_REQUEST_CONNECT);
			List<Map<String, String>> connections = JSON.parseObject(object.toString(), List.class);
			Object properties = json.get(ClientConstant.INSTANCE_PROPERTIES);
			Map<String, Object> propertiesInfo = JSON.parseObject(properties.toString(), new TypeReference<Map<String, Object>>() {
			});
			for (Map<String, String> m : connections) {
				String protocol = m.get(ClientConstant.PROTOCOL);
				if (hasConnections.containsKey(protocol)) {
					continue;
				}
				hasConnections.put(protocol, m);
			}
			// 采集模型id
			String restype = json.get(ClientConstant.MODEL_INS_ID).toString();
			// 清洗模型id
			String transformId = json.get(ClientConstant.MODEL_INS_ID_ST).toString();
			String rType = json.get(ClientConstant.INSTANCE_RESTYPE).toString();
			if (ClientConstant.LINK_RESTYPE.equals(rType)) {
				resultCollectId = resultIns;
			}
			Map<String, CollectionModel> collectionModels = findCollectionModel(restype);
			if (collectionModels == null || collectionModels.size() == 0) {
				continue;
			}
			// 此处添加代码
			// 如果已经包含该协议
			if (hasCollector.containsKey(restype)) {
				List<Map<String, Object>> hasColl = hasCollector.get(restype);
				hasColl.forEach(c -> {
					Object v = c.get(ClientConstant.PARAMS);
					// 如果参数不为空，则从新添加参数
					if (v != null) {
						List<Map<String, String>> listParam = (List<Map<String, String>>) v;
						// 默认获取第一个索引参数列表
						Map<String, String> par = listParam.get(0);
						Map<String, String> newParam = new HashMap<>();
						par.forEach((k, p) -> {
							Object pv = json.get(k);
							if (pv == null) {
								pv = replaceProperty("${" + k + "}", propertiesInfo);
							} // 替换命令中的参数
							newParam.put(k, pv.toString());
						});
						listParam.add(newParam);
						c.put(ClientConstant.PARAMS, listParam);
					}
					Object insIds = c.get(ClientConstant.INSTANCE_ID);
					List<String> newInsIds = null;
					if (insIds instanceof List) {
						newInsIds = (List<String>) insIds;
					} else if (insIds instanceof String) {
						newInsIds = new ArrayList<>();
						newInsIds.add(insIds.toString());
					}
					if (!newInsIds.contains(resultIns)) {
						newInsIds.add(resultIns);
					}
					c.put(ClientConstant.INSTANCE_ID, newInsIds);

					Object paramMap = c.get(ClientConstant.PARAMS_MAP);
					List<Map<String, Object>> listparam = null;
					if (paramMap instanceof List) {
						listparam = (List<Map<String, Object>>) paramMap;
						Map<String, Object> map = listparam.get(0);
						Map<String, Object> mp = new HashMap<>();
						map.forEach((j, l) -> {
							Object property = propertiesInfo.get(ClientConstant.INSTANCE_PROPERTIES_INDEX);
							mp.put(j, l);
							for (Entry<String, Object> m : c.entrySet()) {
								List<String> extractionParams = EntityUtil.extractionParams(m.getValue() != null ? m.getValue().toString() : "");
								if (property != null && extractionParams.size() > 0 && j.equals(m.getKey())) {
									String result = (String) property;
									mp.put(j, result);
									break;
								}
							}
						});
						if (mp.size() > 0) {
							listparam.add(mp);
						}
					}
					c.put(ClientConstant.PARAMS_MAP, listparam);
				});
				continue;
			}
			// 此处循环处理全部采集模型
			collectionModels.forEach((r, model) -> {
				List<Collector> collectors = model.getCollector();
				if (CollectionUtils.isEmpty(collectors)) {
					return;
				}
				collectors.forEach(c -> {
					boolean hasOther = false;
					List<Map<String, String>> ps = new ArrayList<>();// 组装相关参数
					Map<String, Object> coll = new HashMap<>();
					String id = c.getId().trim();
					String cmd = c.getCmd().trim();
					String pro = c.getType().trim();
					boolean isGlobalResult = c.getIsGlobalResult();
					boolean isAvailCmd = c.getIsavailcmd();
					boolean isMonitor = c.getIsMonitor();// 是否监控属性
					if (StringUtils.isEmpty(pro) || (needProtocol != null && !pro.equals(needProtocol)))// 如果当前采集模型协议为空，跳过
					{
						return;
					}
					if (!hasConnections.containsKey(pro)) {// 如果协议不为空并且采集模型协议与当前协议不符，则跳过该协议
						Map<String, String> value = new HashMap<>();
						value.put(ClientConstant.PROTOCOL, pro);
						hasConnections.put(pro, value);
						hasOther = true;
					}
					// 替换CMD中连接信息变量
					cmd = replaceConnect(cmd, (Map<String, String>) hasConnections.get(pro));
					List<Param> cparam = c.getParam();// 采集参数列表
					if (!CollectionUtils.isEmpty(cparam)) {
						List<Map<String, Object>> m = new ArrayList<>();
						Map<String, Object> mp = new HashMap<>();
						cparam.forEach(cp -> {
							coll.put(cp.getKey(), cp.getValue());
							// 替换采集模型中param中value的变量
							List<String> extractionParams = EntityUtil.extractionParams(cp.getValue());
							if (extractionParams.size() > 0) {
								for (String s : extractionParams) {
									Object property = propertiesInfo.get(s);
									if (property != null) {
										String result = (String) property;
										mp.put(cp.getKey(), result);
									}
								}
							} else {
								mp.put(cp.getKey(), cp.getValue());
							}
						});
						m.add(mp);
						coll.put(ClientConstant.PARAMS_MAP, m);
					}
					String timeout = c.getTimeout();
					if (timeout != null) {
						timeout = DateUtil.translateTimeOutUnit(Integer.parseInt(timeout), DateUtil.SECOND_UNIT);
					}
					String retry = c.getRetry();
					String period = c.getPeriod();
					coll.put(ClientConstant.COLLECT_ID, id);
					coll.put(ClientConstant.CMD, cmd);
					coll.put(ClientConstant.PROTOCOL, pro);
					coll.put(ClientConstant.TIME_OUT, timeout);
					coll.put(ClientConstant.RETRY, retry);
					coll.put(ClientConstant.PERIOD, period);
					coll.put(ClientConstant.MODEL_ID, transformId);
					coll.put(ClientConstant.CMD_IS_GLOBALRESULT, isGlobalResult);
					coll.put(ClientConstant.CMD_SUB_INSTANCE_INFO, subInstanceInfo);
					coll.put(ClientConstant.CMD_IS_AVAILCMD, isAvailCmd);
					coll.put(ClientConstant.CMD_ENABLE, isMonitor);
					// 此处开始检查cmd命令中是否存在变量
					List<String> extractionParams = EntityUtil.extractionParams(cmd);
					if (extractionParams.size() == 0) {
						coll.put(ClientConstant.INSTANCE_ID, resultIns);
						collector.add(coll);
						return;
					}
					Map<String, String> param = new HashMap<>();
					for (String extra : extractionParams) {
						Object v = null;
						if (hasOther || pro.equalsIgnoreCase("ping") || extra.contains(".")) {
							String[] keys = extra.split(POINT);
							String key = extra;
							if (keys.length > 1) {
								key = keys[1];
							}
							if ("propinfo".equals(keys[0])) {
								v = replaceProperty(cmd, propertiesInfo);
							} else {
								v = getValueFromConnections(key, hasConnections);
							}
							if (v == null) {
								continue;
							}
						} else {
							v = json.get(extra.trim().toLowerCase());
						}
						param.put(extra.toLowerCase(), v != null ? v.toString() : "");
					}
					ps.add(param);
					coll.put(ClientConstant.PARAMS, ps);
					coll.put(ClientConstant.INSTANCE_ID, resultIns);
					collector.add(coll);
				});
			});
			hasCollector.put(restype, collector);
		}
		params.put(ClientConstant.COLLECT_ID, MethodUtil.md5(resultCollectId));// 此处放置主资源的instanceId，有可能是多根资源情况

		List<Map<String, String>> resultConns = new ArrayList<>();
		hasConnections.forEach((pro, conn) -> resultConns.add((Map<String, String>) conn));
		params.put(ClientConstant.SERVER_REQUEST_CONNECT, resultConns);
		List<Map<String, Object>> resultCollector = new ArrayList<>(1);
		hasCollector.forEach((k, coll) -> {
			coll.forEach(c -> resultCollector.add(c));
		});
		params.put(ClientConstant.COLLECTOR, resultCollector);
	}

	/**
	 * 该方法用于从连接信息中获取对应值
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String getValueFromConnections(String key, Map<String, Object> connections) {
		Set<Entry<String, Object>> entrySet = connections.entrySet();
		String result = null;
		for (Entry<String, Object> entry : entrySet) {
			Map<String, String> cons = (Map<String, String>) entry.getValue();
			result = cons.get(key);
			if (result != null) {
				break;
			}
		}
		return result;
	}

	/**
	 * 该方法用于根据modelId获取对应采集模型，考虑有上级资源情况，此处需要返回Map
	 *
	 * @param modelId
	 * @return
	 */
	private Map<String, CollectionModel> findCollectionModel(String modelId) {
		CollectionModel model = findById(modelId);
		if (model == null) {
			return null;
		}
		Map<String, CollectionModel> results = new HashMap<>();
		results.put(modelId, model);
		return results;
	}

	public String findCollectionModelById(String modelId) {
		List<CollectionModel> result = new ArrayList<>();
		CollectionModel model = findById(modelId);
		if (model == null) {
			return JSON.toJSONString(result);
		}
		result.add(model);
		return JSON.toJSONString(result);
	}

	/**
	 * 替换cmd中的属性信息
	 */
	private String replaceProperty(String cmd, Map<String, Object> properties) {
		String result = "";
		Map<String, String> connMap = EntityUtil.getPropertyParam(cmd);
		for (Entry<String, String> entry : connMap.entrySet()) {
			String property = (String) properties.get(entry.getKey());
			if (!StringUtils.isEmpty(property)) {
				result = property;
			}
		}
		return result;
	}

	/**
	 * 替换cmd中的连接信息
	 */
	private String replaceConnect(String cmd, Map<String, String> connection) {
		String result = cmd;
		Map<String, String> connMap = EntityUtil.getConnectParam(cmd);
		for (Entry<String, String> entry : connMap.entrySet()) {
			if (connection.get(entry.getValue()) != null) {
				result = result.replace(entry.getKey(), connection.get(entry.getValue()));
			}
		}
		return result;
	}

	/**
	 * 该方法用于绑定相关命令和指标关系
	 */

	public Map<String, Object> bindCollectCmdAndMetric(CollectionModel collect, DataminingModel mining) {
		Map<String, Object> result = new HashMap<>();
		//		if (collect == null || mining == null)
		//			return result;
		//		List<Collector> collectors = collect.getCollector();// 获取对应的collector关系
		//		List<Datamining> dm = mining.getMining();
		//		for (Collector c : collectors) {
		//			String id = c.getId();// 获取对应命令的id
		//			List<String> bindMetrics = new ArrayList<>();
		//			for (Datamining d : dm) {
		//				Set<String> alias = new HashSet<>();
		//				List<Datasource> datasource = d.getDatasource();// 获取挖掘命令数据源
		//				for (Datasource ds : datasource) {
		//					String sourcecmd = ds.getSourcecmd();// 如果指令包含对应命令id，则保存该数据源
		//					Matcher matcher = cmdPattern.matcher(sourcecmd);
		//					if (!matcher.find())
		//						continue;
		//					String cmdId = matcher.group(1);
		//					if (id.equals(cmdId))
		//						alias.add(ds.getId());
		//				}
		//				if (alias.size() == 0)
		//					continue;
		//				Type type = d.getType();
		//				if (type == null)
		//					continue;
		//				List<Expression> exp = type.getExp();
		//				for (Expression e : exp) {
		//					String metric = e.getMetric();
		//					Matcher matcher = metricPattern.matcher(metric);
		//					if (!matcher.find() || matcher.groupCount() != 2)
		//						continue;
		//					String metricId = matcher.group(1);
		//					List<String> cmdAlias = new ArrayList<>();
		//					List<com.coredata.coremanager.datamining.model.Param> params = e.getParam();
		//					for (com.coredata.coremanager.datamining.model.Param param : params) {
		//						String mexp = param.getValue();
		//						List<String> al = EntityUtil.extractionCmds(mexp);
		//						cmdAlias.addAll(al);
		//					}
		//					if (cmdAlias.size() == 0)
		//						continue;
		//					for (String a : alias) {
		//						if (cmdAlias.contains(a)) {
		//							bindMetrics.add(metricId);
		//							break;
		//						}
		//					}
		//				}
		//			}
		//			c.setMetrics(bindMetrics);
		//		}
		//		result.put(COLLECT_MODEL, collect);
		//		result.put(DATAMINING_MODEL, mining);
		return result;
	}

	public long findAllCollectCount() {
		return collectionModelRepository.count();
	}

	public void processSaveCollectModel(Map<String, CollectionModel> models) {
		if (models.size() <= 0) {
			return;
		}
		// 循环之后，再次存入数据库
		Set<CollectionModel> results = new HashSet<>();
		models.forEach((k, model) -> {
			String origin = model.getOrigin();
			if (StringUtils.isEmpty(origin)) {// 如果上级为空，直接存入数据库
				results.add(model);
				return;
			}
			CollectionModel clone = cloner.deepClone(model);
			iteratorCollectionModels(clone, origin, models, results);
		});
		results.forEach(r -> save(r));
	}

	/**
	 * 该方法用于循环迭代拼接采集模型
	 *
	 * @param origin
	 * @param models
	 */
	private void iteratorCollectionModels(CollectionModel model, String origin, Map<String, CollectionModel> models, Set<CollectionModel> results) {
		CollectionModel originModel = models.get(origin);
		if (originModel == null) {
			results.add(model);
			return;
		}
		List<Collector> originCollectors = originModel.getCollector();
		if (!CollectionUtils.isEmpty(originCollectors)) {
			List<Collector> cloneOriginCollectors = cloner.deepClone(originCollectors);
			List<Collector> collector = model.getCollector();
			if (collector == null) {
				collector = new ArrayList<>();
				model.setCollector(collector);
			}
			model.getCollector().addAll(cloneOriginCollectors);
		}
		String newOrigin = originModel.getOrigin();
		if (StringUtils.isEmpty(newOrigin)) {
			results.add(model);
			return;
		}
		iteratorCollectionModels(model, newOrigin, models, results);
	}

	public List<CollectionModel> findCollectModelsByOrigin(String origin) {
		List<CollectionModel> models = new ArrayList<>();
		List<CollectionEntity> collections = collectionModelRepository.findByOrigin(origin);
		if (CollectionUtils.isEmpty(collections)) {
			return models;
		}
		collections.forEach(collect -> models.add(collect.getDecryptModel()));
		return models;
	}

}
