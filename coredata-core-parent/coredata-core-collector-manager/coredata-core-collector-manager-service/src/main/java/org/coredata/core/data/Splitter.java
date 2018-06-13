package org.coredata.core.data;

import java.util.List;

public abstract class Splitter {

	public abstract List<PluginConfig> split(JobConfig jobConfig);
}
