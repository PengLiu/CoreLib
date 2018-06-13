package org.coredata.core;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.coredata.core.util.elasticsearch.querydsl.ESFilterBuilder;
import org.coredata.core.util.elasticsearch.vo.CommResult;
import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ElasticsearchService {

	private Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);

	private ObjectMapper mapper = new ObjectMapper();

	@Value("${spring.data.elasticsearch.cluster-nodes}")
	private String clusterNodes;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	private RestHighLevelClient client = null;

	private BulkProcessor bulkProcessor = null;

	@PostConstruct
	public void init() {
		List<HttpHost> posts = new ArrayList<>();
		for (String addr : clusterNodes.split(",")) {
			String[] netAddr = addr.split(":");
			posts.add(new HttpHost(netAddr[0], 9200, "http"));
		}
		client = new RestHighLevelClient(RestClient.builder(posts.toArray(new HttpHost[] {})));

		bulkProcessor = BulkProcessor.builder(elasticsearchTemplate.getClient(), new BulkProcessor.Listener() {
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
				if (logger.isDebugEnabled()) {
					logger.debug("Bulk execution success [" + executionId + "].\n" + "Took (ms): " + response.getTook().getMillis() + " Count: "
							+ response.getItems().length);
				}
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
				logger.error("Bulk execution failed [" + executionId + "].\n" + failure.toString());
			}
		}).setBulkActions(10000).setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB)).setFlushInterval(TimeValue.timeValueSeconds(5)).setConcurrentRequests(1)
				.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)).build();

	}

	@Cacheable(cacheNames = "dimensionCache", keyGenerator = "dimKeyGen")
	public String queryByCondition(String index, String field, Object val) {

		NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder().withIndices(index + "*").withQuery(QueryBuilders.termQuery(field, val));
		return elasticsearchTemplate.query(searchQuery.build(), new ResultsExtractor<String>() {
			@Override
			public String extract(SearchResponse response) {
				if (response.getHits().totalHits > 0) {
					SearchHit searchHit = response.getHits().getAt(0);
					return searchHit.getSourceAsString();
				}
				return null;
			}
		});

	}

	public CommResult queryByCondition(String conditionDsl, String index) throws QuerydslException {

		CommResult result = new CommResult();

		NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
		searchQueryBuilder.withIndices(index + "*");

		if (!StringUtils.isEmpty(conditionDsl)) {
			ESFilterBuilder filterBuilder = new ESFilterBuilder() {
			};
			filterBuilder.prepare(conditionDsl);
			QueryBuilder queryBuilder = filterBuilder.buildFilters();
			searchQueryBuilder.withQuery(queryBuilder);
			filterBuilder.pagination(searchQueryBuilder);
			filterBuilder.orderBy(searchQueryBuilder);
			List<AggregationBuilder> builders = filterBuilder.buildAggregation();
			for (AggregationBuilder builder : builders) {
				searchQueryBuilder.addAggregation((AbstractAggregationBuilder<?>) builder);
			}
		}

		elasticsearchTemplate.query(searchQueryBuilder.build(), new ResultsExtractor<Void>() {
			@Override
			public Void extract(SearchResponse response) {
				result.setUsed(response.getTookInMillis());
				result.setTotal(response.getHits().totalHits);
				response.getHits().forEach(hit -> {
					result.addRecord(hit.getSourceAsString());
				});
				Aggregations aggs = response.getAggregations();
				if (aggs != null) {
					result.getAggregations().putAll(createAggs(null, aggs.asList()));
				}
				return null;
			}
		});

		return result;

	}

	private Map<String, JsonNode> createAggs(ObjectNode parent, List<Aggregation> aggs) {

		Map<String, JsonNode> aggregations = new HashMap<>();

		for (Aggregation agg : aggs) {
			ArrayNode arrayJson = mapper.createArrayNode();
			if (agg instanceof Range) {
				Range tmp = (Range) agg;
				for (Range.Bucket entry : tmp.getBuckets()) {
					String key = entry.getKeyAsString();
					String fromAsString = entry.getFromAsString();
					String toAsString = entry.getToAsString();
					long docCount = entry.getDocCount();
					ObjectNode tmpJson = mapper.createObjectNode();
					tmpJson.put("key", key);
					tmpJson.put("from", fromAsString);
					tmpJson.put("to", toAsString);
					tmpJson.put("count", docCount);
					arrayJson.add(tmpJson);
					if (entry.getAggregations() != null) {
						createAggs(tmpJson, entry.getAggregations().asList());
					}
				}
				aggregations.put(agg.getName(), arrayJson);
			} else if (agg instanceof Histogram) {
				Histogram tmp = (Histogram) agg;
				for (Histogram.Bucket entry : tmp.getBuckets()) {
					ObjectNode tmpJson = mapper.createObjectNode();
					DateTime key = (DateTime) entry.getKey();
					tmpJson.put("key", entry.getKeyAsString());
					tmpJson.put("keyAsMs", key.getMillis());
					tmpJson.put("count", entry.getDocCount());
					arrayJson.add(tmpJson);
					if (entry.getAggregations() != null) {
						createAggs(tmpJson, entry.getAggregations().asList());
					}
				}
				aggregations.put(agg.getName(), arrayJson);
			} else if (agg instanceof Terms) {
				Terms tmp = (Terms) agg;
				for (Terms.Bucket entry : tmp.getBuckets()) {
					ObjectNode tmpJson = mapper.createObjectNode();
					tmpJson.put("key", entry.getKeyAsString());
					tmpJson.put("count", entry.getDocCount());
					arrayJson.add(tmpJson);
					if (entry.getAggregations() != null) {
						createAggs(tmpJson, entry.getAggregations().asList());
					}
				}
				aggregations.put(agg.getName(), arrayJson);

			} else if (agg instanceof Stats) {
				Stats tmp = (Stats) agg;
				ObjectNode tmpJson = mapper.createObjectNode();
				tmpJson.put("min", tmp.getMin());
				tmpJson.put("max", tmp.getMax());
				tmpJson.put("avg", tmp.getAvg());
				tmpJson.put("sum", tmp.getSum());
				tmpJson.put("count", tmp.getCount());
				aggregations.put(agg.getName(), tmpJson);
			} else if (agg instanceof InternalNested) {
				InternalNested tmp = (InternalNested) agg;
				ObjectNode tmpJson = mapper.createObjectNode();
				tmpJson.put("key", tmp.getName());
				tmpJson.put("count", tmp.getDocCount());
				aggregations.put(agg.getName(), tmpJson);
			} else if (agg instanceof Cardinality) {
				Cardinality tmp = (Cardinality) agg;
				ObjectNode tmpJson = mapper.createObjectNode();
				tmpJson.put("key", tmp.getName());
				tmpJson.put("count", tmp.getValue());
				aggregations.put(agg.getName(), tmpJson);
			}
		}

		if (parent != null) {
			parent.set("subAggregations", mapper.valueToTree(aggregations));
		}

		return aggregations;

	}

	public void save(byte[] data, String index) {
		if (bulkProcessor != null) {
			bulkProcessor.add(new IndexRequest(index).source(data, XContentType.JSON));
		}
	}

	public String save(byte[] data, String index, String type) {
		if (bulkProcessor != null) {
			IndexRequest ir = new IndexRequest(index, type);
			ir.source(data, XContentType.JSON);
			bulkProcessor.add(ir);
			return ir.id();
		}
		return "";
	}

	public void save(byte[] data, String index, String type, String id) {
		if (bulkProcessor != null) {
			bulkProcessor.add(new IndexRequest(index, type, id).source(data, XContentType.JSON));
		}
	}

	public void save(List<Map<String, Object>> datas, String index, String type) {
		BulkRequestBuilder bulkRequest = elasticsearchTemplate.getClient().prepareBulk();
		for (Map<String, Object> data : datas) {
			String id = data.get("indexId").toString();
			bulkRequest.add(new IndexRequest(index, type, id).source(data, XContentType.JSON));
		}
		if (bulkRequest.numberOfActions() > 0) {
			BulkResponse bulkResponse = bulkRequest.get();
			if (bulkResponse.hasFailures()) {
				logger.error("batch insert data  has errors.");
			}
		}
	}

	/** 
	 * Force merge yesterday index at every day 2am
	 */
	@Scheduled(cron = "0 0 2 * * *")
	public void forceMerge() {

		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		String indexName = "metric_" + dateFormat.format(cal.getTimeInMillis());

		Map<String, String> params = new HashMap<>();
		params.put("max_num_segments", "1");

		try {
			client.getLowLevelClient().performRequest("POST", "/" + indexName + "/_forcemerge", params, new Header[] {});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Response rawQuery(String method, String endPoint, String jsonData) {
		if (client != null) {
			Map<String, String> params = Collections.emptyMap();
			HttpEntity entity = new NStringEntity(jsonData, ContentType.APPLICATION_JSON);
			try {
				return client.getLowLevelClient().performRequest(method, endPoint, params, entity);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public String getClusterNodes() {
		return clusterNodes;
	}

}
