package org.coredata.core.model.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.coredata.core.util.encryption.PersistEncrypted;

/**
 * 定义的采集模型
 * @author sushi
 *
 */
public class Collector implements Serializable {

	private static final long serialVersionUID = -1650214906938109173L;

	private String id;

	private String desc;

	private String type;

	@PersistEncrypted
	private String cmd;

	private List<Param> param = new ArrayList<>();

	private String datatype;

	private String resulttype;

	private String timeout;

	private String retry;

	private String support;

	private String storage;

	/**
	 * 指令版本，base标识标准，adv可能是高级指令
	 */
	private String classification;

	/**
	 * 指令周期，set值的时候放入datatype
	 */
	private String period;

	/**
	 * 指令是否加入监控
	 */
	private boolean isMonitor = true;

	/**
	 * 指令结果是否全局使用
	 */
	private boolean isGlobalResult = false;

	private boolean isavailcmd = false;

	/**
	 * 指令对应的指标id集合
	 */
	private List<String> metrics;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public List<Param> getParam() {
		return param;
	}

	public void setParam(List<Param> param) {
		this.param = param;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public String getResulttype() {
		return resulttype;
	}

	public void setResulttype(String resulttype) {
		this.resulttype = resulttype;
	}

	public String getSupport() {
		return support;
	}

	public void setSupport(String support) {
		this.support = support;
	}

	public String getStorage() {
		return storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getRetry() {
		return retry;
	}

	public void setRetry(String retry) {
		this.retry = retry;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public boolean getIsavailcmd() {
		return isavailcmd;
	}

	public void setIsavailcmd(boolean isavailcmd) {
		this.isavailcmd = isavailcmd;
	}

	public boolean getIsMonitor() {
		return isMonitor;
	}

	public void setIsMonitor(boolean isMonitor) {
		this.isMonitor = isMonitor;
	}

	/**
	 * @return the isGlobalResult
	 */
	public boolean getIsGlobalResult() {
		return isGlobalResult;
	}

	/**
	 * @param isGlobalResult the isGlobalResult to set
	 */
	public void setIsGlobalResult(boolean isGlobalResult) {
		this.isGlobalResult = isGlobalResult;
	}

	public List<String> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<String> metrics) {
		this.metrics = metrics;
	}
}
