package org.coredata.core.data.schedule;

import org.coredata.core.data.schedule.ScheduleEnum.ScheduleType;
import org.coredata.core.data.schedule.ScheduleEnum.ScheduleUnit;

//格式: [秒] [分] [小时] [日] [月] [周] [年]
//0 0 12 * * ?           每天12点触发 
//0 15 10 ? * *          每天10点15分触发 
//0 15 10 * * ?          每天10点15分触发  
//0 15 10 * * ? *        每天10点15分触发  
//0 15 10 * * ? 2005     2005年每天10点15分触发 
//0 * 14 * * ?           每天下午的 2点到2点59分每分触发 
//0 0/5 14 * * ?         每天下午的 2点到2点59分(整点开始，每隔5分触发)  
//0 0/5 14,18 * * ?        每天下午的 18点到18点59分(整点开始，每隔5分触发)
//
//
//0 0-5 14 * * ?            每天下午的 2点到2点05分每分触发 
//0 10,44 14 ? 3 WED        3月分每周三下午的 2点10分和2点44分触发 
//0 15 10 ? * MON-FRI       从周一到周五每天上午的10点15分触发 
//0 15 10 15 * ?            每月15号上午10点15分触发 
//0 15 10 L * ?             每月最后一天的10点15分触发 
//0 15 10 ? * 6L            每月最后一周的星期五的10点15分触发 
//0 15 10 ? * 6L 2002-2005  从2002年到2005年每月最后一周的星期五的10点15分触发
//
//
//0 15 10 ? * 6#3           每月的第三周的星期五开始触发 
//0 0 12 1/5 * ?            每月的第一个中午开始每隔5天触发一次 
//0 11 11 11 11 ?           每年的11月11号 11点11分触发(光棍节)
public class ScheduleConfig {
	private ScheduleType type; // 每隔多长时间执行,每周几执行,每月几执行,立即执行everytime, everyweek, everymonth, Immediate
	private int interval; // >0 整数
	private ScheduleUnit unit; // Sec,Min,Hour,Day
	private int sec;
	private int min;
	private int hour;

	public String getCronExpression() {
		String[] weekNames = {"SUN", "MON", "TUES", "WED", "THUR", "FRI", "SAT"};
		String conExpression = "";
		String timestr = sec + " " + min + " " + hour + " ";
		switch (type) {
		case everytime:
			switch (unit) {
			case Sec:
				conExpression = "0/" + interval + " * * * * ?";
				break;
			case Min:
				conExpression = "0 0/" + interval + " * * * ?";
				break;
			case Hour:
				conExpression = "0 0 0/" + interval + " * * ?";
				break;
			case Day:
				conExpression = "0 0 0 */" + interval + " * ?";
				break;
			default:
				break;
			}
			break;
		case everyweek:
			conExpression = timestr + "? * " + weekNames[interval-1];
			break;
		case everymonth:
			conExpression = timestr + interval + " * ?";
			break;
		default:
			break;
		}
		return conExpression;
	}

	public boolean isImmediate() {
		return (type == ScheduleType.Immediate);
	}

	public ScheduleType getType() {
		return type;
	}

	public void setType(ScheduleType type) {
		this.type = type;
	}

}
