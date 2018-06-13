package org.coredata.core.stream.transform.filters;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.stream.vo.TransformData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ConditionFieldConvertFilter extends AbsFilter {

	private static final long serialVersionUID = -7866462129602704409L;

	private String ruleExp = "condition_field_convert\\(\\[(.*?)\\],\"(.*?)\",\"(.*?)\"\\)";

	private Pattern p = Pattern.compile(ruleExp);

	/**
	 * 保存过滤规则
	 */
	private String filterRule;

	public ConditionFieldConvertFilter(String filterRule) {
		this.filterRule = filterRule;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void doFilter(TransformData response, FilterChain chain) {
		JsonNode result = response.getResultJson();
		Matcher m = p.matcher(filterRule);
		if (m.find() && m.groupCount() == 3) {
			try {
				String condtion = m.group(1);
				JsonNode transforTo = mapper.readTree("{" + condtion + "}");
				String key = m.group(2);
				String newKey = m.group(3);
				JsonNode value = result.get(key);
				if (value != null) {
					String[] tos = null;
					String to = null;
					if (value instanceof ArrayNode) {
						ArrayNode arr = (ArrayNode) value;
						tos = new String[arr.size()];
						for (int i = 0; i < tos.length; i++) {
							String rkey = arr.get(i).asText();
							JsonNode nvalue = transforTo.get(rkey);
							if (nvalue != null)
								tos[i] = nvalue.asText();
							else
								tos[i] = rkey;
						}
					} else {
						to = "";
						String rkey = value.asText();
						JsonNode nvalue = transforTo.get(rkey);
						if (nvalue != null)
							to = nvalue.asText();
						else
							to = rkey;
					}
					if (StringUtils.isEmpty(newKey)) {
						((ObjectNode) result).remove(key);
						if (tos != null)
							((ObjectNode) result).set(key, mapper.readTree(mapper.writeValueAsString(tos)));
						else
							((ObjectNode) result).put(key, to);
					} else {
						if (tos != null)
							((ObjectNode) result).put(newKey, mapper.readTree(mapper.writeValueAsString(tos)));
						else
							((ObjectNode) result).put(newKey, to);
					}
				}
			} catch (IOException e) {
				logError(e, response);
			}
		}
		chain.doFilter(response);
	}

}
