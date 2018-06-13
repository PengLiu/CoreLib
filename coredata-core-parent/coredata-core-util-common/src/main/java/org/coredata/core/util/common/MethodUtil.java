package org.coredata.core.util.common;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 该类用于编写一些通用方法
 * @author sushi
 *
 */
public class MethodUtil {

	private static final String MD5 = "md5";

	private static final char DEFAULT_HEX_DELIMITER = ':';

	private static final String hexExp = "^[0-9a-zA-Z]{2}(:[0-9a-zA-Z]{2})+";

	private static Pattern hexPattern = Pattern.compile(hexExp);

	private static volatile Charset defaultCharset;

	/**
	* Eight-bit UCS Transformation Format
	*/
	public static final Charset UTF_8 = Charset.forName("UTF-8");

	/**
	 * GBK
	 */
	public static final Charset GBK = Charset.forName("GBK");
	public static final Charset ISO = Charset.forName("ISO-8859-1");
	public static final Charset GB2312 = Charset.forName("GB2312");

	public static String md5(String source) {
		try {
			MessageDigest md = MessageDigest.getInstance(MD5);
			md.update(source.getBytes());
			return new BigInteger(1, md.digest()).toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String transcoding(String source) {
		String delim = "";
		delim += DEFAULT_HEX_DELIMITER;
		if (source == null || !source.contains(delim))
			return source;
		Matcher matcher = hexPattern.matcher(source);
		if (!matcher.find())
			return source;
		StringTokenizer st = new StringTokenizer(source, delim);
		byte[] value = new byte[st.countTokens()];
		for (int n = 0; st.hasMoreTokens(); n++) {
			String s = st.nextToken();
			value[n] = (byte) Integer.parseInt(s, 16);
		}
		if (!isMessyCode(new String(value, GBK)))
			return new String(value, GBK).trim();
		if (!isMessyCode(new String(value, UTF_8)))
			return new String(value, UTF_8).trim();
		if (!isMessyCode(new String(value, GB2312)))
			return new String(value, GB2312).trim();
		if (!isMessyCode(new String(value, ISO)))
			return new String(value, ISO).trim();
		return new String(value, defaultCharset()).trim();
	}

	/**
	 * 转码字符集不受-Dfile.encoding环境变量控制.
	 * 当操作系统为中文操作系统时, 返回GBK
	 *        非中文操作系统时, 返回UTF8
	 */
	public static final Charset defaultCharset() {
		if (defaultCharset == null) {
			synchronized (MethodUtil.class) {
				Locale locale = Locale.getDefault();
				if ("zh".equals(locale.getLanguage())) {
					defaultCharset = GBK;
				} else {
					defaultCharset = UTF_8;
				}
			}
		}
		return defaultCharset;
	}

	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public static boolean isMessyCode(String strName) {
		Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
		Matcher m = p.matcher(strName);
		String after = m.replaceAll("");
		String temp = after.replaceAll("\\p{P}", "");
		char[] ch = temp.trim().toCharArray();
		float count = 0;
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (!Character.isLetterOrDigit(c)) {

				if (!isChinese(c)) {
					count = count + 1;
				}
			}
		}
		if (count > 0) {
			return true;
		} else {
			return false;
		}

	}
}
