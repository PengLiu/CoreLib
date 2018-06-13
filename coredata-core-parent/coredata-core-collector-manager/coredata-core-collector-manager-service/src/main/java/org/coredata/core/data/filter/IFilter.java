package org.coredata.core.data.filter;

import org.coredata.core.data.Record;

public interface IFilter {

	public abstract void doFilter(int columnIndex, Record record);

	public abstract IFilter init(String config);

}