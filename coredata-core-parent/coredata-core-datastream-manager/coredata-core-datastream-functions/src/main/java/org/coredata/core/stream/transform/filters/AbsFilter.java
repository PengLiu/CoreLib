package org.coredata.core.stream.transform.filters;

import java.io.Serializable;

import org.coredata.core.stream.vo.TransformData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbsFilter implements Filter, Serializable {

	private static final long serialVersionUID = 4033340026664051633L;

	final Logger logger = LoggerFactory.getLogger(getClass());

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	protected void logError(Throwable e, TransformData transformData) {
		logger.error(" error " + e.getMessage() + ":" + transformData);
	}
}
