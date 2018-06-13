package org.coredata.core.stream.transform.filters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.stream.vo.TransformData;
import org.coredata.core.util.common.UnitUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UnitConvertMultipleFilter extends AbsFilter {

	private static final long serialVersionUID = -7751057028684396859L;

	private static String ruleExp = "unit_convert_multiple\\(\"(.*?)\",\"(.*?)\"\\)";

	private static Pattern p = Pattern.compile(ruleExp);

	private static String keyExp = "\\$\\{(.*?)\\}";

	private static Pattern kp = Pattern.compile(keyExp);

	private static Pattern numPattern = Pattern.compile("^([0-9]*\\.?[0-9]*)");

	private static Pattern strPattern = Pattern.compile("[a-zA-Z]*$");

	private String filterRule;

	public UnitConvertMultipleFilter() {

	}

	public UnitConvertMultipleFilter(String filterRule) {
		this.filterRule = filterRule;
	}

	@Override
	public void doFilter(TransformData response, FilterChain chain) {
		JsonNode json = response.getResultJson();
		Matcher m = p.matcher(filterRule);
		ObjectMapper map = new ObjectMapper();
		if (m.find() && m.groupCount() == 2) {
			String keyStr = m.group(1);
			String unit = m.group(2);
			List<String> keyList = new ArrayList<String>();
			Matcher tpm = kp.matcher(keyStr);
			while (tpm.find()) {
				keyList.add(tpm.group(1));
			}
			JsonNode result = null;
			JsonNode parent = null;
			for (int i = 0; i < keyList.size(); i++) {
				if (result == null) {
					result = json.get(keyList.get(i));
					parent = json.get(keyList.get(i));
				} else {
					result = result.get(keyList.get(i));
					if (i != keyList.size() - 1) {
						parent = parent.get(keyList.get(i));
					}
				}

			}
			List<String> baseValue = new ArrayList<String>();
			if (result instanceof ArrayNode) {
				Iterator<JsonNode> baseValues = result.elements();
				while (baseValues.hasNext()) {
					JsonNode j = baseValues.next();
					baseValue.add(j.asText());
				}

			} else {
				baseValue.add(result.asText());
			}

			List<Double> resultValue = new ArrayList<Double>();
			for (int i = 0; i < baseValue.size(); i++) {
				double val = 0.0D;
				if (StringUtils.isEmpty(unit)) {
					Matcher numMatcher = numPattern.matcher(baseValue.get(i));
					Matcher strMatcher = strPattern.matcher(baseValue.get(i));
					numMatcher.find();
					strMatcher.find();
					val = Double.parseDouble(numMatcher.group());
					String unitStr = strMatcher.group();
					val = UnitUtils.convertToMin(val, unitStr);
				} else {
					val = Double.valueOf(baseValue.get(i));
					val = UnitUtils.convertToMin(val, unit);
				}
				resultValue.add(val);
			}

			if (result instanceof ArrayNode) {
				for (int i = 0; i < resultValue.size(); i++) {
					try {
						((ArrayNode) result).set(i, map.readTree(String.valueOf(resultValue.get(i))));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				if (keyList.size() >= 2) {
					((ObjectNode) parent).put(keyList.get(keyList.size() - 1), resultValue.get(0));

				} else {
					((ObjectNode) json).put(keyList.get(keyList.size() - 1), resultValue.get(0));
				}
			}
		}
		chain.doFilter(response);
	}

}