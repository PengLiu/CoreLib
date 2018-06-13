package org.coredata.core.stream.transform.functions;

import java.util.Map;

import org.coredata.core.stream.vo.TransformData;

import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class HasErrorFunction extends AbsFunction {

	@Override
	public String getName() {
		return "hasError";
	}

	@Override
	public AviatorObject call(Map<String, Object> env) {
		TransformData data = (TransformData) env.get(COLLET_DATA);
		return new AviatorString(String.valueOf(data.isError()));
	}

}
