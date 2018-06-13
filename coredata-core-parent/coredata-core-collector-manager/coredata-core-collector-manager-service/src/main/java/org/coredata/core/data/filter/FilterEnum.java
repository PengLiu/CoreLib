package org.coredata.core.data.filter;

/**
 * 定义filter枚举常量
 * @author wangwei
 *
 * @date   2018年3月26日
 */
public final class FilterEnum {
	
	/*
	 * 定义filter的动作类型{替换、保留原有增加新列、删除原有增加新列【这里特指删掉原有的并把新列设置在别的index】}
	 */
	public enum ActionType{
		Replace,SaveAndAdd,DelAndAdd
	}
	/*
	 * filter的name
	 */
	public enum FilterName{
		date,ip,split,json,transfor,grok
	}
}
