package org.coredata.core.data.writers.debug;

import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Record;
import org.coredata.core.data.Writer;
import org.coredata.core.data.vo.TableMeta;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DebugWriter extends Writer {

	public void prepare(PluginConfig writerConfig, TableMeta tableMeta) {

	}

	@Override
	public void execute(Record record) {
	}

	@Override
	public void close() {
		logger.info("Debug writer stop.");
	}

}