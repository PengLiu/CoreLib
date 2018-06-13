package org.coredata.core.agent.collector;

public class ProtocolConstants {

	public static final String HEALTH_CHECK_PREFIX = "method@isHealth()";

	public static final String MYSQL_JDBC = "mysql_jdbc";
	public static final String ORACLE_JDBC = "oracle_jdbc";
	public static final String ORACLERAC_JDBC = "oraclerac_jdbc";
	public static final String SQLSERVER_JDBC = "sqlserver_jdbc";
	public static final String DB2_JDBC = "db2_jdbc";
	public static final String POSTGRESQL_JDBC = "postgresql_jdbc";
	public static final String SYBASE_JDBC = "sybase_jdbc";
	public static final String INFORMIX_JDBC = "informix_jdbc";
	public static final String DM_JDBC = "dm_jdbc";
	public static final String SNMP = "snmp";
	public static final String RECEIVING = "receiving";
	public static final String PING = "ping";
	public static final String PING_PORT = "ping_port";
	public static final String IOT_GATEWAGE = "iotgateway";
	public static final String COMMON_JMX = "jmx";
	public static final String URL_HTTP = "url_http";
	public static final String PORT = "port";
	public static final String DNS = "dns";
	public static final String WAS = "was_jmx";
	public static final String WEBSPHERE_MQ = "webspheremq_jmx";
	public static final String VMWARE = "vmware";
	public static final String KVM = "kvm";
	public static final String EMAIL = "email";
	public static final String APACHE_HTTP = "apache_http";
	public static final String NGINX_HTTP = "nginx_http";
	public static final String XGMIOT_HTTP = "xgmiot_http";
	public static final String XGMIOT_KAFKA = "xgmiot_kafka";
	public static final String EMC_SMIS = "emc_smis";
	public static final String MONGO_JDBC = "mongodb_jdbc";
	public static final String IPMI = "ipmi";
	public static final String TCP = "tcp";
	public static final String DOCKER = "docker_socket";
	public static final String SSH = "ssh";
	public static final String TELNET = "telnet";
	public static final String SMARTISYS_HTTP = "smartisys_http";
	public static final String HANA_JDBC = "hana_jdbc";
	public static final String HK_PRI = "hk_pri";

	// COMMON
	public static final String S_PROTOCOL = "protocol";
	public static final String S_IP = "ip";
	public static final String S_PROTOCOL_IP = "protocolIP";
	public static final String S_PORT = "port";
	public static final String S_USERNAME = "username";
	public static final String S_PASSWORD = "password";
	public static final String S_READ_TIMEOUT = "readTimeout";
	public static final String S_READ_RETRY = "readRetry";

	// APPLICATION
	public static final String S_APP_PORT = "appPort";
	public static final String S_APP_USERNAME = "appUsername";
	public static final String S_APP_PASSWORD = "appPassword";
	public static final String S_APP_TYPE = "appType";
	public static final String S_APP_SSL = "ssl";
	public static final String S_APP_FILENKEYTRUSTSTORE = "fileNKeyTrustStore";
	public static final String S_APP_TRUSTSTOREPASSWORD = "trustStorePassword";

	// JMX
	public static final String S_JMX_PRODUCT_TYPE = "jmx_type";
	public static final String S_JMX_IP = "jmx_ip";
	public static final String S_JMX_PORT = "jmx_port";
	public static final String S_JMX_CONN_TIMEOUT = "jmx_timeout";
	public static final String S_JMX_CONN_RETRY = "jmx_retry";
	public static final String S_JMX_USERNAME = "jmx_user";
	public static final String S_JMX_PASSWORD = "jmx_password";
	public static final String S_JMX_SSL = "jmx_ssl";
	public static final String S_JMX_KEYTRUSTSTORE = "jmx_keytruststore";
	public static final String S_JMX_TRUSTSTOREPASSWORD = "jmx_trustStorePassword";

	// weblogic
	public static final String S_WEBLOGIC_FILE_NKEY_TRUST_STORE = "fileNKey_trustStore";
	public static final String S_WEBLOGIC_FILE_NKEY_KEY_STORE = "fileNKey_keyStore";
	public static final String S_WEBLOGIC_MBEAN_TYPE = "weblogicMBeanType";
	public static final String S_WEBLOGIC_CONN_TIMEOUT = "weblogicConnTimeout";
	public static final String S_WEBLOGIC_CONN_RETRY = "weblogicConnRetry";

