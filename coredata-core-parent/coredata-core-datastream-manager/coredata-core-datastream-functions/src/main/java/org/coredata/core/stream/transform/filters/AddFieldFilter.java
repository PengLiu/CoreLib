package org.coredata.core.stream.transform.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.stream.vo.TransformData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.aviator.AviatorEvaluator;

/**
 * 该过滤器用于为原始数据添加字段
 *
 * @author sushiping
 *
 */
public class AddFieldFilter extends AbsFilter {

	private static final long serialVersionUID = -3628404802189590851L;
	private static String ruleExp = "add_field\\(\"(.*?)\",\"(.*?)\"\\)";
	private static Pattern p = Pattern.compile(ruleExp);

	private static String paramExp = "\\$\\{(.*?\\}?)\\}";
	private static Pattern pa = Pattern.compile(paramExp);

	private static String expreg = "^[A-Za-z0-9](.*?)((\\+)|-|(\\*)|%|(.*?\\())";
	private static Pattern exp = Pattern.compile(expreg, Pattern.CASE_INSENSITIVE);

	private static String indexExp = "\\[(.*?)\\]";
	private static Pattern inp = Pattern.compile(indexExp);
	/**
	 * 保存过滤规则
	 */
	private String filterRule;

	public AddFieldFilter() {

	}

	public AddFieldFilter(String filterRule) {
		this.filterRule = filterRule;
	}

	@Override
	public void doFilter(TransformData response, FilterChain chain) {
		JsonNode json = response.getResultJson();
		String params = response.getParams();
		Matcher m = p.matcher(filterRule);
		if (m.find() && m.groupCount() == 2) {
			String key = m.group(1);
			String value = m.group(2);
			JsonNode readTree = null;
			try {
				if (params != null)
					readTree = mapper.readTree(params);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 此处判定Value值中，是否含有变量
			Matcher paramMatch = pa.matcher(value);
			while (paramMatch.find()) {// 如果包含，则先替换相关变量
				String pk = paramMatch.group(1);
				String replaced = "";
				JsonNode replaceJson = null;
				Matcher in = inp.matcher(pk);
				int index = 0;
				if (in.find()) {
					index = Integer.parseInt(in.group(1));
					replaceJson = json.get(pk.replace(in.group(), ""));
					if (replaceJson instanceof ArrayNode) {
						replaced = ((ArrayNode) replaceJson).get(index).asText();
					}
				} else {
					// 变量中有从params中取值的情况如含有${index}等
					Matcher pkParamMatch = pa.matcher(pk);
					String temppk = pk;
					if (pkParamMatch.find()) {
						String paramKey = pkParamMatch.group(1);
						String keyReplaced = readTree.get(paramKey.toLowerCase()).asText();
						temppk = pk.replaceAll("\\$\\{(?i)" + paramKey + "\\}", keyReplaced);
					}
					replaceJson = json.get(temppk);
					if (replaceJson instanceof ArrayNode) {
						replaced = replaceJson.toString();
					} else {
						replaced = replaceJson.asText();
					}
				}
				pk = pk.replace("[", "\\[").replace("]", "\\]").replace("$", "\\$").replace("{", "\\{").replace("}", "\\}");
				value = value.replaceAll("\\$\\{(?i)" + pk + "\\}", replaced);
			}
			// 值为表达式运算的情况
			Matcher expm = exp.matcher(value);
			if (expm.find()) {
				Object exevalue = AviatorEvaluator.execute(value);
				try {
					JsonNode res = mapper.readTree(exevalue.toString());
					((ObjectNode) json).set(key, res);
				} catch (IOException e) {
					((ObjectNode) json).put(key, exevalue.toString());
				}
			} else {
				try {
					// 添加字段时，如果存在多列情况，则添加多列值
					String msg = response.getMsg();
					JsonNode msgnode = mapper.readTree(msg);
					Iterator<Entry<String, JsonNode>> fields = msgnode.fields();
					ArrayNode list = mapper.createArrayNode();
					int size = 0;
					while (fields.hasNext()) {
						size = fields.next().getValue().size();
						for (int i = 0; i < size; i++) {
							list.add(value);
						}
						break;
					}
					;
					// 去header后是數組的情況
					if (json instanceof ArrayNode) {
						Map<String, String> map = new HashMap<String, String>();
						map.put(key, value);

						((ArrayNode) json).add(mapper.readTree(mapper.writeValueAsString(map)));

					} else {
						if (StringUtils.isEmpty(value)) {
							// 默认""的情况
							if (size > 0)
								((ObjectNode) json).set(key, list);
							else
								((ObjectNode) json).put(key, value);
						} else {
							// jsonString
							try {
								((ObjectNode) json).set(key, mapper.readTree(value));
							} catch (Exception e) {
								if (size > 0)
									((ObjectNode) json).set(key, list);
								else
									((ObjectNode) json).put(key, value);
							}

						}
					}
				} catch (Exception e) {
					logError(e, response);
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
