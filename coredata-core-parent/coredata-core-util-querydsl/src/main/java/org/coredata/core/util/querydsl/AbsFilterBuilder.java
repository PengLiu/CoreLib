package org.coredata.core.util.querydsl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public abstract class AbsFilterBuilder {

	private ObjectMapper mapper = new ObjectMapper();

	protected Sort sort;

	protected Pagination pagination;

	protected JsonNode filter;

	protected abstract String filterToString(Filter filter) throws QuerydslException;

	protected abstract String orderBy() throws QuerydslException;

	protected abstract String pagination() throws QuerydslException;

	protected abstract String nativeOps(Ops ops) throws QuerydslException;

	public void prepare(String query) throws QuerydslException {

		try {

			if (StringUtils.isEmpty(query)) {
				return;
			}

			JsonNode condition = mapper.readTree(query);

			if (!condition.has("filter")) {
				throw new QuerydslException("Filter not found in query dsl.");
			}

			filter = condition.get("filter");

			if (condition.has("sort")) {
				sort = mapper.readValue(condition.get("sort").toString(), Sort.class);
			}

			if (condition.has("pagination")) {
				pagination = mapper.readValue(condition.get("pagination").toString(), Pagination.class);
			}
		} catch (IOException e) {
			throw new QuerydslException("Parse query dsl error.", e);
		}
	}

	public String buildOps(Ops ops) throws QuerydslException {
		switch (ops) {
		case lt:
		case lte:
		case gt:
		case gte:
		case neq:
		case eq:
			return ops.toString();
		case like:
		case prefix:
		case contains:
		case notcontains:
		case isnull:
		case notnull:
			return nativeOps(ops);
		case in:
			return ops.toString();
		}
		throw new QuerydslException("Unsupported operator " + ops.toString() + ".");
	}

	private String buildFilters(Ops ops, JsonNode filter) throws QuerydslException {

		StringBuilder builder = new StringBuilder();

		if (filter instanceof ArrayNode) {
			ArrayNode filters = (ArrayNode) filter;
			Iterator<JsonNode> ite = filters.iterator();
			int index = 0;
			builder.append("(");
			while (ite.hasNext()) {
				index++;
				JsonNode tmp = ite.next();
				try {
					Filter f = mapper.readValue(mapper.writeValueAsString(tmp), Filter.class);
					builder.append(filterToString(f)).append(" ");
					if (ops != null && index < filters.size()) {
						builder.append(ops.name()).append(" ");
					}

				} catch (Exception e) {
					Entry<String, JsonNode> entry = tmp.fields().next();
					builder.append(buildFilters(Ops.valueOf(entry.getKey()), entry.getValue()));
					if (ops != null && index < filters.size()) {
						builder.append(" ").append(ops.name()).append(" ");
					}
				}
			}
			builder.append(")");
		} else {
			try {
				Filter f = mapper.readValue(mapper.writeValueAsString(filter), Filter.class);
				if (ops != null) {
					builder.append(" ").append(ops.toString()).append(" ");
				}
				builder.append(filterToString(f));
			} catch (Exception e) {
				Iterator<Entry<String, JsonNode>> ite = filter.fields();
				while (ite.hasNext()) {
					Entry<String, JsonNode> entry = ite.next();
					builder.append(buildFilters(Ops.valueOf(entry.getKey()), entry.getValue()));
					if (ops != null && ite.hasNext()) {
						builder.append(" ").append(ops.name()).append(" ");
					}
				}
			}
		}

		return builder.toString();

	}

	public String buildFilters() throws QuerydslException {
		return buildFilters(null, filter);
	}

}
