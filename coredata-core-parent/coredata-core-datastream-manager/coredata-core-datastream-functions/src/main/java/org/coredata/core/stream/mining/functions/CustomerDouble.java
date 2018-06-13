package org.coredata.core.stream.mining.functions;

import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;

public class CustomerDouble extends AviatorDouble {

	public CustomerDouble(Number number) {
		super(number);
	}

	@Override
	public AviatorObject innerDiv(AviatorNumber other) {
		if (this.number == null || other.getValue(null) == null) {
			return new CustomerDouble(null);
		} else if (other.doubleValue() == 0) {
			return new CustomerDouble(0);
		}
		return new CustomerDouble(this.number.doubleValue() / other.doubleValue());
	}

	@Override
	public AviatorNumber innerAdd(AviatorNumber other) {
		if (this.number == null || other.getValue(null) == null)
			return new CustomerDouble(null);
		return new CustomerDouble(this.number.doubleValue() + other.doubleValue());
	}

	@Override
	public AviatorObject innerMod(AviatorNumber other) {
		if (this.number == null || other.getValue(null) == null)
			return new CustomerDouble(null);
		return new CustomerDouble(this.number.doubleValue() % other.doubleValue());
	}

	@Override
	public AviatorObject innerMult(AviatorNumber other) {
		if (this.number == null || other.getValue(null) == null)
			return new CustomerDouble(null);
		return new CustomerDouble(this.number.doubleValue() * other.doubleValue());
	}

	@Override
	public AviatorObject innerSub(AviatorNumber other) {
		if (this.number == null || other.getValue(null) == null)
			return new CustomerDouble(null);
		return new CustomerDouble(this.number.doubleValue() - other.doubleValue());
	}

}
