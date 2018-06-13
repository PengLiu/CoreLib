package org.coredata.core.business.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.lucene.search.join.ScoreMode;
import org.coredata.core.business.documents.PacketAnalys;
import org.coredata.util.query.AggregationType;
import org.coredata.util.query.Constants;
import org.coredata.util.query.FieldDefUtil;
import org.coredata.util.query.Fuzzy;
import org.coredata.util.query.Operation;
import org.coredata.util.query.TimeRange;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram.Bucket;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Transactional(readOnly = true)
public class PacketAnalysService {

	private String aggName = "agg";

	private String subAggName = "subAgg";

	private String nestedAggName = "nestedAgg";

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;


	private NativeSearchQueryBuilder createQueryBuilder(Map<String, Object> props, Operation operation, Fuzzy fuzzy, TimeRange timeRange) {

		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		if (timeRange != null) {
			queryBuilder.must(QueryBuilders.rangeQuery(timeRange.getTimeField()).lte(timeRange.getEndMs()).gte(timeRange.getStartMs()));
		}

		BoolQueryBuilder attributeQueryBuilder = null;

		BoolQueryBuilder propsQueryBuilder = null;
		NestedQueryBuilder propsNestedQueryBuilder = null;

		BoolQueryBuilder sourceQueryBuilder = null;
		NestedQueryBuilder sourceNestedQueryBuilder = null;

		for (Entry<String, Object> entry : props.entrySet()) {

			BoolQueryBuilder currentQueryBuilder = null;

			String key = entry.getKey();

			if (entry.getKey().startsWith("props.")) {
				if (propsNestedQueryBuilder == null) {
					propsQueryBuilder = QueryBuilders.boolQuery();
					propsNestedQueryBuilder = QueryBuilders.nestedQuery("props", propsQueryBuilder, ScoreMode.None);
				}
				currentQueryBuilder = propsQueryBuilder;
			} else if (PacketAnalys.isAttribute(entry.getKey())) {

				if (attributeQueryBuilder == null) {
					attributeQueryBuilder = QueryBuilders.boolQuery();
				}
				currentQueryBuilder = attributeQueryBuilder;
			} 

			boolean supportFuzzy = true;
			try {
				supportFuzzy = FieldDefUtil.supportFuzzyQuery(PacketAnalys.class.getDeclaredField(key));
			} catch (NoSuchFieldException | SecurityException e) {
				;
			}

			if (Operation.Or == operation) {

				if (!supportFuzzy && fuzzy != Fuzzy.None) {
					currentQueryBuilder.should(QueryBuilders.termQuery(key, entry.getValue()));
				} else if (fuzzy == Fuzzy.Prefix) {
					currentQueryBuilder.should(QueryBuilders.prefixQuery(key, entry.getValue().toString()));
				} else if (fuzzy == Fuzzy.Wildcard) {
					currentQueryBuilder.should(QueryBuilders.wildcardQuery(key, entry.getValue().toString()));
				} else if (fuzzy == Fuzzy.None) {
					currentQueryBuilder.should(QueryBuilders.termQuery(key, entry.getValue()));
				} else if (fuzzy == Fuzzy.Match) {
					currentQueryBuilder.should(QueryBuilders.matchQuery(key, entry.getValue()));
				}
			} else if (Operation.And == operation) {
				if (!supportFuzzy && fuzzy != Fuzzy.None) {
					currentQueryBuilder.must(QueryBuilders.termQuery(key, entry.getValue()));
				} else if (fuzzy == Fuzzy.Prefix) {
					currentQueryBuilder.must(QueryBuilders.prefixQuery(key, entry.getValue().toString()));
				} else if (fuzzy == Fuzzy.Wildcard) {
					currentQueryBuilder.must(QueryBuilders.wildcardQuery(key, entry.getValue().toString()));
				} else if (fuzzy == Fuzzy.None) {
					currentQueryBuilder.must(QueryBuilders.termQuery(key, entry.getValue()));
				} else if (fuzzy == Fuzzy.Match) {
					currentQueryBuilder.must(QueryBuilders.matchQuery(key, entry.getValue()));
				}
			}

		}

		NativeSearchQueryBuilder searchQuery = null;

		if (Operation.Or == operation) {
			if (attributeQueryBuilder != null) {
				queryBuilder.should(attributeQueryBuilder);
			}
			if (propsNestedQueryBuilder != null) {
				queryBuilder.should(propsNestedQueryBuilder);
			}
			if (sourceNestedQueryBuilder != null) {
				queryBuilder.should(sourceNestedQueryBuilder);
			}
		} else {
			if (attributeQueryBuilder != null) {
				queryBuilder.must(attributeQueryBuilder);
			}
			if (propsNestedQueryBuilder != null) {
				queryBuilder.must(propsNestedQueryBuilder);
			}
			if (sourceNestedQueryBuilder != null) {
				queryBuilder.must(sourceNestedQueryBuilder);
			}
		}

		searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder);
		return searchQuery;

	}

	
	private void createAggregation(AggregationBuilder aggBuilder, String[] subAggs) {
		AggregationBuilder tmp = null;
		String prop = subAggs[0];
		String aggName = UUID.randomUUID().toString();
		if (prop.startsWith("props.")) {
			tmp = AggregationBuilders.nested(nestedAggName, "props").subAggregation(AggregationBuilders.terms(aggName).field(prop));
		} else {
			tmp = AggregationBuilders.terms(aggName).field(prop);
		}
		if (aggBuilder == null) {
			aggBuilder = tmp;
		} else {
			aggBuilder.subAggregation(tmp);
		}
		String[] subs = Arrays.copyOfRange(subAggs, 1, subAggs.length);
		if (subs.length > 0) {
			createAggregation(tmp, subs);
		}
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
		} else if (tmp instanceof InternalNested) {
			for (Aggregation nestedAgg : ((InternalNested) tmp).getAggregations().asList()) {
				for (Terms.Bucket bucket : ((Terms) nestedAgg).getBuckets()) {
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

	}
	
	private List<AggregationBuilder> createAggregation(String[] subAggs) {
		List<AggregationBuilder> aggs = new ArrayList<>();
		for (String prop : subAggs) {
			String aggName = UUID.randomUUID().toString();
			if (prop.startsWith("props.")) {
				aggs.add(AggregationBuilders.nested(nestedAggName, "props").subAggregation(AggregationBuilders.terms(aggName).field(prop)));
			} else {
				aggs.add(AggregationBuilders.terms(aggName).field(prop));
			}
		}
		return aggs;
	}
	
	public Map<String, Object> countPacketAnalysByProps(Map<String, Object> props, Operation operation, Fuzzy fuzzy, TimeRange timeRange) {

		Map<String, Object> result = new HashMap<>();

		NativeSearchQueryBuilder searchQuery = createQueryBuilder(props, operation, fuzzy, timeRange);

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
					if (prop.startsWith("props.")) {
						rootAgg = AggregationBuilders.nested(nestedAggName, "props").subAggregation(AggregationBuilders.terms(aggName).field(prop));
					} else {
						rootAgg = AggregationBuilders.terms(aggName).field(prop);
					}
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
				} else if (rootAgg instanceof NestedAggregationBuilder) {
					searchQuery.addAggregation((NestedAggregationBuilder) rootAgg);
				} else if (rootAgg instanceof DateHistogramAggregationBuilder) {
					searchQuery.addAggregation((DateHistogramAggregationBuilder) rootAgg);
				}

			}

		}

		SearchQuery sq = searchQuery.build();

		AggregatedPage<PacketAnalys> alarms = elasticsearchTemplate.queryForPage(sq, PacketAnalys.class);
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