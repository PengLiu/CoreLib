package org.coredata.core.stream.mining.functions;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.type.AviatorBigInt;
import com.googlecode.aviator.runtime.type.AviatorDecimal;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;

public class CustomerLong extends AviatorLong {

	public CustomerLong(Number number) {
		super(number);
	}

	@SuppressWarnings("deprecation")
	@Override
	public AviatorObject innerDiv(AviatorNumber other) {
		if (this.number == null || other.getValue(null) == null)
			return new CustomerDouble(null);
		switch (other.getAviatorType()) {
		case BigInt:
			if (other.toBigInt().intValue() == 0)
				return new CustomerDouble(0);
			return AviatorBigInt.valueOf(this.toBigInt().divide(other.toBigInt()));
		case Decimal:
			if (other.toDecimal().intValue() == 0)
				return new CustomerDouble(0);
			return AviatorDecimal.valueOf(this.toDecimal().divide(other.toDecimal(), AviatorEvaluator.getMathContext()));
		case Long:
			if (other.longValue() == 0)
				return new CustomerDouble(0);
			return AviatorLong.valueOf(this.number.longValue() / other.longValue());
		default:
			if (other.doubleValue() == 0)
				return new CustomerDouble(0);
			return new AviatorDouble(this.number.longValue() / other.doubleValue());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public AviatorObject innerAdd(AviatorNumber other) {
		if (this.number == null || other.getValue(null) == null)
			return new CustomerDouble(null);
		switch (other.getAviatorType()) {
		case BigInt:
			return AviatorBigInt.valueOf(this.toBigInt().add(other.toBigInt()));
		case Decimal:
			return AviatorDecimal.valueOf(this.toDecimal().add(other.toDecimal(), AviatorEvaluator.getMathContext()));
		case Long:
			return AviatorLong.valueOf(this.number.longValue() + other.longValue());
		default:
			return new AviatorDouble(this.number.longValue() + other.doubleValue());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public AviatorObject innerMod(AviatorNumber other) {
		if (this.number == null || other.getValue(null) == null)
			return new CustomerDouble(null);
		switch (other.getAviatorType()) {
		case BigInt:
			return AviatorBigInt.valueOf(this.toBigInt().mod(other.toBigInt()));
		case Decimal:
			return AviatorDecimal.valueOf(this.toDecimal().remainder(other.toDecimal(), AviatorEvaluator.getMathContext()));
		case Long:
			return AviatorLong.valueOf(this.number.longValue() % other.longValue());
		default:
			return new AviatorDouble(this.number.longValue() % other.doubleValue());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public AviatorObject innerMult(AviatorNumber other) {
		if (this.number == null || other.getValue(null) == null)
			return new CustomerDouble(null);
		switch (other.getAviatorType()) {
		case BigInt:
			return AviatorBigInt.valueOf(this.toBigInt().multiply(other.toBigInt()));
		case Decimal:
			return AviatorDecimal.valueOf(this.toDecimal().multiply(other.toDecimal(), AviatorEvaluator.getMathContext()));
		case Long:
			return AviatorLong.valueOf(this.number.longValue() * other.longValue());
		default:
			return new AviatorDouble(this.number.longValue() * other.doubleValue());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public AviatorObject innerSub(AviatorNumber other) {
		if (this.number == null || other.getValue(null) == null)
			return new CustomerDouble(null);
		switch (other.getAviatorType()) {
		case BigInt:
			return AviatorBigInt.valueOf(this.toBigInt().subtract(other.toBigInt()));
		case Decimal:
			return AviatorDecimal.valueOf(this.toDecimal().subtract(other.toDecimal(), AviatorEvaluator.getMathContext()));
		case Long:
			return AviatorLong.valueOf(this.number.longValue() - other.longValue());
		default:
			return new AviatorDouble(this.number.longValue() - other.doubleValue());
		}
	}
}
