package org.coredata.core.olap.model.services;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.coredata.core.TestApp;
import org.coredata.core.datastream.blueprint.exception.BluePrintException;
import org.coredata.core.datastream.blueprint.service.BluePrintService;
import org.coredata.core.datastream.blueprint.vo.BluePrint;
import org.coredata.core.datastream.blueprint.vo.DataWarehouseFragment;
import org.coredata.core.olap.model.entities.ExpensesRecord;
import org.coredata.core.olap.model.entities.OlapDimIndex;
import org.coredata.core.olap.model.entities.OlapFactIndex;
import org.coredata.core.olap.model.entities.OlapFieldDef;
import org.coredata.core.olap.model.entities.OlapModel;
import org.coredata.core.olap.model.entities.UserInfo;
import org.coredata.core.olap.model.entities.Wristband;
import org.coredata.core.olap.model.repositories.ExpensesRecordResp;
import org.coredata.core.olap.model.repositories.UserInfoResp;
import org.coredata.core.olap.model.repositories.WristbandRest;
import org.coredata.core.olap.model.services.vo.FieldMeta;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RunWith(SpringRunner.class)
@EnableAsync
@SpringBootTest(classes = TestApp.class)
public class OlapModelTest {

	private ObjectMapper mapper = new ObjectMapper();
	
	@Value("${spring.kafka.topics.data_import}")
	private String topic;

	@Autowired
	private OlapModelService modelService;

	@Autowired
	private IndexService indexService;

	@Autowired
	private UserInfoResp userInfoResp;

	@Autowired
	private WristbandRest wristbandRest;

	@Autowired
	private ExpensesRecordResp expensesRecordResp;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@Autowired
	private BluePrintService bluePrintService;

	private ExecutorService executor = Executors.newCachedThreadPool();

	@After
	public void cleanup() {
		userInfoResp.deleteAll();
		wristbandRest.deleteAll();
		expensesRecordResp.deleteAll();
		//clean fact index
		indexService.deleteIndex("wristband_fact");
		indexService.deleteIndex("expensesrecord_fact");
	}

	@Before
	@Rollback(false)
	public void init() {
		
		OlapModel starModel = new OlapModel();
		starModel.setName("model001");
		starModel.setDescription("desc");
		starModel.setJobId("job01");

		//用户维度（包含城市，性别，年龄)三个维度
		OlapDimIndex dimension = new OlapDimIndex();
		dimension.setName("userinfo");
		dimension.setSrcIndex("userinfo");
		dimension.setRefName("userinfo");
		dimension.setDimRefId("wristbandId");
		dimension.setFactRefId("id");

		OlapFieldDef fieldDef = new OlapFieldDef();
		fieldDef.setName("location");
		fieldDef.setFieldType("keyword");
		dimension.addFieldDef(fieldDef);

		fieldDef = new OlapFieldDef();
		fieldDef.setName("gender");
		fieldDef.setFieldType("keyword");
		dimension.addFieldDef(fieldDef);

		fieldDef = new OlapFieldDef();
		fieldDef.setName("age");
		fieldDef.setFieldType("integer");
		dimension.addFieldDef(fieldDef);

		//事实表定义
		OlapFactIndex fact = new OlapFactIndex();
		fact.setJobId("job01");
		fact.setIndexName("wristband_fact");
		fact.setName("手环数据统计");
		fact.setDescription("手环数据原始数据表");
		fact.setSrcIndex("wristband");

		fieldDef = new OlapFieldDef();
		fieldDef.setName("heartBeat");
		fieldDef.setFieldType("integer");
		fact.addFieldDef(fieldDef);

		fieldDef = new OlapFieldDef();
		fieldDef.setName("calories");
		fieldDef.setFieldType("double");
		fact.addFieldDef(fieldDef);
		fact.addDimension(dimension);

		starModel.setFactIndex(fact);
		String json = JSON.toJSONString(starModel);
		modelService.save(starModel);
		
		
		starModel = new OlapModel();
		starModel.setName("model002");
		starModel.setDescription("desc");
		starModel.setJobId("job02");

		//用户维度（包含城市，性别，年龄)三个维度
		dimension = new OlapDimIndex();
		dimension.setName("userinfo");
		dimension.setSrcIndex("userinfo");
		dimension.setRefName("userinfo");
		dimension.setDimRefId("expensesRecordId");
		dimension.setFactRefId("id");

		fieldDef = new OlapFieldDef();
		fieldDef.setName("location");
		fieldDef.setFieldType("keyword");
		dimension.addFieldDef(fieldDef);

		fieldDef = new OlapFieldDef();
		fieldDef.setName("gender");
		fieldDef.setFieldType("keyword");
		dimension.addFieldDef(fieldDef);

		fieldDef = new OlapFieldDef();
		fieldDef.setName("age");
		fieldDef.setFieldType("integer");
		dimension.addFieldDef(fieldDef);

		//事实表定义
		fact = new OlapFactIndex();
		fact.setJobId("job02");
		fact.setIndexName("expensesrecord_fact");
		fact.setName("一卡通数据统计");
		fact.setDescription("一卡通数据原始数据表");
		fact.setSrcIndex("expenses_record");

		fieldDef = new OlapFieldDef();
		fieldDef.setName("amount");
		fieldDef.setFieldType("double");
		fact.addFieldDef(fieldDef);
		fact.addDimension(dimension);
		starModel.setFactIndex(fact);
		
		modelService.save(starModel);

		//create test data
		UserInfo user = new UserInfo();
		user.setAge(18);
		user.setId("000001");
		user.setExpensesId("e00001");
		user.setWristbandId("w00001");
		user.setUserName("张三");
		user.setGender("male");
		user.setLocation("北京");
		userInfoResp.save(user);

		user = new UserInfo();
		user.setAge(20);
		user.setId("000002");
		user.setExpensesId("e00002");
		user.setWristbandId("w00002");
		user.setUserName("李四");
		user.setGender("male");
		user.setLocation("天津");
		userInfoResp.save(user);

		user = new UserInfo();
		user.setAge(20);
		user.setId("000003");
		user.setExpensesId("e00003");
		user.setWristbandId("w00003");
		user.setUserName("王五");
		user.setGender("female");
		user.setLocation("北京");
		userInfoResp.save(user);
		
		modelService.updateModelTemplates();
	}

