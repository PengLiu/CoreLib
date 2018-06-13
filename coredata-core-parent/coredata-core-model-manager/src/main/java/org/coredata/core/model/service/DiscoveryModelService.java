package org.coredata.core.model.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.coredata.core.model.common.Restype;
import org.coredata.core.model.common.VendorFirm;
import org.coredata.core.model.common.VendorType;
import org.coredata.core.model.constants.ClientConstant;
import org.coredata.core.model.discovery.Conditioncheck;
import org.coredata.core.model.discovery.Discovery;
import org.coredata.core.model.discovery.DiscoveryModel;
import org.coredata.core.model.discovery.Instance;
import org.coredata.core.model.entities.DiscoveryEntity;
import org.coredata.core.model.entity.ConnectionInfo;
import org.coredata.core.model.repositories.DiscoveryModelRepository;
import org.coredata.core.util.common.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@Transactional
public class DiscoveryModelService {

    private final Logger logger = LoggerFactory.getLogger(DiscoveryModelService.class);

    private static final String PROTOCOL = "protocol";

    private static final String CONTENT = "content";

    private static final String SNMP_ORIGIN = "snmp";

    private static final String ID = "id";

    private static final String VALUE = "value";

    private static final String DETAILS = "details";

    private static final String TITLE = "title";

    private static final String DES = "des";

    private static final String SYSOID_CMD = "1.3.6.1.2.1.1.2.0";

    private static final String SYSOID_NAME = "systemoid";

    private static final String WIRELESSAC_ID = "wirelessac";

    @Autowired
    private DiscoveryModelRepository discoveryModelRepository;

    @Autowired
    private VendorService vendorService;

    @Autowired
    private RestypeService restypeService;

    ParserContext parserContext = new ParserContext() {
        @Override
        public boolean isTemplate() {
            return true;
        }

        @Override
        public String getExpressionPrefix() {
            return "${";
        }

        @Override
        public String getExpressionSuffix() {
            return "}";
        }
    };


    public DiscoveryModel findById(String id) {
        DiscoveryEntity entity = discoveryModelRepository.findById(id);
        if (entity == null) {
            return null;
        }
        return entity.getDecryptModel();
    }


    public List<DiscoveryModel> findByRestype(String restype) {
        List<DiscoveryModel> models = new ArrayList<>();
        List<DiscoveryEntity> entitys = discoveryModelRepository.findByRestype(restype);
        if (CollectionUtils.isEmpty(entitys)) {
            return models;
        }
        entitys.forEach(entity -> models.add(entity.getDecryptModel()));
        return models;
    }


    public void save(DiscoveryModel model) {
        if (model == null) {
            return;
        }
        String id = model.getId();
        DiscoveryEntity disEntity = discoveryModelRepository.findById(id);
        if (disEntity != null) {
            discoveryModelRepository.delete(disEntity);
        }
        disEntity = new DiscoveryEntity();
        disEntity.setDisModel(model);
        discoveryModelRepository.save(disEntity);
    }


    public void deleteAll() {
        discoveryModelRepository.deleteAll();
    }


    public String findAgentTestContent(String discoverId) {
        DiscoveryModel model = findCommonDiscoveryModel(discoverId);
        if (model == null) {
            return null;
        }
        //获取全部测试项
        List<Conditioncheck> checks = model.getConditioncheck();
        List<Map<String, Object>> contents = new ArrayList<>();
        checks.forEach(c -> {
            Map<String, Object> result = new HashMap<>();
            result.put(PROTOCOL, c.getType());
            List<Map<String, Object>> showContents = new ArrayList<>();
            c.getField().forEach(f -> {
                Map<String, Object> content = new HashMap<>();
                content.put(TITLE, f.getId());
                content.put(DES, f.getTitle());
                List<Map<String, String>> details = new ArrayList<>();
                f.getResultarea().forEach(r -> {
                    Map<String, String> detail = new HashMap<>();
                    detail.put(ID, r.getId());
                    detail.put(VALUE, r.getTitle());
                    details.add(detail);
                });
                content.put(DETAILS, details);
                showContents.add(content);
            });
            result.put(CONTENT, showContents);
            contents.add(result);
        });
        return JSON.toJSONString(contents);
    }


