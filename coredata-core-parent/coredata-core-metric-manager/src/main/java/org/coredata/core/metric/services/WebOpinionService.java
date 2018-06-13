package org.coredata.core.metric.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.coredata.core.metric.documents.WebOpinion;
import org.coredata.core.metric.repositories.WebOpinionResp;
import org.coredata.core.util.elasticsearch.querydsl.ESFilterBuilder;
import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.coredata.util.query.AggregationType;
import org.coredata.util.query.Constants;
import org.coredata.util.query.FieldDefUtil;
import org.coredata.util.query.Fuzzy;
import org.coredata.util.query.Operation;
import org.coredata.util.query.TimeRange;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram.Bucket;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Transactional(readOnly = true)
public class WebOpinionService {

	private String aggName = "agg";

	@Autowired
	private WebOpinionResp webOpinionResp;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Transactional
	public void save(List<WebOpinion> opinions) {
		if (CollectionUtils.isEmpty(opinions)) {
			return;
		}
		webOpinionResp.saveAll(opinions);
	}

	@Transactional
	public WebOpinion save(WebOpinion opinion) {
		return webOpinionResp.save(opinion);
	}

	public long count() {
		return webOpinionResp.count();
	}

	public Page<WebOpinion> findOpinionByCondition(String querydsl) throws QuerydslException {
		ESFilterBuilder filterBuilder = new ESFilterBuilder() {
		};
		filterBuilder.prepare(querydsl);
		QueryBuilder queryBuilder = filterBuilder.buildFilters();
		NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
		searchQueryBuilder.withQuery(queryBuilder);
		filterBuilder.pagination(searchQueryBuilder);
		filterBuilder.orderBy(searchQueryBuilder);
		return elasticsearchTemplate.queryForPage(searchQueryBuilder.build(), WebOpinion.class);
	}

	public Map<String, Object> countOpinionByCondition(String querydsl) throws QuerydslException {

		ESFilterBuilder filterBuilder = new ESFilterBuilder() {
		};
		filterBuilder.prepare(querydsl);
		QueryBuilder queryBuilder = filterBuilder.buildFilters();
		NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();
		searchQuery.withQuery(queryBuilder);
		List<AggregationBuilder> rootAggs = filterBuilder.buildAggregation();
		for (AggregationBuilder builder : rootAggs) {
			searchQuery.addAggregation((AbstractAggregationBuilder<?>) builder);
		}
		SearchQuery sq = searchQuery.build();
		AggregatedPage<WebOpinion> webOpinions = elasticsearchTemplate.queryForPage(sq, WebOpinion.class);
		Map<String, Object> result = new HashMap<>();
		if (webOpinions.getAggregations() != null) {
			for (Aggregation tmp : webOpinions.getAggregations()) {
				createCounter(result, tmp);
			}
		} else {
			result.put(Constants.DEFAULT_COUNT, webOpinions.getTotalElements());
		}
		return result;
	}

	private void createAggregation(AggregationBuilder aggBuilder, String[] subAggs) {

		String prop = subAggs[0];
		String aggName = UUID.randomUUID().toString();
		AggregationBuilder tmp = AggregationBuilders.terms(aggName).field(prop);
		aggBuilder.subAggregation(tmp);
		String[] subs = Arrays.copyOfRange(subAggs, 1, subAggs.length);
		if (subs.length > 0) {
			createAggregation(tmp, subs);
		}
	}

	private List<AggregationBuilder> createAggregation(String[] subAggs) {
		List<AggregationBuilder> aggs = new ArrayList<>();
		for (String prop : subAggs) {
			String aggName = UUID.randomUUID().toString();
			aggs.add(AggregationBuilders.terms(aggName).field(prop));
		}
		return aggs;
	}

