package org.coredata.core.metric.services;

import org.coredata.core.metric.documents.Metric;
import org.coredata.core.metric.repositories.MetricResp;
import org.coredata.core.metric.vos.MetricVal;
import org.coredata.util.query.TimeRange;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.aggregations.metrics.max.InternalMax;
import org.elasticsearch.search.aggregations.metrics.min.InternalMin;
import org.elasticsearch.search.aggregations.metrics.tophits.InternalTopHits;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class MetricService {

	private String aggName = "agg";

	private String subAggName = "subAgg";

	private String dateAggName = "dateAgg";

	private String topSubAggName = "topSubAgg";

	@Autowired
	private MetricResp metricResp;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@PostConstruct
	public void init() {
		elasticsearchTemplate.putMapping(Metric.class);
	}

	@Transactional
	public void save(List<Metric> metrics) {
		metricResp.saveAll(metrics);
	}

	@Transactional
	public Metric save(Metric metric) {
		return metricResp.save(metric);
	}

	private NativeSearchQueryBuilder createQueryBuilder(String entityId, String metricId, TimeRange timeRange) {
		RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(timeRange.getTimeField()).from(timeRange.getStartMs()).to(timeRange.getEndMs());
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(rangeQueryBuilder);
		if (!StringUtils.isEmpty(entityId)) {
			boolQueryBuilder.must(QueryBuilders.termQuery(Metric.entityFieldId, entityId));
		}
		if (!StringUtils.isEmpty(metricId)) {
			boolQueryBuilder.must(QueryBuilders.termQuery(Metric.metricFieldId, metricId));
		}
		return new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);
	}

	private NativeSearchQueryBuilder createQueryBuilder(String entityId, String[] metricIds, TimeRange timeRange) {
		RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(timeRange.getTimeField()).from(timeRange.getStartMs()).to(timeRange.getEndMs());
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(rangeQueryBuilder);
		if (!StringUtils.isEmpty(entityId)) {
			boolQueryBuilder.must(QueryBuilders.termQuery(Metric.entityFieldId, entityId));
		}

        BoolQueryBuilder conditionBuilder = QueryBuilders.boolQuery();
		for (String metricId : metricIds) {
            conditionBuilder.should(QueryBuilders.termQuery(Metric.metricFieldId, metricId));
		}
        boolQueryBuilder.must(conditionBuilder);

		return new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);
	}

	private NativeSearchQueryBuilder createQueryBuilder(String[] entityIds, String metricId, TimeRange timeRange) {
		RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(timeRange.getTimeField()).from(timeRange.getStartMs()).to(timeRange.getEndMs());
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(rangeQueryBuilder);
		if (!StringUtils.isEmpty(metricId)) {
			boolQueryBuilder.must(QueryBuilders.termQuery(Metric.metricFieldId, metricId));
		}

        BoolQueryBuilder conditionBuilder = QueryBuilders.boolQuery();
        for (String entityId : entityIds) {
            conditionBuilder.should(QueryBuilders.termQuery(Metric.entityFieldId, entityId));
		}
        boolQueryBuilder.must(conditionBuilder);

		return new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);
	}

	public Page<Metric> loadMetricByEntityAndTimeRange(String entityId, String metricId, TimeRange timeRange, Pageable pageable) {
		NativeSearchQueryBuilder queryBuilder = createQueryBuilder(entityId, metricId, timeRange);
		SearchQuery searchQuery = queryBuilder.withPageable(pageable).build();
		return elasticsearchTemplate.queryForPage(searchQuery, Metric.class);
	}

	public Collection<MetricVal> loadMetricByEntityAndTimeRange(String entityId, String metricId, TimeRange timeRange, String[] algs) {

		Collection<MetricVal> vals = new ArrayList<>();

		NativeSearchQueryBuilder queryBuilder = createQueryBuilder(entityId, metricId, timeRange);

		if (timeRange.getInterval() != null) {
			DateHistogramAggregationBuilder aggBuilder = AggregationBuilders.dateHistogram(aggName).field(timeRange.getTimeField())
					.dateHistogramInterval(timeRange.getInterval()).minDocCount(0)
					.extendedBounds(new ExtendedBounds(timeRange.getStartMs(), timeRange.getEndMs()));
			if (algs != null)
				setAlgForBuilder(algs, aggBuilder);
			queryBuilder.addAggregation(aggBuilder);
		}

		SearchQuery searchQuery = queryBuilder.build();

		AggregatedPage<Metric> pages = elasticsearchTemplate.queryForPage(searchQuery, Metric.class);

		InternalDateHistogram agg = (InternalDateHistogram) pages.getAggregation(aggName);
		for (InternalDateHistogram.Bucket bucket : agg.getBuckets()) {
			MetricVal metricVal = new MetricVal(((DateTime) bucket.getKey()).getMillis());
			for (String alg : algs) {
				if (Metric.ALG_AVG.equals(alg)) {
					InternalAvg avg = (InternalAvg) bucket.getAggregations().get(alg);
					metricVal.setAvg(avg.getValue());
				} else if (Metric.ALG_MAX.equals(alg)) {
					InternalMax avg = (InternalMax) bucket.getAggregations().get(alg);
					metricVal.setMax(avg.getValue());
				} else if (Metric.ALG_MIN.equals(alg)) {
					InternalMin avg = (InternalMin) bucket.getAggregations().get(alg);
					metricVal.setMin(avg.getValue());
				}
			}
			vals.add(metricVal);
		}

		return vals;
	}

	public Page<Metric> findLastMetricVal(String metricId, Pageable pageable) {
		TermQueryBuilder entityQueryBuilder = QueryBuilders.termQuery(Metric.metricFieldId, metricId);
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(entityQueryBuilder).build();
		searchQuery.setPageable(pageable);
		Page<Metric> metrics = elasticsearchTemplate.queryForPage(searchQuery, Metric.class);
		return metrics;
	}

	public Map<String, Collection<Metric>> findLastMetricsByEntities(String[] entityIds) {

		Map<String, Collection<Metric>> entityMetrics = new HashMap<>();

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		for (String entityId : entityIds) {
			boolQueryBuilder.should(QueryBuilders.termQuery(Metric.entityFieldId, entityId));
		}

		TopHitsAggregationBuilder tophitAggBuilder = AggregationBuilders.topHits(topSubAggName).sort(Metric.timeFieldId, SortOrder.DESC).size(1);
		TermsAggregationBuilder subAggBuilder = AggregationBuilders.terms(subAggName).field(Metric.metricFieldId).subAggregation(tophitAggBuilder);
		TermsAggregationBuilder aggBuilder = AggregationBuilders.terms(aggName).field(Metric.entityFieldId).size(entityIds.length)
				.subAggregation(subAggBuilder);

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).addAggregation(aggBuilder).build();

		AggregatedPage<Metric> metrics = elasticsearchTemplate.queryForPage(searchQuery, Metric.class);

		StringTerms agg = (StringTerms) metrics.getAggregation(aggName);
		for (Bucket bucket : agg.getBuckets()) {
			StringTerms subAgg = (StringTerms) bucket.getAggregations().get(subAggName);
			Collection<Metric> metricList = new ArrayList<>();
			for (Bucket topBucket : subAgg.getBuckets()) {
				InternalTopHits topHit = (InternalTopHits) topBucket.getAggregations().asList().iterator().next();
				metricList.add(Metric.inst(topHit.getHits().getHits()[0].getSourceAsMap()));
			}
			entityMetrics.put(bucket.getKeyAsString(), metricList);
		}

		return entityMetrics;
	}

    public List<Metric> findLastByEntitiesAndMetric(String[] entityIds,String metricId) {

        List<Metric> metricList = new ArrayList<>();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder contionQuery = QueryBuilders.boolQuery();
        for (String entityId : entityIds) {
            contionQuery.should(QueryBuilders.termQuery(Metric.entityFieldId, entityId));
        }
        boolQueryBuilder.must(contionQuery);
        boolQueryBuilder.must(QueryBuilders.termQuery(Metric.metricFieldId,metricId));

        TopHitsAggregationBuilder tophitAggBuilder = AggregationBuilders.topHits(topSubAggName).sort(Metric.timeFieldId, SortOrder.DESC).size(1);
        TermsAggregationBuilder aggBuilder = AggregationBuilders.terms(aggName).field(Metric.entityFieldId).size(entityIds.length)
                .subAggregation(tophitAggBuilder);

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).addAggregation(aggBuilder).build();

        AggregatedPage<Metric> metrics = elasticsearchTemplate.queryForPage(searchQuery, Metric.class);

        StringTerms agg = (StringTerms) metrics.getAggregation(aggName);
        for (Bucket bucket : agg.getBuckets()) {
                InternalTopHits topHit = (InternalTopHits) bucket.getAggregations().asList().iterator().next();
            metricList.add(Metric.inst(topHit.getHits().getHits()[0].getSourceAsMap()));
        }

        return metricList;
    }

	public Map<String, Collection<MetricVal>> loadMetricsByEntityAndTimeRange(String entityId, String[] metricIds, TimeRange timeRange, String[] algs) {

		Map<String, Collection<MetricVal>> metricVals = new HashMap<>();

		NativeSearchQueryBuilder queryBuilder = createQueryBuilder(entityId, metricIds, timeRange);

		addAggregation(timeRange, algs, queryBuilder);

		SearchQuery searchQuery = queryBuilder.build();

		AggregatedPage<Metric> metrics = elasticsearchTemplate.queryForPage(searchQuery, Metric.class);

		StringTerms agg = (StringTerms) metrics.getAggregation(aggName);
		for (Bucket bucket : agg.getBuckets()) {
			StringTerms subAgg = bucket.getAggregations().get(subAggName);
			for (Bucket topBucket : subAgg.getBuckets()) {

				InternalDateHistogram dateAgg = topBucket.getAggregations().get(dateAggName);
				Collection<MetricVal> vals = new ArrayList<>();

				for (InternalDateHistogram.Bucket dateBucket : dateAgg.getBuckets()) {
					MetricVal metricVal = new MetricVal(((DateTime) dateBucket.getKey()).getMillis());
					for (String alg : algs) {
						if (Metric.ALG_AVG.equals(alg)) {
							InternalAvg avg = (InternalAvg) dateBucket.getAggregations().get(alg);
							metricVal.setAvg(avg.getValue());
						} else if (Metric.ALG_MAX.equals(alg)) {
							InternalMax avg = (InternalMax) dateBucket.getAggregations().get(alg);
							metricVal.setMax(avg.getValue());
						} else if (Metric.ALG_MIN.equals(alg)) {
							InternalMin avg = (InternalMin) dateBucket.getAggregations().get(alg);
							metricVal.setMin(avg.getValue());
						}
					}
					vals.add(metricVal);
				}
				metricVals.put(topBucket.getKeyAsString(), vals);
			}

		}

		return metricVals;
	}

	public Map<String, Collection<MetricVal>> loadMetricByEntitiesAndTimeRange(String[] entityIds, String metricId, TimeRange timeRange, String[] algs) {

		Map<String, Collection<MetricVal>> metricVals = new HashMap<>();

		NativeSearchQueryBuilder queryBuilder = createQueryBuilder(entityIds, metricId, timeRange);

		addAggregation(timeRange, algs, queryBuilder);

		SearchQuery searchQuery = queryBuilder.build();

		AggregatedPage<Metric> metrics = elasticsearchTemplate.queryForPage(searchQuery, Metric.class);

		StringTerms agg = (StringTerms) metrics.getAggregation(aggName);
		for (Bucket bucket : agg.getBuckets()) {
			StringTerms subAgg = bucket.getAggregations().get(subAggName);
			for (Bucket topBucket : subAgg.getBuckets()) {

				InternalDateHistogram dateAgg = topBucket.getAggregations().get(dateAggName);
				Collection<MetricVal> vals = new ArrayList<>();

				for (InternalDateHistogram.Bucket dateBucket : dateAgg.getBuckets()) {
					MetricVal metricVal = new MetricVal(((DateTime) dateBucket.getKey()).getMillis());
					for (String alg : algs) {
						if (Metric.ALG_AVG.equals(alg)) {
							InternalAvg avg = (InternalAvg) dateBucket.getAggregations().get(alg);
							metricVal.setAvg(avg.getValue());
						} else if (Metric.ALG_MAX.equals(alg)) {
							InternalMax avg = (InternalMax) dateBucket.getAggregations().get(alg);
							metricVal.setMax(avg.getValue());
						} else if (Metric.ALG_MIN.equals(alg)) {
							InternalMin avg = (InternalMin) dateBucket.getAggregations().get(alg);
							metricVal.setMin(avg.getValue());
						}
					}
					vals.add(metricVal);
				}
                metricVals.put(bucket.getKeyAsString(), vals);
			}

		}

		return metricVals;
	}

	public Map<String, Object> loadMaxMetricByEntitiesAndTimeRange(String[] entityIds, String metricId, TimeRange timeRange) {


		NativeSearchQueryBuilder queryBuilder = createQueryBuilder(entityIds, metricId, timeRange);

        TermsAggregationBuilder aggBuilder = AggregationBuilders.terms(aggName).field(Metric.entityFieldId);
        TopHitsAggregationBuilder sub = AggregationBuilders.topHits(topSubAggName).sort(Metric.valFieldId, SortOrder.DESC).size(1);
        aggBuilder.subAggregation(sub);


        queryBuilder.addAggregation(aggBuilder);
		SearchQuery searchQuery = queryBuilder.build();

		AggregatedPage<Metric> metrics = elasticsearchTemplate.queryForPage(searchQuery, Metric.class);

		StringTerms agg = (StringTerms) metrics.getAggregation(aggName);
        Map<String, Object> map = new HashMap<>();
		for (Bucket bucket : agg.getBuckets()) {
            TopHits topHits = bucket.getAggregations().get(topSubAggName);
            for (SearchHit hit : topHits.getHits()) {
                Map<String, Object> fields = hit.getSourceAsMap();
                if (fields.get(Metric.valFieldId) != null) {
                    map.put(fields.get(Metric.entityFieldId).toString(), fields.get(Metric.valFieldId));
                }

            }

		}

		return map;
	}

	public Map<String, Collection<Metric>> loadRawMetricsByEntityAndTimeRange(String entityId, String[] metricIds, TimeRange timeRange) {

		Map<String, Collection<Metric>> entityMetrics = new HashMap<>();

		NativeSearchQueryBuilder queryBuilder = createQueryBuilder(entityId, metricIds, timeRange);

		addAggregation(timeRange, null, queryBuilder);

		SearchQuery searchQuery = queryBuilder.build();

		AggregatedPage<Metric> pages = elasticsearchTemplate.queryForPage(searchQuery, Metric.class);

		List<Metric> metricList = pages.getContent();

		for (Metric metric : metricList) {
			if (entityMetrics.get(metric.getMetricId()) != null) {
				entityMetrics.get(metric.getMetricId()).add(metric);
			} else {
				List<Metric> _metrics = new ArrayList<>();
				_metrics.add(metric);
				entityMetrics.put(metric.getMetricId(), _metrics);
			}
		}

		return entityMetrics;
	}

	private void addAggregation(TimeRange timeRange, String[] algs, NativeSearchQueryBuilder queryBuilder) {
		TermsAggregationBuilder subAggBuilder;

		if (algs != null) {
			DateHistogramAggregationBuilder dateBuilder = AggregationBuilders.dateHistogram(dateAggName).field(timeRange.getTimeField())
					.dateHistogramInterval(timeRange.getInterval()).minDocCount(0)
					.extendedBounds(new ExtendedBounds(timeRange.getStartMs(), timeRange.getEndMs()));
			setAlgForBuilder(algs, dateBuilder);
			subAggBuilder = AggregationBuilders.terms(subAggName).field(Metric.metricFieldId).subAggregation(dateBuilder);
		} else {
			subAggBuilder = AggregationBuilders.terms(subAggName).field(Metric.metricFieldId);
		}

		TermsAggregationBuilder aggBuilder = AggregationBuilders.terms(aggName).field(Metric.entityFieldId).subAggregation(subAggBuilder);
		queryBuilder.addAggregation(aggBuilder);
	}

	private void setAlgForBuilder(String[] algs, DateHistogramAggregationBuilder dateBuilder) {
		for (String alg : algs) {
			if (Metric.ALG_AVG.equals(alg)) {
				AggregationBuilder subAggregation = AggregationBuilders.avg(alg).field(Metric.valFieldId);
				dateBuilder.subAggregation(subAggregation);
			} else if (Metric.ALG_MAX.equals(alg)) {
				AggregationBuilder subAggregation = AggregationBuilders.max(alg).field(Metric.valFieldId);
				dateBuilder.subAggregation(subAggregation);
			} else if (Metric.ALG_MIN.equals(alg)) {
				AggregationBuilder subAggregation = AggregationBuilders.min(alg).field(Metric.valFieldId);
				dateBuilder.subAggregation(subAggregation);
			} else {
				AggregationBuilder subAggregation = AggregationBuilders.avg(alg).field(Metric.valFieldId);
				dateBuilder.subAggregation(subAggregation);
			}
		}
	}

}