package org.coredata.core.stream.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.metric.documents.Metric;
import org.coredata.core.model.mining.Expression;
import org.coredata.core.model.mining.Param;
import org.coredata.core.stream.mining.entity.MetricInfo;
import org.coredata.core.stream.vo.CMDInfo;
import org.coredata.core.stream.vo.CMDInfo.SourceType;
import org.coredata.core.stream.vo.DSInfo;
import org.coredata.core.stream.vo.TransformData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModelExpHelper {

	private static final Logger logger = LoggerFactory.getLogger(ModelExpHelper.class);

	private static String cmd = "cmd\\.\\$\\{(.+?)\\}";
	private static Pattern cmdPattern = Pattern.compile(cmd);

	private static String dataSourceExp = "\\$\\{(.*?)\\}(\\.\\$\\{(.*?)\\})+";
	private static Pattern dataSourcePattern = Pattern.compile(dataSourceExp);

	private static String metricExp = "^(.*?):(.*?)$";
	private static Pattern metricPattern = Pattern.compile(metricExp);

	private static String keyExp = "\\$\\{(.*?)\\}";
	private static Pattern kp = Pattern.compile(keyExp);

	private static ObjectMapper mapper = new ObjectMapper();

	public static MetricInfo process(String instId, Expression exp, String type, Set<String> notSupportAlias) {
		String metric = exp.getMetric();
		Matcher matcher = metricPattern.matcher(metric);
		if (matcher.find() && matcher.groupCount() == 2) {
			String metricId = matcher.group(1);
			String dsExp = matcher.group(2);
			List<DSInfo> dsList = new ArrayList<>();
			List<Param> param = exp.getParam();
			for (Param p : param) {
				String excuteExp = p.getValue();
				Matcher dsMatcher = dataSourcePattern.matcher(excuteExp);
				boolean matchSource = false;//是否匹配上${}.${}的格式
				while (dsMatcher.find()) {
					matchSource = true;
					String key = "";
					String dsStr = dsMatcher.group();
					String alias = dsMatcher.group(1);
					//alias是数据源的别名，A，B等，此处需要判定是否是黑名单内容
					if (notSupportAlias != null && notSupportAlias.contains(alias))
						continue;
					List<String> keys = new ArrayList<String>();
					Matcher v = kp.matcher(dsStr);
					int index = 0;
					while (v.find()) {
						if (index != 0) {
							keys.add(v.group(1));
							key = key + "." + v.group(1);
						}
						index++;
					}
					dsList.add(new DSInfo(dsStr, alias, key.substring(1), keys));
				}
				if (!matchSource) {//如果无法按照${}.${}匹配，则单独匹配${}，保证后续执行挖掘方法
					Matcher v = kp.matcher(excuteExp);
					while (v.find()) {
						String dsStr = v.group();
						String alias = v.group(1);
						dsList.add(new DSInfo(dsStr, alias, alias, new ArrayList<String>()));
					}
				}
			}

			return new MetricInfo(metricId, instId, dsExp, type, dsList.toArray(new DSInfo[] {}));
		}
		return null;
	}

	public static CMDInfo processCmd(String exp) {
		Matcher m = cmdPattern.matcher(exp);
		if (m.find()) {
			String cmd = m.group(1);
			CMDInfo cmdInfo = new CMDInfo(cmd);
			return cmdInfo;
		}
		return null;
	}

	public static CMDInfo processMiningCmd(String exp) {
		Matcher m = cmdPattern.matcher(exp);
		CMDInfo cmdInfo = null;
		if (m.find()) {
			String cmd = m.group(1);
			cmdInfo = new CMDInfo(cmd);
			cmdInfo.setSourceType(SourceType.cmd.toString());
			return cmdInfo;
		}
		if (SourceType.property.toString().equals(exp)) {
			cmdInfo = new CMDInfo(exp);
			cmdInfo.setSourceType(SourceType.property.toString());
		} else if (SourceType.conninfo.toString().equals(exp)) {
			cmdInfo = new CMDInfo(exp);
			cmdInfo.setSourceType(SourceType.conninfo.toString());
		}
		return cmdInfo;
	}

	public static Metric mining(MetricInfo info, TransformData data, Object value) {

		if (value == null) {
			return new Metric(UUID.randomUUID().toString(), info.getId(), null, null, null, data.getInstanceId(), System.currentTimeMillis(),
					data.getCustomerId());
		}

		Double val = null;
		if (value instanceof Integer) {
			val = ((Integer) value).doubleValue();
		} else if (value instanceof Long) {
			val = ((Long) value).doubleValue();
		} else if (value instanceof Double) {
			val = ((Double) value);
		} else {
			//此处新增判断，如果metric的datatype属于某几种类型，则不做处理
			if (info.isNeedScale())
				try {
					val = Double.parseDouble(value.toString());
				} catch (NumberFormatException e) {
					val = null;
				}
		}
		if (val != null) {
			//设置保留两位小数
			if (val.isNaN())
				return null;
			BigDecimal b = new BigDecimal(val);
			val = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			return new Metric(UUID.randomUUID().toString(), info.getId(), null, val, null, data.getInstanceId(), System.currentTimeMillis(),
					data.getCustomerId());
		} else {
			//判定是否为json格式数据，如果是，保存到objVal字段
			try {
				JsonNode json = mapper.readTree(value.toString());
				if (json.isObject()) {
					if (logger.isDebugEnabled())
						logger.debug("save metric on objVal:" + info.getId());
					//Base64编码
					String encode = Base64.getEncoder().encodeToString(value.toString().getBytes("utf-8"));
					return new Metric(UUID.randomUUID().toString(), info.getId(), null, null, encode, data.getInstanceId(), System.currentTimeMillis(),
							data.getCustomerId());
				}
				return new Metric(UUID.randomUUID().toString(), info.getId(), null, null, value.toString(), data.getInstanceId(), System.currentTimeMillis(),
						data.getCustomerId());
			} catch (IOException e) {
				return new Metric(UUID.randomUUID().toString(), info.getId(), null, null, value.toString(), data.getInstanceId(), System.currentTimeMillis(),
						data.getCustomerId());
			}
		}

	}

}