	private NativeSearchQueryBuilder createQueryBuilder(Map<String, Object> props, Operation operation, Fuzzy fuzzy, TimeRange timeRange)
			throws NoSuchFieldException, SecurityException {

		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		if (timeRange != null) {
			queryBuilder.must(QueryBuilders.rangeQuery(timeRange.getTimeField()).lte(timeRange.getEndMs()).gte(timeRange.getStartMs()));
		}

		for (Entry<String, Object> entry : props.entrySet()) {

			String key = entry.getKey();

			if (Operation.Or == operation) {

				if (fuzzy != Fuzzy.None && !FieldDefUtil.supportFuzzyQuery(WebOpinion.class.getDeclaredField(key))) {
					queryBuilder.should(QueryBuilders.termQuery(key, entry.getValue()));
				} else if (fuzzy == Fuzzy.Prefix) {
					queryBuilder.should(QueryBuilders.prefixQuery(key, entry.getValue().toString()));
				} else if (fuzzy == Fuzzy.Wildcard) {
					queryBuilder.should(QueryBuilders.wildcardQuery(key, entry.getValue().toString()));
				} else if (fuzzy == Fuzzy.Match) {
					queryBuilder.should(QueryBuilders.matchQuery(key, entry.getValue().toString()));
				} else if (fuzzy == Fuzzy.None) {
					queryBuilder.should(QueryBuilders.termQuery(key, entry.getValue()));
				}
			} else if (Operation.And == operation) {
				if (fuzzy != Fuzzy.None && !FieldDefUtil.supportFuzzyQuery(WebOpinion.class.getDeclaredField(key))) {
					queryBuilder.must(QueryBuilders.termQuery(key, entry.getValue()));
				} else if (fuzzy == Fuzzy.Prefix) {
					queryBuilder.must(QueryBuilders.prefixQuery(key, entry.getValue().toString()));
				} else if (fuzzy == Fuzzy.Wildcard) {
					queryBuilder.must(QueryBuilders.wildcardQuery(key, entry.getValue().toString()));
				} else if (fuzzy == Fuzzy.Match) {
					queryBuilder.must(QueryBuilders.matchQuery(key, entry.getValue().toString()));
				} else if (fuzzy == Fuzzy.None) {
					queryBuilder.must(QueryBuilders.termQuery(key, entry.getValue()));
				}
			}

		}

		NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder);
		return searchQuery;

	}

	private void createCounter(Map<String, Object> result, Aggregation tmp) {

		if (tmp instanceof InternalDateHistogram) {
			InternalDateHistogram idh = (InternalDateHistogram) tmp;
			for (Bucket bucket : idh.getBuckets()) {
				Map<String, Object> dataResult = new HashMap<>();
				if (CollectionUtils.isEmpty(bucket.getAggregations().asList())) {
					dataResult.put(Constants.DEFAULT_COUNT, bucket.getDocCount());
					result.put(bucket.getKeyAsString(), dataResult);
				} else {
					bucket.getAggregations().forEach(action -> {
						createCounter(dataResult, action);
					});
					result.put(bucket.getKeyAsString(), dataResult);
				}
			}
		} else if (tmp instanceof Terms) {
			for (Terms.Bucket bucket : ((Terms) tmp).getBuckets()) {
				Map<String, Object> dataResult = new HashMap<>();
				if (bucket.getAggregations() != null && bucket.getAggregations().asList().size() > 0) {
					for (Aggregation agg : bucket.getAggregations().asList()) {
						createCounter(dataResult, agg);
					}
					result.put(bucket.getKeyAsString(), dataResult);
				} else {
					result.put(bucket.getKeyAsString(), bucket.getDocCount());
				}
			}
		}

	}

	public Map<String, Object> countByCondition(Map<String, Object> props, Operation operation, Fuzzy fuzzy, TimeRange timeRange) {

		Map<String, Object> result = new HashMap<>();

		NativeSearchQueryBuilder searchQuery = null;
		try {
			searchQuery = createQueryBuilder(props, operation, fuzzy, timeRange);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}

		AggregationBuilder rootAgg = null;

		if (timeRange.getInterval() != null) {
			rootAgg = AggregationBuilders.dateHistogram(aggName).field(timeRange.getTimeField()).dateHistogramInterval(timeRange.getInterval()).minDocCount(0)
					.extendedBounds(new ExtendedBounds(timeRange.getStartMs(), timeRange.getEndMs()));
		}

		if (timeRange.getGroupByProp() != null && timeRange.getGroupByProp().length > 0) {

			if (timeRange.getAggregationType() == AggregationType.Tiling) {
				List<AggregationBuilder> subAggs = createAggregation(timeRange.getGroupByProp());
				for (AggregationBuilder subAgg : subAggs) {
					if (rootAgg != null) {
						rootAgg.subAggregation(subAgg);
					} else {
						if (subAgg instanceof TermsAggregationBuilder) {
							searchQuery.addAggregation((TermsAggregationBuilder) subAgg);
						} else if (subAgg instanceof NestedAggregationBuilder) {
							searchQuery.addAggregation((NestedAggregationBuilder) subAgg);
						}
					}
				}

			} else {
				AggregationBuilder aggBuilder = null;
				if (rootAgg != null) {
					createAggregation(rootAgg, timeRange.getGroupByProp());
					aggBuilder = rootAgg;
				} else {
					String prop = timeRange.getGroupByProp()[0];
					rootAgg = AggregationBuilders.terms(aggName).field(prop);
					String[] subs = Arrays.copyOfRange(timeRange.getGroupByProp(), 1, timeRange.getGroupByProp().length);
					createAggregation(rootAgg, subs);
					aggBuilder = rootAgg;
				}
				if (aggBuilder instanceof TermsAggregationBuilder) {
					searchQuery.addAggregation((TermsAggregationBuilder) aggBuilder);
				} else if (aggBuilder instanceof NestedAggregationBuilder) {
					searchQuery.addAggregation((NestedAggregationBuilder) aggBuilder);
				}
			}

		} else {
			if (rootAgg != null) {
				if (rootAgg instanceof TermsAggregationBuilder) {
					searchQuery.addAggregation((TermsAggregationBuilder) rootAgg);
				} else if (rootAgg instanceof DateHistogramAggregationBuilder) {
					searchQuery.addAggregation((DateHistogramAggregationBuilder) rootAgg);
				}

			}

		}

		SearchQuery sq = searchQuery.build();

		AggregatedPage<WebOpinion> alarms = elasticsearchTemplate.queryForPage(sq, WebOpinion.class);
		if (alarms.getAggregations() != null) {
			for (Aggregation tmp : alarms.getAggregations()) {
				createCounter(result, tmp);
			}
		} else {
			result.put(Constants.DEFAULT_COUNT, alarms.getTotalElements());
		}

		return result;
	}

}