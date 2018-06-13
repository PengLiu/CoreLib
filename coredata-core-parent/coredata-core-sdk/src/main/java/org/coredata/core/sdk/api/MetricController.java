package org.coredata.core.sdk.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.metric.documents.Metric;
import org.coredata.core.metric.services.MetricService;
import org.coredata.core.metric.vos.MetricVal;
import org.coredata.core.sdk.api.common.ResponseMap;
import org.coredata.util.query.TimeRange;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@RestController
@RequestMapping("/ap1/v1/metric")
public class MetricController {
    @Autowired
    MetricService metricService;

    private ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(path = "/last/entities",method = RequestMethod.GET)
    public ResponseMap findLastMetricsByEntities(@RequestBody String condition){
        ResponseMap badResult = ResponseMap.BadRequestInstance();
        if (StringUtils.isEmpty(condition)) {
            badResult.setMessage("查询条件不能为空");
            return badResult;
        }
        String[] instIdArray = null;
        try {
            List<String>   instIds = mapper.readValue(condition, List.class);
            instIdArray = instIds.toArray(new String[0]);
        } catch (Exception e) {
            badResult.setMessage("查询条件错误:" + e.getMessage());
            return badResult;
        }
        try {
            Map<String, Collection<Metric>> metrics = metricService.findLastMetricsByEntities(instIdArray);

            ResponseMap result = ResponseMap.SuccessInstance();
            result.setResult(metrics);
            return result;
        } catch (Exception e) {
            badResult.setMessage("查询错误:" + e.getMessage());
            return badResult;
        }
    }

    @RequestMapping(path = "/his/metrics/entities",method = RequestMethod.GET)
    public ResponseMap findHisByMetricIdsAndEntities(@RequestBody String condition){

        try {
            Map<String, Map<String, Collection<MetricVal>>> resultMap = new HashMap<>();

            JsonNode json = mapper.readTree(condition);

            long startMs = json.get("startMs").asLong();
            long endMs = json.get("endMs").asLong();
            int size = json.get("interval").asInt();
            int interval = Integer.valueOf((endMs-startMs)/(1000*size)+"");

            ArrayNode arrary = (ArrayNode) json.get("data");
            arrary.forEach(item -> {
                String instId = item.get("instId").asText();

                ArrayNode metricJson = (ArrayNode) item.get("metrics");
                String[] metrics = new String[metricJson.size()];

                for (int i = 0; i < metricJson.size(); i++) {
                    metrics[i]=(metricJson.get(i).asText());
                }
                TimeRange timeRange = new TimeRange(startMs, endMs,DateHistogramInterval.seconds(interval));
                resultMap.put(instId, metricService.loadMetricsByEntityAndTimeRange(instId, metrics, timeRange,new String[]{Metric.ALG_AVG}));
            });
            ResponseMap result = ResponseMap.SuccessInstance();
            result.setResult(resultMap);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            ResponseMap badResult = ResponseMap.BadRequestInstance();
            badResult.setMessage("查询错误:" + e.getMessage());
            return badResult;
        }

    }

    @RequestMapping(path = "/max/byentities",method = RequestMethod.GET)
    public ResponseMap loadMaxMetricByEntitiesAndTimeRange(@RequestBody String condition){

        try {

            JsonNode json = mapper.readTree(condition);

            long startMs = json.get("startMs").asLong();
            long endMs = json.get("endMs").asLong();
            String metric = json.get("metric").asText();

            ArrayNode arrary = (ArrayNode) json.get("entityIds");
            List<String> entityIds = new ArrayList();
            arrary.forEach(item -> {
                String entityId = item.get("entityId").asText();
                entityIds.add(entityId);
            });
            TimeRange timeRange = new TimeRange(startMs, endMs);
            ResponseMap result = ResponseMap.SuccessInstance();
            result.setResult(metricService.loadMaxMetricByEntitiesAndTimeRange(entityIds.toArray(new String[]{}), metric, timeRange));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            ResponseMap badResult = ResponseMap.BadRequestInstance();
            badResult.setMessage("查询错误:" + e.getMessage());
            return badResult;
        }

    }

    @GetMapping(path="/last/entities/metricid")
    public ResponseMap findLastByEntitiesAndMetric(@RequestBody String condition){
        try {

            JsonNode json = mapper.readTree(condition);

            String metric = json.get("metric").asText();

            ArrayNode arrary = (ArrayNode) json.get("entityIds");
            List<String> entityIds = new ArrayList();
            arrary.forEach(item -> {
                String entityId = item.asText();
                entityIds.add(entityId);
            });
            ResponseMap result = ResponseMap.SuccessInstance();
            result.setResult(metricService.findLastByEntitiesAndMetric(entityIds.toArray(new String[]{}), metric));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            ResponseMap badResult = ResponseMap.BadRequestInstance();
            badResult.setMessage("查询错误:" + e.getMessage());
            return badResult;
        }
    }

    @GetMapping(path="/entities/metric/timerange/alg")
    public ResponseMap loadMetricByEntitiesAndTimeRange(@RequestBody String condition){
        try {

            JsonNode json = mapper.readTree(condition);

            String metric = json.get("metric").asText();

            ArrayNode arrary = (ArrayNode) json.get("entityIds");
            List<String> entityIds = new ArrayList();
            arrary.forEach(item -> {
                String entityId = item.asText();
                entityIds.add(entityId);
            });
            long startMs = json.get("startMs").asLong();
            long endMs = json.get("endMs").asLong();
            int size = json.get("interval")==null?1:json.get("interval").asInt();
            ResponseMap result = ResponseMap.SuccessInstance();
            TimeRange timeRange = new TimeRange(startMs, endMs, DateHistogramInterval.seconds(Integer.valueOf(((endMs-startMs)/(size*1000))+"")));
            result.setResult(metricService.loadMetricByEntitiesAndTimeRange(entityIds.toArray(new String[]{}), metric,timeRange,new String[] { "avg", "max", "min" }));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            ResponseMap badResult = ResponseMap.BadRequestInstance();
            badResult.setMessage("查询错误:" + e.getMessage());
            return badResult;
        }
    }
}
