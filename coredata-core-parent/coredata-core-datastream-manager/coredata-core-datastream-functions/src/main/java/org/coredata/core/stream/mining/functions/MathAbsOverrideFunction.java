package org.coredata.core.stream.mining.functions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBigInt;
import com.googlecode.aviator.runtime.type.AviatorDecimal;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.utils.TypeUtils;

public class MathAbsOverrideFunction extends AbstractFunction {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
		try {
			Number number = FunctionUtils.getNumberValue(arg1, env);
			if (number == null) {
				return new AviatorDouble(0);
			}
			if (TypeUtils.isDecimal(number)) {
				return new AviatorDecimal(((BigDecimal) number).abs(AviatorEvaluator.getOption(Options.MATH_CONTEXT)));
			} else if (TypeUtils.isBigInt(number)) {
				return new AviatorBigInt(((BigInteger) number).abs());
			} else if (TypeUtils.isDouble(number)) {
				return new AviatorDouble(Math.abs(number.doubleValue()));
			} else {
				return AviatorLong.valueOf(Math.abs(number.longValue()));
			}
		} catch (Exception e) {
			logger.error("MathAbs error" + e);
		}
		return new AviatorDouble(null);
	}

	@Override
	public String getName() {
		return "math.abs";
	}
}
