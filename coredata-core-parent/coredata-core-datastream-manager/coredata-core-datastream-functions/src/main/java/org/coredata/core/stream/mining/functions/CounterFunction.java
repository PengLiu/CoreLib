package org.coredata.core.stream.mining.functions;

import java.util.List;
import java.util.Map;

import org.coredata.core.stream.vo.Unit;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorObject;

public class CounterFunction extends AbsFunction {

	@Override
	public String getName() {
		return "counter";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		String instId = env.get(INSTANCE_ID).toString();
		String pName = ((AviatorJavaType) exp).getName();
		String metricId = env.get(METRIC_ID).toString();
		long tasktime = Long.parseLong(env.get(TASK_TIME).toString());
		Map<String, String> aliasCmds = (Map<String, String>) env.get(CMD_NAME);
		Map<String, String> keys = (Map<String, String>) env.get(P_KEY);
		Object expObj = FunctionUtils.getJavaObject(exp, env);
		if (expObj == null)
			return new CustomerDouble(null);
		String key = keys.get(pName);
		String cmdId = aliasCmds.get(pName);
		double val = 0;
		if (expObj instanceof List) {
			List results = (List) expObj;
			for (Object r : results)
				val += Double.valueOf(r.toString());
		} else
			val = Double.valueOf(expObj.toString());
		Unit oldUnit = getOldUnit(instId, cmdId, key, metricId);
		Unit newUnit = new Unit(val, tasktime);
		saveNewUnit(instId, cmdId, key, metricId, newUnit);
		if (oldUnit != null) {
			double incremental = val - oldUnit.getValue();
			return new CustomerDouble(incremental);
		}
		return new CustomerDouble(null);
	}

}
