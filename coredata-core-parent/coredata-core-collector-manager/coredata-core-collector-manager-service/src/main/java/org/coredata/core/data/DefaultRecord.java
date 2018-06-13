package org.coredata.core.data;

import java.util.ArrayList;
import java.util.List;

public class DefaultRecord implements Record {

	private final List<Object> fields;

	public DefaultRecord() {
		fields = new ArrayList<>();
	}

	public DefaultRecord(int fieldCount) {
		this();
	}

	@Override
	public void add(int index, Object field) {
		fields.add(index, field);
	}

	@Override
	public void add(Object field) {
		fields.add(field);
	}

	@Override
	public Object get(int index) {
		return fields.get(index);
	}

	@Override
	public void set(int index, Object obj) {
		if (index < fields.size()) {
			fields.add(index, obj);
			fields.remove(index + 1);
		} else {
			for (int i = fields.size(); i < index; i++) {
				fields.add(null);
			}
			fields.add(index, obj);
		}
	}

	@Override
	public int size() {
		return fields.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (int i = 0, len = fields.size(); i < len; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(fields.get(i));
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public void remove(int index) {
		fields.remove(index);
	}

	public List<Object> getFields() {
		return fields;
	}
	
}
