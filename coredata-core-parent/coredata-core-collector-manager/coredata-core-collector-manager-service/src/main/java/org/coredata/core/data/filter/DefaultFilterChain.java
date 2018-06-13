package org.coredata.core.data.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.coredata.core.data.Record;
import org.springframework.util.CollectionUtils;

public class DefaultFilterChain implements IFilterChain, Serializable {

	private static final long serialVersionUID = 2407738362469168626L;

	private List<IFilter> filters = new ArrayList<>();

	private int columnIndex;

	@Override
	public IFilterChain registFilter(IFilter filter) {
		filters.add(filter);
		return this;
	}

	@Override
	public void doFilter(Record record) {
		if (CollectionUtils.isEmpty(filters)) {
			return;
		}
		for (IFilter filter : filters) {
			filter.doFilter(columnIndex, record);
		}
	}

	@Override
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

}