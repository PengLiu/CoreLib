package org.coredata.core.agent.collector.mailbox;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.coredata.core.agent.collector.Cmd;

import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.Envelope;
import akka.dispatch.MailboxType;
import akka.dispatch.MessageQueue;
import akka.dispatch.ProducesMessageQueue;

public class CmdMailbox implements MailboxType, ProducesMessageQueue<CmdMailbox.MyMessageQueue> {

	public static class MyMessageQueue implements MessageQueue, CmdUnboundedJMessageQueueSemantics {

		private final Queue<Envelope> queue = new ConcurrentLinkedQueue<Envelope>();

		@Override
		public void enqueue(ActorRef receiver, Envelope handle) {
			for (Envelope e : queue) {
				if (e.message() instanceof Cmd && handle.message() instanceof Cmd && e.message().equals(handle.message())) {
					((Cmd) e.message()).appendTaskTime(((Cmd) handle.message()).getTasktimes().get(0));
					return;
				}
			}
			queue.offer(handle);
		}

		@Override
		public Envelope dequeue() {
			return queue.poll();
		}

		@Override
		public int numberOfMessages() {
			return queue.size();
		}

		@Override
		public boolean hasMessages() {
			return !queue.isEmpty();
		}

		@Override
		public void cleanUp(ActorRef owner, MessageQueue deadLetters) {
			for (Envelope handle : queue) {
				deadLetters.enqueue(owner, handle);
			}
		}
	}

	public CmdMailbox(ActorSystem.Settings settings, Config config) {

	}

	@Override
	public MessageQueue create(scala.Option<ActorRef> arg0, scala.Option<ActorSystem> arg1) {
		return new MyMessageQueue();
	}

}