package org.coredata.core.stream.mining.functions;

import java.util.Map;

import org.coredata.core.stream.mining.entity.MetricInfo;
import org.coredata.core.stream.vo.DSInfo;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class GenProfileIndexFunction extends AbsFunction {

	@Override
	public String getName() {
		return "genProfileIndex";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		String expStr = FunctionUtils.getStringValue(exp, env);
		MetricInfo metricInfo = (MetricInfo) env.get(METRIC_INFO);
		DSInfo[] dsInfos = metricInfo.getDsinfo();
		if (dsInfos != null) {
			for (DSInfo dsInfo : dsInfos)
				expStr = expStr.replace(dsInfo.getId(), dsInfo.getValue().toString());
		}
		String oidIndexFromProfileName = getOidIndexFromProfileName(expStr);
		return new AviatorString(oidIndexFromProfileName);
	}

	private String getOidIndexFromProfileName(String profileName) {
		char[] name = profileName.toCharArray();
		StringBuilder oidindex = new StringBuilder();
		oidindex.append(name.length);
		for (int i = 0; i < name.length; i++) {
			oidindex.append(".").append((int) name[i]);
		}
		return oidindex.toString();
	}
}
