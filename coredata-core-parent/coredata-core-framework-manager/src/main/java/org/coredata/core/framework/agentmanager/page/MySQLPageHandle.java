package org.coredata.core.framework.agentmanager.page;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * 该类用于mysql数据库分页方式
 * @author sushi
 *
 */
@Component
public class MySQLPageHandle implements PageHandle {

	private Logger logger = Logger.getLogger(MySQLPageHandle.class);

	@Override
	public String handlePagingSQL(String sql, int pageNo, int limit) {
		StringBuilder pagingSql = new StringBuilder(sql);
		if (limit > 0) {
			int firstResult = (pageNo - 1) * limit;
			if (firstResult <= 0)
				pagingSql.append(" LIMIT 0,").append(limit);
			else
				pagingSql.append(" LIMIT ").append(firstResult).append(",").append(limit);
		}
		logger.info("------paging SQL------" + pagingSql);
		return pagingSql.toString();
	}

	@Override
	public String handleTotalCountSQL(String sql) {
		StringBuilder countSql = new StringBuilder("SELECT COUNT(*) ");
		int subIndex = sql.indexOf("FROM");
		if (subIndex < 0)
			subIndex = sql.indexOf("from");
		String subSql = sql.substring(subIndex);
		countSql.append(subSql);
		return countSql.toString();
	}

}
