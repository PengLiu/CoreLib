package org.coredata.core.agent.collector;

import org.coredata.core.agent.collector.protocol.Protocol;
import org.coredata.core.agent.collector.service.CollectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import akka.actor.UntypedAbstractActor;

@Component
@Scope("prototype")
public class CmdRunner extends UntypedAbstractActor {

	private static final Logger logger = LoggerFactory.getLogger(CmdRunner.class);

	@Autowired
	private CollectService service;

	public CmdRunner(CollectService service) {
		this.service = service;
	}

	@Override
	public void onReceive(Object obj) {

		if (obj instanceof Cmd) {
			Cmd cmd = (Cmd) obj;
			try {
				String instanceId = cmd.getInstanceId();
				String cmdName = cmd.getName();
				boolean support = service.checkIfSupportCmd(instanceId, cmdName);//支持的命令才进行采集
				if (support) {
					Protocol protocol = Protocol.getProtocolRunner(cmd.getProtocol());
					cmd.setSender(getSender());
					cmd.setSelf(getSelf());
					String result = protocol.run(cmd);
					if (!cmd.isAsyn()) {
						getSender().tell(new CmdResult(result, cmd), getSelf());
					}
				}
			} catch (Throwable e) {
				logger.error("Execute Cmd Failed.Cmd (" + cmd.getCmd() + ")", e);
				getSender().tell(new CmdResult(e, cmd), getSelf());
			}
		} else {
			if (logger.isInfoEnabled())
				logger.info("Cmd not Support.");
			unhandled(obj);
		}

	}

}