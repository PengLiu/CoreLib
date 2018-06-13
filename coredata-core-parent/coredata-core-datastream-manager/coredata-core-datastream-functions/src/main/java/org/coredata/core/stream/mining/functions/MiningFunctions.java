package org.coredata.core.stream.mining.functions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.coredata.core.stream.mining.entity.MetricInfo;
import org.coredata.core.stream.vo.DSInfo;
import org.coredata.core.stream.vo.TransformData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.aviator.AviatorEvaluator;

public class MiningFunctions {

	static {
		// register function
		AviatorEvaluator.addFunction(new CountFunction());
		AviatorEvaluator.addFunction(new LiteralsFunction());
		AviatorEvaluator.addFunction(new SumFunction());
		AviatorEvaluator.addFunction(new SpeedFunction());
		AviatorEvaluator.addFunction(new ConvertFunction());
		AviatorEvaluator.addFunction(new ShellScriptFunction());
		AviatorEvaluator.addFunction(new StringSplitFunction());
		AviatorEvaluator.addFunction(new AvgFunction());
		AviatorEvaluator.addFunction(new ConditionOrFunction());
		AviatorEvaluator.addFunction(new MaxFunction());
		AviatorEvaluator.addFunction(new CounterFunction());
		AviatorEvaluator.addFunction(new StrBuilderFunction());
		AviatorEvaluator.addFunction(new MatchRegularFunction());
		AviatorEvaluator.addFunction(new MatchConditionFunction());
		AviatorEvaluator.addFunction(new SumArrayFunction());
		AviatorEvaluator.addFunction(new MathAbsOverrideFunction());
		AviatorEvaluator.addFunction(new GenH3cSNFunction());
		AviatorEvaluator.addFunction(new GenProfileIndexFunction());
		AviatorEvaluator.addFunction(new ConditionChooseFunction());
		AviatorEvaluator.addFunction(new GrowthRateFunction());
		AviatorEvaluator.addFunction(new OperationSystemFunction());
		AviatorEvaluator.addFunction(new KeepPostiveFunction());
		AviatorEvaluator.addFunction(new MultiValueFunction());
		AviatorEvaluator.addFunction(new MD5MultipleFunction());
		AviatorEvaluator.addFunction(new LiteMultipleFunction());
		AviatorEvaluator.addFunction(new GetIndexFunction());
		AviatorEvaluator.addFunction(new GetIndexValueFunction());
		AviatorEvaluator.addFunction(new SpeedExtFunction());
		AviatorEvaluator.addFunction(new EvalDefultFunction());
		AviatorEvaluator.addFunction(new IsNotNullFunction());
		AviatorEvaluator.addFunction(new DateFormatTransformFunction());
		AviatorEvaluator.addFunction(new IndexFilterFunction());
		AviatorEvaluator.addFunction(new RoundingFunction());
		AviatorEvaluator.addFunction(new RemoveInvalidFunction());
		AviatorEvaluator.addFunction(new ConvertLongFunction());
		AviatorEvaluator.addFunction(new MultiJoinFunction());
		AviatorEvaluator.addFunction(new GetOidIndexFunction());
		//		AviatorEvaluator.setOption(Options.TRACE, true);
	}

	private static ObjectMapper mapper = new ObjectMapper();

	public static Object covertDirect(List<String> keys, TransformData data) {
		try {
			return findValByKey(keys, mapper.readTree(data.getResult()));
		} catch (Exception e) {
			return null;
		}
	}

	public static final Object findValByKey(List<String> keys, JsonNode content) {
		Object value = null;
		List<Object> result = new ArrayList<>();
		int keynum = keys.size();
		if (keynum == 0) {//如果没有对应key，可能是取对应全部结果
			value = content == null ? null : content.toString();
			return value;
		}
		if (content instanceof ArrayNode) {
			ArrayNode arrayNode = (ArrayNode) content;
			Iterator<JsonNode> ite = arrayNode.iterator();
			while (ite.hasNext()) {
				JsonNode tmp = getParentJsonNode(keys, keynum, ite.next());
				if (tmp != null) {
					try {
						value = Double.parseDouble(tmp.asText());
					} catch (Exception e) {
						value = tmp.asText();
					}

				}
			}
		} else {
			JsonNode val = getParentJsonNode(keys, keynum, content);
			if (val instanceof ArrayNode) {
				ArrayNode array = (ArrayNode) val;
				if (array.size() > 0) {
					for (JsonNode n : array) {
						try {
							result.add(Double.parseDouble(n.asText()));
						} catch (Exception e) {
							result.add(n.asText());
						}
					}
				}
			} else if (val != null) {
				if (val instanceof ObjectNode) {
					try {
						value = Double.parseDouble(val.toString());
					} catch (Exception e) {
						value = val.toString();
					}
				} else {
					try {
						value = Double.parseDouble(val.asText());
					} catch (Exception e) {
						value = val.asText();
					}
				}
			}
		}
		return result.size() > 0 ? result : value;
	}

	/**
	 * 原始注释：这里的key是从模型文件中传过来的，我们没有规定格式，但是content里面的key都是小写的，所以keys变小写后在检索json
	 * 重新调整规则，获取清洗后的值时不再特意将key转小写，保留清洗时的key，未来业务对接时，用户也可能自行填写大小写区分的key值。
	 * @param keys
	 * @param keynum
	 * @param content
	 * @return
	 */

	public static JsonNode getParentJsonNode(List<String> keys, int keynum, JsonNode content) {
		JsonNode parent = null;
		JsonNode val = null;
		String key = null;
		for (int i = 0; i < keynum; i++) {
			key = keys.get(i);
			if (parent == null) {
				parent = content.get(key);
			} else {
				if (i != keynum - 1)
					parent = parent.get(key);
			}
		}
		key = keys.get(keynum - 1);
		if (keynum >= 2) {
			val = parent.get(key);
		} else {
			val = content.get(key);
		}
		return val;
	}

	public static void binding(MetricInfo metric, TransformData data, String dataAlias) {

		JsonNode restult = null;
		try {
			restult = mapper.readTree(data.getResult());
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (DSInfo dsInfo : metric.getDsinfo()) {
			if (dataAlias.equals(dsInfo.getAlias())) {
				Object val = findValByKey(dsInfo.getKeys(), restult);
				dsInfo.setValue(val);
			}
		}
	}

}