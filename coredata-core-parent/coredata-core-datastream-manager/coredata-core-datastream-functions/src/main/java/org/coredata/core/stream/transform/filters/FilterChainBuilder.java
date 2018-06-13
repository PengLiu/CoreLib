package org.coredata.core.stream.transform.filters;

public class FilterChainBuilder {

	public static FilterChain buildChain(FilterChain chain, String... filters) {

		for (String filter : filters) {
			if (filter.startsWith("transfor_field_col")) {
				chain.registFilter(new TransforColFilter(filter));
			} else if (filter.startsWith("add_field")) {
				chain.registFilter(new AddFieldFilter(filter));
			} else if (filter.startsWith("modify_key")) {
				chain.registFilter(new KeyFilter(filter));
			} else if (filter.startsWith("unit_convert_multiple")) {
				chain.registFilter(new UnitConvertMultipleFilter(filter));
			} else if (filter.startsWith("time_convert")) {
				chain.registFilter(new TimeConvertFilter(filter));
			} else if (filter.startsWith("keepmatchvalue_field")) {
				chain.registFilter(new KeepMatchValueFilter(filter));
			} else if (filter.startsWith("get_value_by_key")) {
				chain.registFilter(new GetValueByKeyFilter(filter));
			} else if (filter.startsWith("duration_convert")) {
				chain.registFilter(new DurationFilter(filter));
			} else if (filter.startsWith("ip_convert")) {
				chain.registFilter(new IPConvertFilter(filter));
			} else if (filter.startsWith("condition_field_convert")) {
				chain.registFilter(new ConditionFieldConvertFilter(filter));
			} else if (filter.startsWith("counter_assignment")) {
				chain.registFilter(new CounterFilter(filter));
			}
		}
		return chain;

	}

}
