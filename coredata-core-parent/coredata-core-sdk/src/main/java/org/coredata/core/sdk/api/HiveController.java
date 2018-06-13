package org.coredata.core.sdk.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.entities.DataImportJob;
import org.coredata.core.data.entities.SqlModel;
import org.coredata.core.data.service.HdfsService;
import org.coredata.core.data.service.HiveService;
import org.coredata.core.data.vo.HDFSMeta;
import org.coredata.core.data.vo.TableMeta;
import org.coredata.core.data.writers.hdfs.HDFSWriterProperties;
import org.coredata.core.sdk.api.common.ResponseMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

/**
 * 封装 Hive 相关操作
 * 
 * @author liupeng
 *
 */
@RestController
@RequestMapping("/api/v1/hive")
public class HiveController {

	@Autowired
	private HiveService hiveService;

	@Autowired
	private HdfsService hdfsService;

	/**
	 * hive显示数据库列表
	 * 
	 * @param job
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/showdbs", method = RequestMethod.GET)
	public ResponseMap showdbs() {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			Collection<String> records = hiveService.showDbs();
			result.setResult(records);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("showdbs：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * hive显示数据库下的所有表
	 * 
	 * @param job
	 * @return
	 */
	@RequestMapping(path = "/tables/{db}", method = RequestMethod.GET)
	public ResponseMap findTable(@PathVariable String db) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			Collection<String> records = hiveService.showTables(db);
			result.setResult(records);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("hivetables：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * hive显示数据库下的所有表
	 * 
	 * @param job
	 * @return
	 */
	@RequestMapping(path = "/alltables/", method = RequestMethod.GET)
	public ResponseMap findTable() {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();
			Collection<String> dbs = hiveService.showDbs();
			if (dbs != null) {
				for (String db : dbs) {
					Map<String, Object> infos = new HashMap<String, Object>();
					infos.put("name", db);
					try {
						Collection<String> tables = hiveService.showTables(db);
						if (tables != null && !tables.isEmpty()) {
							List<Map<String, String>> list = new ArrayList<Map<String, String>>();
							for (String table : tables) {
								Map<String, String> map = new HashMap<String, String>();
								map.put("name", table);
								list.add(map);
							}
							infos.put("children", list);
						} else {
							infos.put("children", null);
						}
					} catch (Throwable e) {
						;
					}
					records.add(infos);
				}
			}
			result.setResult(records);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("alltables：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * hive显示表下的所有列信息
	 * 
	 * @param job
	 * @return
	 */
	@RequestMapping(path = "/columns/{db}/{table}", method = RequestMethod.GET)
	public ResponseMap findColumns(@PathVariable String db, @PathVariable String table) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			TableMeta records = hiveService.showColumns(table, db);
			result.setResult(records);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("hivecolumns：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * hive建表
	 * 
	 * @param job
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/createtable", method = RequestMethod.POST)
	public ResponseMap createHiveTable(@RequestBody DataImportJob job) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			if (!StringUtils.isEmpty(job.getTableMeta()) && !StringUtils.isEmpty(job.getWriteConfig())) {
				PluginConfig writeConfig = JSON.parseObject(job.getWriteConfig(), PluginConfig.class);
				String user = (String) writeConfig.get(HDFSWriterProperties.HADOOP_USER);
				HDFSMeta hdfsMeta = new HDFSMeta();
				hdfsMeta.setFieldSplitter("\\t");
				hdfsMeta.setLineSplitter("\\n");
				String path = hdfsService.getHdfsPath(user, job.getJobId());
				hdfsMeta.setPath(path);
				TableMeta tableMeta = JSON.parseObject(job.getTableMeta(), TableMeta.class);
				hiveService.createTable(tableMeta, hdfsMeta, true);
			}
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("createtable：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * hive显示表下的所有列信息
	 * 
	 * @param job
	 * @return
	 */
	@RequestMapping(path = "/runSql/{db}", method = RequestMethod.POST)
	public Object runSql(@PathVariable String db, @RequestBody SqlModel sql) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			return hiveService.runSql(sql.getContent(), db);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("runSql：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

}
