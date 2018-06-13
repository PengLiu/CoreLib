package org.coredata.core.util.event;

public interface IEvent {

	enum Type {
		Discovery, NTM, StateChanged, AlarmOccurred, Metrics, AlarmRecory, RunOnceColl, BusinessIpConfig, BusinessLogConfig, BusinessActionConfig, BusinessId
	}

}
