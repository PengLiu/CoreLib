package org.coredata.core.stream.transform.filters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.stream.util.TransformUtil;
import org.coredata.core.stream.vo.TransformData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class KeepMatchValueFilter extends AbsFilter {

	private static final long serialVersionUID = 6104601226256228912L;

	private static String ruleExp = "keepmatchvalue_field\\(\"(.*?)\",\"(.*?)\"\\)";

	private static Pattern p = Pattern.compile(ruleExp);

	private static String propertyExp = "\\$\\{(.*?)\\}";

	private static Pattern pp = Pattern.compile(propertyExp);

	private String filterRule;

	public KeepMatchValueFilter() {

	}

	public KeepMatchValueFilter(String filterRule) {
		this.filterRule = filterRule;
	}

	@Override
	public void doFilter(TransformData response, FilterChain chain) {
		JsonNode json = response.getResultJson();
		Matcher m = p.matcher(filterRule);
		if (m.find() && m.groupCount() == 2) {
			String regx = m.group(1);
			String propertyStr = m.group(2);
			List<String> propertyList = new ArrayList<String>();
			Matcher v = pp.matcher(propertyStr);
			Pattern tp = Pattern.compile(regx);
			while (v.find()) {
				propertyList.add(v.group(1));
			}
			String value = "";
			List<String> values = new ArrayList<String>();
			boolean isArray = false;
			int keynum = propertyList.size();
			JsonNode val = TransformUtil.getCurrentJsonNode(propertyList, keynum, json);
			if (val instanceof ArrayNode) {
				ArrayNode array = (ArrayNode) val;
				isArray = true;
				Iterator<JsonNode> ite = array.iterator();
				while (ite.hasNext()) {
					Matcher tm = tp.matcher(ite.next().asText());
					if (tm.find()) {
						values.add(tm.group());
					}
				}
			} else if (val != null) {
				value = val.asText();
			}

			JsonNode parent = TransformUtil.getParentJsonNode(propertyList, keynum, json);

			try {

				if (isArray) {
					((ObjectNode) parent).set(propertyList.get(propertyList.size() - 1), mapper.readTree(values.toString()));
				} else {
					((ObjectNode) parent).set(propertyList.get(propertyList.size() - 1), mapper.readTree(value));
				}

			} catch (Exception e) {
				logError(e, response);
			}
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
