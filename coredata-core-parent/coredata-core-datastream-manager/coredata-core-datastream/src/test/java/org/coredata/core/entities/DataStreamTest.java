package org.coredata.core.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import javax.annotation.Resource;

import org.coredata.core.TestApp;
import org.coredata.core.model.transform.TransformModel;
import org.coredata.core.stream.service.StreamService;
import org.coredata.core.stream.service.TransformStreamService;
import org.coredata.core.stream.transform.FilterFlow;
import org.coredata.core.stream.transform.SysTransformModelFlow;
import org.coredata.core.stream.transform.functions.AbsFunction;
import org.coredata.core.stream.util.LookupTool;
import org.coredata.core.test.config.StreamConfig;
import org.coredata.core.util.redis.service.RedisService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializerSettings;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class DataStreamTest {

	@Autowired
	private RedisService redisService;

	@Autowired
	private StreamConfig streamConfig;

	@Resource(type = TransformStreamService.class)
	private StreamService transformService;

	@Autowired
	private LookupTool lookupTool;

	private ActorMaterializer mat;

	private Source<String, NotUsed> source;

	//同步清洗模型flow
	private SysTransformModelFlow<String, String> transformModelFlow;

	//清洗flow
	private FilterFlow<String, String> filterFlow;

	private List<String> collSources = new ArrayList<>();

	@Before
	public void init() {
		AbsFunction.initFunction(lookupTool);
		ActorSystem system = ActorSystem.create("test");
		//单元测试，初始化模型信息
		this.mat = ActorMaterializer.create(ActorMaterializerSettings.create(system).withInputBuffer(64, 64), system);
		String path = TestApp.class.getClassLoader().getResource("source/collect_linux.txt").getPath();
		File file = new File(path);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = br.readLine()) != null)
				collSources.add(line);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		this.source = Source.from(collSources);
		this.transformModelFlow = new SysTransformModelFlow<>(redisService, streamConfig.getSubTransform());
		this.filterFlow = new FilterFlow<>(transformService);
		//初始化清洗模型
		String transform = "{\"id\":\"linux\",\"isSystem\":1,\"name\":\"Linux\",\"origin\":\"ucd_unix\",\"storagetype\":\"time\",\"transform\":[{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${LinuxSysMem}\"},\"name\":\"内存页数据\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${LinuxSysPro}\"},\"name\":\"僵死进程及等待处理队列数据\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${LinuxSysCou}\"},\"name\":\"用户数据\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${DeadProcessNum}\"},\"name\":\"僵死进程数\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${HarddiskNum}\"},\"name\":\"硬盘个数\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${MemoryInOutRate}\"},\"name\":\"内存调进调出速率\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${PartitionSSHData}\"},\"name\":\"分区数据\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${memSwapData}\"},\"name\":\"Swap Space数据\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${memoryData}\"},\"name\":\"内存数据\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${laLoad}\"},\"name\":\"CPU平均负载数据\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${CPUPercentageData}\"},\"name\":\"CPU百分比数据\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${HDReadandWriteRate}\"},\"name\":\"硬盘平均读写速率\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${snmpProtocolStatus}\"},\"filter\":[\"transfor_field_col([\\\"isNotNull('${1.3.6.1.2.1.1.2.0}')\\\":\\\"ok\\\",\\\"hasError()\\\":\\\"error\\\"],\\\"error\\\",\\\"1.3.6.1.2.1.1.2.0\\\")\"],\"name\":\"SNMP协议状态\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${pingStatus}\"},\"filter\":[\"add_field(\\\"pingstatus\\\",\\\"${status}\\\")\",\"transfor_field_col([\\\"1\\\":\\\"ok\\\",\\\"hasError()\\\":\\\"error\\\"],\\\"error\\\",\\\"pingstatus\\\")\"],\"name\":\"ping状态\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${sysbaseinfo}\"},\"name\":\"系统基础信息\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${ifPhysAddress}\"},\"name\":\"MAC地址\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${sysUpTime}\"},\"name\":\"持续运行时间\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${ipv6Address}\"},\"name\":\"IPv6地址\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${icmpMsgs}\"},\"name\":\"ICMP包数据\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${hrStorageEntry}\"},\"filter\":[\"counter_assignment(\\\"${1.3.6.1.2.1.25.2.3.1.4}\\\",\\\"2147483647\\\",\\\"2\\\")\",\"counter_assignment(\\\"${1.3.6.1.2.1.25.2.3.1.5}\\\",\\\"2147483647\\\",\\\"2\\\")\",\"counter_assignment(\\\"${1.3.6.1.2.1.25.2.3.1.6}\\\",\\\"2147483647\\\",\\\"2\\\")\"],\"name\":\"存储相关数据\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${NumberOfCPU}\"},\"name\":\"CPU数据\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${hrDeviceData}\"},\"name\":\"hrDevice数据\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${NumberOfProcess}\"},\"name\":\"进程数\",\"persistence\":\"true\"},{\"datasource\":{\"datatype\":{\"name\":\"resultset\",\"withheader\":\"yes\"},\"source\":\"cmd.${NumberOfNIC}\"},\"name\":\"接口个数\",\"persistence\":\"true\"}],\"type\":\"transform\",\"version\":\"1.0\"}";
		TransformModel m = JSON.parseObject(transform, TransformModel.class);
		redisService.saveData(RedisService.TRANSFORM, "linux", m);
	}

	/**
	 * 用于数据清洗测试
	 */
	@Test
	public void dataTransformTest() {
		Sink<String, CompletionStage<Done>> printlnSink = Sink.<String> foreach(chunk -> {
			System.err.println("清洗后数据：" + chunk);
		});
		source.via(this.transformModelFlow).via(this.filterFlow).to(printlnSink).run(mat);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
