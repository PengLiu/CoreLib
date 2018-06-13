package org.coredata.core.stream.transform.filters;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.stream.vo.TransformData;
import org.coredata.core.util.common.DateUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DurationFilter extends AbsFilter {

	private static final long serialVersionUID = 2121885130972802877L;
	private String ruleExp = "duration_convert\\(\"(.*?)\",\"(.*?)\"\\)";
	private Pattern p = Pattern.compile(ruleExp);

	private String paramExp = "\\$\\{(.*?)\\}";
	private Pattern par = Pattern.compile(paramExp);

	/**
	 * 保存过滤规则
	 */
	private String filterRule;

	public DurationFilter(String filterRule) {
		this.filterRule = filterRule;
	}

	@Override
	public void doFilter(TransformData response, FilterChain chain) {
		JsonNode json = response.getResultJson();
		Matcher m = p.matcher(filterRule);
		if (m.find() && m.groupCount() == 2) {
			String format = m.group(2);//获取时间格式化的值
			String keyExp = m.group(1);//获取对应字段的key值
			Matcher matcher = par.matcher(keyExp);
			if (matcher.find()) {
				String key = matcher.group(1);
				JsonNode node = json.get(key);
				if (node != null) {
					String time = null;
					if (node instanceof ArrayNode) {
						ArrayNode arrays = (ArrayNode) node;
						time = arrays.get(0).asText();
					} else
						time = node.asText();
					long ms = Long.parseLong(time);//将时间转换为毫秒

					String dateconverter = DateUtil.format(new Date(ms), format);
					((ObjectNode) json).put(key, dateconverter);
				}
			}
		}
		chain.doFilter(response);
	}

}
