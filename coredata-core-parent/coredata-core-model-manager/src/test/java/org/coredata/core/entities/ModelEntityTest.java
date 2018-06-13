package org.coredata.core.entities;

import com.alibaba.fastjson.JSON;
import org.coredata.core.TestApp;
import org.coredata.core.model.collection.CollectionModel;
import org.coredata.core.model.common.Restype;
import org.coredata.core.model.entities.CollectionEntity;
import org.coredata.core.model.entities.RestypeEntity;
import org.coredata.core.model.service.ModelService;
import org.coredata.core.util.encryption.EncryptionAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
@Transactional
public class ModelEntityTest {

    static {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

	@Autowired
	private ModelService modelService;

	@Before
	public void init() {
		EncryptionAlgorithm.init();
		String res = "{\"id\": \"host\", \"desc\": \"主机\", \"name\": \"主机\", \"isroot\": \"true\", \"hasFirm\": false, \"isAsset\": false, \"isSystem\": 1, \"parentid\": \"itandiot\", \"defaultType\": 1, \"onlyclassify\": true}";
		RestypeEntity entity = new RestypeEntity();
		entity.setRestype(JSON.parseObject(res, Restype.class));
		modelService.saveRestype(entity);

		String col = "{\"id\": \"host_snmp\", \"name\": \"主机\", \"type\": \"collect\", \"origin\": \"\", \"restype\": \"host\", \"storage\": {\"type\": \"hdfs\"}, \"version\": \"1.0\", \"isSystem\": 1, \"collector\": [{\"id\": \"snmpProtocolStatus\", \"cmd\": \"fdac7203799a90704532a733bde002fb8d66dd4a260d27e880606a275ef20ef7\", \"desc\": \"SNMP协议状态\", \"type\": \"snmp\", \"param\": [{\"key\": \"collectType\", \"value\": \"GET\"}], \"retry\": \"0\", \"period\": \"30s\", \"storage\": \"hdfs\", \"timeout\": \"3000\", \"datatype\": \"avail\", \"isMonitor\": true, \"isavailcmd\": true, \"resulttype\": \"resultset\", \"classification\": \"base\", \"isGlobalResult\": false}, {\"id\": \"pingStatus\", \"cmd\": \"c5dbc24a75f42c57701af59e86a26fb5511f8dfd1504ab6def6626a0c2263b33\", \"desc\": \"PING数据\", \"type\": \"ping\", \"param\": [], \"retry\": \"0\", \"period\": \"30s\", \"storage\": \"hdfs\", \"timeout\": \"3000\", \"datatype\": \"avail\", \"isMonitor\": true, \"isavailcmd\": true, \"resulttype\": \"resultset\", \"classification\": \"base\", \"isGlobalResult\": false}, {\"id\": \"hrStorageEntry\", \"cmd\": \"4e96778cfa16ee4c0449f310e9555351d405069d4af0b16d0f76fd947db40a303de7065462d9f0a8707f6a974ac21b35e150f8582c4478e7bcc5a36770325cb907d9681c1e00b76d1b06aed4d6f2a3dc7cfb81f01e9ccfb027cdf7f43959582b\", \"desc\": \"主机存储条目数据\", \"type\": \"snmp\", \"param\": [{\"key\": \"collectType\", \"value\": \"WALK\"}], \"retry\": \"0\", \"period\": \"5m\", \"storage\": \"hdfs\", \"timeout\": \"3000\", \"datatype\": \"info\", \"isMonitor\": true, \"isavailcmd\": false, \"resulttype\": \"resultset\", \"classification\": \"base\", \"isGlobalResult\": false}, {\"id\": \"IPv6Address\", \"cmd\": \"25c19cdf7fb126472521049c44cd223d586cf798367d3eba1b89261317d3361a\", \"desc\": \"IPv6地址\", \"type\": \"snmp\", \"param\": [{\"key\": \"collectType\", \"value\": \"WALK\"}], \"retry\": \"0\", \"period\": \"5m\", \"storage\": \"hdfs\", \"timeout\": \"3000\", \"datatype\": \"conf\", \"isMonitor\": true, \"isavailcmd\": false, \"resulttype\": \"resultset\", \"classification\": \"base\", \"isGlobalResult\": false}, {\"id\": \"NumberOfCPU\", \"cmd\": \"7d74bf8cbac3a48ea8a3035ec086c261f8692882d64cc088af26a410342b3d1e\", \"desc\": \"CPU数据\", \"type\": \"snmp\", \"param\": [{\"key\": \"collectType\", \"value\": \"WALK\"}], \"retry\": \"0\", \"period\": \"5m\", \"storage\": \"hdfs\", \"timeout\": \"3000\", \"datatype\": \"conf\", \"isMonitor\": true, \"isavailcmd\": false, \"resulttype\": \"resultset\", \"classification\": \"base\", \"isGlobalResult\": false}, {\"id\": \"sysbaseinfo\", \"cmd\": \"c484e2849e297dbb8c5094514ba1d91023c59ed3bfcd0bcb10c5d93ed9cde81ac6e4ddc998030d9d8350c620f7a3d6d3049a839cef5b43fb888c16b5c14293c6\", \"desc\": \"设备基础信息\", \"type\": \"snmp\", \"param\": [{\"key\": \"collectType\", \"value\": \"GET\"}], \"retry\": \"0\", \"period\": \"5m\", \"storage\": \"hdfs\", \"timeout\": \"3000\", \"datatype\": \"info\", \"isMonitor\": true, \"isavailcmd\": false, \"resulttype\": \"resultset\", \"classification\": \"base\", \"isGlobalResult\": false}, {\"id\": \"ifPhysAddress\", \"cmd\": \"ab4efb7382ef3b6369082597d605163b3612315322142fb9df57018023aff921\", \"desc\": \"设备接口Mac地址\", \"type\": \"snmp\", \"param\": [{\"key\": \"collectType\", \"value\": \"WALK\"}], \"retry\": \"0\", \"period\": \"5m\", \"storage\": \"hdfs\", \"timeout\": \"3000\", \"datatype\": \"info\", \"isMonitor\": true, \"isavailcmd\": false, \"resulttype\": \"resultset\", \"classification\": \"base\", \"isGlobalResult\": false}, {\"id\": \"icmpMsgs\", \"cmd\": \"9edcba2df9617ccc2e2fce42c87f0100638abae53c82c65afaf5ada839cb830f9580625978f46aca0c7463f1813490a6\", \"desc\": \"设备ICMP包数据\", \"type\": \"snmp\", \"param\": [{\"key\": \"collectType\", \"value\": \"GET\"}], \"retry\": \"0\", \"period\": \"5m\", \"storage\": \"hdfs\", \"timeout\": \"3000\", \"datatype\": \"perf\", \"isMonitor\": true, \"isavailcmd\": false, \"resulttype\": \"resultset\", \"classification\": \"base\", \"isGlobalResult\": false}, {\"id\": \"hrDeviceData\", \"cmd\": \"7d74bf8cbac3a48ea8a3035ec086c2618737bc4a7540afce303d4e8e94bc8416a33000feb7d9fb8afa0a88f246c28277\", \"desc\": \"hrDevice数据\", \"type\": \"snmp\", \"param\": [{\"key\": \"collectType\", \"value\": \"WALK\"}], \"retry\": \"0\", \"period\": \"5m\", \"storage\": \"hdfs\", \"timeout\": \"3000\", \"datatype\": \"conf\", \"isMonitor\": true, \"isavailcmd\": false, \"resulttype\": \"resultset\", \"classification\": \"base\", \"isGlobalResult\": false}, {\"id\": \"NumberOfNIC\", \"cmd\": \"6d51134d3738ac65b50bbd02738f68bb8d66dd4a260d27e880606a275ef20ef7\", \"desc\": \"接口个数\", \"type\": \"snmp\", \"param\": [{\"key\": \"collectType\", \"value\": \"GET\"}], \"retry\": \"0\", \"period\": \"5m\", \"storage\": \"hdfs\", \"timeout\": \"3000\", \"datatype\": \"conf\", \"isMonitor\": true, \"isavailcmd\": false, \"resulttype\": \"resultset\", \"classification\": \"base\", \"isGlobalResult\": false}, {\"id\": \"sysUpTime\", \"cmd\": \"21d614e7958dbad6715316856f5803fecc62d629edee49a0fdd800202068a795\", \"desc\": \"设备系统持续运行时间\", \"type\": \"snmp\", \"param\": [{\"key\": \"collectType\", \"value\": \"GET\"}], \"retry\": \"0\", \"period\": \"5m\", \"storage\": \"hdfs\", \"timeout\": \"3000\", \"datatype\": \"perf\", \"isMonitor\": true, \"isavailcmd\": false, \"resulttype\": \"resultset\", \"classification\": \"base\", \"isGlobalResult\": false}, {\"id\": \"NumberOfProcess\", \"cmd\": \"21d614e7958dbad6715316856f5803fe249c74d4d8793a4181a0cae4a3be50a6\", \"desc\": \"进程数\", \"type\": \"snmp\", \"param\": [{\"key\": \"collectType\", \"value\": \"GET\"}], \"retry\": \"0\", \"period\": \"5m\", \"storage\": \"hdfs\", \"timeout\": \"3000\", \"datatype\": \"info\", \"isMonitor\": true, \"isavailcmd\": false, \"resulttype\": \"resultset\", \"classification\": \"base\", \"isGlobalResult\": false}]}";
		CollectionEntity centity = new CollectionEntity();
		centity.setColModel(JSON.parseObject(col, CollectionModel.class));
		modelService.saveCollectionModel(centity);
	}

	@Test
	public void restypeTest() {
		String id = "host";
		RestypeEntity resEntity = modelService.findRestypeById(id);
		assertNotEquals(null, resEntity);
		Restype restype = resEntity.getRestype();
		assertNotEquals(null, restype);
		String name = restype.getName();
		assertEquals("主机", name);
	}

	@Test
	public void collectionTest() {
		String id = "host_snmp";
		CollectionEntity colEntity = modelService.findCollectionById(id);
		assertNotEquals(null, colEntity);
		CollectionModel collect = colEntity.getColModel();
		assertNotEquals(null, collect);
		String name = collect.getName();
		assertEquals("主机", name);
	}

}
