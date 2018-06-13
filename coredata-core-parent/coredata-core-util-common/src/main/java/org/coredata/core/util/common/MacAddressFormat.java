package org.coredata.core.util.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MacAddressFormat {

	public static String format(String mac) throws MacAddressFormatException {
		if (!isValid(mac)) {
			throw new MacAddressFormatException("Invalid MAC [" + mac + "].");
		}

		return standard(mac);
	}

	private static String standard(String mac) {
		return mac.replaceAll("(\\.|\\,|\\-)", ":");
	}

	private static final Pattern REGULAR = Pattern.compile("^([0-9A-Fa-f]{2}[\\.:-]){5}([0-9A-Fa-f]{2})$");

	public static boolean isValid(String mac) {
		if (mac == null) {
			return false;
		}

		Matcher matcher = REGULAR.matcher(mac);
		return matcher.matches();
	}
	
	public static final class MacAddressFormatException extends Exception {

		public MacAddressFormatException() {}
		
		public MacAddressFormatException(String msg) {
			super(msg);
		}

		private static final long serialVersionUID = 8447660516207281067L;
	}
}
