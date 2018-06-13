package org.coredata.core.vo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ElasticSearchResult {

	private final LinkedList<String> columns = new LinkedList<>();;
	private final List<Object> fields = new ArrayList<>();

	public LinkedList<String> getColumns() {
		return columns;
	}

	public List<Object> getFields() {
		return fields;
	}

	public void addColumn(String column) {
		columns.add(column);
	}

	public void addField(Object field) {
		fields.add(field);
	}

}
