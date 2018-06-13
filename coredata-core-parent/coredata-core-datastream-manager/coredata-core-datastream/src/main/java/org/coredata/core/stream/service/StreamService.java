package org.coredata.core.stream.service;

import java.util.List;
import java.util.Map;

import org.coredata.core.stream.mining.entity.MetricInfo;
import org.coredata.core.stream.mining.entity.MiningData;
import org.coredata.core.stream.transform.filters.FilterChain;
import org.coredata.core.stream.vo.PrepareMiningData;
import org.coredata.core.stream.vo.TransformData;

/**
 * stream相关处理的接口类，所有在stream中的service都通过这个接口转发
 * @author sue
 *
 */
public interface StreamService {

	/**
	 * 用于进行清洗的方法
	 * @param chain
	 * @param source
	 * @return
	 * @throws Throwable
	 */
	public String processTransform(FilterChain chain, Map<String, Object> source) throws Throwable;

	/**
	 * 该方法用于挖掘前的数据准备工作
	 * @param datas
	 * @param transform
	 * @return
	 */
	public List<MiningData> process(List<PrepareMiningData> datas, TransformData transform);

	/**
	 * 挖掘数据保存时小数设置
	 * @param info
	 */
	public void setScale(MetricInfo info);

}
