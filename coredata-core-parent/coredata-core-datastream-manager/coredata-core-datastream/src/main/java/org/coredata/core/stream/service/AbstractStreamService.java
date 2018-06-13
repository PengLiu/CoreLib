package org.coredata.core.stream.service;

import java.util.List;
import java.util.Map;

import org.coredata.core.stream.mining.entity.MetricInfo;
import org.coredata.core.stream.mining.entity.MiningData;
import org.coredata.core.stream.transform.filters.FilterChain;
import org.coredata.core.stream.vo.PrepareMiningData;
import org.coredata.core.stream.vo.TransformData;

public class AbstractStreamService implements StreamService {

	/**
	 * 用于进行清洗的方法
	 * @param chain
	 * @param source
	 * @return
	 * @throws Throwable
	 */
	@Override
	public String processTransform(FilterChain chain, Map<String, Object> source) throws Throwable {
		return null;
	}

	/**
	 * 该方法用于挖掘前的数据准备工作
	 * @param datas
	 * @param transform
	 * @return
	 */
	@Override
	public List<MiningData> process(List<PrepareMiningData> datas, TransformData transform) {
		return null;
	}

	@Override
	public void setScale(MetricInfo info) {

	}

}
