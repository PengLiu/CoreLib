package org.coredata.core.stream.transform.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.stream.vo.TransformData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CounterFilter extends AbsFilter {

	private static final long serialVersionUID = -5582452603579779436L;

	private static final Logger logger = LoggerFactory.getLogger(CounterFilter.class);

	private String ruleExp = "counter_assignment\\(\"(.*?)\",\"(.*?)\",\"(.*?)\"\\)";
	private Pattern p = Pattern.compile(ruleExp);

	private static String paramExp = "\\$\\{(.*?\\}?)\\}";
	private static Pattern pa = Pattern.compile(paramExp);

	private String filterRule;

	public CounterFilter(String filterRule) {
		this.filterRule = filterRule;
	}

	@Override
	public void doFilter(TransformData response, FilterChain chain) {
		JsonNode resultJson = response.getResultJson();//获取采集后的结果值
		String params = response.getParams();//获取对应变量参数例如：index
		Matcher m = p.matcher(filterRule);
		if (m.find() && m.groupCount() == 3) {
			String key = m.group(1);
			String maxValue = m.group(2);
			String multiplier = m.group(3);
			JsonNode paresult = null;
			try {
				if (params != null)
					paresult = mapper.readTree(params);
			} catch (Exception e) {
				logger.error("Read params error.", e);
			}
			// 此处判定key值中，是否含有变量
			Matcher paramMatch = pa.matcher(key);
			while (paramMatch.find()) {// 如果包含，则先替换相关变量
				String pk = paramMatch.group(1);
				JsonNode replaceJson = null;
				// 变量中有从params中取值的情况如含有${index}等
				Matcher pkParamMatch = pa.matcher(pk);
				String temppk = pk;
				if (pkParamMatch.find()) {
					String paramKey = pkParamMatch.group(1);
					String keyReplaced = paresult.get(paramKey.toLowerCase()).asText();
					temppk = pk.replaceAll("\\$\\{(?i)" + paramKey + "\\}", keyReplaced);
				}
				replaceJson = resultJson.get(temppk);
				if (replaceJson instanceof ArrayNode) {
					ArrayNode arrays = (ArrayNode) replaceJson;
					ArrayNode results = mapper.createArrayNode();
					for (JsonNode a : arrays) {
						long source = a.asLong();
						if (source >= 0) {
							results.add(source);
							continue;
						}
						long result = Long.parseLong(maxValue) * Integer.parseInt(multiplier) - Math.abs(source);
						results.add(result);
					}
					((ObjectNode) resultJson).set(temppk, results);
				} else {
					long source = replaceJson.asLong();
					if (source < 0) {
						long result = Long.parseLong(maxValue) * Integer.parseInt(multiplier) - Math.abs(source);
						((ObjectNode) resultJson).put(temppk, result);
					}
				}
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
