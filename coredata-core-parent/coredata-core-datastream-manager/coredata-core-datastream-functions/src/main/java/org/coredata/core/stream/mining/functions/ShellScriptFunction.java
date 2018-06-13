package org.coredata.core.stream.mining.functions;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.stream.mining.entity.MetricInfo;
import org.coredata.core.stream.vo.DSInfo;
import org.coredata.core.stream.vo.TransformData;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class ShellScriptFunction extends AbsFunction {

	private static String paramExp = "\\$\\{(.*?)\\}";

	private static Pattern paramPattern = Pattern.compile(paramExp);

	private static final String COMMON = ",";

	@Override
	public String getName() {
		return "shellScriptExp";
	}

	@SuppressWarnings("unchecked")
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		String expStr = FunctionUtils.getStringValue(exp, env);//获取对应表达式
		//然后正则匹配相关结果
		String[] processExp = processExp(expStr);
		MetricInfo metricInfo = (MetricInfo) env.get(METRIC_INFO);
		String metricId = metricInfo.getId();
		Map<String, TransformData> data = (Map<String, TransformData>) env.get(metricId);
		if (data == null)
			data = (Map<String, TransformData>) env.get(TRANSFORM_DATA);//获取对应清洗后的原始数据
		try {
			DSInfo[] dsInfos = metricInfo.getDsinfo();//获取要替换的变量
			if (dsInfos != null) {
				for (DSInfo dsInfo : dsInfos) {
					String alias = dsInfo.getAlias();
					if (data.containsKey(alias)) {//包含对应结果集
						TransformData transformData = data.get(alias);//根据alias获取对应结果集
						String name = transformData.getName();
						JsonNode node = mapper.readTree(transformData.getResult()).get(name);//根据命令的id，获取对应结果
						String result = node.asText();//获取对应结果字符串
						String processResult = processResultByTagIndex(result, processExp[1], processExp[2]);
						expStr = expStr.replace(dsInfo.getId(), processResult);
					}
				}
			}
			return new AviatorString(expStr);
		} catch (Exception e) {
			logError(e, metricInfo);
		}
		return null;
	}

	/**
	 * 该方法用于拆分方法中对应的参数
	 * @param expStr
	 * @return
	 */
	private String[] processExp(String expStr) {
		String[] results = new String[3];
		Matcher matcher = paramPattern.matcher(expStr);
		int flag = 0;
		while (matcher.find()) {
			results[flag] = matcher.group(1);
			flag++;
			if (flag == 3)
				break;
		}
		return results;
	}

	/**
	 * 该方法用于根据结果拆分匹配最终结果
	 * @param result
	 * @param tag
	 * @param index
	 * @return
	 */
	private String processResultByTagIndex(String result, String tag, String index) {
		String tagExp = "\\<" + tag + "\\>(.*?)\\<\\/" + tag + "-(\\d+)\\>";
		Pattern tagPattern = Pattern.compile(tagExp);
		Matcher matcher = tagPattern.matcher(result);
		String returnValue = "";
		if (matcher.find()) {
			String process = matcher.group(1);//根据正则表达式，拆分内部数据
			String[] results = process.split(COMMON);
			int flag = Integer.parseInt(index);
			if (flag < results.length)
				returnValue = results[flag];
		}
		return returnValue;
	}

}
