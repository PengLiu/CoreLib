package org.coredata.core.stream.transform.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.stream.util.IPSeeker;
import org.coredata.core.stream.util.IPSeeker.IPLocation;
import org.coredata.core.stream.vo.TransformData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * ip转换过滤器
 * @author sushi
 *
 */
public class IPConvertFilter extends AbsFilter {

	private static final long serialVersionUID = -8106884219927314779L;

	private String ruleExp = "ip_convert\\(\"(.*?)\",\"(.*?)\"\\)";

	private Pattern p = Pattern.compile(ruleExp);

	/**
	 * 保存过滤规则
	 */
	private String filterRule;

	public IPConvertFilter(String filterRule) {
		this.filterRule = filterRule;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void doFilter(TransformData response, FilterChain chain) {
		JsonNode json = response.getResultJson();
		Matcher m = p.matcher(filterRule);
		if (m.find() && m.groupCount() == 2) {
			String key = m.group(1);
			String newKey = m.group(2);
			try {
				String[] citys = null;
				String city = null;
				JsonNode result = json.get(key);
				if (result instanceof ArrayNode) {//如果是数组的情况，将全部转城市，也是个数组
					ArrayNode results = (ArrayNode) result;
					citys = new String[results.size()];
					for (int i = 0; i < citys.length; i++) {
						String ipAddress = results.get(i).asText();
						String cityValue = "";
						IPLocation location = IPSeeker.getInstance().getLocation(ipAddress);
						if (location != null && !StringUtils.isEmpty(location.getCity()))
							cityValue = location.getCity();
						citys[i] = cityValue;
					}
				} else {
					city = "";
					String ipAddress = result.asText();
					IPLocation location = IPSeeker.getInstance().getLocation(ipAddress);
					if (location != null && !StringUtils.isEmpty(location.getCity()))
						city = location.getCity();
				}

				if (StringUtils.isEmpty(newKey)) {
					((ObjectNode) json).remove(key);
					if (citys != null)
						((ObjectNode) json).set(key, mapper.readTree(mapper.writeValueAsString(citys)));
					else
						((ObjectNode) json).put(key, city);
				} else {
					if (citys != null)
						((ObjectNode) json).put(newKey, mapper.readTree(mapper.writeValueAsString(citys)));
					else
						((ObjectNode) json).put(newKey, city);
				}
			} catch (Exception e) {
				logError(e, response);
			}
		}
		chain.doFilter(response);
	}

}
