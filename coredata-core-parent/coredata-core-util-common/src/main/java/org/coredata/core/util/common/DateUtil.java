package org.coredata.core.util.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类，用于进行相关日期或者时间的转换
 *
 */
public class DateUtil {
	   /**
     * 英文简写（默认）如：2010-12-01
     */
    public static String FORMAT_SHORT = "yyyy-MM-dd";
    /**
     * 英文全称 如：2010-12-01 23:15:06
     */
    public static String FORMAT_LONG = "yyyy-MM-dd HH:mm:ss";
    /**
     * 精确到毫秒的完整时间 如：yyyy-MM-dd HH:mm:ss.S
     */
    public static String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss.S";
    /**
     * 中文简写 如：2010年12月01日
     */
    public static String FORMAT_SHORT_CN = "yyyy年MM月dd";
    /**
     * 中文全称 如：2010年12月01日 23时15分06秒
     */
    public static String FORMAT_LONG_CN = "yyyy年MM月dd日  HH时mm分ss秒";
    /**
     * 精确到毫秒的完整中文时间
     */
    public static String FORMAT_FULL_CN = "yyyy年MM月dd日  HH时mm分ss秒SSS毫秒";

    public static final String MIN_UNIT = "m";
    public static final String SECOND_UNIT = "s";
    public static final String MILLIONSECOND_UNIT = "ms";

    /**
     * 获得默认的 date pattern
     */
    public static String getDatePattern() {
        return FORMAT_LONG;
    }

    /**
     * 根据预设格式返回当前日期
     * 
     * @return
     */
    public static String getNow() {
        return format(new Date());
    }

    /**
     * 根据用户格式返回当前日期
     * 
     * @param format
     * @return
     */
    public static String getNow(String format) {
        return format(new Date(), format);
    }

    /**
     * 使用预设格式格式化日期
     * 
     * @param date
     * @return
     */
    public static String format(Date date) {
        return format(date, getDatePattern());
    }

    /**
     * 使用用户格式格式化日期
     * 
     * @param date
     *            日期
     * @param pattern
     *            日期格式
     * @return
     */
    public static String format(Date date, String pattern) {
        String returnValue = "";
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            returnValue = df.format(date);
        }
        return (returnValue);
    }

    /**
     * 使用预设格式提取字符串日期
     * 
     * @param strDate
     *            日期字符串
     * @return
     */
    public static Date parse(String strDate) {
        return parse(strDate, getDatePattern());
    }

    /**
     * 使用用户格式提取字符串日期
     * 
     * @param strDate
     *            日期字符串
     * @param pattern
     *            日期格式
     * @return
     */
    public static Date parse(String strDate, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 在日期上增加数个整月
     * 
     * @param date
     *            日期
     * @param n
     *            要增加的月数
     * @return
     */
    public static Date addMonth(Date date, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, n);
        return cal.getTime();
    }

    /**
     * 在日期上增加天数
     * 
     * @param date
     *            日期
     * @param n
     *            要增加的天数
     * @return
     */
    public static Date addDay(Date date, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, n);
        return cal.getTime();
    }

    /**
     * 获取时间戳
     */
    public static String getTimeString() {
        SimpleDateFormat df = new SimpleDateFormat(FORMAT_FULL);
        Calendar calendar = Calendar.getInstance();
        return df.format(calendar.getTime());
    }

    /**
     * 获取日期年份
     * 
     * @param date
     *            日期
     * @return
     */
    public static String getYear(Date date) {
        return format(date).substring(0, 4);
    }

    /**
     * 按默认格式的字符串距离今天的天数
     * 
     * @param date
     *            日期字符串
     * @return
     */
    public static int countDays(String date) {
        long t = Calendar.getInstance().getTime().getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(parse(date));
        long t1 = c.getTime().getTime();
        return (int) (t / 1000 - t1 / 1000) / 3600 / 24;
    }

    /**
     * 按用户格式字符串距离今天的天数
     * 
     * @param date
     *            日期字符串
     * @param format
     *            日期格式
     * @return
     */
    public static int countDays(String date, String format) {
        long t = Calendar.getInstance().getTime().getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(parse(date, format));
        long t1 = c.getTime().getTime();
        return (int) (t / 1000 - t1 / 1000) / 3600 / 24;
    }
	/**
	 * 该方法用于进行日期转换，对应的标准格式
	 * @param source
	 * @param format
	 * @return
	 */
	public static String dateconverter(long ms, String format) {
		return getStrOfSeconds(ms, format);
	}

	private static String getStrOfSeconds(final long seconds, String format) {
		if (seconds < 0) {
			return String.valueOf(seconds);
		}
		long one_day = 60 * 60 * 24 * 1000;
		long one_hour = 60 * 60 * 1000;
		long one_minute = 60 * 1000;
		long day, hour, minute, second = 0L;
		;

		day = seconds / one_day;
		hour = seconds % one_day / one_hour;
		minute = seconds % one_day % one_hour / one_minute;
		second = seconds % one_day % one_hour % one_minute;
		return String.format(format, day, hour, minute, second);
		//		if (seconds < one_minute) {
		//			return seconds + "秒";
		//		} else if (seconds >= one_minute && seconds < one_hour) {
		//			return minute + "分" + second + "秒";
		//		} else if (seconds >= one_hour && seconds < one_day) {
		//			return hour + "时" + minute + "分" + second + "秒";
		//		} else {
		//			return day + "天" + hour + "时" + minute + "分" + second + "秒";
		//		}
	}

    public static String translateTimeOutUnit(int mss, String unit) {
        String result = mss + "ms";
        if (mss < 1000) {
            return result;
        } else {
            byte var4 = -1;
            switch(unit.hashCode()) {
                case 109:
                    if (unit.equals("m")) {
                        var4 = 1;
                    }
                    break;
                case 115:
                    if (unit.equals("s")) {
                        var4 = 0;
                    }
            }

            switch(var4) {
                case 0:
                    long seconds = Math.round((double)mss / 1000.0D);
                    result = seconds + "s";
                    break;
                case 1:
                    long minutes = Math.round((double)mss / 60000.0D);
                    result = minutes + "m";
            }

            return result;
        }
    }

    public static String translateDatatypeForPeriod(String datatype) {
        return DateUtil.DATATYPE.getPeriod(datatype);
    }
    public static enum DATATYPE {
        avail("avail", "30s"),
        info("info", "5m"),
        perf("perf", "5m"),
        conf("conf", "5m");

        private String datatype;
        private String period;

        private DATATYPE(String datatype, String period) {
            this.datatype = datatype;
            this.period = period;
        }

        public static String getPeriod(String datatype) {
            DateUtil.DATATYPE[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                DateUtil.DATATYPE d = var1[var3];
                if (d.getDatatype().equals(datatype)) {
                    return d.period;
                }
            }

            return null;
        }

        public String getDatatype() {
            return this.datatype;
        }

        public void setDatatype(String datatype) {
            this.datatype = datatype;
        }

        public String getPeriod() {
            return this.period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }
    }
}
