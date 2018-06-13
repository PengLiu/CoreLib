package org.coredata.core.framework.agentmanager.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.coredata.core.framework.agentmanager.api.client.ClientConstant;
import org.coredata.core.framework.agentmanager.cmds.*;
import org.coredata.core.framework.agentmanager.cmds.response.CollectResponse;
import org.coredata.core.framework.agentmanager.cmds.response.DetectResponse;
import org.coredata.core.framework.agentmanager.cmds.response.InstanceResponse;
import org.coredata.core.framework.agentmanager.cmds.response.TestResponse;
import org.coredata.core.framework.agentmanager.dto.ActionDto;
import org.coredata.core.framework.agentmanager.dto.AgentDto;
import org.coredata.core.framework.agentmanager.dto.ControllerDTO;
import org.coredata.core.framework.agentmanager.dto.InstanceDto;
import org.coredata.core.framework.agentmanager.entity.Agent;
import org.coredata.core.framework.agentmanager.page.PageParam;
import org.coredata.core.framework.agentmanager.repository.AgentRepository;
import org.coredata.core.framework.agentmanager.util.DatetimeUtil;
import org.coredata.core.framework.agentmanager.util.StringUtil;
import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;
import org.coredata.core.framework.agentmanager.websocket.WebsocketUtil;
import org.coredata.core.framework.coremanager.api.client.InstanceCallback;
import org.coredata.core.model.constants.ApiConstant;
import org.coredata.core.model.repositories.CollectionModelRepository;
import org.coredata.core.util.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 该类作为Agent管理Service类的实现
 *
 * @author sushi
 */
@Service
@Transactional
public class AgentManagerService {

    private static final Logger logger = Logger.getLogger(AgentManagerService.class);

    private static final String COMMON = ",";

    private DelayQueue<Agent> agentCache = new DelayQueue<>();

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgentTaskService agentTaskService;

//	@Autowired
//	private InstanceRestypeRepository instanceRestypeRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CollectionModelRepository collectionModelRepository;

    @Value("${usercenter}")
    private String userCenterAddr = null;

