package org.coredata.core.stream.mining.functions;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.stream.vo.Unit;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;

public class SpeedFunction extends AbsFunction {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp, AviatorObject unitStr) {

		String unit = FunctionUtils.getStringValue(unitStr, env);//获取对应单位
		String instId = env.get(INSTANCE_ID).toString();
		String metricId = env.get("metricId").toString();
		long tasktime = Long.parseLong(env.get(TASK_TIME).toString());
		Map<String, String> aliasCmds = (Map<String, String>) env.get(CMD_NAME);
		Map<String, String> keys = (Map<String, String>) env.get(P_KEY);
		String pName = "";
		try {
			pName = ((AviatorJavaType) exp).getName();
		} catch (Exception e) {
			pName = env.get("paramName").toString();
		}
		if (StringUtils.isEmpty(pName))
			return new CustomerDouble(null);
		String key = keys.get(pName);
		String cmdId = aliasCmds.get(pName);
		double val = 0;
		if (exp instanceof AviatorNumber)
			val = FunctionUtils.getNumberValue(exp, env).doubleValue();
		else {
			Object expObj = FunctionUtils.getJavaObject(exp, env);
			if (expObj == null)
				return new CustomerDouble(null);
			if (expObj instanceof List) {
				List results = (List) expObj;
				for (Object r : results)
					val += Double.valueOf(r.toString());
			} else {
				try {
					val = Double.valueOf(expObj.toString());
				} catch (Exception e) {
					return new CustomerDouble(null);
				}
			}
		}
		Unit oldUnit = getOldUnit(instId, cmdId, key, metricId);
		Unit newUnit = new Unit(val, tasktime);
		saveNewUnit(instId, cmdId, key, metricId, newUnit);
		if (oldUnit != null) {
			double speed = 0;
			double time = tasktime - oldUnit.getTimestamp();
			if (time == 0)
				return new CustomerDouble(null);
			speed = (val - oldUnit.getValue()) / time;
			speed = unitConvert(speed, unit);
			return new CustomerDouble(speed);
		}
		return new CustomerDouble(null);
	}

	private double unitConvert(double val, String unit) {
		switch (unit) {
		case "m":
			return val * 1000 * 60;
		case "h":
			return val * 1000 * 60 * 60;
		case "d":
			return val * 1000 * 60 * 60 * 24;
		default:
			return val * 1000;
		}

	}

	@Override
	public String getName() {
		return "speed";
	}

}