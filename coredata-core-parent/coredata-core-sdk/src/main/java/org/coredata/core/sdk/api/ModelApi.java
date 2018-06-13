package org.coredata.core.sdk.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.coredata.core.model.action.model.Action;
import org.coredata.core.model.action.model.ActionModel;
import org.coredata.core.model.action.model.ActionModelVO;
import org.coredata.core.model.action.model.ActionVO;
import org.coredata.core.model.action.model.Controller;
import org.coredata.core.model.action.model.ControllerVO;
import org.coredata.core.model.common.Metric;
import org.coredata.core.model.discovery.DiscoveryModel;
import org.coredata.core.model.service.ActionService;
import org.coredata.core.model.service.CollectionService;
import org.coredata.core.model.service.DataminingService;
import org.coredata.core.model.service.DecisionService;
import org.coredata.core.model.service.DiscoveryModelService;
import org.coredata.core.model.service.ModelService;
import org.coredata.core.model.service.TransformService;
import org.coredata.core.sdk.api.common.ResponseMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@RestController
@RequestMapping("/api/v1/model")
public class ModelApi {

    private static final String CUSTOMER_ID = "customerId";

    private static final String COLLECT_MODEL = "collect";

    private static final String TRANSFORM_MODEL = "transform";

    private static final String DATAMINING_MODEL = "datamining";

    private static final String DECISION_MODEL = "decision";

    private static final String NULL_ARRAY = "[]";


    @Autowired
    DataminingService dataminingService;

    @Autowired
    CollectionService collectionService;

    @Autowired
    DiscoveryModelService discoveryService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private TransformService transformService;

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private ActionService actionService;

    private ObjectMapper mapper = new ObjectMapper();


    @GetMapping(value = "/metrics/{id}")
    public ResponseMap findLastMetricsByEntities(@PathVariable String id) {
        ResponseMap badResult = ResponseMap.BadRequestInstance();
        if (StringUtils.isEmpty(id)) {
            badResult.setMessage("查询条件不能为空");
            return badResult;
        }
        List<Map<String, String>> result = new ArrayList<>();
        try {
            result = dataminingService.findAllMetricInfo(id);
        } catch (Exception e) {
            badResult.setMessage("查询错误:" + e.getMessage());
            return badResult;
        }
        ResponseMap responseMap = ResponseMap.SuccessInstance();
        responseMap.setResult(result);
        return responseMap;
    }

    /**
     * 根据资产id实时数据采集
     *
     * @param customerId
     * @param instId
     * @param response
     */
    @GetMapping(value = "/metrics/realtime/{instId}")
    public void realtimeCollect(@RequestHeader(value = CUSTOMER_ID, required = false) String customerId, @PathVariable String instId,
                                HttpServletResponse response) {
        try {
            //            collectionService.processRealtimeCollect(customerId, instId);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 按restype返回发现模型ID和名称
     *
     * @return
     */
    @RequestMapping(value = "/discover/{restype}", method = RequestMethod.GET)
    public String findDiscoveryModelByRestype(@PathVariable String restype) {
        List<DiscoveryModel> models = discoveryService.findByRestype(restype);
        //暂时调整代码，支持sigma，物联网关模型id获取
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        if (CollectionUtils.isEmpty(models)) {
            discoveryService.findByChildRestype(restype, list);
            String jsonStr = JSON.toJSONString(list);
            return jsonStr;
        }
        for (DiscoveryModel model : models) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("modelId", model.getId());
            map.put("name", model.getName());
            list.add(map);
        }
        String jsonStr = JSON.toJSONString(list);
        return jsonStr;
    }

    /**
     * 根据资产类型返回发现模型id和name
     *
     * @param content
     * @return
     */
    //TODO:需要详细解释
    @RequestMapping(value = "/discover/restype", method = RequestMethod.POST)
    public String findDiscoveryModelByRestypes(@RequestBody String content) {

        try {
            JsonNode json = mapper.readTree(content);
            Iterator<JsonNode> restypes = json.iterator();
            List<Map<String, List<Map<String, String>>>> maplist = new ArrayList<Map<String, List<Map<String, String>>>>();
            while (restypes.hasNext()) {
                JsonNode restype = restypes.next();
                List<DiscoveryModel> models = discoveryService.findByRestype(restype.asText());
                List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                for (DiscoveryModel model : models) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("modelId", model.getId());
                    map.put("name", model.getName());
                    list.add(map);

                }

                Map<String, List<Map<String, String>>> map = new HashMap<String, List<Map<String, String>>>();
                map.put(restype.asText(), list);
                maplist.add(map);

            }

            String jsonStr = JSON.toJSONString(maplist);
            return jsonStr;
        } catch (Exception e) {
            return e.getMessage();
        }

    }

