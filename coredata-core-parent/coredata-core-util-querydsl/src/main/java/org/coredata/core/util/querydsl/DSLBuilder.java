package org.coredata.core.util.querydsl;

import org.coredata.core.util.querydsl.FilterGroupBuilder.FilterGroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DSLBuilder {

	protected ObjectMapper mapper = new ObjectMapper();

	protected String filterField = "filter";

	protected String paginationField = "pagination";

	protected String sortField = "sort";

	protected ObjectNode root = mapper.createObjectNode();

	protected ObjectNode filters = mapper.createObjectNode();

	protected LogicOps filterOps;

	public static DSLBuilder instance() {
		return new DSLBuilder();
	}

	public DSLBuilder filter(LogicOps ops) {
		filterOps = ops;
		root.set(filterField, filters);
		return this;
	}

	public DSLBuilder filter(QueryOps ops, String field, Object value) {
		filterOps = LogicOps.and;
		Filter filter = new Filter();
		filter.setOps(Ops.valueOf(ops.name()));
		filter.setField(field);
		filter.setValue(value);
		root.set(filterField, mapper.valueToTree(filter));
		return this;
	}

	public DSLBuilder filterGroup(FilterGroup group) {
		if (group.getOps() != null) {
			filters.set(group.getOps().name(), group.getFilters());
		} else {
			filters.set(filterOps.name(), group.getFilters());
		}
		return this;
	}

	public DSLBuilder pageable(int page, int pageSize) {
		Pageable pageable = new Pageable(page, pageSize);
		this.root.set(paginationField, pageable.getNode());
		return this;
	}

	public DSLBuilder sort(Direction direction, String... fields) {
		Sort sort = new Sort();
		sort.sort(direction, fields);
		this.root.set(sortField, sort.getNode());
		return this;
	}

	public String build() {
		try {
			return mapper.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			return "";
		}
	}

	protected class Pageable extends DSLBuilder {

		private ObjectNode node = mapper.createObjectNode();

		public Pageable(int page, int pageSize) {
			this.node.put("size", pageSize);
			this.node.put("page", page);
		}

		protected ObjectNode getNode() {
			return this.node;
		}

	}

	protected class Sort extends DSLBuilder {

		private ObjectNode node = mapper.createObjectNode();

		protected ObjectNode getNode() {
			return node;
		}

		public Sort sort(Direction direction, String... fields) {
			this.node.put("direction", direction.toString());
			ArrayNode fieldArray = node.putArray("fields");
			for (String field : fields) {
				fieldArray.add(field);
			}
			return this;
		}

	}

}