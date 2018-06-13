package org.coredata.core.data.writers.elasticsearch;

import org.coredata.core.data.writers.CommProperties;

public interface ElasticSearchProperties extends CommProperties{

	public static final String INDEX_NAME = "index.name";
	public static final String INDEX_TEMPLATE = "index.template";
	public static final String INDEX_FIELDS = "index.fields";
	public static final String CLUSTER_ADDR = "cluster.address";

}
