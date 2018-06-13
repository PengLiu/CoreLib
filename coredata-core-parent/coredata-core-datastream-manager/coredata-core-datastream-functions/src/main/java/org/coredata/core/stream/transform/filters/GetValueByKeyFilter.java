package org.coredata.core.stream.transform.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.stream.util.TransformUtil;
import org.coredata.core.stream.vo.TransformData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class GetValueByKeyFilter extends AbsFilter {

	private static final long serialVersionUID = 3728628826937268721L;

	private static String ruleExp = "get_value_by_key\\(\"(.*?)\"\\)";

	private static Pattern p = Pattern.compile(ruleExp);

	private static String propertyExp = "\\$\\{(.*?)\\}";

	private static Pattern pp = Pattern.compile(propertyExp);

	//数组元素的index
	private static String indexExp = ".*\\[(.*?)\\]";

	private static Pattern ip = Pattern.compile(indexExp);

	private String filterRule;

	public GetValueByKeyFilter() {

	}

	public GetValueByKeyFilter(String filterRule) {
		this.filterRule = filterRule;
	}

	@Override
	public void doFilter(TransformData response, FilterChain chain) {
		JsonNode json = response.getResultJson();
		Matcher m = p.matcher(filterRule);
		if (m.find()) {
			String propertyStr = m.group(1);
			;
			List<String> propertyList = new ArrayList<String>();
			Matcher propertyMatcher = pp.matcher(propertyStr);
			while (propertyMatcher.find()) {
				propertyList.add(propertyMatcher.group(1));
			}
			int keynum = propertyList.size();
			JsonNode val = TransformUtil.getCurrentJsonNode(propertyList, keynum, json);
			Matcher indexMatcher = ip.matcher(propertyStr);
			if (indexMatcher.find()) {
				int index = 0;
				//判断是否为资源索引
				if ("index".equals(indexMatcher.group(1))) {
					index = Integer.parseInt(response.getIndex()) - 1;
				} else {
					index = Integer.parseInt(indexMatcher.group(1));
				}
				if (val instanceof ArrayNode) {
					ArrayNode array = (ArrayNode) val;
					val = array.get(index);
				}
			}
			response.setResultJson(val);
		}
		chain.doFilter(response);
	}

	public String getFilterRule() {
		return filterRule;
	}

	public void setFilterRule(String filterRule) {
		this.filterRule = filterRule;
	}
}