	// AGENT
	public static final String S_AGENT_PROTOCOL = "agentProtocol";
	public static final String S_AGENT_ACTION = "agentAction";
	public static final String S_AGENT_SHELL = "shell";
	public static final String S_AGENT_RUNPATH = "runpath";
	public static final String S_AGENT_ICMPS = "icmps";
	public static final String S_AGENT_NAMESPACE = "nameSpace";
	public static final String S_AGENT_WQL = "wql";
	public static final String S_AGENT_PARAMETERS = "parameters";
	public static final String S_AGENT_CONN_TIMEOUT = "agentConnTimeout";
	public static final String S_AGENT_CONN_RETRY = "agentConnRetry";
	public static final String S_AGENT_IS_USE_KEY_FILE = "isUsePrivateFile";
	public static final String S_AGENT_RSA_PUBLIC_FILE_KEY = "rsaPublicKeyFile";
	public static final String S_AGENT_RSA_PRIVATE_FILE_KEY = "rsaPrivateKeyFile";
	public static final String S_AGENT_RSA_PRIVATE_FILE_PWD = "rsaPrivateKeyPwd";

	// POP3 Email
	public static final String S_POP3_CONN_TIMEOUT = "pop3ConnTimeout";
	public static final String S_POP3_CONN_RETRY = "pop3ConnRetry";
	public static final String S_POP3_SERVER = "pop3Server";
	public static final String S_POP3_USERNAME = "pop3UserName";
	public static final String S_POP3_PASSWORD = "pop3Password";
	public static final String S_POP3_PORT = "pop3Port";

	// SMTP Email
	public static final String S_SMTP_CONN_TIMEOUT = "smtpConnTimeout";
	public static final String S_SMTP_CONN_RETRY = "smtpConnRetry";
	public static final String S_SMTP_SERVER = "smtpServer";
	public static final String S_SMTP_USERNAME = "smtpUserName";
	public static final String S_SMTP_PASSWORD = "smtpPassword";
	public static final String S_SMTP_PORT = "smtpPort";
	public static final String S_SEND_ADDRESS = "sendAddress";
	public static final String S_RECV_ADDRESS = "recvAddress";
	public static final String S_SMTP_SSL = "smtpSSL";
	// websphere
	public static final String S_WEBSPHERE_USERNAME = "was_jmx_user";
	public static final String S_WEBSPHERE_PASSWORD = "was_jmx_password";
	public static final String S_WEBSPHERE_IP = "was_jmx_ip";
	public static final String S_WEBSPHERE_PORT = "was_jmx_port";
	public static final String S_WEBSPHERE_SERVER_TYPE = "was_jmx_type";
	public static final String S_WEBSPHERE_SECURITY_ENABLED = "was_jmx_enabled";
	public static final String S_WEBSPHERE_ND_IP = "was_jmx_nd_ip";
	public static final String S_WEBSPHERE_ND_PORT = "was_jmx_nd_port";
	public static final String S_FILE_NKEY_TRUST_STORE = "was_jmx_trustStore";
	public static final String S_FILE_NKEY_KEY_STORE = "was_jmx_keyStore";
	public static final String S_WEBSPHERE_TRUST_STORE_PW = "was_jmx_trustStorePw";
	public static final String S_WEBSPHERE_KEY_STORE_PW = "was_jmx_keySorePw";
	public static final String S_WEBSPHERE_CONN_TIMEOUT = "was_jmx_timeout";
	public static final String S_WEBSPHERE_CONN_RETRY = "was_jmx_retry";
	// webspheremq
	public static final String S_WEBSPHEREMQ_IP = "webspheremq_jmx_ip";
	public static final String S_WEBSPHEREMQ_PORT = "webspheremq_jmx_port";
	public static final String S_WEBSPHEREMQ_INSTALLDIRECTORY = "webspheremq_jmx_installDirectory";
	public static final String S_WEBSPHEREMQ_SERVERCHANNELNAME = "webspheremq_jmx_serverChannelName";
	public static final String S_WEBSPHEREMQ_CHARIDENTIFY = "webspheremq_jmx_charIdentify";
	public static final String S_WEBSPHEREMQ_CONN_TIMEOUT = "webspheremq_jmx_connTimeout";
	public static final String S_WEBSPHEREMQ_CONN_RETRY = "webspheremq_jmx_connRetry";
	// webspheremb
	public static final String S_WEBSPHEREMB_QUEUEMANGERNAME = "queueManagerName";

	// SunJES
	public static final String S_SUNJES_INSTANCENAME = "instanceName";
	public static final String S_SUNJES_INSTALLDIR = "installDir";
	public static final String S_SUNJES_CONN_TIMEOUT = "sunjesConnTimeout";
	public static final String S_SUNJES_CONN_RETRY = "sunjesConnRetry";

