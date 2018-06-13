package org.coredata.core.data.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.coredata.core.data.entities.DataImportJob;
import org.coredata.core.data.entities.DataSourceType;
import org.coredata.core.data.repositories.ImportJobResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class ImportJobService {

	@Autowired
	private ImportJobResp jobResp;

	public long count() {
		return jobResp.count();
	}

	public Page<DataImportJob> findJobs(final String name, final List<String> dataSourceTypes, final List<String> types, String token, Pageable pageable) {
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		List<DataSourceType> ts = null;
		if (dataSourceTypes != null && !dataSourceTypes.isEmpty()) {
			ts = new ArrayList<DataSourceType>();
			for (String ds : dataSourceTypes) {
				if (DataSourceType.fromString(ds) != null) {
					ts.add(DataSourceType.fromString(ds));
				}
			}
		}

		String nameParam = "";
		if (name != null) {
			nameParam = name;
		}
		List<String> typeParam = null;
		if (types != null && !types.isEmpty()) {
			typeParam = types;
		}
		if (ts != null && typeParam != null) {
			return jobResp.findByTokenAndNameContainingAndDataSourceTypeInAndTypeIn(token, nameParam, ts, typeParam, pageable);
		} else if (ts != null) {
			return jobResp.findByTokenAndNameContainingAndDataSourceTypeIn(token, nameParam, ts, pageable);
		} else if (typeParam != null) {
			return jobResp.findByTokenAndNameContainingAndTypeIn(token, nameParam, typeParam, pageable);
		} else {
			return jobResp.findByTokenAndNameContaining(token, nameParam, pageable);
		}
	}

	public Page<DataImportJob> findAllJobs(final String name, final List<String> dataSourceTypes, final List<String> types, String token, int page, int pageSize) {
		Sort sort = new Sort(Direction.DESC, "createdTime");
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		return findJobs(name, dataSourceTypes, types, token, pageable);
	}

	public DataImportJob findById(String id) {
		Optional<DataImportJob> op = jobResp.findById(id);
		if (op.isPresent()) {
			return (DataImportJob) op.get();
		} else {
			return null;
		}
	}

	public Iterable<DataImportJob> findAll() {
		return jobResp.findAll();
	}

	@Transactional
	public DataImportJob saveJob(DataImportJob job) {
		DataImportJob result = (DataImportJob) jobResp.save(job);
		return result;
	}

	@Transactional
	public void removeJob(DataImportJob job) {
		jobResp.delete(job);
	}

	@Transactional
	public void removeJob(String id) {
		jobResp.deleteById(id);
	}

	@Transactional
	public void removeAllJob(List<DataImportJob> entities) {
		jobResp.deleteAll(entities);
	}

	
	public Page<DataImportJob> findRun(final String name, final List<String> dataSourceTypes, final List<String> types, String token, Pageable pageable) {
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		List<DataSourceType> ts = null;
		if (dataSourceTypes != null && !dataSourceTypes.isEmpty()) {
			ts = new ArrayList<DataSourceType>();
			for (String ds : dataSourceTypes) {
				if (DataSourceType.fromString(ds) != null) {
					ts.add(DataSourceType.fromString(ds));
				}
			}
		}

		String nameParam = "";
		if (name != null) {
			nameParam = name;
		}
		List<String> typeParam = null;
		if (types != null && !types.isEmpty()) {
			typeParam = types;
		}
		if (ts != null && typeParam != null) {
			return jobResp.findByTokenAndNameContainingAndDataSourceTypeInAndTypeInAndIndexNameIsNotNull(token, nameParam, ts, typeParam, pageable);
		} else if (ts != null) {
			return jobResp.findByTokenAndNameContainingAndDataSourceTypeInAndIndexNameIsNotNull(token, nameParam, ts, pageable);
		} else if (typeParam != null) {
			return jobResp.findByTokenAndNameContainingAndTypeInAndIndexNameIsNotNull(token, nameParam, typeParam, pageable);
		} else {
			return jobResp.findByTokenAndNameContainingAndIndexNameIsNotNull(token, nameParam, pageable);
		}
	}
	
	public Page<DataImportJob> findRun(final String name, final List<String> dataSourceTypes, final List<String> types, String token, int page, int pageSize) {
		Sort sort = new Sort(Direction.DESC, "createdTime");
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		return findRun(name, dataSourceTypes, types, token, pageable);
	}

}
