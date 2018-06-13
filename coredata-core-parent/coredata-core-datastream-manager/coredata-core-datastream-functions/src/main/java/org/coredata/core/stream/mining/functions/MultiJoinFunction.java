package org.coredata.core.stream.mining.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class MultiJoinFunction extends AbsFunction {

	public static final String leftCdtExp = "\\{LEFT}(\\.\\{(.*?)\\})+";
	public static final Pattern leftCdtPattern = Pattern.compile(leftCdtExp);

	public static final String rightCdtExp = "\\{RIGHT}(\\.\\{(.*?)\\})+";
	public static final Pattern rightCdtPattern = Pattern.compile(rightCdtExp);

	private static final String LEFT = "left";

	private static final String RIGHT = "right";

	@Override
	public String getName() {
		return "multiJoin";
	}

	@SuppressWarnings("unchecked")
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject leftSource, AviatorObject rightSource, AviatorObject condition, AviatorObject joinType) {
		Map<String, Object> joinTable = new HashMap<>();
		try {
			String leftData = FunctionUtils.getStringValue(leftSource, env);//获取第一个数据源
			String rightData = FunctionUtils.getStringValue(rightSource, env);//获取第二个数据源
			String cdt = FunctionUtils.getStringValue(condition, env);//获取对应条件表达式
			String join = FunctionUtils.getStringValue(joinType, env);//获取拼接方式
			Matcher leftMatcher = leftCdtPattern.matcher(cdt);
			Matcher rightMatcher = rightCdtPattern.matcher(cdt);
			if (leftMatcher.find() && rightMatcher.find()) {
				String leftCdt = leftMatcher.group();
				String rightCdt = rightMatcher.group();
				String leftKey = leftMatcher.group(2);
				String rightKey = rightMatcher.group(2);
				JSONObject leftS = JSON.parseObject(leftData);
				JSONObject rightS = JSON.parseObject(rightData);
				if (leftS != null && rightS != null) {
					//首先拼接表头，将两个集合中的表头拼接在一起
					Set<String> leftkeys = leftS.keySet();
					leftkeys.forEach(lk -> joinTable.put(lk, new ArrayList<>()));
					Set<String> rightkeys = rightS.keySet();
					rightkeys.forEach(rk -> joinTable.put(rk, new ArrayList<>()));
					//表头拼接后，开始合并相关结果集
					if (LEFT.equalsIgnoreCase(join)) {//如果以左表为主，则先将左表中全部数据填入结果table中
						leftkeys.forEach(lk -> {
							JSONArray datas = (JSONArray) leftS.get(lk);
							for (int i = 0; i < datas.size(); i++) {
								((List<Object>) joinTable.get(lk)).add(datas.get(i));
							}
						});
						//开始循环比较表达式
						JSONArray lcdt = leftS.getJSONArray(leftKey);
						JSONArray rcdt = rightS.getJSONArray(rightKey);
						for (int i = 0; i < lcdt.size(); i++) {
							boolean hasValue = false;
							int addIndex = 0;
							String exec = cdt.replace(leftCdt, lcdt.get(i).toString());
							for (int j = 0; j < rcdt.size(); j++) {
								String exe = exec.replace(rightCdt, rcdt.get(j).toString());
								Boolean execute = (Boolean) AviatorEvaluator.execute(exe);
								if (execute == null || !execute)
									continue;
								hasValue = true;
								addIndex = j;
								break;
							}
							for (String r : rightkeys) {
								if (leftkeys.contains(r))
									continue;
								JSONArray otherValues = rightS.getJSONArray(r);
								if (hasValue) {
									((ArrayList<Object>) joinTable.get(r)).add(i, otherValues.get(addIndex));
								} else
									((ArrayList<Object>) joinTable.get(r)).add(i, "");
							}
						}
					} else if (RIGHT.equalsIgnoreCase(join)) {
						rightkeys.forEach(rk -> {
							JSONArray datas = (JSONArray) rightS.get(rk);
							for (int i = 0; i < datas.size(); i++) {
								((List<Object>) joinTable.get(rk)).add(datas.get(i));
							}
						});
						//开始循环比较表达式
						JSONArray lcdt = leftS.getJSONArray(leftKey);
						JSONArray rcdt = rightS.getJSONArray(rightKey);
						for (int i = 0; i < rcdt.size(); i++) {
							boolean hasValue = false;
							int addIndex = 0;
							String exec = cdt.replace(rightCdt, rcdt.get(i).toString());
							for (int j = 0; j < lcdt.size(); j++) {
								String exe = exec.replace(leftCdt, lcdt.get(j).toString());
								Boolean execute = (Boolean) AviatorEvaluator.execute(exe);
								if (execute == null || !execute)
									continue;
								hasValue = true;
								addIndex = j;
								break;
							}
							for (String r : leftkeys) {
								if (rightkeys.contains(r))
									continue;
								JSONArray otherValues = leftS.getJSONArray(r);
								if (hasValue) {
									((ArrayList<Object>) joinTable.get(r)).add(i, otherValues.get(addIndex));
								} else
									((ArrayList<Object>) joinTable.get(r)).add(i, "");
							}
						}
					} else {
						//开始循环比较表达式
						JSONArray lcdt = leftS.getJSONArray(leftKey);
						JSONArray rcdt = rightS.getJSONArray(rightKey);
						for (int i = 0; i < lcdt.size(); i++) {
							boolean hasValue = false;
							int addIndex = 0;
							String exec = cdt.replace(leftCdt, lcdt.get(i).toString());
							for (int j = 0; j < rcdt.size(); j++) {
								String exe = exec.replace(rightCdt, rcdt.get(j).toString());
								Boolean execute = (Boolean) AviatorEvaluator.execute(exe);
								if (execute == null || !execute)
									continue;
								hasValue = true;
								addIndex = j;
								break;
							}
							if (!hasValue)
								continue;
							for (String lk : leftkeys) {
								JSONArray datas = (JSONArray) leftS.get(lk);
								((List<Object>) joinTable.get(lk)).add(datas.get(i));
							}
							for (String rk : rightkeys) {
								if (leftkeys.contains(rk))
									continue;
								JSONArray otherValues = rightS.getJSONArray(rk);
								((ArrayList<Object>) joinTable.get(rk)).add(otherValues.get(addIndex));
							}
						}
					}
				}
			}
			return new AviatorString(JSON.toJSONString(joinTable));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return new AviatorString(null);
	}

}