	// http
	public static final String S_HTTP_NAME = "http_name";
	public static final String S_HTTP_URL = "http_url";
	public static final String S_HTTP_REQUESTTYPE = "http_requesttype";
	public static final String S_HTTP_CHARSET = "http_charset";
	public static final String S_HTTP_AUTHIP = "http_authip";
	public static final String S_HTTP_AUTHPORT = "http_authport";
	public static final String S_HTTP_USERNAME = "http_username";
	public static final String S_HTTP_PASSWORD = "http_password";
	public static final String S_HTTP_PROXYIP = "http_proxyip";
	public static final String S_HTTP_PROXYPORT = "http_proxyport";
	public static final String S_HTTP_PROXYUSERNAME = "http_proxyusername";
	public static final String S_HTTP_PROXYPASSWORD = "http_proxypassword";
	public static final String S_HTTP_IP = "http_ip";
	public static final String S_HTTP_PORT = "http_port";
	public static final String S_HTTP_ISSSL = "http_isssl";
	public static final String S_HTTP_CONN_TIMEOUT = "http_conntimeout";
	public static final String S_HTTP_READ_TIMEOUT = "http_readtimeout";
	public static final String S_HTTP_CONN_RETRY = "http_connretry";
	public static final String S_HTTP_ENABLESECURITY = "http_enablesecurity";
	public static final String S_HTTP_KEYSTOREFILE = "http_keystorefile";
	public static final String S_HTTP_KEYSTOREPASSWORD = "http_keystorepassword";
	public static final String S_HTTP_KEYSTORETYPE = "http_keystoretype";
	public static final String S_HTTP_ENABLESIMULATELOGIN = "http_enablesimulatelogin";
	public static final String S_HTTP_LOGINSUCCESSURL = "http_loginsuccessurl";
	public static final String S_HTTP_LOGINPARAMS = "http_loginparams";
	public static final String S_HTTP_PROTOCOLTYPE = "http_protocoltype";
	public static final String S_XGMIOT_KAFKAIP = "kafka_ip";
	public static final String S_XGMIOT_KAFKAPORT = "kafka_port";
	public static final String S_XGMIOT_KAFKATOPIC = "kafka_topic";

	// Smis
	public static final String S_SMIS_IP = "smis_ip";
	public static final String S_SMIS_PORT = "smis_port";
	public static final String S_SMIS_USERNAME = "smis_user";
	public static final String S_SMIS_PASSWORD = "smis_password";
	public static final String S_SMIS_CONN_TIMEOUT = "smis_timeout";
	public static final String S_SMIS_CONN_RETRY = "smis_retry";
	public static final String S_SMIS_NAMESPACE = "namespace";
	public static final String S_SMIS_ASS = "ass";
	public static final String S_SMIS_REF = "ref";

	public static final String S_HTTP_KAFKAIP = "http_kafkaIp";
	public static final String S_HTTP_KAFKAPORT = "http_kafkaPort";
	public static final String S_HTTP_KAFKATOPIC = "http_kafkaTopic";
	// docker
	public static final String S_DOCKER_IP = "sock_ip";
	public static final String S_DOCKER_PORT = "sock_port";
	public static final String S_DOCKER_USERNAME = "sock_user";
	public static final String S_DOCKER_PASSWORD = "sock_password";
	public static final String S_DOCKER_CONN_TIMEOUT = "sock_timeout";
	public static final String S_DOCKER_CONN_RETRY = "sock_retry";
	public static final String DOCKER_CONTAINERS = "Containers";
	public static final String DOCKER_IMAGES = "Images";
	public static final String DOCKER_INFO = "Info";
	public static final String DOCKER_VERSION = "Version";
	public static final String DOCKER_SERVICE = "Services";
	public static final String DOCKER_NETWORK = "Networks";
	public static final String DOCKER_VOLUME = "Volumes";
	public static final String DOCKER_STATS = "Stats";
	public static final String DOCKER_PING = "Ping";
	public static final String DOCKER_CHECK = "check";
	// SSH or telnet
	public static final String S_SSH_IP = "ssh_ip";
	public static final String S_SSH_PORT = "ssh_port";
	public static final String S_SSH_USERNAME = "ssh_user";
	public static final String S_SSH_PASSWORD = "ssh_password";
	public static final String S_SSH_CONN_TIMEOUT = "ssh_timeout";
	public static final String S_SSH_CONN_RETRY = "ssh_retry";
	public static final String S_SSH_UPLOADRUN_ACTION = "uploadrun";
	public static final String S_SSH_REMOTERUN_ACTION = "remoterun";
	public static final String S_SSH_SHELL = "shell";
	public static final String S_SSH_RUNPATH = "runpath";
	public static final String S_SSH_PARAMETERS = "parameters";

	//apache
	public static final String S_APACHE_STATUS = "server-status";
	//nginx
	public static final String S_NGINX_STATUS = "stub_status";

	//HK
	public static final String HK_PRI_IP = "hk_pri_ip";
	public static final String HK_PRI_PORT = "hk_pri_port";
	public static final String HK_PRI_USERNAME = "hk_pri_user";
	public static final String HK_PRI_PASSWORD = "hk_pri_password";
	public static final String HK_PRI_TIMEOUT = "hk_pri_timeout";
	public static final String HK_PRI_RETRY = "hk_pri_retry";

	//析格玛
	public enum XGMTypeEnum {
		ANTENNA, CLASSBOARD, INTEGRATEDEQUIPMENT, SERVER, ROUTER, SWITCH, CARBRAKE, PERSONBRAKE, VIDEOEQUIPMENT, KEEPWEBSITE, SCHOOL
	}
}
