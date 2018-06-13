package org.coredata.core.util.actor;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import akka.actor.UntypedAbstractActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;

public class ClusterMonitor extends UntypedAbstractActor {

	private Cluster cluster = Cluster.get(getContext().system());

	private CoreDataHash<String, String> hash = null;

	private ReentrantReadWriteLock lock;

	public ClusterMonitor(CoreDataHash<String, String> hash, ReentrantReadWriteLock lock) {
		this.hash = hash;
		this.lock = lock;
	}

	@Override
	public void preStart() {
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
	}

	@Override
	public void postStop() {
		cluster.unsubscribe(getSelf());
	}

	@Override
	public void onReceive(Object message) {
		if (message instanceof MemberUp) {
			MemberUp mUp = (MemberUp) message;
			String clusterId = mUp.member().uniqueAddress().address().hashCode() + "";
			updateNodes(clusterId, true);
		} else if (message instanceof UnreachableMember) {
			UnreachableMember mUnreachable = (UnreachableMember) message;
			String clusterId = mUnreachable.member().uniqueAddress().address().hashCode() + "";
			updateNodes(clusterId, false);
		} else if (message instanceof MemberRemoved) {
			MemberRemoved mRemoved = (MemberRemoved) message;
			String clusterId = mRemoved.member().uniqueAddress().address().hashCode() + "";
			updateNodes(clusterId, false);
		} else if (message instanceof MemberEvent) {
			// ignore
		} else {
			unhandled(message);
		}
	}

	private void updateNodes(String nodeId, boolean isAdd) {

		lock.writeLock().lock();
		try {
			if (isAdd) {
				hash.add(nodeId);
			} else {
				hash.remove(nodeId);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

}