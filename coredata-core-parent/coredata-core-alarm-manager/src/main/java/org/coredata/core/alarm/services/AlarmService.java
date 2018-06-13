package org.coredata.core.alarm.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.alarm.documents.Alarm;
import org.coredata.core.alarm.repositories.AlarmResp;
import org.coredata.core.util.elasticsearch.querydsl.Constants;
import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram.Bucket;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
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
public class AlarmService {

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Autowired
	private AlarmResp alarmResp;

	@Transactional
	public Alarm save(Alarm alarm) {
		return alarmResp.save(alarm);
	}

	@Transactional
	public void save(List<Alarm> alarms) {
		alarmResp.saveAll(alarms);
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

	public Page<Alarm> findAlarmByCondition(String queryStr) throws QuerydslException {
		AlarmFilterBuilder filterBuilder = new AlarmFilterBuilder(queryStr);
		QueryBuilder queryBuilder = filterBuilder.buildFilters();
		NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
		searchQueryBuilder.withQuery(queryBuilder);
		filterBuilder.pagination(searchQueryBuilder);
		filterBuilder.orderBy(searchQueryBuilder);
		return elasticsearchTemplate.queryForPage(searchQueryBuilder.build(), Alarm.class);
	}

	public Map<String, Object> countAlarmByCondition(String queryStr) throws QuerydslException {

		AlarmFilterBuilder filterBuilder = new AlarmFilterBuilder(queryStr);
		QueryBuilder queryBuilder = filterBuilder.buildFilters();
		NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();
		searchQuery.withQuery(queryBuilder);
		
		List<AggregationBuilder> rootAggs = filterBuilder.buildAggregation();
		for(AggregationBuilder builder : rootAggs) {
			searchQuery.addAggregation((AbstractAggregationBuilder<?>)builder);
		}		
		
		SearchQuery sq = searchQuery.build();
		AggregatedPage<Alarm> alarms = elasticsearchTemplate.queryForPage(sq, Alarm.class);

		Map<String, Object> result = new HashMap<>();
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