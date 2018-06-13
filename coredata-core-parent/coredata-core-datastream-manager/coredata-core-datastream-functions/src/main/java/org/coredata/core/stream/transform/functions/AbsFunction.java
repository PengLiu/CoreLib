package org.coredata.core.stream.transform.functions;

import org.coredata.core.stream.util.LookupTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;

public abstract class AbsFunction extends AbstractFunction {

	final Logger logger = LoggerFactory.getLogger(getClass());

	public static final String COLLET_DATA = "CollectData";

	public static void initFunction(LookupTool lookupTool) {
		AviatorEvaluator.addFunction(new IsNotNullFunction());
		AviatorEvaluator.addFunction(new HasErrorFunction());
		AviatorEvaluator.addFunction(new IsTrueFunction());
		AviatorEvaluator.addFunction(new IsContainFunction());
		AviatorEvaluator.addFunction(new GetVendorFunction(lookupTool));
	}

}
