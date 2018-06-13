package org.coredata.core.framework.util;

import org.springframework.expression.ParserContext;

public class InstanceParserContext implements ParserContext {

	@Override
	public boolean isTemplate() {
		return true;
	}

	@Override
	public String getExpressionPrefix() {
		return "${";
	}

	@Override
	public String getExpressionSuffix() {
		return "}";
	}

}
