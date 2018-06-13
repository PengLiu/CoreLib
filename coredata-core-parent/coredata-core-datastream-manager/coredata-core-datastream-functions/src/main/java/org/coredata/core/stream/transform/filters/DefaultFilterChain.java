package org.coredata.core.stream.transform.filters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.coredata.core.stream.vo.TransformData;
import org.springframework.util.CollectionUtils;

public class DefaultFilterChain implements FilterChain, Serializable {

	private static final long serialVersionUID = 3775466453277225562L;

	private List<Filter> filters = new ArrayList<>();

	private int index;

	@Override
	public FilterChain registFilter(Filter filter) {
		filters.add(filter);
		return this;
	}

	@Override
	public void doFilter(TransformData response) {
		if (CollectionUtils.isEmpty(filters)) {
			return;
		} else if (index == filters.size()) {
			index = 0;
			return;
		}
		Integer i = index;
		Filter filter = filters.get(i);
		index++;
		filter.doFilter(response, this);
	}

}
