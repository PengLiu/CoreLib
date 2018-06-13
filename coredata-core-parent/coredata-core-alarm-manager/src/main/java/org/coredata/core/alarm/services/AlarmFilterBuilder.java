package org.coredata.core.alarm.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.lucene.search.join.ScoreMode;
import org.coredata.core.alarm.documents.Alarm;
import org.coredata.core.util.elasticsearch.querydsl.ESFilterBuilder;
import org.coredata.core.util.querydsl.Direction;
import org.coredata.core.util.querydsl.Filter;
import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

public class AlarmFilterBuilder extends ESFilterBuilder {

	public AlarmFilterBuilder(String querydsl) throws QuerydslException {
		super.prepare(querydsl);
	}

	@Override
	public void orderBy(NativeSearchQueryBuilder queryBuilder) throws QuerydslException {

		if (sort != null) {
			for (String field : sort.getFields()) {
				FieldSortBuilder fsb = SortBuilders.fieldSort(field).order(sort.getDirection() == Direction.ASC ? SortOrder.ASC : SortOrder.DESC);
				if (field.startsWith("props.")) {
					fsb.setNestedPath("props");
				} else if (!Alarm.isAttribute(field)) {
					fsb = SortBuilders.fieldSort("alarmSources." + field).order(sort.getDirection() == Direction.ASC ? SortOrder.ASC : SortOrder.DESC);
					fsb.setNestedPath("alarmSources");
				}
				queryBuilder.withSort(fsb);
			}
		}
	}

	@Override
	protected QueryBuilder filterToBuilder(Filter filter) throws QuerydslException {

		QueryBuilder builder = null;

		String field = filter.getField();

		if (filter.getField().startsWith("props.")) {

		} else if (Alarm.isAttribute(filter.getField())) {

		} else {
			field = "alarmSources." + field;
		}

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

		if (filter.getField().startsWith("props.")) {
			return QueryBuilders.nestedQuery("props", builder, ScoreMode.None);
		} else if (Alarm.isAttribute(filter.getField())) {
			return builder;
		} else {
			return QueryBuilders.nestedQuery("alarmSources", builder, ScoreMode.None);
		}

	}

	@Override
	protected List<AggregationBuilder> createAggregation(String[] subAggs) {
		List<AggregationBuilder> aggs = new ArrayList<>();
		for (String prop : subAggs) {
			String aggName = UUID.randomUUID().toString();
			if (prop.startsWith("props.")) {
				aggs.add(AggregationBuilders.nested(nestedAggName, "props").subAggregation(AggregationBuilders.terms(aggName).field(prop)));
			} else if (!Alarm.isAttribute(prop)) {
				prop = "alarmSources." + prop;
				aggs.add(AggregationBuilders.nested(nestedAggName, "alarmSources").subAggregation(AggregationBuilders.terms(aggName).field(prop)));
			} else {
				aggs.add(AggregationBuilders.terms(aggName).field(prop));
			}
		}
		return aggs;
	}

	@Override
	protected void createAggregation(AggregationBuilder aggBuilder, String[] subAggs) {
		AggregationBuilder tmp = null;
		String prop = subAggs[0];
		String aggName = UUID.randomUUID().toString();
		if (prop.startsWith("props.")) {
			tmp = AggregationBuilders.nested(nestedAggName, "props").subAggregation(AggregationBuilders.terms(aggName).field(prop));
		} else if (!Alarm.isAttribute(prop)) {
			prop = "alarmSources." + prop;
			tmp = AggregationBuilders.nested(nestedAggName, "alarmSources").subAggregation(AggregationBuilders.terms(aggName).field(prop));
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

	@Override
	protected AggregationBuilder createAggregation(String field) {
		if (field.startsWith("props.")) {
			return AggregationBuilders.nested(nestedAggName, "props").subAggregation(AggregationBuilders.terms(aggName).field(field));
		} else if (!Alarm.isAttribute(field)) {
			field = "alarmSources." + field;
			return AggregationBuilders.nested(nestedAggName, "alarmSources").subAggregation(AggregationBuilders.terms(aggName).field(field));
		} else {
			return AggregationBuilders.terms(aggName).field(field);
		}

	}

}