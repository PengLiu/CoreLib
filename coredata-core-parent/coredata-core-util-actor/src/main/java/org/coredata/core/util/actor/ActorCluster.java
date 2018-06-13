package org.coredata.core.util.actor;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import de.aktey.akka.k8s.SeednodeConfig;

public class ActorCluster {

	private Logger logger = LoggerFactory.getLogger(ActorCluster.class);

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private final Funnel<CharSequence> strFunnel = Funnels.stringFunnel(Charset.defaultCharset());

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private final CoreDataHash<String, String> hash = new CoreDataHash(Hashing.murmur3_128(), strFunnel, strFunnel, new ArrayList<>());

	private ClusterType type = null;

	private ActorSystem actorSystem = null;

	private ActorRef mediator = null;

	private Map<String, ActorRef> dispatcherCache = new ConcurrentHashMap<>();

	private static final String DEFAULT_CLUSTER = "CoreDataTransformCluster";

	private static final String DEFAULT_DISPATCHER = "Dispatcher-";

	public ActorSystem initActorSystem(ClusterType type, Config clusterConfig) {
		return initActorSystem(type, DEFAULT_CLUSTER, clusterConfig);
	}

	public ActorSystem initActorSystem(ClusterType type, String clusterName, Config clusterConfig) {
		switch (type) {
		case K8S:
			createK8SCluster(clusterName);
			this.type = ClusterType.K8S;
			break;
		default:
			createLocalCluster(clusterName);
			this.type = ClusterType.Local;
		}
		return actorSystem;
	}

	private void createK8SCluster(String clusterName) {
		Config config = SeednodeConfig.getConfig(clusterName).withFallback(ConfigFactory.load()).resolve();
		actorSystem = ActorSystem.create(clusterName, config);
		actorSystem.actorOf(Props.create(ClusterMonitor.class, hash, lock), "ClusterMonitor");
		mediator = DistributedPubSub.get(actorSystem).mediator();
	}

	private void createLocalCluster(String clusterName) {
		actorSystem = ActorSystem.create(clusterName, ConfigFactory.load("local"));
	}

	private void registerSelf(String clusterId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Register self " + clusterId);
		}
		lock.writeLock().lock();
		try {
			hash.add(clusterId);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public final ActorRef createActor(Props props) {
		if (actorSystem == null) {
			initActorSystem(ClusterType.Local, "ActorSystem", null);
		}
		return actorSystem.actorOf(props);
	}

	public final ActorRef registerDispatcher(String name, Props props) {

		if (name == null) {
			name = DEFAULT_DISPATCHER;
		}

		ActorRef dispatcher = null;

		switch (type) {
		case K8S:
			Cluster cluster = Cluster.get(actorSystem);
			int clusterId = cluster.selfUniqueAddress().address().hashCode();
			dispatcher = actorSystem.actorOf(props, name + clusterId);
			dispatcherCache.put(name, dispatcher);
			registerSelf(clusterId + "");
			if (logger.isDebugEnabled()) {
				logger.debug("Create actor for k8s env " + dispatcher.toString());
			}
		default:
			dispatcher = actorSystem.actorOf(props);
			dispatcherCache.put(name, dispatcher);
		}
		return dispatcher;
	}

	public final ActorRef registerDispatcher(Props props) {
		return registerDispatcher(null, props);
	}

	public final void dispatchMsg(Object msg, String key) {
		dispatchMsg(null, msg, key);
	}

	public final void dispatchMsg(String dispatcher, Object msg, String key) {

		if (dispatcher == null) {
			dispatcher = DEFAULT_DISPATCHER;
		}

		if (type == ClusterType.K8S) {
			String nid = getClusterNodeId(key);
			if (logger.isDebugEnabled()) {
				logger.debug("Send message to /user/" + dispatcher + nid + "  actor on k8s env.");
			}
			mediator.tell(new DistributedPubSubMediator.Send("/user/" + dispatcher + nid, msg, false), ActorRef.noSender());
		} else {
			ActorRef dis = dispatcherCache.get(dispatcher);
			dis.tell(msg, ActorRef.noSender());
		}
	}

	public final String getClusterNodeId(String key) {
		lock.readLock().lock();
		try {
			return hash.get(key);
		} finally {
			lock.readLock().unlock();
		}
	}

}