    /**
     * 该方法用于下发获取sysoid方法
     */
    @SuppressWarnings("unchecked")
    private void processGetSysOidParams(Map<String, Object> params, ConnectionInfo info) {
        params.put(ClientConstant.SERVER_REQUEST_ACTION, ClientConstant.SERVER_REQUEST_ACTION_INSTANCE);
        params.put(ClientConstant.SERVER_REQUEST_SEQ, info.getSeq());
        //相关协议连接信息
        List<Map<String, String>> connections = new ArrayList<>();
        //相关实例化信息
        List<Map<String, String>> instance = new ArrayList<>();
        String connect = info.getConnect();
        JSONObject connJson = JSON.parseObject(connect);
        Set<String> protocols = connJson.keySet();
        for (String protocol : protocols) {//循环拼接相关命令
            Map<String, String> connection = (Map<String, String>) connJson.get(protocol);
            String realProtocol = protocol;
            connection.put(ClientConstant.PROTOCOL, realProtocol);
            connection.remove(ClientConstant.CONNECTION_DBTYPE);//去掉dbtype属性
            //此处将超时时间转换一下单位
            String timeout = connection.get(ClientConstant.TIME_OUT);
            if (timeout != null) {
                String resultTime = DateUtil.translateTimeOutUnit(Integer.parseInt(timeout), DateUtil.SECOND_UNIT);
                connection.put(ClientConstant.TIME_OUT, resultTime);
            }
            connections.add(connection);
            Map<String, String> ins = new HashMap<>();
            ins.put(ClientConstant.TEST_NAME, SYSOID_NAME);
            ins.put(ClientConstant.CMD, SYSOID_CMD);
            ins.put(ClientConstant.PROTOCOL, realProtocol);
            ins.put("collectType", "GET");
            instance.add(ins);
        }
        params.put(ClientConstant.SERVER_REQUEST_CONNECT, connections);
        params.put(ClientConstant.SERVER_REQUEST_ACTION_INSTANCE, instance);
    }


    public DiscoveryModel findCommonDiscoveryModel(String discoveryId) {
        //首先根据discoveryId获取是否有厂商集合
        List<VendorType> vendors = vendorService.findVendorTypeEntityByRestype(discoveryId);
        List<Map<String, String>> fs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(vendors)) {
            VendorType vendorType = vendors.get(0);
            List<VendorFirm> firms = vendorType.getFirms();
            for (VendorFirm firm : firms) {
                Map<String, String> f = new HashMap<>();
                f.put("id", firm.getId());
                f.put("name", firm.getName());
                fs.add(f);
            }
            VendorFirm firm = firms.get(0);
            discoveryId = firm.getId();
        }
        DiscoveryModel model = findById(discoveryId);
        if (model == null) {
            model = new DiscoveryModel();
            model.getFirms().addAll(fs);
            return model;
        }
        List<Discovery> discovery = model.getDiscovery();
        List<Conditioncheck> checks = model.getConditioncheck();
        String origin = model.getOrigin();
        if (CollectionUtils.isEmpty(discovery) || !StringUtils.isEmpty(origin)) {
            DiscoveryModel originModel = findById(origin);
            if (originModel != null) {
                discovery.addAll(originModel.getDiscovery());
                checks.addAll(originModel.getConditioncheck());
                //origin资源为主，其他资源为辅
                Collections.reverse(discovery);
                Collections.reverse(checks);
            }
        }
        model.getFirms().addAll(fs);
        return model;
    }


    public DiscoveryModel findVendorTypeDiscoveryModel(String discoveryId) {
        DiscoveryModel model = findById(discoveryId);
        if (model == null) {
            return model;
        }
        List<Discovery> discovery = model.getDiscovery();
        List<Conditioncheck> checks = model.getConditioncheck();
        String origin = model.getOrigin();
        if (CollectionUtils.isEmpty(discovery) || !StringUtils.isEmpty(origin)) {
            DiscoveryModel originModel = findById(origin);
            if (originModel != null) {
                discovery.addAll(originModel.getDiscovery());
                checks.addAll(originModel.getConditioncheck());
            }
        }
        return model;
    }


    public long findAllDiscoveryCount() {
        return discoveryModelRepository.count();
    }


    public void findByChildRestype(String restype, List<Map<String, String>> results) {
        Restype res = restypeService.findById(restype);
        if (res == null) {
            return;
        }
        List<DiscoveryModel> models = iteratorParentRestyps(res);
        if (CollectionUtils.isEmpty(models)) {
            return;
        }
        for (DiscoveryModel model : models) {
            List<Instance> instance = model.getInstance();
            Optional<Instance> optional = instance.stream().filter(i -> i.getRestype().equals(restype)).findAny();
            if (!optional.isPresent()) {
                continue;
            }
            Instance ins = optional.get();
            String sourcemodel = ins.getSourcemodel();
            Map<String, String> map = new HashMap<String, String>();
            map.put("modelId", sourcemodel);
            map.put("name", res.getName());
            results.add(map);
        }
    }

    private List<DiscoveryModel> iteratorParentRestyps(Restype res) {
        if (res == null) {
            return null;
        }
        String parentRes = res.getParentid();
        List<DiscoveryModel> models = findByRestype(parentRes);
        if (!CollectionUtils.isEmpty(models)) {
            return models;
        }
        Restype pres = restypeService.findById(parentRes);
        return iteratorParentRestyps(pres);
    }
}
