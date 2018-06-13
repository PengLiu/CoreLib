package org.coredata.core.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import javax.annotation.Resource;

import org.coredata.core.TestApp;
import org.coredata.core.metric.documents.Metric;
import org.coredata.core.model.mining.DataminingModel;
import org.coredata.core.stream.mining.MiningDataFlow;
import org.coredata.core.stream.mining.PrepareMiningDataFlow;
import org.coredata.core.stream.mining.SysMiningModelFlow;
import org.coredata.core.stream.mining.entity.MiningData;
import org.coredata.core.stream.service.MiningStreamService;
import org.coredata.core.stream.service.StreamService;
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

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializerSettings;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class DataMiningTest {

	@Autowired
	private RedisService redisService;

	@Autowired
	private StreamConfig streamConfig;

	@Resource(type = MiningStreamService.class)
	private StreamService miningService;

	private ActorMaterializer mat;

	private Source<String, NotUsed> source;

	private List<String> transformSources = new ArrayList<>();

	private SysMiningModelFlow<String, String> miningModelFlow;

	private PrepareMiningDataFlow<String, List<MiningData>> prepareFlow;

	private MiningDataFlow<List<MiningData>, List<Metric>> miningFlow;

	@Before
	public void init() {
		ActorSystem system = ActorSystem.create("test");
		//单元测试，初始化模型信息
		this.mat = ActorMaterializer.create(ActorMaterializerSettings.create(system).withInputBuffer(64, 64), system);
		String path = TestApp.class.getClassLoader().getResource("source/transform_linux.txt").getPath();
		File file = new File(path);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = br.readLine()) != null)
				transformSources.add(line);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		this.source = Source.from(transformSources);
		this.miningModelFlow = new SysMiningModelFlow<>(redisService, streamConfig.getSubMining());
		this.prepareFlow = new PrepareMiningDataFlow<>(miningService);
		this.miningFlow = new MiningDataFlow<>(redisService, miningService);
		//初始化挖掘模型
		String mining = "{\"id\":\"linux\",\"isSystem\":1,\"mining\":[{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${sysbaseinfo}\",\"sourceres\":\"linux\"}],\"id\":\"OSVersion\",\"name\":\"操作系统内核版本\",\"type\":{\"exp\":[{\"metric\":\"KernelVersion:stringSplit(a,' ', 2)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.2.1.1.1.0}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${memoryData}\",\"sourceres\":\"ucd_host\"}],\"id\":\"memoryData\",\"name\":\"内存数据\",\"type\":{\"exp\":[{\"metric\":\"MEMRate:(lite(a)-lite(b)-lite(c)-lite(d))/lite(a)*100\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.4.5.0}\"},{\"key\":\"b\",\"value\":\"${A}.${1.3.6.1.4.1.2021.4.6.0}\"},{\"key\":\"c\",\"value\":\"${A}.${1.3.6.1.4.1.2021.4.14.0}\"},{\"key\":\"d\",\"value\":\"${A}.${1.3.6.1.4.1.2021.4.15.0}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${CPUPercentageData}\",\"sourceres\":\"ucd_host\"}],\"id\":\"CPUPercentageData\",\"name\":\"CPU百分比数据\",\"type\":{\"exp\":[{\"metric\":\"CPURate:100-avg(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.11.11.0}\"}]},{\"metric\":\"SystemModeCPURate:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.11.10.0}\"}]},{\"metric\":\"UserModeCPURate:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.11.9.0}\"}]},{\"metric\":\"CPUIdleTimePercent:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.11.11.0}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${DeadProcessNum}\",\"sourceres\":\"ucd_host\"}],\"id\":\"DeadProcessNum\",\"name\":\"僵死进程数\",\"type\":{\"exp\":[{\"metric\":\"NumOfZombiePro:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${countOfZombieProcess}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${HarddiskNum}\",\"sourceres\":\"ucd_host\"}],\"id\":\"HarddiskNum\",\"name\":\"硬盘个数\",\"type\":{\"exp\":[{\"metric\":\"NumberOfHardDisk:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${countOfDisk}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${MemoryInOutRate}\",\"sourceres\":\"ucd_host\"}],\"id\":\"MemoryInOutRate\",\"name\":\"内存调进调出速率\",\"type\":{\"exp\":[{\"metric\":\"MemPageInRate:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${pageInSpeed}\"}]},{\"metric\":\"MemPageOutRate:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${pageOutSpeed}\"}]},{\"metric\":\"PageSwapRate:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${pageSwapSpeed}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${PartitionSSHData}\",\"sourceres\":\"ucd_host\"}],\"id\":\"PartitionSSHData\",\"name\":\"分区数据\",\"type\":{\"exp\":[{\"metric\":\"TotalFSSpace:sum(a)/1024\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${capacity}\"}]},{\"metric\":\"NumberOfFileSystem:countByKey(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${capacity}\"}]},{\"metric\":\"FSUtilRatio:sum(b)/sum(a)*100\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${capacity}\"},{\"key\":\"b\",\"value\":\"${A}.${used}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${memSwapData}\",\"sourceres\":\"ucd_host\"}],\"id\":\"memSwapData\",\"name\":\"Swap Space数据\",\"type\":{\"exp\":[{\"metric\":\"SwapSpaceUtilRatio:(lite(a)-lite(b))/lite(a)*100\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.4.3.0}\"},{\"key\":\"b\",\"value\":\"${A}.${1.3.6.1.4.1.2021.4.4.0}\"}]},{\"metric\":\"UsedPagingSpaceSize:(lite(a)-lite(b))/1024\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.4.3.0}\"},{\"key\":\"b\",\"value\":\"${A}.${1.3.6.1.4.1.2021.4.4.0}\"}]},{\"metric\":\"TotalSwapSpace:lite(a)/1024\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.4.3.0}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${laLoad}\",\"sourceres\":\"ucd_host\"}],\"id\":\"laLoad\",\"name\":\"CPU平均负载\",\"type\":{\"exp\":[{\"metric\":\"CPULoad1m:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.10.1.3.1}\"}]},{\"metric\":\"CPULoad5m:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.10.1.3.2}\"}]},{\"metric\":\"CPULoad15m:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.10.1.3.3}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${HDReadandWriteRate}\",\"sourceres\":\"ucd_host\"}],\"id\":\"HDReadandWriteRate\",\"name\":\"硬盘平均读写速率\",\"type\":{\"exp\":[{\"metric\":\"HDReadRate:keepPostive(speed(sum(a),'s'),0)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.13.15.1.1.12}\"}]},{\"metric\":\"HDWriteRate:keepPostive(speed(sum(a),'s'),0)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.4.1.2021.13.15.1.1.13}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${NumberOfCPU}\",\"sourceres\":\"host\"}],\"id\":\"NumberOfCPU\",\"name\":\"CPU个数\",\"type\":{\"exp\":[{\"metric\":\"NumberOfCPU:countByKey(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.2.1.25.3.3.1.2}\"}]}],\"interval\":\"\",\"method\":\"\",\"period\":\"\"}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${snmpProtocolStatus}\",\"sourceres\":\"host\"}],\"id\":\"snmpProtocolStatus\",\"name\":\"SNMP协议状态算法\",\"type\":{\"exp\":[{\"metric\":\"SnmpAvailStatus:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.2.1.1.2.0}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${pingStatus}\",\"sourceres\":\"host\"}],\"id\":\"pingStatus\",\"name\":\"Ping数据算法\",\"type\":{\"exp\":[{\"metric\":\"PingAvailStatus:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${pingstatus}\"}]},{\"metric\":\"DelayedPing:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${avg_time}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${sysUpTime}\",\"sourceres\":\"host\"}],\"id\":\"sysUpTime\",\"name\":\"持续运行时间算法\",\"type\":{\"exp\":[{\"metric\":\"SystemUptime:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.2.1.25.1.1.0}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${icmpMsgs}\",\"sourceres\":\"host\"}],\"id\":\"icmpMsgs\",\"name\":\"ICMP包速率算法\",\"type\":{\"exp\":[{\"metric\":\"ICMPPacketRate:keepPostive(speed(a,'s'),0)+keepPostive(speed(b,'s'),0)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.2.1.5.1.0}\"},{\"key\":\"b\",\"value\":\"${A}.${1.3.6.1.2.1.5.14.0}\"}]},{\"metric\":\"InICMPPacketRate:keepPostive(speed(a,'s'),0)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.2.1.5.1.0}\"}]},{\"metric\":\"OutICMPPacketRate:keepPostive(speed(a,'s'),0)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.2.1.5.14.0}\"}]}]}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${hrStorageEntry}\",\"sourceres\":\"host\"}],\"id\":\"TotalMemGB\",\"name\":\"物理内存容量\",\"type\":{\"exp\":[{\"metric\":\"TotalMemGB:matchCondition(indexFilter(a,\\\"'{a}' == '1.3.6.1.2.1.25.2.1.2'\\\"),\\\"sumArray({b},{c},'*')/1024/1024/1024\\\")\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.2.1.25.2.3.1.2}\"},{\"key\":\"b\",\"value\":\"${A}.${1.3.6.1.2.1.25.2.3.1.4}\"},{\"key\":\"c\",\"value\":\"${A}.${1.3.6.1.2.1.25.2.3.1.5}\"}]}],\"interval\":\"\",\"method\":\"\",\"period\":\"\"}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${hrDeviceData}\",\"sourceres\":\"linux\"}],\"id\":\"NumberOfHardDisk\",\"name\":\"硬盘个数\",\"type\":{\"exp\":[{\"metric\":\"NumberOfHardDisk:matchCondition(indexFilter(a,\\\"{a} == 3\\\"),\\\"countByKey({a})\\\")\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.2.1.25.3.6.1.2}\"}]}],\"interval\":\"\",\"method\":\"\",\"period\":\"\"}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${NumberOfNIC}\",\"sourceres\":\"linux\"}],\"id\":\"NumberOfNIC\",\"name\":\"接口数\",\"type\":{\"exp\":[{\"metric\":\"NumberOfNIC:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.2.1.2.1.0}\"}]}],\"interval\":\"\",\"method\":\"\",\"period\":\"\"}},{\"category\":\"stream\",\"datasource\":[{\"id\":\"A\",\"sourcecmd\":\"cmd.${NumberOfProcess}\",\"sourceres\":\"host\"}],\"id\":\"NumberOfProcess\",\"name\":\"进程数\",\"type\":{\"exp\":[{\"metric\":\"NumberOfProcess:lite(a)\",\"param\":[{\"key\":\"a\",\"value\":\"${A}.${1.3.6.1.2.1.25.1.6.0}\"}]}],\"interval\":\"\",\"method\":\"\",\"period\":\"\"}}],\"name\":\"Linux\",\"origin\":\"ucd_unix\",\"type\":\"mining\",\"version\":\"1.0\"}";
		DataminingModel m = JSON.parseObject(mining, DataminingModel.class);
		redisService.saveData(RedisService.MINING, "linux", m);
		//初始化资产数据
		ResEntity entity = new ResEntity();
		Map<String, Object> props = new HashMap<>();
		props.put("dataminingId", "linux");
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

	/**
	 * 用于数据挖掘测试
	 */
	@Test
	public void dataMiningTest() {
		Sink<List<Metric>, CompletionStage<Done>> printlnSink = Sink.<List<Metric>> foreach(chunk -> {
			chunk.forEach(c -> System.err.println("挖掘后数据：" + JSON.toJSONString(c)));
		});
		source.via(this.miningModelFlow).via(this.prepareFlow).via(this.miningFlow).to(printlnSink).run(mat);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
