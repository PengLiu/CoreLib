package org.coredata.core.agent.collector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

	public static String zookeeperAddr;

	public static String kafkaType;

	public static boolean packageFlag;

	@Value("${zookeeper.addr}")
	public void setZookeeperAddr(String zookeeperAddr) {
		AgentConfig.zookeeperAddr = zookeeperAddr;
	}

	@Value("${kafka.addrType}")
	public void setKafkaType(String kafkaType) {
		AgentConfig.kafkaType = kafkaType;
	}

	@Value("${agentmanager.packageflag:false}")
	public void setPackageFlag(boolean packageFlag) {
		AgentConfig.packageFlag = packageFlag;
	}

}
