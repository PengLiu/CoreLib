package org.coredata.core.model.service;

import com.alibaba.fastjson.JSON;
import org.coredata.core.model.common.Metric;
import org.coredata.core.model.discovery.DiscoveryModel;
import org.coredata.core.model.discovery.Instance;
import org.coredata.core.model.entities.CollectionEntity;
import org.coredata.core.model.entities.MetricEntity;
import org.coredata.core.model.entities.RestypeEntity;
import org.coredata.core.model.mining.Datamining;
import org.coredata.core.model.mining.DataminingModel;
import org.coredata.core.model.mining.Expression;
import org.coredata.core.model.mining.Type;
import org.coredata.core.model.repositories.CollectionModelRepository;
import org.coredata.core.model.repositories.MetricRepository;
import org.coredata.core.model.repositories.RestypeRepository;
import org.coredata.core.util.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class ModelService {

    private static final String CHILD = "child";

    private static final String METRIC_ID = "id";

    private static final String METRIC_NAME = "name";

    private static final String METRIC_TYPE = "type";

    @Autowired
    private RestypeRepository restypeRepository;

    @Autowired
    private CollectionModelRepository collectionModelRepository;

    @Autowired
    private DiscoveryModelService discoveryService;

    @Autowired
    private DataminingService dataminingService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MetricRepository metricRepository;


    public RestypeEntity findRestypeById(String id) {
        return restypeRepository.findById(id);
    }

    public RestypeEntity saveRestype(RestypeEntity entity) {
        return restypeRepository.save(entity);
    }

    public CollectionEntity findCollectionById(String id) {
        return collectionModelRepository.findById(id);
    }

    public CollectionEntity saveCollectionModel(CollectionEntity entity) {
        return collectionModelRepository.save(entity);
    }

    public String findMetricByRestype(String restype, String nodeLevel, String joinType) {
        List<Map<String, String>> result = new ArrayList<>();
        Set<String> modelIds = new HashSet<>();
        if (CHILD.equals(nodeLevel)) {//如果是子资产，拆分一下restype
            String[] restypes = restype.split("@");
            List<DiscoveryModel> models = discoveryService.findByRestype(restypes[0]);
            if (CollectionUtils.isEmpty(models)) {
                return null;
            }
            for (DiscoveryModel model : models) {
                List<Instance> instances = model.getInstance();
                if (CollectionUtils.isEmpty(instances)) {
                    continue;
                }
                Optional<Instance> inst = instances.stream().filter(ins -> ins.getRestype().equals(restypes[1])).findFirst();
                if (!inst.isPresent()) {
                    continue;
                }
                Instance instance = inst.get();
                modelIds.add(instance.getSourcemodel());
            }
            //获取全部模型id
        } else {
            List<DiscoveryModel> models = discoveryService.findByRestype(restype);
            if (CollectionUtils.isEmpty(models)) {
                return null;
            }
            for (DiscoveryModel model : models) {
                modelIds.add(model.getId());
            }
            //获取全部模型id
        }
        result = findAllMetrics(modelIds, joinType);
        return JSON.toJSONString(result);
    }

    public List<Map<String, String>> findAllMetrics(Set<String> modelIds, String joinType) {
        List<Map<String, String>> result = new ArrayList<>();
        if (joinType.equals("andSet")) {
            Set<String> metrics = new HashSet<>();
            for (String modelId : modelIds) {
                DataminingModel datamining = dataminingService.findById(modelId);
                if (datamining == null) {
                    continue;
                }
                List<Datamining> minings = datamining.getMining();
                if (CollectionUtils.isEmpty(minings)) {
                    continue;
                }
                for (Datamining m : minings) {
                    Type type = m.getType();
                    if (type == null) {
                        continue;
                    }
                    List<Expression> exps = type.getExp();
                    if (exps == null || exps.size() <= 0) {
                        continue;
                    }
                    for (Expression exp : exps) {
                        String mt = exp.getMetric();
                        String eid = mt.split(":")[0];
                        // 根据id获取一下对应metric信息，获取对应名称
                        //String metricStr = redisService.loadMetricById(eid);
                        Metric metric = (Metric) redisService.loadDataByTableAndKey(RedisService.METRIC_INFO, eid);// JSON.parseObject(metricStr, Metric.class);
                        if (metric == null || metrics.contains(eid)) {
                            continue;
                        }
                        Map<String, String> r = new HashMap<>();
                        r.put(METRIC_ID, eid);
                        r.put(METRIC_NAME, metric.getName());
                        r.put(METRIC_TYPE, metric.getMetrictype());
                        result.add(r);
                        metrics.add(eid);
                    }
                }
            }
        } else {
            Map<String, List<String>> allMetrics = new HashMap<>();
            for (String modelId : modelIds) {
                DataminingModel datamining = dataminingService.findById(modelId);
                if (datamining == null) {
                    continue;
                }
                List<Datamining> minings = datamining.getMining();
                if (CollectionUtils.isEmpty(minings)) {
                    continue;
                }
                List<String> ms = new ArrayList<>();
                for (Datamining m : minings) {
                    Type type = m.getType();
                    if (type == null) {
                        continue;
                    }
                    List<Expression> exps = type.getExp();
                    if (exps == null || exps.size() <= 0) {
                        continue;
                    }
                    for (Expression exp : exps) {
                        String mt = exp.getMetric();
                        String eid = mt.split(":")[0];
                        ms.add(eid);
                    }
                }
                allMetrics.put(modelId, ms);
            }
            Set<Map.Entry<String, List<String>>> sets = allMetrics.entrySet();
            List<String> results = new ArrayList<>();
            int index = 0;
            for (Map.Entry<String, List<String>> set : sets) {
                List<String> mids = set.getValue();
                if (index == 0) {
                    results.addAll(mids);
                    index++;
                    continue;
                }
                results.retainAll(mids);
            }
            for (String eid : results) {
                // 根据id获取一下对应metric信息，获取对应名称
                //String metricStr = redisService.loadDataByTableAndKey(RedisService.METRIC_INFO,eid);
                Metric metric = (Metric) redisService.loadDataByTableAndKey(RedisService.METRIC_INFO, eid);// JSON.parseObject(metricStr, Metric.class);
                if (metric == null) {
                    continue;
                }
                Map<String, String> r = new HashMap<>();
                r.put(METRIC_ID, eid);
                r.put(METRIC_NAME, metric.getName());
                r.put(METRIC_TYPE, metric.getMetrictype());
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Find metric defs by ids .
     *
     * @param ids the ids
     * @return the map
     */
    public Map<String, Metric> findMetricDefsByIds(String[] ids) {
        Map<String, Metric> result = new HashMap<>();
        for (String id : ids) {
            Metric metric = findMetricById(id);
            result.put(id, metric);
        }
        return result;
    }

    /**
     * Find metric by id .
     *
     * @param id the id
     * @return the metric
     */
    public Metric findMetricById(String id) {
        MetricEntity metricEntity = metricRepository.findById(id);
        if (metricEntity == null)
        {return null;}
        return metricEntity.getMetricModel();
    }
}