    /**
     * 该方法用于获取指标列表
     *
     * @param headers
     * @param restype
     * @param nodeLevel
     * @param joinType
     * @return
     */
    @RequestMapping(value = "/metrics/{restype}/{nodeLevel}/{joinType}", method = RequestMethod.GET)
    public String findMetricByRestype(@RequestHeader(required = false) HttpHeaders headers, @PathVariable String restype, @PathVariable String nodeLevel,
                                      @PathVariable String joinType) {
        return modelService.findMetricByRestype(restype, nodeLevel, joinType);
    }

    /**
     * 根据一组指标ID取得指标定义
     *
     * @param content
     * @param response
     * @return
     */
    @RequestMapping(value = "/metrics", method = RequestMethod.POST)
    public ResponseMap findMetricByIds(@RequestBody String content, HttpServletResponse response) {
        try {
            JsonNode json = mapper.readTree(content);
            String ids = json.get("ids").asText();
            ResponseMap responseMap = ResponseMap.SuccessInstance();
            Map<String, Metric> result = modelService.findMetricDefsByIds(ids.split(","));
            responseMap.setResult(result);
            return responseMap;
        } catch (IOException e) {
            ResponseMap badResult = ResponseMap.BadRequestInstance();
            badResult.setMessage(e.getMessage());
            return badResult;
        }
    }

    /**
     * 该方法用于根据模型id和类型返回该模型信息
     *
     * @return
     */
    @RequestMapping(value = "/models/{type}/{modelId}", method = RequestMethod.GET)
    public ResponseMap getAllModels(@PathVariable String type, @PathVariable String modelId) {
        String result = "";
        switch (type) {
            case COLLECT_MODEL:
                result = collectionService.findCollectionModelById(modelId);
                if ("[]".equals(result)) {
                    String mid = modelId.split("-")[0];
                    result = collectionService.findCollectionModelById(mid);
                }
                break;
            case TRANSFORM_MODEL:
                result = transformService.findTransformModelById(modelId);
                break;
            case DATAMINING_MODEL:
                result = dataminingService.findDataminingModelById(modelId);
                if ("[]".equals(result)) {
                    String mid = modelId.split("-")[0];
                    result = dataminingService.findDataminingModelById(mid);
                }
                break;
            case DECISION_MODEL:
                result = decisionService.findDecisionModelById(modelId);
                if ("[]".equals(result)) {
                    String mid = modelId.split("-")[0];
                    result = decisionService.findDecisionModelById(mid);
                }
                break;
            default:
                break;
        }
        ResponseMap responseMap = ResponseMap.SuccessInstance();
        responseMap.setResult(result);
        return responseMap;
    }


    /**
     * 返回发现测试页面的提示信息
     *
     * @return
     */
    @RequestMapping(value = "/{discoverId}/test/content", method = RequestMethod.GET)
    public ResponseMap findAgentTestContent(@PathVariable String discoverId) {
        String result = discoveryService.findAgentTestContent(discoverId);
        result = result == null ? NULL_ARRAY : result;
        ResponseMap responseMap = ResponseMap.SuccessInstance();
        responseMap.setResult(result);
        return responseMap;
    }

    /**
     * 该方法用于根据模型ID获取资产的控制模型
     */
    @RequestMapping(value = "/actions/bymodels", method = RequestMethod.GET)
    public String getActionsByModels(@RequestBody String content) {

        Map<String, ActionModel> actionModels = new HashMap<String, ActionModel>();
        Map<String, ActionModelVO> result = new HashMap<String, ActionModelVO>();

        try {
            JsonNode json = mapper.readTree(content);
            ArrayNode ids = (ArrayNode) json.get("modelIds");
            ids.forEach(id -> {
                ActionModel actionModel = actionModels.get(id.textValue());
                if (actionModel == null) {
                    actionModel = actionService.findActionModelByModelId(id.textValue());
                    actionModels.put(id.textValue(), actionModel);
                }
                if (actionModel != null) {
                    ActionModelVO mvo = new ActionModelVO();
                    mvo.setId(actionModel.getId());
                    List<ActionVO> avos = new ArrayList<ActionVO>();
                    for (Action action : actionModel.getAction()) {
                        ActionVO actionVO = new ActionVO();
                        actionVO.setId(action.getId());
                        actionVO.setName(action.getName());
                        actionVO.setDatatype(action.getDatatype());
                        actionVO.setSourcemetric(action.getSourcemetric());
                        List<ControllerVO> cvos = new ArrayList<ControllerVO>();
                        for (Controller controller : action.getController()) {
                            ControllerVO controllerVO = new ControllerVO();
                            controllerVO.setId(controller.getId());
                            controllerVO.setName(controller.getName());
                            controllerVO
                                    .setMetricvalue(controller.getParam().stream().filter(p -> "metricvalue".equals(p.getKey())).findFirst().get().getValue());
                            cvos.add(controllerVO);
                        }
                        actionVO.setController(cvos);
                        avos.add(actionVO);
                    }
                    mvo.setAction(avos);
                    result.put(id.textValue(), mvo);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JSON.toJSONString(result);
    }

}
