
package org.coredata.core.data;

import org.coredata.core.data.vo.TableMeta;

public abstract class JobConfig extends Configuration {

	private static final long serialVersionUID = 1L;

	public abstract PluginConfig getReaderConfig();

	public abstract PluginConfig getWriterConfig();

	public abstract TableMeta getTableMeta();
	
	public abstract String getReaderName();

	public abstract String getWriterName();
	
	public abstract String getToken();
	

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{readerName:").append(getReaderName()).append(" writerName:").append(getWriterName()).append(" readerConfig:").append(getReaderConfig())
				.append(" writerConfig:").append(getWriterConfig());
		return sb.toString();

	}
}
