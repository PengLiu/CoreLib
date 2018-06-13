package org.coredata.core.data.writers.elasticsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ElasticSaver {

	private Logger logger = LoggerFactory.getLogger(ElasticSaver.class);

	private BlockingQueue<DataCache> data = new LinkedBlockingQueue<>();

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Scheduled(fixedDelay = 1000)
	public void init() {

		List<DataCache> tmp = new ArrayList<>();
		data.drainTo(tmp);
		if (!CollectionUtils.isEmpty(tmp)) {
			try {
				List<IndexQuery> indexQueries = new ArrayList<IndexQuery>();
				for (DataCache dc : tmp) {
					indexQueries.add(new IndexQueryBuilder().withSource(dc.getData()).withIndexName(dc.getIndex()).withType(dc.getType()).build());
				}
				elasticsearchTemplate.bulkIndex(indexQueries);
			} catch (Exception e) {
				logger.error("Save index error.", e);
			}
		}

	}

	@Async
	public void addData(DataCache cache) {
		data.add(cache);
	}

}
