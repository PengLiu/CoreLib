package org.coredata.core.util.elasticsearch.querydsl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.coredata.core.util.querydsl.Direction;
import org.coredata.core.util.querydsl.Filter;
import org.coredata.core.util.querydsl.Ops;
import org.coredata.core.util.querydsl.Pagination;
import org.coredata.core.util.querydsl.Sort;
import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.ip.IpRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public abstract class ESFilterBuilder {

	private ObjectMapper mapper = new ObjectMapper();

	protected String aggName = "agg";

	protected String nestedAggName = "nestedAgg";

	protected Sort sort;

	protected Pagination pagination;

	protected TimeRangeFilter timeRangeFilter;

	protected JsonNode aggregations;

	protected JsonNode filter;

	protected QueryBuilder filterToBuilder(Filter filter) throws QuerydslException {

		QueryBuilder builder = null;

		String field = filter.getField();

		switch (filter.getOps()) {
		case lt:
			builder = QueryBuilders.rangeQuery(field).lt(filter.getValue());
			break;
		case lte:
			builder = QueryBuilders.rangeQuery(field).lte(filter.getValue());
			break;
		case gt:
			builder = QueryBuilders.rangeQuery(field).gt(filter.getValue());
			break;
		case gte:
			builder = QueryBuilders.rangeQuery(field).gte(filter.getValue());
			break;
		case eq:
			builder = QueryBuilders.termQuery(field, filter.getValue());
			break;
		case neq:
			builder = QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(field, filter.getValue()));
			break;
		case isnull:
			builder = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(field));
			break;
		case notnull:
			builder = QueryBuilders.existsQuery(field);
			break;
		case contains:
			builder = QueryBuilders.matchQuery(field, filter.getValue());
			break;
		case notcontains:
			builder = QueryBuilders.boolQuery().mustNot(QueryBuilders.matchQuery(field, filter.getValue()));
			break;
		case like:
			builder = QueryBuilders.wildcardQuery(field, filter.getValue().toString());
			break;
		case prefix:
			if (filter.getValue() instanceof String) {
				builder = QueryBuilders.prefixQuery(field, filter.getValue().toString());
			} else {
				builder = QueryBuilders.matchQuery(field, filter.getValue());
			}
			break;
		default:
			throw new QuerydslException("Unsupported operator " + filter.getOps().toString() + ".");
		}

		return builder;
	}

	public void orderBy(NativeSearchQueryBuilder queryBuilder) throws QuerydslException {
		if (sort != null) {
			for (String field : sort.getFields()) {
				FieldSortBuilder fsb = SortBuilders.fieldSort(field).order(sort.getDirection() == Direction.ASC ? SortOrder.ASC : SortOrder.DESC);
				queryBuilder.withSort(fsb);
			}
		}
	}

	public void pagination(NativeSearchQueryBuilder queryBuilder) throws QuerydslException {
		Pageable pageable = null;
		if (pagination != null) {
			pageable = PageRequest.of(pagination.getPage() - 1, pagination.getSize());
		} else {
			pageable = PageRequest.of(0, 50);
		}
		queryBuilder.withPageable(pageable);
	}

	protected List<AggregationBuilder> createAggregation(String[] subAggs) {

		List<AggregationBuilder> aggs = new ArrayList<>();
		for (String prop : subAggs) {
			String aggName = UUID.randomUUID().toString();
			aggs.add(AggregationBuilders.terms(aggName).field(prop));
		}
		return aggs;
	}

	protected void createAggregation(AggregationBuilder aggBuilder, String[] subAggs) {
		String prop = subAggs[0];
		String aggName = UUID.randomUUID().toString();
		AggregationBuilder tmp = AggregationBuilders.terms(aggName).field(prop);
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

	protected AggregationBuilder createAggregation(String field) {
		return AggregationBuilders.terms(aggName).field(field);
	}

	public void prepare(String query) throws QuerydslException {

		try {
			JsonNode condition = mapper.readTree(query);

			if (condition.has("filter")) {
				filter = condition.get("filter");
			}

			if (condition.has("aggregations")) {
				aggregations = condition.get("aggregations");
			}

			if (condition.has("sort")) {
				sort = mapper.readValue(condition.get("sort").toString(), Sort.class);
			}

			if (condition.has("pagination")) {
				pagination = mapper.readValue(condition.get("pagination").toString(), Pagination.class);
			}

			if (condition.has("timeRange")) {
				timeRangeFilter = mapper.readValue(condition.get("timeRange").toString(), TimeRangeFilter.class);
			}

		} catch (IOException e) {
			throw new QuerydslException("Parse query dsl error.", e);
		}
	}

	private QueryBuilder buildFilters(Ops ops, JsonNode filter) throws QuerydslException {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		if (filter instanceof ArrayNode) {
			ArrayNode filters = (ArrayNode) filter;
			Iterator<JsonNode> ite = filters.iterator();
			while (ite.hasNext()) {
				JsonNode tmp = ite.next();
				try {
					Filter f = mapper.readValue(mapper.writeValueAsString(tmp), Filter.class);
					if (ops == Ops.and) {
						boolQueryBuilder.must(filterToBuilder(f));
					} else if (ops == Ops.or) {
						boolQueryBuilder.should(filterToBuilder(f));
					}
				} catch (Exception e) {
					Entry<String, JsonNode> entry = tmp.fields().next();
					if (ops == Ops.and) {
						boolQueryBuilder.must(buildFilters(Ops.valueOf(entry.getKey()), entry.getValue()));
					} else if (ops == Ops.or) {
						boolQueryBuilder.should(buildFilters(Ops.valueOf(entry.getKey()), entry.getValue()));
					}
				}
			}

		} else {
			try {
				Filter f = mapper.readValue(mapper.writeValueAsString(filter), Filter.class);
				if (ops != null) {
					if (ops == Ops.and) {
						boolQueryBuilder.must().add(filterToBuilder(f));
					} else if (ops == Ops.or) {
						boolQueryBuilder.should().add(filterToBuilder(f));
					}
				} else {
					boolQueryBuilder.must(filterToBuilder(f));
				}
			} catch (Exception e) {
				Iterator<Entry<String, JsonNode>> ite = filter.fields();
				while (ite.hasNext()) {
					Entry<String, JsonNode> entry = ite.next();
					if (ops == Ops.and) {
						boolQueryBuilder.must(buildFilters(Ops.valueOf(entry.getKey()), entry.getValue()));
					} else if (ops == Ops.or) {
						boolQueryBuilder.should(buildFilters(Ops.valueOf(entry.getKey()), entry.getValue()));
					} else {
						return buildFilters(Ops.valueOf(entry.getKey()), entry.getValue());
					}
				}
			}
		}

		return boolQueryBuilder;

	}

	public QueryBuilder timeRangeQuery() {
		if (timeRangeFilter != null) {
			return QueryBuilders.boolQuery()
					.must(QueryBuilders.rangeQuery(timeRangeFilter.getField()).gte(timeRangeFilter.getStartMs()).lte(timeRangeFilter.getEndMs()));
		}
		return null;
	}

	public AggregationBuilder buildAggregation(AggregationBuilder parent, JsonNode aggregationDef) {

		String aggName = aggregationDef.has("name") ? aggregationDef.get("name").asText() : UUID.randomUUID().toString();

		AggregationBuilder tmpBuilder = null;
		AggregationType aggType = AggregationType.valueOf(aggregationDef.get("type").asText());
		switch (aggType) {
		case Range:
			String field = aggregationDef.get("field").asText();
			RangeAggregationBuilder rangeBuilder = AggregationBuilders.range(aggName).field(field);
			ArrayNode ranges = (ArrayNode) aggregationDef.get("ranges");
			if (ranges != null) {
				ranges.forEach(range -> {
					if (range.has("from") && range.has("to")) {
						rangeBuilder.addRange(range.get("from").asDouble(), range.get("to").doubleValue());
					} else if (range.has("from") && !range.has("to")) {
						rangeBuilder.addUnboundedFrom(range.get("from").asDouble());
					} else if (!range.has("from") && range.has("to")) {
						rangeBuilder.addUnboundedFrom(range.get("to").asDouble());
					}
				});
			}
			tmpBuilder = rangeBuilder;
			break;
		case Distinct:
			field = aggregationDef.get("field").asText();
			tmpBuilder = AggregationBuilders.cardinality(aggName).field(field);
			break;
		case Stats:
			field = aggregationDef.get("field").asText();
			tmpBuilder = AggregationBuilders.stats(aggName).field(field);
			break;
		case Term:
			field = aggregationDef.get("field").asText();
			TermsAggregationBuilder addBuilder = AggregationBuilders.terms(aggName).field(field);
			tmpBuilder = addBuilder;
			break;
		case Nested:
			String path = aggregationDef.get("path").asText();
			field = aggregationDef.get("field").asText();
			tmpBuilder = AggregationBuilders.nested(aggName, path);
			break;
		case DateHistogram:
			field = aggregationDef.get("field") != null ? aggregationDef.get("field").asText() : timeRangeFilter.getField();
			DateHistogramAggregationBuilder aggBuilder = AggregationBuilders
					.dateHistogram(aggName).field(field).minDocCount(0);
			tmpBuilder = aggBuilder;
			String interval = aggregationDef.get("interval").asText();
			String unit = interval.substring(interval.length() - 1);
			int val = Integer.valueOf(interval.substring(0, interval.length() - 1));
			switch (unit) {
			case "y":
				aggBuilder.dateHistogramInterval(DateHistogramInterval.YEAR);
				break;
			case "M":
				aggBuilder.dateHistogramInterval(DateHistogramInterval.MONTH);
				break;
			case "w":
				aggBuilder.dateHistogramInterval(DateHistogramInterval.weeks(val));
				break;
			case "d":
				aggBuilder.dateHistogramInterval(DateHistogramInterval.days(val));
				break;
			case "h":
				aggBuilder.dateHistogramInterval(DateHistogramInterval.hours(val));
				break;
			case "m":
				aggBuilder.dateHistogramInterval(DateHistogramInterval.minutes(val));
				break;
			case "s":
				aggBuilder.dateHistogramInterval(DateHistogramInterval.seconds(val));
				break;
			default:
				break;
			}
			break;

		case IPRange:
			field = aggregationDef.get("field").toString();
			IpRangeAggregationBuilder ipRangeBuilder = AggregationBuilders.ipRange(aggName).field(field);
			tmpBuilder = ipRangeBuilder;
			ArrayNode masks = (ArrayNode) aggregationDef.get("masks");
			if (masks != null) {
				masks.forEach(mask -> {
					ipRangeBuilder.addMaskRange(mask.asText());
				});
			}
			ranges = (ArrayNode) aggregationDef.get("ranges");
			if (ranges != null) {
				ranges.forEach(range -> {
					String from = range.get("from") != null ? range.get("from").asText() : null;
					String to = range.get("to") != null ? range.get("to").asText() : null;
					if (from != null && to != null) {
						ipRangeBuilder.addRange(from, to);
					} else if (from != null && to == null) {
						ipRangeBuilder.addUnboundedFrom(from);
					} else if (from == null && to != null) {
						ipRangeBuilder.addUnboundedTo(to);
					}
				});
			}
			break;
		default:
			break;
		}

		if (aggregationDef.has("subAggs")) {
			JsonNode subAggs = aggregationDef.get("subAggs");
			if (subAggs instanceof ArrayNode) {
				for (JsonNode subAgg : (ArrayNode) subAggs) {
					buildAggregation(tmpBuilder, subAgg);
				}
			} else {
				buildAggregation(tmpBuilder, subAggs);
			}

		}

		if (parent != null) {
			parent.subAggregation(tmpBuilder);
			return parent;
		}
		return tmpBuilder;

	}

	public List<AggregationBuilder> buildAggregation() throws QuerydslException {
		List<AggregationBuilder> builders = new ArrayList<>();
		if (aggregations != null) {
			if (aggregations instanceof ArrayNode) {
				for (JsonNode agg : (ArrayNode) aggregations) {
					builders.add(buildAggregation(null, agg));
				}
			} else {
				builders.add(buildAggregation(null, aggregations));
			}
		}
		return builders;
	}

	public QueryBuilder buildFilters() throws QuerydslException {
		if (timeRangeFilter != null) {
			return QueryBuilders.boolQuery().must(buildFilters(null, filter)).must(timeRangeQuery());
		}
		if (filter != null) {
			return buildFilters(null, filter);
		}
		return null;
	}

}
