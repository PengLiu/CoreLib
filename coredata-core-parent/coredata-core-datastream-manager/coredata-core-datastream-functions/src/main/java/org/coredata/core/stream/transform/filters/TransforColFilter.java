package org.coredata.core.stream.transform.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.stream.transform.functions.AbsFunction;
import org.coredata.core.stream.vo.TransformData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.aviator.AviatorEvaluator;

/**
 * 键值转换过滤器，支持三个参数的过滤器
 * @author sushi
 *
 */
public class TransforColFilter extends AbsFilter {

	private static final long serialVersionUID = 1140557249197713159L;
	private String ruleExp = "transfor_field_col\\(\\[(.*?)\\],\"(.*?)\",\"(.*?)\"\\)";
	private Pattern p = Pattern.compile(ruleExp);

	private String formatSourceExp = "\"((.*?)+\\((.*?)\\))\"";
	private Pattern formatSourcePattern = Pattern.compile(formatSourceExp);

	private static String paramExp = "\\$\\{(.*?)\\}";
	private static Pattern pa = Pattern.compile(paramExp);

	private String filterRule;

	public TransforColFilter(String filterRule) {
		this.filterRule = filterRule;
	}

	@Override
	public void doFilter(TransformData response, FilterChain chain) {
		JsonNode json = response.getResultJson();
		Matcher m = p.matcher(filterRule);
		try {
			if (m.find() && m.groupCount() == 3) {
				String fields = m.group(1);
				String defaultVal = m.group(2);
				String key = m.group(3);
				//此处判定key值中，是否含有变量
				Matcher paramMatch = pa.matcher(key);
				Matcher paramMatVal = pa.matcher(defaultVal);
				if (paramMatch.find()) {//如果包含，则先替换相关变量
					String pk = paramMatch.group(1);
					String params = response.getParams();
					JsonNode readTree = mapper.readTree(params);
					if (!readTree.isNull() && readTree.get(pk.toLowerCase()) != null) {
						String replaced = readTree.get(pk.toLowerCase()).asText();
						key = key.replaceAll("\\$\\{(?i)" + pk + "\\}", replaced);
					} else {
						key = key.replaceAll("\\$\\{(?i)" + pk + "\\}", pk);
					}
				}
				//支持表达式中第二个参数是变量的情况
				if (paramMatVal.find()) {//如果包含，则先替换相关变量
					String pk = paramMatVal.group(1);
					String result = null;
					if (json instanceof ArrayNode) {
						for (JsonNode jn : (ArrayNode) json) {
							result = jn.get(pk).asText();
							if (result != null)
								break;
						}
					} else {
						result = json.get(pk).asText();
					}
					defaultVal = defaultVal.replaceAll("\\$\\{(?i)" + pk + "\\}", result);
				}
				boolean isDoHeaderFilter = false;
				JsonNode result = null;
				if (json instanceof ArrayNode) {
					isDoHeaderFilter = true;
					for (JsonNode jn : (ArrayNode) json) {
						result = jn.get(key);
						if (result != null)
							break;
					}
				} else {
					result = json.get(key);
				}

				//根据key值先获取对应值
				if (result == null) {//如果没有对应键值，则放入默认值
					if (isDoHeaderFilter) {
						Map<String, String> map = new HashMap<String, String>();
						map.put(key, defaultVal);
						try {
							((ArrayNode) json).add(mapper.readTree(mapper.writeValueAsString(map)));
						} catch (Exception e) {
							logError(e, response);
						}
					} else {
						((ObjectNode) json).put(key, defaultVal);
					}
				} else {
					String tmp = null;
					if (result instanceof ArrayNode) {
						ArrayNode arrays = (ArrayNode) result;
						tmp = arrays.size() == 0 ? "" : arrays.get(0).asText();
					} else
						tmp = result.asText();
					//支持多字段值的转换，并且支持isContainFunciton函数，如transfor_field_col(["isContain('${serverinfo}','Tomcat')":"ok"],"error","${serverinfo}")
					int pointIndex = fields.indexOf(",");
					int maoIndex = fields.indexOf(":");
					if (pointIndex > 0 && maoIndex > 0 && maoIndex > pointIndex) {
						//此处新增判断，是否为表达式
						String[] split = fields.split(":");
						String condition = split[0];//获取默认值，判定是否为表达式
						Matcher matcher = formatSourcePattern.matcher(condition);
						Matcher conMatch = pa.matcher(condition);
						if (matcher.find()) {
							Map<String, Object> env = new HashMap<>();
							env.put(AbsFunction.COLLET_DATA, response);
							condition = (String) AviatorEvaluator.execute(matcher.group(1), env);
							if (condition.equals("true")) {
								fields = "\"" + tmp + "\":" + split[1];
							}
						} else if (conMatch.find()) {
							String port = conMatch.group(1);
							condition = condition.replaceAll("'\\$\\{(?i)" + port + "\\}'", tmp);
							String con = (String) AviatorEvaluator.exec(trimFirstAndLastChar(condition, "\"") + "?'" + tmp + "':'false'");
							if (con.equals(tmp)) {
								fields = "\"" + con + "\":" + split[1];
							}
						}
					} else {
						String[] elements = fields.split(",");
						for (String element : elements) {
							//此处新增判断，是否为表达式
							String[] split = element.split(":");
							String condition = split[0];//获取默认值，判定是否为表达式
							Matcher matcher = formatSourcePattern.matcher(condition);
							Matcher conMatch = pa.matcher(condition);
							if (matcher.find()) {
								Map<String, Object> env = new HashMap<>();
								env.put(AbsFunction.COLLET_DATA, response);
								condition = (String) AviatorEvaluator.execute(matcher.group(1), env);
								if (condition.equals("true")) {
									fields = "\"" + tmp + "\":" + split[1];
									break;
								}
							} else if (conMatch.find()) {
								String port = conMatch.group(1);
								condition = condition.replaceAll("'\\$\\{(?i)" + port + "\\}'", tmp);
								String con = (String) AviatorEvaluator.exec(trimFirstAndLastChar(condition, "\"") + "?'" + tmp + "':'false'");
								if (con.equals(tmp)) {
									fields = "\"" + con + "\":" + split[1];
									break;
								}
							}

						}
					}

					JsonNode transforTo = mapper.readTree("{" + fields + "}");
					//根据获取的值，获取需要转换的对象
					JsonNode to = transforTo.get(tmp.trim());
					if (to != null)
						if (isDoHeaderFilter) {
							for (JsonNode jn : (ArrayNode) json) {
								JsonNode jsnode = jn.get(key);
								if (jsnode != null) {
									((ObjectNode) jn).set(key, to);
									break;
								}
							}
						} else {
							((ObjectNode) json).set(key, to);
						}
					else if (isDoHeaderFilter) {
						Map<String, String> map = new HashMap<String, String>();
						map.put(key, defaultVal);
						try {
							((ArrayNode) json).add(mapper.readTree(mapper.writeValueAsString(map)));
						} catch (Exception e) {
							logError(e, response);
						}
					} else {
						((ObjectNode) json).put(key, defaultVal);
					}
				}
			}
		} catch (IOException e) {
			logger.error("TransforColFilter execute failed.", e);
		}
		chain.doFilter(response);
	}

	public String trimFirstAndLastChar(String source, String string) {
		boolean beginIndexFlag = true;
		boolean endIndexFlag = true;
		do {
			int beginIndex = source.indexOf(string) == 0 ? 1 : 0;
			int endIndex = source.lastIndexOf(string) + 1 == source.length() ? source.lastIndexOf(string) : source.length();
			source = source.substring(beginIndex, endIndex);
			beginIndexFlag = (source.indexOf(string) == 0);
			endIndexFlag = (source.lastIndexOf(string) + 1 == source.length());
		} while (beginIndexFlag || endIndexFlag);
		return source;
	}

}
