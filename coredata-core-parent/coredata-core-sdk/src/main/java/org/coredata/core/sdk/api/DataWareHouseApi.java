package org.coredata.core.sdk.api;

import org.coredata.core.ElasticsearchService;
import org.coredata.core.util.elasticsearch.vo.CommResult;
import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dw")
public class DataWareHouseApi {

	@Autowired
	private ElasticsearchService elasticsearchService;

	@PutMapping("/{index}")
	public CommResult findResult(@PathVariable String index, @RequestBody String queryDsl) throws QuerydslException {
		return elasticsearchService.queryByCondition(queryDsl, index);
	}

}
