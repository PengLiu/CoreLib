package org.coredata.core.util.common;

public class UnitUtils {

	enum Unit {
		Bit, Byte, KB, MB, GB, TB, PB, BPS, KBPS, MBPS, GBPS, GiB, TiB
	}

	public static final double convertToMin(double val, String unit) {

		switch (Unit.valueOf(unit)) {
		case Bit:
			return val / 8;
		case KB:
			return val * 1024;
		case MB:
			return convertToMin(val, "KB") * 1024;
		case GB:
			return convertToMin(val, "MB") * 1024;
		case TB:
			return convertToMin(val, "GB") * 1024;
		case PB:
			return convertToMin(val, "TB") * 1024;
		case KBPS:
			return val * 1024;
		case MBPS:
			return convertToMin(val, "KBPS") * 1024;
		case GBPS:
			return convertToMin(val, "MBPS") * 1024;
		case GiB:
			return val * 1024 * 1024 * 1024;
		case TiB:
			return val * 1024 * 1024 * 1024 * 1024;
		default:
			return val;
		}
	}
	
	public static final double convertToFix(double val, String unit,String fix) {

		if(unit.equals(fix)){
			return val;
		}
		
		double BTypeVal=convertToMin(val,unit);
		
		switch (Unit.valueOf(fix)) {
		case Bit:
			return BTypeVal * 8;
		case KB:
			return BTypeVal / 1024;
		case MB:
			return BTypeVal / 1024 / 1024;
		case GB:
			return BTypeVal / 1024 / 1024 / 1024;
		case TB:
			return BTypeVal / 1024 / 1024 / 1024 / 1024; 
		case PB:
			return BTypeVal / 1024 / 1024 / 1024 / 1024 / 1024;
		case KBPS:
			return BTypeVal / 1024;
		case MBPS:
			return BTypeVal / 1024 / 1024;
		case GBPS:
			return BTypeVal / 1024 / 1024 / 1024;
		case GiB:
			return BTypeVal / 1024 / 1024 / 1024;
		case TiB:
			return BTypeVal / 1024 / 1024 / 1024 / 1024;
		default:
			return BTypeVal;
		}
	}

}
