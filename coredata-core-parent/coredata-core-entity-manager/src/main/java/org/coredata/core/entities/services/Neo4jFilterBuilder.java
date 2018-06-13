package org.coredata.core.entities.services;

import org.coredata.core.util.querydsl.AbsFilterBuilder;
import org.coredata.core.util.querydsl.Filter;
import org.coredata.core.util.querydsl.Ops;
import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class Neo4jFilterBuilder extends AbsFilterBuilder {

	public Neo4jFilterBuilder(String querydsl) throws QuerydslException {
		super.prepare(querydsl);
	}

	public Pageable pageable() {
		if (pagination != null) {
			return PageRequest.of(pagination.getPage() - 1, pagination.getSize());
		} else {
			return PageRequest.of(0, 10);
		}

	}

	@Override
	protected String orderBy() throws QuerydslException {
		StringBuilder sortBuilder = new StringBuilder();
		if (sort != null) {
			int index = 0;
			for (String field : sort.getFields()) {
				field = "e." + field;
				sortBuilder.append(" ORDER BY ").append(field);
				index++;
				if (index < sort.getFields().length) {
					sortBuilder.append(",");
				}
			}
			sortBuilder.append(" ").append(sort.getDirection().toString());
		}
		return sortBuilder.toString();
	}

	@Override
	protected String pagination() throws QuerydslException {
		StringBuilder pageBuilder = new StringBuilder();
		if (pagination != null) {
			int skip = (pagination.getPage() - 1) * pagination.getSize();
			int limit = pagination.getSize();
			pageBuilder.append(" SKIP ").append(skip).append(" LIMIT ").append(limit);
		}
		return pageBuilder.toString();
	}

	@Override
	protected String filterToString(Filter filter) throws QuerydslException {

		StringBuilder builder = new StringBuilder();

		String key = filter.getField();
		if (key.startsWith("props.") || key.startsWith("conn.")) {
			key = "e['" + key + "']";
		} else {
			key = "e." + key;
		}

		builder.append(key);

		if (filter.getValue() == null) {

			if (filter.getOps() == Ops.isnull || filter.getOps() == Ops.notnull) {
				builder.append(buildOps(filter.getOps()));
			} else {
				throw new QuerydslException("Filter value is null.");
			}

		} else {
			if (filter.getValue() instanceof String) {
				if (filter.getOps() == Ops.notcontains) {
					builder.append(" NOT ");
				}
				builder.append(buildOps(filter.getOps()));
				String value = filter.getValue().toString();
				if (filter.getOps() == Ops.like) {
					value = ".*" + value + ".*";
				} else if (filter.getOps() == Ops.prefix) {
					value = "^" + value + ".*";
				} else if (filter.getOps() == Ops.contains) {

				}
				builder.append("'").append(value).append("'");
			} else {
				if (filter.getOps() == Ops.like || filter.getOps() == Ops.prefix) {
					builder.append("=");
				} else {
					builder.append(" ").append(buildOps(filter.getOps())).append(" ");
				}
				builder.append(filter.getValue());
			}
		}

		return builder.toString();
	}

	@Override
	protected String nativeOps(Ops ops) throws QuerydslException {
		switch (ops) {
		case isnull:
			return " IS NULL ";
		case notnull:
			return " IS NOT NULL ";
		case contains:
		case notcontains:
			return "CONTAINS";
		case like:
		case prefix:
			return "=~";
		}
		throw new QuerydslException("Unsupported operator " + ops);
	}

}