package org.coredata.core.business.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BusinessService {

	private String aggName = "agg";

	private String subAggName = "subAgg";

	private String nestedAggName = "nestedAgg";

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;


	

}