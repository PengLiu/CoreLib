package org.coredata.core.stream.transform.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.stream.vo.TransformData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class KeyFilter extends AbsFilter {

	private static final long serialVersionUID = -2064022284093112415L;

	private static final Logger logger = LoggerFactory.getLogger(KeyFilter.class);

	private String ruleExp = "modify_key\\(\"(.*?)\",\"(.*?)\"\\)";

	private Pattern p = Pattern.compile(ruleExp);

	private String filterRule;

	public KeyFilter(String filterRule) {
		this.filterRule = filterRule;
	}

	@Override
	public void doFilter(TransformData response, FilterChain chain) {

		JsonNode json = response.getResultJson();
		Matcher m = p.matcher(filterRule);
		if (m.find() && m.groupCount() == 2) {
			modifyKey(m.group(1), m.group(2), json);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("message not match rule");
			}
		}
		chain.doFilter(response);

	}

	private void modifyKey(String key, String newKey, JsonNode json) {
		JsonNode val = json.get(key);
		((ObjectNode) json).set(newKey, val);
		((ObjectNode) json).remove(key);
	}

}