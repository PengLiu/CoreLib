package org.coredata.core.data;

import org.coredata.core.data.vo.TableMeta;

public class DefaultJobConfig extends JobConfig {

	private PluginConfig readerConfig;
	private PluginConfig writerConfig;
	private TableMeta tableMeta;
	private String readerName;
	private String writerName;
	private String token;
	private static final long serialVersionUID = 1L;

	public DefaultJobConfig(String readerName, PluginConfig readerConfig, String writerName,
			PluginConfig writerConfig) {
		super();
		this.readerName = readerName;
		this.readerConfig = readerConfig;
		this.writerName = writerName;
		this.writerConfig = writerConfig;
	}

	public DefaultJobConfig(String readerName, PluginConfig readerConfig, String writerName, PluginConfig writerConfig,
			TableMeta tableMeta, String token) {
		super();
		this.readerName = readerName;
		this.readerConfig = readerConfig;
		this.writerName = writerName;
		this.writerConfig = writerConfig;
		this.tableMeta = tableMeta;
		this.token = token;
	}

	@Override
	public PluginConfig getReaderConfig() {
		return readerConfig;
	}

	@Override
	public PluginConfig getWriterConfig() {
		return writerConfig;
	}

	@Override
	public TableMeta getTableMeta() {
		return tableMeta;
	}

	@Override
	public String getReaderName() {
		return readerName;
	}

	@Override
	public String getWriterName() {
		return writerName;
	}

	@Override
	public String getToken() {
		return token;
	}

}
