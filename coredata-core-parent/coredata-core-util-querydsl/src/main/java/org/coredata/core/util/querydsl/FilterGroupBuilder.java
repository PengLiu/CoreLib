package org.coredata.core.util.querydsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class FilterGroupBuilder {

	protected ObjectMapper mapper = new ObjectMapper();

	private LogicOps ops;

	public static FilterGroupBuilder instance() {
		return new FilterGroupBuilder();
	}

	public FilterGroup filterGroup(LogicOps ops) {
		this.ops = ops;
		FilterGroup filterGroup = new FilterGroup();
		return filterGroup;
	}
	
	public FilterGroup filterGroup() {
		FilterGroup filterGroup = new FilterGroup();
		return filterGroup;
	}
	
	public class FilterGroup {

		private ArrayNode filters = mapper.createArrayNode();

		public FilterGroup() {
		}

		public LogicOps getOps() {
			return ops;
		}

		public ArrayNode getFilters() {
			return filters;
		}

		public FilterGroup filter(QueryOps ops, String field, Object value) {
			Filter filter = new Filter();
			filter.setOps(Ops.valueOf(ops.name()));
			filter.setField(field);
			filter.setValue(value);
			filters.add(mapper.valueToTree(filter));
			return this;
		}

	}

}
