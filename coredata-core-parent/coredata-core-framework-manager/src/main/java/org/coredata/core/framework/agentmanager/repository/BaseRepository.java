package org.coredata.core.framework.agentmanager.repository;

import org.coredata.core.framework.agentmanager.page.PageHandle;
import org.coredata.core.framework.agentmanager.page.PageParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class BaseRepository  {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Resource
	private PageHandle pageHandle;

	
	public <T> List<T> find(String sql, Object[] params, Class<T> tClass) {
		List<T> resultList = null;
		if (params != null && params.length > 0)
			resultList = jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<T>(tClass));
		else
			resultList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<T>(tClass));
		return resultList;
	}

	
	public <T> PageParam<T> queryPagination(String sql, Object[] params, int pageNo, int limit, Class<T> tClass) {
		String pagingSql = pageHandle.handlePagingSQL(sql, pageNo, limit);
		Integer totalCount = 0;
		List<T> resultList = null;
		String countSql = pageHandle.handleTotalCountSQL(sql);
		if (params != null && params.length > 0) {
			totalCount = jdbcTemplate.queryForObject(countSql, params, Integer.class);
			resultList = jdbcTemplate.query(pagingSql, params, new BeanPropertyRowMapper<T>(tClass));
		} else {
			totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
			resultList = jdbcTemplate.query(pagingSql, new BeanPropertyRowMapper<T>(tClass));
		}
		PageParam<T> page = new PageParam<T>(pageNo, limit, totalCount, resultList);
		return page;
	}

	
	public <T> int addUpdateOrDelete(String sql, Object[] params) {
		int result = 0;
		if (params != null && params.length > 0) {
			result = jdbcTemplate.update(sql, new PreparedStatementSetter() {
				
				public void setValues(PreparedStatement ps) throws SQLException {
					for (int i = 0; i < params.length; i++) {
						ps.setObject(i + 1, params[i]);
					}
				}
			});
		} else
			result = jdbcTemplate.update(sql);
		return result;
	}

	
	public <T> int batchAddUpdateOrDelete(String sql, List<Object[]> params) {
		int result = 0;
		int[] batch = null;
		if (!CollectionUtils.isEmpty(params)) {
			batch = jdbcTemplate.batchUpdate(sql, params);
		} else
			batch = jdbcTemplate.batchUpdate(sql);
		for (int num : batch)
			result += num;
		return result;
	}

	
	public <T> T queryForOne(String sql, Object[] params, Class<T> tClass) {
		if (params != null && params.length > 0) {
			return jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<T>(tClass));
		}
		return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<T>(tClass));
	}

	
	public int queryForCount(String sql, Object[] params) {
		if (params != null && params.length > 0) {
			return jdbcTemplate.queryForObject(sql, params, Integer.class);
		}
		return jdbcTemplate.queryForObject(sql, Integer.class);
	}

	
	public <T> T queryForResult(String sql, Object[] params, Class<T> tClass) {
		if (params != null && params.length > 0) {
			return jdbcTemplate.queryForObject(sql, params, tClass);
		}
		return jdbcTemplate.queryForObject(sql, tClass);
	}

	
	public Map<String, Object> findForMap(String sql, Object[] params) {
		return jdbcTemplate.queryForMap(sql, params);
	}

	
	public List<Map<String, Object>> queryForMap(String sql, Object[] params) {
		return jdbcTemplate.queryForList(sql, params);
	}

	
	public Map<String, Object> queryForCustomMap(String sql, Object[] params, ResultSetExtractor<Map<String, Object>> extractor) {
		if (params != null && params.length > 0) {
			return jdbcTemplate.query(sql, params, extractor);
		}

		return jdbcTemplate.query(sql, extractor);
	}

	
	public <T> List<T> queryForList(String sql, Object[] params, Class<T> tClass) {
		List<T> resultList = null;
		if (params != null && params.length > 0)
			resultList = jdbcTemplate.queryForList(sql, tClass, params);
		else
			resultList = jdbcTemplate.queryForList(sql, tClass);
		return resultList;
	}

}
