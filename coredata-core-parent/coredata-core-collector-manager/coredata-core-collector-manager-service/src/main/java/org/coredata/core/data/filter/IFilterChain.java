package org.coredata.core.data.filter;

import org.coredata.core.data.Record;

public interface IFilterChain {

	public IFilterChain registFilter(IFilter filter);

	public void doFilter(Record record);

	public void setColumnIndex(int columnIndex);
}
