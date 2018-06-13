package org.coredata.core.data.vo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class TableMeta {

	public TableMeta() {
	}
	private String dbName;

	private String name;

	private List<ColumnMeta> columns = new ArrayList<>();

	private int[] indexSelected;

	public boolean isNotEmpty() {
		return !isEmpty();
	}

	public boolean isEmpty() {
		return StringUtils.isEmpty(dbName) && StringUtils.isEmpty(name) && columns.isEmpty();
	}
	public void addColumn(ColumnMeta meta) {
		columns.add(meta);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ColumnMeta> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnMeta> columns) {
		this.columns = columns;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public int[] getIndexSelected() {
		return indexSelected;
	}

	public void setIndexSelected(int[] indexSelected) {
		this.indexSelected = indexSelected;
	}

}
