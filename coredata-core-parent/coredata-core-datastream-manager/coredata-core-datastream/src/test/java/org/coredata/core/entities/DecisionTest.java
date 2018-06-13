package org.coredata.core.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.TestApp;
import org.coredata.core.metric.documents.Metric;
import org.coredata.core.metric.services.MetricService;
import org.coredata.core.model.decision.DecisionModel;
import org.coredata.core.stream.decision.DecisionSink;
import org.coredata.core.stream.decision.SysDecisionModelFlow;
import org.coredata.core.stream.util.KafkaUtils;
import org.coredata.core.test.config.StreamConfig;
import org.coredata.core.util.redis.service.RedisService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializerSettings;
import akka.stream.Graph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class DecisionTest {

	@Autowired
	private RedisService redisService;

	@Autowired
	private StreamConfig streamConfig;

	@Autowired
	private MetricService metricService;

	private ActorMaterializer mat;

	private List<Metric> metricSource = new ArrayList<>();

	private Source<Metric, NotUsed> source;

	private SysDecisionModelFlow<List<Metric>, List<Metric>> decisionModelFlow;

	@SuppressWarnings("rawtypes")
	private Graph decisionSink;

	@Before
	public void init() {
		ActorSystem system = ActorSystem.create("test");
		//单元测试，初始化模型信息
		this.mat = ActorMaterializer.create(ActorMaterializerSettings.create(system).withInputBuffer(64, 64), system);
		String path = TestApp.class.getClassLoader().getResource("source/mining_linux.txt").getPath();
		File file = new File(path);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = br.readLine()) != null)
				metricSource.add(JSON.parseObject(line, Metric.class));
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		this.source = Source.from(metricSource);
		//决策流程
		this.decisionModelFlow = new SysDecisionModelFlow<>(redisService, streamConfig.getSubDecision());
		String kafkaAddr = KafkaUtils.getBrokers(streamConfig.getKafkaType(), streamConfig.getZkAddr());
		this.decisionSink = Sink.fromGraph(new DecisionSink<>(kafkaAddr, system, streamConfig.getParallelNum(), streamConfig.getAlarmTopic(), metricService));
		String decision = "{\"decision\":[{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"MemPageInRate\"]},\"enable\":false,\"id\":\"MemPageInRateAlarm\",\"name\":\"内存页面调进速率预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"内存页面调进速率严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${MemPageInRate}>3000\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"内存页面调进速率重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${MemPageInRate}>2000 && ${?}.${MemPageInRate}<=3000\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"MemPageOutRate\"]},\"enable\":false,\"id\":\"MemPageOutRateAlarm\",\"name\":\"内存页面调出速率预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"内存页面调出速率严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${MemPageOutRate}>3000\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"内存页面调出速率重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${MemPageOutRate}>2000 && ${?}.${MemPageOutRate}<=3000\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"PageSwapRate\"]},\"enable\":false,\"id\":\"PageSwapRateAlarm\",\"name\":\"内存页交换速率预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"内存页交换速率严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${PageSwapRate}>300000\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"内存页交换速率重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${PageSwapRate}>200000 && ${?}.${PageSwapRate}<=300000\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"CPURate\"]},\"enable\":false,\"id\":\"CPURateAlarm\",\"name\":\"CPU利用率预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"CPU利用率严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${CPURate}>90\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"CPU利用率重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${CPURate}>80 && ${?}.${CPURate}<=90\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"MEMRate\"]},\"enable\":false,\"id\":\"MEMRateAlarm\",\"name\":\"内存利用率预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"内存利用率严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${MEMRate}>90\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"内存利用率重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${MEMRate}>80 && ${?}.${MEMRate}<=90\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"CPULoad1m\"]},\"enable\":false,\"id\":\"CPULoad1mAlarm\",\"name\":\"CPU平均负载(1分钟)预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"CPU平均负载(1分钟)严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":false,\"exp\":\"${?}.${CPULoad1m}>80\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"CPU平均负载(1分钟)重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":false,\"exp\":\"${?}.${CPULoad1m}>70 && ${?}.${CPULoad1m}<=80\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"CPULoad5m\"]},\"enable\":false,\"id\":\"CPULoad5mAlarm\",\"name\":\"CPU平均负载(5分钟)预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"CPU平均负载(5分钟)严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":false,\"exp\":\"${?}.${CPULoad5m}>80\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"CPU平均负载(5分钟)重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":false,\"exp\":\"${?}.${CPULoad5m}>70 && ${?}.${CPULoad5m}<=80\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"SystemModeCPURate\"]},\"enable\":false,\"id\":\"SystemModeCPURateAlarm\",\"name\":\"CPU系统模式百分比预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"CPU系统模式百分比严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":false,\"exp\":\"${?}.${SystemModeCPURate}>80\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"CPU系统模式百分比重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":false,\"exp\":\"${?}.${SystemModeCPURate}>70 && ${?}.${SystemModeCPURate}<=80\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"UserModeCPURate\"]},\"enable\":false,\"id\":\"UserModeCPURateAlarm\",\"name\":\"CPU用户模式百分比预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"CPU用户模式百分比严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":false,\"exp\":\"${?}.${UserModeCPURate}>80\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"CPU用户模式百分比重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":false,\"exp\":\"${?}.${UserModeCPURate}>70 && ${?}.${UserModeCPURate}<=80\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"CPUIdleTimePercent\"]},\"enable\":false,\"id\":\"CPUIdleTimePercentAlarm\",\"name\":\"CPU空闲模式百分比预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"CPU空闲模式百分比严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":false,\"exp\":\"${?}.${CPUIdleTimePercent}<10\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"CPU空闲模式百分比重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":false,\"exp\":\"${?}.${CPUIdleTimePercent}<30 && ${?}.${CPUIdleTimePercent}>=10\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"SwapSpaceUtilRatio\"]},\"enable\":false,\"id\":\"SwapSpaceUtilRatioAlarm\",\"name\":\"Swap Space利用率预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"Swap Space利用率严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${SwapSpaceUtilRatio}>80\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"Swap Space利用率重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${SwapSpaceUtilRatio}>70 && ${?}.${SwapSpaceUtilRatio}<=80\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"HDReadRate\"]},\"enable\":false,\"id\":\"HDReadRateAlarm\",\"name\":\"硬盘平均读速率预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"硬盘平均读速率严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${HDReadRate}>15728640\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"硬盘平均读速率重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${HDReadRate}>10485760 && ${?}.${HDReadRate}<=15728640\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"HDWriteRate\"]},\"enable\":false,\"id\":\"HDWriteRateAlarm\",\"name\":\"硬盘平均写速率预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"硬盘平均写速率严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${HDWriteRate}>15728640\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"硬盘平均写速率重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${HDWriteRate}>10485760 && ${?}.${HDWriteRate}<=15728640\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"NumberOfCPU\"]},\"enable\":false,\"id\":\"NumberOfCPUAlarm\",\"name\":\"CPU个数变更预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":1,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"CPU个数变更预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${NumberOfCPU} != ${?}.${NumberOfCPU}(last)\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"NumberOfFileSystem\"]},\"enable\":false,\"id\":\"NumberOfFileSystemAlarm\",\"name\":\"分区个数变更预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":1,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"分区个数变更预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${NumberOfFileSystem} != ${?}.${NumberOfFileSystem}(last)\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"ICMPPacketRate\"]},\"enable\":false,\"id\":\"ICMPPacketRateAlarm\",\"name\":\"ICMP包速率预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"ICMP包速率严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":false,\"exp\":\"${?}.${ICMPPacketRate}>800\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"ICMP包速率重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":false,\"exp\":\"${?}.${ICMPPacketRate}>500 && ${?}.${ICMPPacketRate}<=800\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"DelayedPing\"]},\"enable\":false,\"id\":\"DelayedPingAlarm\",\"name\":\"Ping时延预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"Ping时延严重预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${DelayedPing}>1000\"},{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"2\",\"param\":[{\"key\":\"content\",\"value\":\"Ping时延重要预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${DelayedPing}>500 && ${?}.${DelayedPing}<=1000\"}]},{\"associatedres\":{\"instid\":[\"?\"],\"metric\":[\"SnmpAvailStatus\",\"PingAvailStatus\"]},\"enable\":true,\"id\":\"windows_Alarm\",\"name\":\"主机可用状态预警\",\"rule\":[{\"action\":[{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"type\":\"stateTransition\"},{\"enable\":true,\"flapping\":{\"count\":3,\"frequency\":\"30m\",\"type\":\"consecutive\"},\"level\":\"3\",\"param\":[{\"key\":\"content\",\"value\":\"主机可用状态异常预警\"}],\"type\":\"sendAlarm\"}],\"enable\":true,\"exp\":\"${?}.${SnmpAvailStatus}!='ok'&&${?}.${PingAvailStatus}!='ok'\"}]}],\"id\":\"linux\",\"isSystem\":1,\"name\":\"Linux\",\"origin\":\"ucd_unix\",\"type\":\"decision\",\"version\":\"1.0\"}";
		DecisionModel d = JSON.parseObject(decision, DecisionModel.class);
		redisService.saveData(RedisService.DECISION, "linux", d);
		//初始化资产数据
		ResEntity entity = new ResEntity();
		Map<String, Object> props = new HashMap<>();
		props.put("decisionId", "linux");
		entity.setEntityId("b6fec5658d783a092423bf55dcfd44b3");
		entity.setProps(props);
		Map<String, Object> conn = new HashMap<>();
		String snmpcon = "[{\"snmp_securityname\":\"\",\"snmp_authprotocol\":\"md5\",\"snmp_securitylevel\":\"1\",\"snmp_authpassword\":\"\",\"snmp_connretry\":\"1\",\"snmp_conntimeout\":\"2000\",\"snmp_readwritecommunity\":\"\",\"snmp_authprivatepassword\":\"\",\"snmp_authprivateprotocol\":\"des\",\"protocol\":\"snmp\",\"snmp_readonlycommunity\":\"public\",\"snmp_udpport\":\"161\",\"snmp_version\":\"1\",\"snmp_ip\":\"172.16.2.201\",\"snmp_contextname\":\"\"}]";
		List<Map<String, String>> conns = JSON.parseObject(snmpcon, new TypeReference<List<Map<String, String>>>() {
		});
		conn.put("snmp", conns);
		entity.setConn(conn);
		entity.setCreatedTime(System.currentTimeMillis());
		entity.setName("deta-snmpsim2-201");
		entity.setType("linux");
		redisService.saveData(RedisService.INSTANCE, "b6fec5658d783a092423bf55dcfd44b3", entity);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void decisionTest() {
		source.map(s -> Arrays.asList(s)).via(this.decisionModelFlow).to(this.decisionSink).run(mat);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
