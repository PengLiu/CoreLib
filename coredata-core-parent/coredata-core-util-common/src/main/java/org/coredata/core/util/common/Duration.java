package org.coredata.core.util.common;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public class Duration {

	public static final TimeUnit DEFAULT_UNIT = TimeUnit.MILLISECONDS;

	private final long duration;
	private final TimeUnit unit;

	public Duration(long duration, TimeUnit unit) {
		this.duration = duration;
		this.unit = Optional.fromNullable(unit).or(TimeUnit.SECONDS);
	}

	public long convert(final TimeUnit unit) {
		return unit.convert(this.duration, this.unit);
	}

	public long toMilliseconds() {
		return convert(TimeUnit.MILLISECONDS);
	}

	public String toPeriod() {

		long millis = toMilliseconds();
		long seconds = millis / 1000;
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / (60 * 60)) % 24;
		long d = (seconds / (60 * 60 * 24)) % 24;
		return String.format("%02dd%02dh%02dm%02ds", d, h, m, s);
	}

	public static Duration of(long duration, TimeUnit unit) {
		return new Duration(duration, unit);
	}

	public static Duration ofMilliseconds(long duration) {
		return of(duration, TimeUnit.MILLISECONDS);
	}

	public Duration withUnit(final TimeUnit other) {
		return new Duration(duration, other);
	}

	public String toDSL() {
		return Long.toString(duration) + unitSuffix(unit);
	}

	private static final Pattern PATTERN = Pattern.compile("^(\\d+)([a-zA-Z]*)$");

	private static Map<String, TimeUnit> units = ImmutableMap.<String, TimeUnit>builder()
			.put("ms", TimeUnit.MILLISECONDS).put("s", TimeUnit.SECONDS).put("m", TimeUnit.MINUTES)
			.put("h", TimeUnit.HOURS).put("H", TimeUnit.HOURS).put("d", TimeUnit.DAYS).build();

	public static Duration parseDuration(final String string) {
		final Matcher m = PATTERN.matcher(string);

		if (!m.matches()) {
			throw new IllegalArgumentException("Invalid duration: " + string);
		}

		final long duration = Long.parseLong(m.group(1));
		final String unitString = m.group(2);

		if (unitString.isEmpty()) {
			return new Duration(duration, DEFAULT_UNIT);
		}

		if ("w".equals(unitString)) {
			return new Duration(duration * 7, TimeUnit.DAYS);
		}

		final TimeUnit unit = units.get(unitString);

		if (unit == null) {
			throw new IllegalArgumentException("Invalid unit (" + unitString + ") in duration: " + string);
		}

		return new Duration(duration, unit);
	}

	public static String unitSuffix(TimeUnit unit) {
		switch (unit) {
		case MILLISECONDS:
			return "ms";
		case SECONDS:
			return "s";
		case MINUTES:
			return "m";
		case HOURS:
			return "h";
		case DAYS:
			return "d";
		default:
			throw new IllegalStateException("Unit not supported for serialization: " + unit);
		}
	}
}