    @PostConstruct
    public void init() {

        List<Agent> agents = agentRepository.findAllAgents();
        for (Agent agent : agents) {
            agent.updateDeatTime();
            agentCache.add(agent);
        }
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (; ; ) {
                    try {
                        Agent agent = agentCache.take();
                        //清理task
                        agentTaskService.updateAgentTaskPending(agent.getIpAddress());
                        //删除agent节点
                        agentRepository.removeByIp(agent.getIpAddress());
                    } catch (InterruptedException e) {
                        ;
                    }
                }
            }
        }, 0, 60 * 1000);
    }


    public void updateAgentRecord(String ip) {
        agentCache.forEach(agent -> {
            if (agent.getIpAddress().equals(ip)) {
                agent.updateDeatTime();
            }
        });
    }

    /**
     * 该方法返回全部agent信息
     *
     * @return
     */

    public List<Agent> findAllAgent() {
        return agentRepository.findAllAgents();
    }

    /**
     * 该方法根据agentId返回相关设置信息
     *
     * @return
     */

    public Map<String, Object> findAgentSettingByAgentId(Long agentId) {
        return agentRepository.findAgentSettingByAgentId(agentId);
    }


    public PageParam<Agent> findPagingAgent(AgentDto dto) {
        PageParam<Agent> pageParam = agentRepository.findPagingAgent(dto);
        return pageParam;
    }

    /**
     * 该方法用于新增Agent信息，如果已经存在相同ip的agent，直接更新重连时间
     */

    public void addAgentInfo(LoginCmd cmd) {
        String ip = cmd.getIp();
        Agent agent = agentRepository.findAgentByIpCredential(ip, cmd.getCredentials());
        if (agent == null) {
            Object[] params = new Object[7];
            params[0] = cmd.getType();
            params[1] = ip;
            params[2] = ApiConstant.ON_LINE;
            params[3] = cmd.getCredentials();
            params[4] = DatetimeUtil.getCurrentTimestamp();
            params[5] = DatetimeUtil.getCurrentTimestamp();
            params[6] = StringUtil.join(cmd.getFeatures(), null);
            agentRepository.addAgentInfo(params);
            return;
        }
        //否则更新在线时间
        updateAgentLastOnlineTime(agent);
        //更新在线状态
        updateAgentStatus(agent.getId(), ApiConstant.ON_LINE);
        //更新协议列表
        updateAgentFeatures(agent.getId(), StringUtil.join(cmd.getFeatures(), null));
    }


    public int updateAgentLastOnlineTime(Agent agent) {
        Object[] params = new Object[2];
        params[0] = DatetimeUtil.getCurrentTimestamp();
        params[1] = agent.getId();
        return agentRepository.updateAgentLastOnlineTime(params);
    }


    public int updateAgentLastOfflineTime(Agent agent) {
        Object[] params = new Object[2];
        params[0] = DatetimeUtil.getCurrentTimestamp();
        params[1] = agent.getId();
        return agentRepository.updateAgentLastOfflineTime(params);
    }


    public void processCloseSession(String ip, String credential) {
        Agent agent = agentRepository.findAgentByIpCredential(ip, credential);
        if (agent == null) {
            return;
        }
        //否则更新离线时间
        updateAgentLastOfflineTime(agent);
        //更新离线状态
        updateAgentStatus(agent.getId(), ApiConstant.OFF_LINE);
    }


    public int updateAgentStatus(Long agentId, int status) {
        Object[] params = new Object[2];
        params[0] = status;
        params[1] = agentId;
        return agentRepository.updateAgentStatus(params);
    }


    public int updateAgentFeatures(Long agentId, String features) {
        Object[] params = new Object[2];
        params[0] = features;
        params[1] = agentId;
        return agentRepository.updateAgentFeatures(params);
    }

    /**
     * 该方法用于向Agent下发相关命令
     *
     * @return
     * @throws Exception
     */

    public String sendAgentCmd(String cmd, String... agentIps) throws Exception {
        String result = "";
        JSONObject json = JSON.parseObject(cmd);
        String action = json.getString(WebsocketConstant.ACTION);
        if (action == null) {
            throw new Exception("Cmd has error.Can not process.");
        }
        switch (action) {
            case WebsocketConstant.BUSINESS_INTEGRATION:
                WebsocketUtil.sendMessage(cmd, agentIps);
                break;
            case WebsocketConstant.ACTION_TEST://下发测试命令
                //向Agent发送相关命令
                TestCmd testCmd = new TestCmd().init(cmd);
                Map<String, TestCmd> testcmds = processAgentTest(testCmd);
                result = processCmdResults(testcmds, testCmd.getSeq(), false);
                break;
            case WebsocketConstant.ACTION_INSTANCE://下发实例化命令，此处稍加处理，经过不同方法处理不同协议
                InstanceCmd icmd = new InstanceCmd().init(cmd);
                Map<String, InstanceCmd> inscmds = processAgentInstance(icmd);
                result = processCmdResults(inscmds, icmd.getSeq(), true);
                break;
            case WebsocketConstant.ACTION_REALTIME_COLLECT:
                CollectNowCmd cnc = new CollectNowCmd().init(cmd);
                Map<String, CollectCmd> cncs = processAgentCollect(cnc);
                result = processCmdResults(cncs, cnc.getSeq(), false);
                break;
            case WebsocketConstant.ACTION_COLLECT://下发采集命令
                CollectCmd ccmd = new CollectCmd().init(cmd);
                Map<String, CollectCmd> ccmds = processAgentCollect(ccmd);
                result = processCmdResults(ccmds, ccmd.getSeq(), false);
                break;
            case WebsocketConstant.ACTION_DETECT:
                DetectCmd dcmd = new DetectCmd().init(cmd);
                dcmd = processAgentDetect(dcmd, agentIps);
                result = JSON.toJSONString(dcmd.getResponse());
                break;
            case WebsocketConstant.ACTION_ACTION:
                ActionCmd acmd = new ActionCmd().init(cmd);
                Map<String, ActionCmd> acmds = processAgentAction(acmd);
                result = processCmdResults(acmds, acmd.getSeq(), false);
                break;
            case WebsocketConstant.ACTION_RECEIVING:
                ReceivingCmd rcmd = new ReceivingCmd().init(cmd);
                processReceivingAction(rcmd);
                break;
            case WebsocketConstant.ACTION_SERVER:
                ServerCmd scmd = new ServerCmd().init(cmd);
                scmd = processServer(scmd);
                result = scmd.getResult();
                break;
        }
        return result;
    }

    /**
     * 该方法用于处理Agent登录请求
     *
     * @return
     */

    public String processAgentLogin(LoginCmd cmd) {
        //获取Agent请求的证书信息
        //		String credentials = cmd.getCredentials();
        //
        //		RestTemplate restTemplate = new RestTemplate();
        //		ResponseEntity<String> response = restTemplate.getForEntity(userCenterUrl + credentials, String.class);
        //		if (!response.getStatusCode().is2xxSuccessful()) {
        //			return WebsocketConstant.RESPONSE_CREDENTIALS_ERR;
        //		}
        //
        //		try {
        //			JsonNode resp = mapper.readTree(response.getBody());
        //			if (resp.get("status").asInt() != 0) {
        //				return WebsocketConstant.RESPONSE_CREDENTIALS_ERR;
        //			}
        //		} catch (IOException e) {
        //			return WebsocketConstant.RESPONSE_CREDENTIALS_ERR;
        //		}
        //否则将该Agent信息插入数据库
//		addAgentInfo(cmd);
        return WebsocketConstant.RESPONSE_OK;
    }

    /**
     * 该方法用于处理向Agent发送测试命令方法
     */
    private Map<String, TestCmd> processAgentTest(TestCmd cmd) throws Exception {
        Map<String, TestCmd> sendCmds = new HashMap<>();
        Map<String, TestCmd> results = new HashMap<>();
        List<Map<String, String>> connect = cmd.getConnections();//获取此次命令的连接信息
        List<Map<String, String>> testcase = cmd.getPrerequired();
        //此处将单份命令，拆分成多份
        connect.forEach(conn -> {
            String protocol = conn.get(ApiConstant.PROTOCOL);//获取对应协议名称
            Agent availableAgent = findAvailableAgent(protocol, conn);
            if (availableAgent == null) {//表明没有对应的Agent可用
                results.put(protocol, new TestCmd(ApiConstant.NO_AGENT));
                return;
            }
            TestCmd test = new TestCmd();
            test.getConnections().add(conn);
            test.getPrerequired().addAll(findTestCaseByProtocol(testcase, protocol));
            sendCmds.put(protocol + COMMON + availableAgent.getIpAddress(), test);
            results.put(protocol, test);
        });
        sendCmds.forEach((k, v) -> {
            v.setCmd(JSON.toJSONString(v));
            try {
                String[] agents = k.split(COMMON);
                String result = v.executeSyncTimeOut(agents[1], 10, TimeUnit.SECONDS);
                v.setResult(result);
            } catch (CommandException e) {
                logger.error("Wait Agent Response Time out.", e);
                //此处设置超时提示
                v.setResult(ApiConstant.TIME_OUT);
            }
        });
        return results;
    }

    /**
     * 该方法用于给Agent下发实例化命令
     *
     * @param cmd
     * @return
     * @throws Exception
     */
    private Map<String, InstanceCmd> processAgentInstance(InstanceCmd cmd) throws Exception {
        Map<String, InstanceCmd> sendCmds = new HashMap<>();
        Map<String, InstanceCmd> results = new HashMap<>();
        List<Map<String, String>> connect = cmd.getConnections();//获取此次命令的连接信息
        List<InstanceDto> instance = cmd.getInstance();//获取实例化内容，排除不需要发送的命令，如果包含snmp命令则特殊处理一下
        //此处将单份命令，拆分成多份
        connect.forEach(conn -> {
            String protocol = conn.get(ApiConstant.PROTOCOL);//获取对应协议名称
            Agent availableAgent = findAvailableAgent(protocol, conn);
            if (availableAgent == null) {//表明没有对应的Agent可用
                results.put(protocol, new InstanceCmd(ApiConstant.NO_AGENT));
                return;
            }
            if (instance.size() <= 0)//说明没有需要采集的命令实例化
            {
                return;
            }
            InstanceCmd inst = new InstanceCmd();
            inst.getConnections().add(conn);
            inst.getInstance().addAll(findCaseByProtocol(instance, protocol));
            sendCmds.put(protocol + COMMON + availableAgent.getIpAddress(), inst);
            results.put(protocol, inst);
        });
        //此处运行其它协议实例化命令
        sendCmds.forEach((k, v) -> {
            v.setCmd(JSON.toJSONString(v));
            try {
                String[] agents = k.split(COMMON);
                String result = v.executeSyncTimeOut(agents[1], 1, TimeUnit.MINUTES);//暂时写死超时时间
                v.setResult(result);
            } catch (CommandException e) {
                logger.error("Wait Agent Response Time out.", e);
                //此处设置超时提示
                v.setResult(ApiConstant.TIME_OUT);
            }
        });
        return results;
    }

    /**
     * 该方法用于处理给Agent下发采集命令
     *
     * @param cmd
     * @return
     */
    private Map<String, CollectCmd> processAgentCollect(CollectCmd cmd) {
        Map<String, CollectCmd> sendCmds = new HashMap<>();
        Map<String, CollectCmd> results = new HashMap<>();
        List<Map<String, String>> connections = cmd.getConnections();
        List<Map<String, Object>> collector = cmd.getCollector();
        connections.forEach(conn -> {
            String protocol = conn.get(ApiConstant.PROTOCOL);//获取对应协议名称
            //如果是没有agent对象的协议，此处做出判定
            Object[] params = new Object[1];
            params[0] = ApiConstant.ON_LINE;
            List<Agent> agents = agentRepository.findAgentByFeatures(protocol, params);
            if (agents == null)//表明没有对应的Agent可用
            {
                return;
            }
            CollectCmd ccmd = new CollectCmd();
            if (cmd instanceof CollectNowCmd) {
                ccmd = new CollectNowCmd();
            }
            ccmd.getConnections().add(conn);
            List<Map<String, Object>> collectors = findCollectorByProtocol(collector, protocol);
            if (CollectionUtils.isEmpty(collectors)) {
                return;
            }
            ccmd.getCollector().addAll(collectors);
            ccmd.setId(cmd.getId());
            sendCmds.put(protocol + COMMON + agents.get(0).getIpAddress(), ccmd);
            results.put(protocol, ccmd);
        });
        sendCmds.forEach((k, v) -> {
            v.setCmd(JSON.toJSONString(v));
            try {
                String[] agents = k.split(COMMON);
                String response = v.executeSyncTimeOut(agents[1], 10, TimeUnit.SECONDS);
                v.setResult(response);
            } catch (CommandException e) {
                logger.error("Wait Agent Response Time out.", e);
                //此处设置超时提示
                v.setResult(ApiConstant.TIME_OUT);
            }
        });
        return results;
    }

    /**
     * 该方法用于根据协议获取相关case
     *
     * @param tcase
     * @param protocol
     * @return
     */
    private List<InstanceDto> findCaseByProtocol(List<InstanceDto> tcase, String protocol) {
        List<InstanceDto> resultCases = new ArrayList<>();
        if (tcase.size() <= 0) {
            return null;
        }
        resultCases.addAll(tcase.stream().filter(t -> t.getProtocol().equals(protocol)).collect(Collectors.toList()));
        return resultCases;
    }

    /**
     * 该方法用于根据协议获取相关case
     *
     * @param tcase
     * @param protocol
     * @return
     */
    private List<Map<String, String>> findTestCaseByProtocol(List<Map<String, String>> tcase, String protocol) {
        List<Map<String, String>> resultCases = new ArrayList<>();
        if (tcase.size() <= 0) {
            return null;
        }
        resultCases.addAll(tcase.stream().filter(t -> t.get(ApiConstant.PROTOCOL).equals(protocol)).collect(Collectors.toList()));
        return resultCases;
    }

    /**
     * 该方法用于根据协议获取相关采集命令
     *
     * @param collectors
     * @param protocol
     * @return
     */
    private List<Map<String, Object>> findCollectorByProtocol(List<Map<String, Object>> collectors, String protocol) {
        List<Map<String, Object>> resultCases = new ArrayList<>();
        if (collectors.size() <= 0) {
            return null;
        }
        resultCases.addAll(collectors.stream().filter(t -> t.get(ApiConstant.PROTOCOL).equals(protocol)).collect(Collectors.toList()));
        return resultCases;
    }

    /**
     * 该方法用于处理instance返回结果，此处调用neo4j，存入相关数据
     *
     * @param results
     * @return
     */
    private String processCmdResults(Map<String, ? extends Command> results, String seq, boolean isInstance) {
        Map<String, Object> responseMap = new HashMap<>();//返回给前台的响应对象
        responseMap.put(WebsocketConstant.SEQ, seq);//设置响应seq内容
        if (results.size() <= 0) {
            if (isInstance)
                return JSON.toJSONString(responseMap);
            responseMap.put(WebsocketConstant.AGENT_RESULT, ApiConstant.SUCCESS_FLAG);
            List<Map<String, Object>> result = new ArrayList<>();
            result.add(responseMap);
            return JSON.toJSONString(result);
        }
        List<Map<String, Object>> responseList = new ArrayList<>();
        List<CollectResponse> colRes = new ArrayList<>();
        results.forEach((k, v) -> {
            Map<String, Object> resdetail = new HashMap<>();
            resdetail.put(ApiConstant.PROTOCOL, k);
            /**
             * 暂时注释此部分代码，用于不对协议进行处理
             * if (k.contains(ApiConstant.CONNECT)) {//处理协议相关内容
             String[] protocol = k.split(ApiConstant.CONNECT);
             resdetail.put(ApiConstant.PROTOCOL, protocol[1]);
             }
             */
            String result = v.getResult();
            if (result == null) {
                return;
            }
            //可能分为如下几种情况
            switch (result) {
                case ApiConstant.NO_AGENT://无对应Agent请求
                    resdetail.put(ApiConstant.STATUS, ApiConstant.NO_AGENT_FLAG);
                    responseList.add(resdetail);
                    break;
                case ApiConstant.TIME_OUT://如果请求超时
                    resdetail.put(ApiConstant.STATUS, ApiConstant.TIMEOUT_FLAG);
                    responseList.add(resdetail);
                    break;
                default:
                    v.processResult(result);
                    resdetail.put(ApiConstant.STATUS, ApiConstant.SUCCESS_FLAG);
                    if (v instanceof InstanceCmd) {
                        InstanceCmd icmd = (InstanceCmd) v;
                        InstanceResponse response = icmd.getResponse();
                        resdetail.put(ApiConstant.DETAILS, response.getResults());
                    } else if (v instanceof TestCmd) {
                        TestCmd tcmd = (TestCmd) v;
                        TestResponse response = tcmd.getResponse();
                        resdetail.put(ApiConstant.DETAILS, response.getTransformResults());
                    } else if (v instanceof CollectCmd) {
                        CollectCmd ccmd = (CollectCmd) v;
                        CollectResponse response = ccmd.getResponse();
                        colRes.add(response);//如果是采集命令，直接返回该结果
                    }
                    responseList.add(resdetail);
                    break;
            }
        });
        responseMap.put(ApiConstant.RESULTS, responseList);
        if (colRes.size() > 0) {
            return JSON.toJSONString(colRes);
        }
        return JSON.toJSONString(responseMap);
    }


    public List<Agent> findAgentByFeatures(String features) {
        Object[] params = new Object[1];
        params[0] = ApiConstant.ON_LINE;
        return agentRepository.findAgentByFeatures(features, params);
    }

    /**
     * 该方法用于处理检测相关命令
     *
     * @param cmd
     */
    private DetectCmd processAgentDetect(DetectCmd cmd, String... agentIps) throws CommandException {
        cmd.setCmd(JSON.toJSONString(cmd));
        try {
            String result = cmd.executeSyncTimeOut(agentIps[0], 20, TimeUnit.SECONDS);
            cmd.setResult(result);
        } catch (CommandException e) {
            logger.error("Wait Agent Response Time out.", e);
            //此处设置超时提示
            DetectResponse r = new DetectResponse(cmd, ApiConstant.TIME_OUT);
            cmd.setResult(JSON.toJSONString(r));
        } catch (Exception ex) {
            logger.error("Process Agent Detect Fail.", ex);
            //此处设置超时提示
            DetectResponse r = new DetectResponse(cmd, ApiConstant.FAIL);
            cmd.setResult(JSON.toJSONString(r));
        }
        cmd.processResult(cmd.getResult());
        return cmd;
    }


    public int updateAgentCurrentTask(Long agentId) {
        Object[] params = new Object[1];
        params[0] = agentId;
        return agentRepository.updateAgentCurrentTask(params);
    }


    @SuppressWarnings("unchecked")
    public Agent findAvailableAgent(String protocol, Object connections) {
        Agent ra = null;
        Object[] params = new Object[1];
        params[0] = ApiConstant.ON_LINE;
        //首先根据协议，获取对应最少任务数的agent TODO
        List<Agent> agents = agentRepository.findAgentByFeatures(protocol, params);
        //表明没有对应的Agent可用
        if (agents == null) {
            return ra;
        }
        List<Map<String, String>> connects = new ArrayList<>();
        if (connections instanceof List) {
            List<Map<String, String>> results = (List<Map<String, String>>) connections;
            connects.addAll(results);
        } else if (connections instanceof Map) {
            Map<String, String> result = (Map<String, String>) connections;
            connects.add(result);
        } else if (connections instanceof String) {
            List<Map<String, String>> results = JSON.parseObject(connections.toString(), List.class);
            connects.addAll(results);
        }
        String cmd = new DetectCmd().joinDetectCmd(connects);
        for (Agent agent : agents) {
            try {
                //首先拼接检测命令
                String result = sendAgentCmd(cmd, agent.getIpAddress());
                if (result == null || ClientConstant.NULL_VALUE.equals(result)) {
                    continue;
                }
                DetectResponse response = JSON.parseObject(result, DetectResponse.class);
                //如果测试结果不成功，则跳过此agent
                if (!ClientConstant.RESPONSE_SUCCESS_FLAG.equals(response.getResult())) {
                    continue;
                }
                ra = agent;
                break;
            } catch (Exception e) {
                logger.error("Bind task for this agent failed,continue other agent.", e);
                continue;
            }
        }
        return ra;
    }

    /**
     * 该方法用于处理给Agent下发动作命令
     *
     * @param cmd
     * @return
     */
    private Map<String, ActionCmd> processAgentAction(ActionCmd cmd) {
        Map<String, ActionCmd> sendCmds = new HashMap<>();
        Map<String, ActionCmd> results = new HashMap<>();
        List<Map<String, String>> connections = cmd.getConnections();
        List<ControllerDTO> controllers = cmd.getController();
        connections.forEach(conn -> {
            String protocol = conn.get(ApiConstant.PROTOCOL);//获取对应协议名称
            //如果是没有agent对象的协议，此处做出判定
            Object[] params = new Object[1];
            params[0] = ApiConstant.ON_LINE;
            List<Agent> agents = agentRepository.findAgentByFeatures(protocol, params);
            if (agents == null)//表明没有对应的Agent可用
                return;
            ActionCmd ccmd = new ActionCmd();
            ccmd.getConnections().add(conn);
            ccmd.getController().addAll(controllers.stream().filter(c -> protocol.equals(c.getProtocol())).collect(Collectors.toList()));
            ccmd.setId(cmd.getId());
            sendCmds.put(protocol + COMMON + agents.get(0).getIpAddress(), ccmd);
            results.put(protocol, ccmd);
        });
        sendCmds.forEach((k, v) -> {
            v.setCmd(JSON.toJSONString(v));
            try {
                String[] agents = k.split(COMMON);
                String response = v.executeSyncTimeOut(agents[1], 10, TimeUnit.SECONDS);
                v.setResult(response);
            } catch (CommandException e) {
                logger.error("Wait Agent Response Time out.", e);
                //此处设置超时提示
                v.setResult(ApiConstant.TIME_OUT);
            }
        });
        return results;
    }

    /**
     * 用于给Agent发送接收到的数据
     *
     * @param cmd
     */
    private void processReceivingAction(ReceivingCmd cmd) {
        String protocol = cmd.getProtocol();//对应协议
        Object[] params = new Object[1];
        params[0] = ApiConstant.ON_LINE;
        List<Agent> agents = agentRepository.findAgentByFeatures(protocol, params);
        if (agents == null)//表明没有对应的Agent可用
            return;
        String ipAddress = agents.get(0).getIpAddress();
        cmd.setCmd(JSON.toJSONString(cmd));
        cmd.executeAsyncCmd(ipAddress);
    }


    @Async
    public void getInstCmdResult(Map<String, Object> params, InstanceCallback callback) {
        String cmd = JSON.toJSONString(params);
        String result = null;
        try {
            result = sendAgentCmd(cmd);
            callback.process(result);
        } catch (Exception e) {
            logger.error("getInstCmdResult (" + cmd + "), result (" + result + ").", e);
        }
    }


    public String changeInstanceStatus(ActionDto dto) {
        String result = ApiConstant.FAIL;
//		String instId = dto.getInstId();
//		String oid = dto.getOid();
//		Integer state = dto.getState();
//		if (StringUtils.isEmpty(instId) || StringUtils.isEmpty(oid) || state == null)
//			return result;
//
//		InstanceRestype instance = (InstanceRestype) redisService.loadInstById(instId);
//		if (instance == null) {
//			instance = instanceRestypeRepository.findInstByInstId(instId);
//			if (instance == null)
//				return result;
//			redisService.saveInstByInstId(instance.getInstId(), instance);
//		}
//		String connections = instance.getConnections();
//		if (StringUtils.isEmpty(connections))
//			return result;
//		Object[] params = new Object[1];
//		params[0] = ApiConstant.ON_LINE;
//		List<Agent> agents = agentRepository.findAgentByFeatures(ApiConstant.SNMP_PROTOCOL, params);
//		if (agents == null)//表明没有对应的Agent可用
//			return result;
//		List<Map<String, String>> conns = JSON.parseObject(connections, new TypeReference<List<Map<String, String>>>() {
//		});
//		SnmpSetCmd setCmd = new SnmpSetCmd();
//		setCmd.setConnections(conns);
//		setCmd.setOid(oid);
//		setCmd.setState(state);
//		setCmd.setCmd(JSON.toJSONString(setCmd));
//		try {
//			result = setCmd.executeSync(agents.get(0).getIpAddress());
//			JSONObject response = JSON.parseObject(result);
//			result = response.get(WebsocketConstant.AGENT_RESULT).toString();
//		} catch (CommandException e) {
//			logger.error("ChangeInstanceStatus ExecuteSync Fail.", e);
//		}
        return result;
    }


    public List<Agent> findAgentsByCustomerId(String customerId) {
        if (StringUtils.isEmpty(customerId)) {
            return null;
        }
        String id = customerId.split("/")[0];
        return agentRepository.findAgentsByCustomerId(id);
    }


    public void processReceivingData(String data) {
//		JSONObject params = JSON.parseObject(data);
//		Object customerId = params.get("customerId");
//		Object ip = params.get("mainIp");
//		if (customerId == null || ip == null)
//			return;
//		String cusId = customerId.toString();//获取对应资产id
//		String mainIp = ip.toString();
//		if (StringUtils.isEmpty(cusId))
//			return;
//		cusId = "^(" + cusId + ").*$";
//		List<InstanceRestype> instances = instanceRestypeRepository.findAllInstanceByIp(mainIp, cusId);
//		if (CollectionUtils.isEmpty(instances))
//			return;
//		for (InstanceRestype instance : instances) {
//			if (instance == null)
//				return;
//			String modelId = instance.getModelId();//获取采集模型id
//			String instanceId = instance.getInstId();
//			//根据采集模型id获取对应采集模型
//			CollectionEntity colEntity = collectionModelRepository.findById(modelId);
//			if (colEntity == null)
//				return;
//			CollectionModel model = colEntity.getDecryptModel();
//			Object result = params.get("result");
//			if (result == null)
//				return;
//			List<Collector> collectors = model.getCollector();
//			List<Map<String, Object>> colls = new ArrayList<>();
//			JSONObject resultObj = JSON.parseObject(result.toString());
//			for (Collector c : collectors) {
//				String type = c.getType();
//				if (!ApiConstant.RECEIVING_PROTOCOL.equals(type))
//					continue;
//				Map<String, Object> coll = new HashMap<>();
//				coll.put(com.coredata.coremanager.api.client.ClientConstant.COLLECT_ID, c.getId());
//				coll.put(com.coredata.coremanager.api.client.ClientConstant.PROTOCOL, type);
//				coll.put(com.coredata.coremanager.api.client.ClientConstant.MODEL_ID, modelId);
//				coll.put(com.coredata.coremanager.api.client.ClientConstant.RESPONSE_RESULT, resultObj.toString());
//				coll.put(com.coredata.coremanager.api.client.ClientConstant.INSTANCERESTYPE_ID, instanceId);
//				colls.add(coll);
//			}
//			//开始拼接下发任务
//			Map<String, Object> cmd = new HashMap<>();
//			cmd.put(WebsocketConstant.ACTION, WebsocketConstant.ACTION_RECEIVING);
//			cmd.put(ApiConstant.PROTOCOL, ApiConstant.RECEIVING_PROTOCOL);
//			cmd.put(com.coredata.coremanager.api.client.ClientConstant.COLLECT_ID, MethodUtil.md5(instanceId));//此处放置主资源的instanceId，有可能是多根资源情况
//			cmd.put(com.coredata.coremanager.api.client.ClientConstant.COLLECTOR, colls);
//			try {
//				sendAgentCmd(JSON.toJSONString(cmd));
//			} catch (Exception e) {
//				logger.error("sendAgentCmd (" + JSON.toJSONString(cmd) + ") error.", e);
//			}
//		}
    }


    public String processServerRequest(String params) {
        try {
            JSONObject param = JSON.parseObject(params);
            int port = param.getIntValue("server_port");
            String serverName = param.getString("server_name");
            String protocol = param.getString("server_protocol");
            String seq = param.getString(WebsocketConstant.SEQ);
            int status = param.getIntValue("server_status");
            Map<String, Object> cmd = new HashMap<String, Object>();
            cmd.put(WebsocketConstant.ACTION, WebsocketConstant.ACTION_SERVER);
            cmd.put(ApiConstant.PROTOCOL, protocol);
            cmd.put(WebsocketConstant.SERVER_PORT, port);
            cmd.put(WebsocketConstant.SERVER_NAME, serverName);
            cmd.put(WebsocketConstant.SERVER_STATUS, status);
            cmd.put(WebsocketConstant.SEQ, seq);
            String cmdResult = sendAgentCmd(JSON.toJSONString(cmd));
            return cmdResult;
        } catch (Exception e) {
            logger.error("processServerRequest(" + params + ") error.", e);
        }
        return ApiConstant.SUCCESS;
    }

    /**
     * 用于给Agent发送启动服务的命令
     *
     * @param cmd
     */
    private ServerCmd processServer(ServerCmd cmd) {
        Map<String, Object> result = new HashMap<>();
        result.put(WebsocketConstant.ACTION, cmd.getAction());
        result.put(WebsocketConstant.SEQ, cmd.getSeq());
        result.put(WebsocketConstant.AGENT_RESULT, "0");
        String protocol = cmd.getProtocol();//对应协议
        Object[] params = new Object[1];
        params[0] = ApiConstant.ON_LINE;
        List<Agent> agents = agentRepository.findAgentByFeatures(protocol, params);
        if (agents == null) {//表明没有对应的Agent可用
            result.put(WebsocketConstant.RESULT_MSG, "无可用的Agent");
            cmd.setResult(JSON.toJSONString(result));
            return cmd;
        }
        cmd.setCmd(JSON.toJSONString(cmd));
        try {
            String cmdResult = cmd.executeSyncTimeOut(agents.get(0).getIpAddress(), 20, TimeUnit.SECONDS);
            cmd.setResult(cmdResult);
        } catch (CommandException e) {
            logger.error("Wait Agent Response Time out.", e);
            result.put(WebsocketConstant.RESULT_MSG, "等待Agent响应超时");
            cmd.setResult(JSON.toJSONString(result));
        } catch (Exception ex) {
            logger.error("Process Agent Detect Fail.", ex);
            result.put(WebsocketConstant.RESULT_MSG, "发送Agent命令错误");
            cmd.setResult(JSON.toJSONString(result));
        }
        return cmd;
    }


    public void sysNotSupportCmds(String instId, String cmds) {
        redisService.saveData(RedisService.INSTANCE, instId, cmds);
    }
}