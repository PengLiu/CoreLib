package org.coredata.core.data.service;

import java.util.List;

import org.coredata.core.data.entities.SqlModel;
import org.coredata.core.data.repositories.SqlModelResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SqlModelService {

	@Autowired
	private SqlModelResp sqlResp;

	public long count() {
		return sqlResp.count();
	}

	public Page<SqlModel> findAll(int page, int pageSize) {
		Sort sort = new Sort(Direction.DESC, "createdTime");
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		return sqlResp.findAll(pageable);
	}

	public SqlModel findById(String id) {
		return sqlResp.findById(id).get();
	}

	@Transactional
	public SqlModel save(SqlModel model) {
		SqlModel result = sqlResp.save(model);
		return result;
	}

	@Transactional
	public void remove(SqlModel model) {
		sqlResp.delete(model);
	}

	@Transactional
	public void removeById(String id) {
		sqlResp.deleteById(id);
	}

	@Transactional
	public void removeAll(List<SqlModel> entities) {
		sqlResp.deleteAll(entities);
	}

}
