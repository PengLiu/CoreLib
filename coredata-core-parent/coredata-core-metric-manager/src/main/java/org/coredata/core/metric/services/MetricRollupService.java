package org.coredata.core.metric.services;

import java.util.ArrayList;
import java.util.List;

import org.coredata.core.metric.documents.Metric;
import org.coredata.core.metric.documents.MetricRollup;
import org.coredata.core.metric.repositories.MetricRollupResp;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.stats.extended.InternalExtendedStats;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MetricRollupService {

	private long batchSize = 1;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Autowired
	private MetricRollupResp metricRollupResp;

	/**
	 * Rollup metric index per hour
	 */
	@Scheduled(cron = "0 0 * * * *")
	public void rollUp() {

		long now = System.currentTimeMillis();
		long startMs = now - 3600 * 1000;

		RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(Metric.timeFieldId).from(startMs).to(now);

		DateHistogramAggregationBuilder dateBuilder = AggregationBuilders.dateHistogram("agg").field(Metric.timeFieldId)
				.dateHistogramInterval(DateHistogramInterval.hours(1)).minDocCount(0).extendedBounds(new ExtendedBounds(startMs, now));
		dateBuilder.subAggregation(AggregationBuilders.terms("sugAgg").field(Metric.rollUpFieldId)
				.subAggregation(AggregationBuilders.extendedStats("sugAgg2").field(Metric.valFieldId)));

		NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder().withQuery(rangeQueryBuilder).addAggregation(dateBuilder);

		AggregatedPage<Metric> pages = elasticsearchTemplate.queryForPage(searchQuery.build(), Metric.class);

		InternalDateHistogram agg = (InternalDateHistogram) pages.getAggregation("agg");

		List<MetricRollup> cache = new ArrayList<>();

		for (InternalDateHistogram.Bucket bucket : agg.getBuckets()) {
			for (Aggregation subAgg : bucket.getAggregations().asList()) {
				StringTerms st = (StringTerms) subAgg;
				for (StringTerms.Bucket termsBubkcet : st.getBuckets()) {
					InternalExtendedStats stats = (InternalExtendedStats) termsBubkcet.getAggregations().get("sugAgg2");
					String[] ids = termsBubkcet.getKeyAsString().split("_");
					MetricRollup mr = new MetricRollup();
					mr.setCreatedTime(((DateTime) bucket.getKey()).getMillis());
					mr.setToken(ids[0]);
					mr.setEntityId(ids[1]);
					mr.setMetricId(ids[2]);
					mr.setAvg(stats.getAvg());
					mr.setMax(stats.getMax());
					mr.setMin(stats.getMin());
					mr.setCount(stats.getCount());
					mr.setSum(stats.getSum());
					mr.setSumOfSquares(stats.getSumOfSquares());
					mr.setStdDeviation(stats.getStdDeviation());
					mr.setVariance(stats.getVariance());
					cache.add(mr);
					if (cache.size() >= batchSize) {
						metricRollupResp.saveAll(cache);
						cache.clear();
					}
				}
			}
		}
		if (cache.size() > 0) {
			metricRollupResp.saveAll(cache);
		}
	}

}
