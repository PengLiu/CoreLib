package org.coredata.core.agent.collector.protocol;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.coredata.core.agent.collector.Cmd;
import org.coredata.core.agent.collector.ProtocolConstants;
import org.coredata.core.agent.collector.exception.ProtocolException;
import org.coredata.core.util.common.ResourceHelper;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Protocol {

	protected final ObjectMapper mapper = new ObjectMapper();

	protected static final Map<String, Protocol> protocols = new HashMap<>();

	protected static final ResourceHelper i18n = new ResourceHelper("error_message");

	protected static final Set<String> blankCmds = new HashSet<>();

	public abstract String run(Cmd cmd) throws ProtocolException;

	public static Protocol getProtocolRunner(String key) {
		return protocols.get(key);
	}

	public static final void Init() {
		i18n.init();
		protocols.put(ProtocolConstants.MYSQL_JDBC, new MySqlJDBC());
		protocols.put(ProtocolConstants.SQLSERVER_JDBC, new SQLServerJDBC());
		protocols.put(ProtocolConstants.ORACLE_JDBC, new OracleJDBC());
		//		protocols.put(Constants.ORACLERAC_JDBC, new OracleRACJDBC());
		//		protocols.put(Constants.DB2_JDBC, new DB2JDBC());
		//		protocols.put(Constants.POSTGRESQL_JDBC, new PostgreSqlJDBC());
		//		protocols.put(Constants.SYBASE_JDBC, new SybaseJDBC());
		//		protocols.put(Constants.INFORMIX_JDBC, new InformixJDBC());
		//		protocols.put(Constants.DM_JDBC, new DMJDBC());
		//		protocols.put(Constants.SNMP, new SNMP());
		//		protocols.put(Constants.PING, new PING());
		//		protocols.put(Constants.PING_PORT, new PingPort());
		//		protocols.put(Constants.IOT_GATEWAGE, new RZIotGateWay());
		//		protocols.put(Constants.COMMON_JMX, new JMX());
		//		protocols.put(Constants.URL_HTTP, new URL());
		//		protocols.put(Constants.PORT, new PORT());
		//		protocols.put(Constants.DNS, new DNS());
		//		protocols.put(Constants.WAS, new WAS());
		//		protocols.put(Constants.WEBSPHERE_MQ, new WebSphereMQ());
		//		protocols.put(Constants.VMWARE, new VMWare());
		//		protocols.put(Constants.KVM, new KVM());
		//		protocols.put(Constants.EMAIL, new Email());
		//		protocols.put(Constants.APACHE_HTTP, new ApacheHTTP());
		//		protocols.put(Constants.NGINX_HTTP, new NginxHTTP());
		//		protocols.put(Constants.XGMIOT_HTTP, new XGMIotHTTP());
		//		protocols.put(Constants.XGMIOT_KAFKA, new XGMIotKafka());
		//		protocols.put(Constants.IPMI, new IPMI());
		//		protocols.put(Constants.RECEIVING, new RECEIVING());
		//		protocols.put(Constants.DOCKER, new DockerSock());
		//		protocols.put(Constants.SSH, new SSHOrTelnet());
		//		protocols.put(Constants.TELNET, new SSHOrTelnet());
		//		protocols.put(Constants.SMARTISYS_HTTP, new SmartisysIotGateWay());
		//		protocols.put(Constants.HANA_JDBC, new HanaJDBC());
		//		protocols.put(Constants.EMC_SMIS, new SMIS());
		//		protocols.put(Constants.MONGO_JDBC, new MongoJDBC());
	}

	public static final boolean supportProtocol(String protocol) {
		return protocols.containsKey(protocol);
	}

	public abstract String healthCheck(Cmd cmd) throws ProtocolException;

	/**
	 * 创建异常信息
	 *
	 * @param e
	 * @param cmd
	 * @return
	 */
	ProtocolException buildException(Exception e, String cmd) {
		if (e instanceof UnknownHostException) {
			return new ProtocolException(ProtocolException.Type.ConnErr, ProtocolException.ErrNum.Conn_Refuse, "IP地址输入有误", cmd, e);
		}
		if (e instanceof AttributeNotFoundException) {
			return new ProtocolException(ProtocolException.Type.CollErr, ProtocolException.ErrNum.Coll_CMD_Err, "属性未找到", cmd, e);
		}
		if (e instanceof InstanceNotFoundException) {
			return new ProtocolException(ProtocolException.Type.CollErr, ProtocolException.ErrNum.Coll_CMD_Err, "实例未找到", cmd, e);
		}
		if (e instanceof MBeanException) {
			return new ProtocolException(ProtocolException.Type.CollErr, ProtocolException.ErrNum.Coll_CMD_Err, "MBean异常", cmd, e);
		}
		if (e instanceof ReflectionException) {
			return new ProtocolException(ProtocolException.Type.CollErr, ProtocolException.ErrNum.Coll_CMD_Err, "系统异常", cmd, e);
		}
		if (e instanceof RemoteException) {
			return new ProtocolException(ProtocolException.Type.CollErr, ProtocolException.ErrNum.Coll_CMD_Err, "系统异常", cmd, e);
		}
		if (e instanceof IOException) {
			return new ProtocolException(ProtocolException.Type.CollErr, ProtocolException.ErrNum.Coll_CMD_Err, "系统异常", cmd, e);
		}
		if (e instanceof ClassNotFoundException) {
			return new ProtocolException(ProtocolException.Type.CollErr, ProtocolException.ErrNum.Coll_CMD_Err, "类不存在异常", cmd, e);
		}
		if (e instanceof NoSuchFieldException) {
			return new ProtocolException(ProtocolException.Type.CollErr, ProtocolException.ErrNum.Coll_CMD_Err, "属性不存在异常", cmd, e);
		}
		if (e instanceof SecurityException) {
			return new ProtocolException(ProtocolException.Type.CollErr, ProtocolException.ErrNum.Coll_CMD_Err, "违背安全原则异常", cmd, e);
		}
		if (e instanceof IllegalArgumentException) {
			return new ProtocolException(ProtocolException.Type.CollErr, ProtocolException.ErrNum.Coll_CMD_Err, "不合法的参数异常", cmd, e);
		}
		if (e instanceof IllegalAccessException) {
			return new ProtocolException(ProtocolException.Type.CollErr, ProtocolException.ErrNum.Coll_CMD_Err, "违法的访问异常", cmd, e);
		}
		return new ProtocolException(ProtocolException.Type.UnKnowErr, ProtocolException.ErrNum.Conn_Timeout, "系统异常", cmd, e);
	}
}