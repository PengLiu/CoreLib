package org.coredata.core.agent.collector.config;

public class AgentSettings {
	
	public static final String CUSTOMER_ID = "customerId";
	
	private static String customerId;

	public static String getCustomerId() {
		return customerId;
	}

	public static void setCustomerId(String customerId) {
		AgentSettings.customerId = customerId;
	}
	
	
	
}
