package org.coredata.core.data.readers.kafka;

import java.util.List;

import org.apache.kafka.common.network.ListenerName;

import kafka.cluster.Broker;
import kafka.cluster.BrokerEndPoint;
import kafka.utils.ZkUtils;
import scala.collection.JavaConversions;

public class KafkaUtils {

	public static String getBrokers(String type, String zkAddr) {
		ZkUtils zk = ZkUtils.apply(zkAddr, 6000, 6000, true);
		List<Broker> brokers = JavaConversions.seqAsJavaList(zk.getAllBrokersInCluster());
		StringBuilder builder = new StringBuilder();
		int index = 1;
		for (Broker broker : brokers) {
			BrokerEndPoint endPoint = broker.getBrokerEndPoint(ListenerName.normalised(type));
			builder.append(endPoint.host()).append(":").append(endPoint.port());
			if(index < brokers.size()) {
				builder.append(",");
			}
		}
		zk.close();
		return builder.toString();
	}

}