	@Test
	public void modelTest() throws InterruptedException, JsonProcessingException, BluePrintException {
		
		OlapModel model = modelService.findByName("model001");
		Assert.assertNotNull(model);
		Assert.assertEquals("model001", model.getName());
		
		BluePrint bluePrint = new BluePrint();
		bluePrint.addDataWarehouseFragment(new DataWarehouseFragment(model));
		
		model = modelService.findByName("model002");
		Assert.assertNotNull(model);
		Assert.assertEquals("model002", model.getName());
		bluePrint.addDataWarehouseFragment(new DataWarehouseFragment(model));
		
		
		bluePrintService.buildGraph(bluePrint);
		bluePrint.run();
		
		
		kafkaTemplate.send(topic, "hello world");
		Thread.sleep(1000);
		Assert.assertEquals(0, indexService.count(model.getFactIndex().getIndexName()));

		Runnable wristbandTask = new Runnable() {

			@Override
			public void run() {
				try {
					for (int i = 0; i < 1000; i++) {

						int seed = new Random().nextInt(3 - 1 + 1) + 1;

						Wristband wristband = new Wristband();
						wristband.setId("w0000" + seed);
						wristband.setHeartBeat((int) (Math.random() * 120));
						wristband.setPower((int) (Math.random() * 100));
						wristband.setCalories(Math.random());
						wristband.setSrcIndex("wristband");

						ObjectNode wristbandData = mapper.createObjectNode();
						wristbandData.put("jobId", "job01");
						wristbandData.set("data", mapper.valueToTree(wristband));

						String outputStr = mapper.writeValueAsString(wristbandData);
						kafkaTemplate.send(topic, outputStr);
					}
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}

			}

		};
		
		Runnable expensesRecordTask = new Runnable() {

			@Override
			public void run() {
				try {
					for (int i = 0; i < 1000; i++) {

						int seed = new Random().nextInt(3 - 1 + 1) + 1;

						ExpensesRecord record = new ExpensesRecord();
						record.setId("e0000" + seed);
						record.setAmount(Math.random());
						record.setSrcIndex("expenses_record");

						ObjectNode recordwristbandData = mapper.createObjectNode();
						recordwristbandData.put("jobId", "job02");
						recordwristbandData.set("data", mapper.valueToTree(record));

						String outputStr = mapper.writeValueAsString(recordwristbandData);
						kafkaTemplate.send(topic, outputStr);
					}
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}

			}

		};

		executor.submit(wristbandTask);
		executor.submit(expensesRecordTask);
		executor.submit(wristbandTask);
		executor.submit(expensesRecordTask);

		executor.awaitTermination(10, TimeUnit.SECONDS);

		Collection<FieldMeta> filedMetas = indexService.loadIndexMeta(model.getFactIndex().getIndexName());
		Assert.assertTrue(filedMetas.size() > 0);
		for (FieldMeta meta : filedMetas) {
			if (meta.getFieldName().equals("calories")) {
				Assert.assertEquals("double", meta.getType());
			}
			if (meta.getFieldName().equals("userinfo.age")) {
				Assert.assertEquals("integer", meta.getType());
			}

			if (meta.getFieldName().equals("userinfo.gender")) {
				Assert.assertEquals("keyword", meta.getType());
			}
		}
		Assert.assertEquals(2000, indexService.count("wristband_fact"));
		Assert.assertEquals(2000, indexService.count("expensesrecord_fact"));

	}

}
