package org.coredata.core.sdk.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.coredata.core.ElasticsearchService;
import org.coredata.core.data.Constants;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.entities.DataImportJob;
import org.coredata.core.data.entities.DataSourceType;
import org.coredata.core.data.readers.crawler.CrawlerReaderProperties;
import org.coredata.core.data.readers.crawler.SpiderParam;
import org.coredata.core.data.readers.jdbc.ISQLBuilder;
import org.coredata.core.data.readers.jdbc.JDBCReaderProperties;
import org.coredata.core.data.readers.jdbc.SqlBuilderFactory;
import org.coredata.core.data.schedule.JobScheduler;
import org.coredata.core.data.schedule.Status;
import org.coredata.core.data.service.HdfsService;
import org.coredata.core.data.service.HiveService;
import org.coredata.core.data.service.ImportJobService;
import org.coredata.core.data.service.PluginService;
import org.coredata.core.data.util.JsonStringUtil;
import org.coredata.core.data.vo.HDFSMeta;
import org.coredata.core.data.vo.TableMeta;
import org.coredata.core.data.writers.elasticsearch.ElasticSearchProperties;
import org.coredata.core.data.writers.hdfs.HDFSWriterProperties;
import org.coredata.core.sdk.api.common.ResponseMap;
import org.coredata.core.sdk.api.util.ElasticsearchTemplateUtil;
import org.coredata.core.sdk.api.util.FileTool;
import org.coredata.core.vo.DataImportSearchCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;

@RestController
@RequestMapping("/api/v1/importor")
public class JobController {

	@Value("${spider.src}")
	private String spiderSrc;
	@Value("${spider.command}")
	private String spiderCommand;

	@Value("${temppath}")
	private String temppath;

	@Autowired
	private ImportJobService service;
	@Autowired
	private PluginService pluginService;
	@Autowired
	private HiveService hiveService;

	@Autowired
	private HdfsService hdfsService;

	@Autowired
	private ElasticsearchService elasticsearchService;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	// private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping(value = "/init", method = RequestMethod.POST)
	public ResponseMap initJobs(@RequestBody DataImportJob job) throws Exception {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			pluginService.initScheduleJobs();
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job insert：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(value = "/insert", method = RequestMethod.POST)
	public ResponseMap create(@RequestBody DataImportJob job) {
		ResponseMap result = ResponseMap.SuccessInstance();
		DataImportJob record = null;
		try {
			// if (JsonStringUtil.isEmpty(job.getWriteConfig())) {
			// record = service.saveJob(job);
			// // 生成job后设置默认输出源
			// PluginConfig writeConfig =
			// hdfsService.getDefaultHdfsPath(record.getCreator(), record.getJobId());
			// String writeDef = JSON.toJSONString(writeConfig);
			// record.setWriteConfig(writeDef);
			// }
			record = service.saveJob(job);
			result.setResult(record);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job insert：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public ResponseMap update(@RequestBody DataImportJob job) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			DataImportJob record = service.saveJob(job);
			// pluginService.runSchedule(job);
			result.setResult(record);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job update：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(value = "/count", method = RequestMethod.POST)
	public ResponseMap findAll() {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			result.setResult(service.count());
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job count：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/find/{jobId}", method = RequestMethod.POST)
	public ResponseMap find(@PathVariable String jobId) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			DataImportJob record = service.findById(jobId);

			// 设置job状态
			Status status = JobScheduler.getStatus().get(jobId);
			if (status != null) {
				record.setStatus(status);
			}
			result.setResult(record);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job find：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/findall/{page}/{pageSize}", method = RequestMethod.POST)
	public ResponseMap findAll(@PathVariable int page, @PathVariable int pageSize,
			@RequestBody DataImportSearchCondition jobParam) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {

			Iterable<DataImportJob> jobs = service.findAllJobs(jobParam.getName(), jobParam.getDataSourceType(),
					jobParam.getType(), jobParam.getToken(), page, pageSize);
			if (jobs != null) {
				// 设置job状态
				for (Iterator<DataImportJob> iterator = jobs.iterator(); iterator.hasNext();) {
					DataImportJob job = iterator.next();
					Status status = JobScheduler.getStatus().get(job.getJobId());
					if (status != null) {
						job.setStatus(status);
					}
				}
			}
			result.setResult(jobs);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job findall：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/remove/{jobId}", method = RequestMethod.POST)
	public ResponseMap remove(@PathVariable String jobId) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			DataImportJob job = service.findById(jobId);
			String writeConfigDef = job.getWriteConfig();
			String tableMetaDef = job.getTableMeta();
			service.removeJob(jobId);
			JobScheduler.getInstance().deleteJob(jobId);
			DataSourceType target = job.getDataTargetType();
			if (!StringUtils.isEmpty(writeConfigDef)) {
				PluginConfig writeConfig = JSON.parseObject(writeConfigDef, PluginConfig.class);

				if (target.equals(DataSourceType.esWriter)) {
					String indexName = writeConfig.getString(ElasticSearchProperties.INDEX_NAME);
					if (!StringUtils.isEmpty(indexName)) {
						elasticsearchTemplate.deleteIndex(indexName + "_*");
					}
				} else if (target.equals(DataSourceType.hdfsWriter)) {
					String path = writeConfig.getString(HDFSWriterProperties.PATH);
					if (!StringUtils.isEmpty(path)) {
						hdfsService.deleteWithFullPath(path);
					}
					if (!StringUtils.isEmpty(tableMetaDef)) {
						TableMeta tableMeta = JSON.parseObject(tableMetaDef, TableMeta.class);
						hiveService.dropTable(tableMeta);
					}
				}
			}
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job remove：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(value = "/removeall", method = RequestMethod.POST)
	public ResponseMap removeAll(@RequestBody List<DataImportJob> jobs) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			List<String> indexs = new ArrayList<>();
			for (DataImportJob job : jobs) {
				DataSourceType target = job.getDataTargetType();
				String writeConfigDef = job.getWriteConfig();
				if (!StringUtils.isEmpty(writeConfigDef)) {
					PluginConfig writeConfig = JSON.parseObject(writeConfigDef, PluginConfig.class);
					if (target.equals(DataSourceType.esWriter)) {
						String indexName = writeConfig.getString(ElasticSearchProperties.INDEX_NAME);
						if (!StringUtils.isEmpty(indexName)) {
							indexs.add(indexName);
						}
					}
				}
			}
			service.removeAllJob(jobs);
			for (String indexName : indexs) {
				elasticsearchTemplate.deleteIndex(indexName + "_*");
			}
			List<String> jobIds = new ArrayList<String>();
			// 删除定时任务
			for (DataImportJob job : jobs) {
				jobIds.add(job.getJobId());
			}
			JobScheduler.getInstance().deleteJob(jobIds);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job removeAll：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 接收上传的模型文件
	 * 
	 * @param jobId
	 * @param requestEntity
	 * @param response
	 * @return
	 */
	@RequestMapping(path = "/upload/{jobId}", method = RequestMethod.POST)
	public ResponseMap upload(@PathVariable String jobId, @RequestParam("file") MultipartFile file,
			HttpServletResponse response) {
		ResponseMap result = ResponseMap.SuccessInstance();

		try {
			if (temppath.lastIndexOf("/") + 1 != temppath.length()) {
				temppath = temppath + "/";
			}
			String name = file.getOriginalFilename();
			String suffix = "";
			if (!StringUtils.isEmpty(name) && name.indexOf(".") != -1) {
				suffix = name.substring(name.lastIndexOf("."), name.length());
			}
			byte[] data = file.getBytes();
			String path = temppath + jobId;
			if (!StringUtils.isEmpty(suffix)) {
				path = path + suffix;
			}
			FileTool.writeFile(path, data);
			DataImportJob job = service.findById(jobId);
			String readConfigDef = job.getReadConfig();
			PluginConfig readerConfig = null;
			if (JsonStringUtil.isNotEmpty(readConfigDef)) {
				readerConfig = JSON.parseObject(readConfigDef, PluginConfig.class);
				readerConfig.put("path", path);
			} else {
				readerConfig = new PluginConfig();
				readerConfig.put("path", path);
			}
			String readerDef = JSON.toJSONString(readerConfig);
			job.setReadConfig(JSON.toJSONString(readerConfig));
			service.saveJob(job);
			result.setResult(readerDef);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job upload file：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 接收上传的模型文件
	 * 
	 * @param modelType
	 * @param requestEntity
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(path = "/getmetadata/{dofilter}", method = RequestMethod.POST)
	public ResponseMap getmetadata(@PathVariable boolean dofilter, @RequestBody DataImportJob job) {

		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			service.saveJob(job);
			String readerDef = job.getReadConfig();
			PluginConfig readerConfig = JSON.parseObject(readerDef, PluginConfig.class);
			String type = readerConfig.getString(CrawlerReaderProperties.HTTPTYPE);
			if (type.equals(CrawlerReaderProperties.SPIDER)) {
				// 网络爬虫需要先根据参数获取py文件内容,然后请求抓取
				String spiderDef = readerConfig.getString(CrawlerReaderProperties.SPIDERDEF);
				SpiderParam spiderParam = JSON.parseObject(spiderDef, SpiderParam.class);
				String param = getSpider(JSON.toJSONString(spiderParam));
				if (!StringUtils.isEmpty(param)) {
					readerConfig.put(CrawlerReaderProperties.PARAM, param);
					readerConfig.put(CrawlerReaderProperties.URL, spiderSrc);
					readerConfig.put(CrawlerReaderProperties.CONTENTTYPE, "text/plain");
					// 保存爬虫的param参数
					job.setReadConfig(JSON.toJSONString(readerConfig));
					service.saveJob(job);
				}
			}
			// 数据预览设置预览条数
			readerConfig.put(Constants.RECORD_LIMIT, 10);
			if (job.getDataSourceType().equals(DataSourceType.jdbcReader)) {
				// 数据预览时设置为10,默认取10条
				readerConfig.put(JDBCReaderProperties.MAX_SIZE_PER_FETCH, 10);
				// JDBC数据库查询库,表,表列时去除检查更新列干扰
				removeDisturb(readerConfig);
			}
			// 预览数据时要加入过滤器,显示过滤结果,显示原始数据不需要
			if (!dofilter) {
				readerConfig.remove(Constants.FILTER_CONFIG);
			}
			job.setReadConfig(JSON.toJSONString(readerConfig));
			JobDetail detail = pluginService.getData(job);
			result.setResult(detail);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job getmetadata：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 根据spider定义获取参数
	 * 
	 * @param json
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	private String getSpider(String json) throws IOException {
		CloseableHttpClient client = null;
		try {
			CookieStore cookieStore = new BasicCookieStore();
			// 配置超时时间（连接服务端超时1秒，请求数据返回超时2秒）
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(180000)
					.setConnectionRequestTimeout(180000).build();
			// 设置默认跳转以及存储cookie
			client = HttpClientBuilder.create().setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
					.setRedirectStrategy(new DefaultRedirectStrategy()).setDefaultRequestConfig(requestConfig)
					.setDefaultCookieStore(cookieStore).build();
			String result = post(client, spiderCommand, json, "application/json");
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	private String post(CloseableHttpClient client, String url, String json, String contentType) {
		HttpPost post = new HttpPost(url);
		CloseableHttpResponse res = null;
		try {
			StringEntity se = new StringEntity(json, Charset.forName("UTF-8"));
			se.setContentType(contentType);
			post.setEntity(se);
			res = client.execute(post);
			if (res != null) {
				if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					return null;
				} else {
					String result = EntityUtils.toString(res.getEntity(), "utf-8");
					return result;
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (res != null) {
				try {
					res.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (post != null) {
				post.releaseConnection();
			}
		}
		return null;
	}

	/**
	 * 接收上传的模型文件
	 * 
	 * @param modelType
	 * @param requestEntity
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/importdata", method = RequestMethod.POST)
	public ResponseMap importdata(@RequestBody DataImportJob job) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			if (JsonStringUtil.isNotEmpty(job.getTableMeta()) && JsonStringUtil.isNotEmpty(job.getWriteConfig())) {
				TableMeta tableMeta = JSON.parseObject(job.getTableMeta(), TableMeta.class);
				if (tableMeta.isNotEmpty()) {
					DataSourceType target = job.getDataTargetType();
					PluginConfig writeConfig = JSON.parseObject(job.getWriteConfig(), PluginConfig.class);
					if (target.equals(DataSourceType.esWriter)) {
						String indexName = writeConfig.getString(ElasticSearchProperties.INDEX_NAME);
						job.setIndexName(indexName);
						String template = ElasticsearchTemplateUtil.getEsTempalte(tableMeta);
						writeConfig.put(ElasticSearchProperties.INDEX_TEMPLATE, template);
						job.setWriteConfig(JSON.toJSONString(writeConfig));
					
						
					} else if (target.equals(DataSourceType.hdfsWriter)) {
						// String user = (String) writeConfig.get(HDFSReaderProperties.HADOOP_USER);
						HDFSMeta hdfsMeta = new HDFSMeta();
						hdfsMeta.setFieldSplitter("\\t");
						hdfsMeta.setLineSplitter("\\n");
						// String path = hdfsService.getHdfsPath(user, job.getJobId());
						String path = writeConfig.getString(HDFSWriterProperties.PATH);
						hdfsMeta.setPath(path);
						hiveService.createTable(tableMeta, hdfsMeta, true);
					}
				}
			}
			service.saveJob(job);
			pluginService.runSchedule(job);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job importdata：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * hive建表
	 * 
	 * @param job
	 * @return
	 */
	@RequestMapping(path = "/getwriter/hdfsWriter/{jobId}/{user}", method = RequestMethod.GET)
	public ResponseMap getHdfsWriterConfig(@PathVariable String jobId, @PathVariable String user) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			DataImportJob job = service.findById(jobId);
			String def = job.getWriteConfig();
			if (JsonStringUtil.isNotEmpty(def)) {
				PluginConfig writeConfig = JSON.parseObject(def, PluginConfig.class);
				if (writeConfig.get(HDFSWriterProperties.HDFS_URI) != null) {
					// 如果已经存在hdfs设置，返回job中的信息
					result.setResult(JSON.toJSONString(writeConfig));
				}
			}
			if (result.getResult() == null) {
				String writeConfigDef = JSON.toJSONString(hdfsService.getDefaultHdfsPath(user, jobId));
				result.setResult(writeConfigDef);
			}
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job getwriter：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * hive建表
	 * 
	 * @param job
	 * @return
	 */
	@RequestMapping(path = "/getwriter/esWriter/{jobId}/{user}", method = RequestMethod.GET)
	public ResponseMap getEsWriterConfig(@PathVariable String jobId, @PathVariable String user) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			DataImportJob job = service.findById(jobId);
			String def = job.getWriteConfig();
			if (JsonStringUtil.isNotEmpty(def)) {
				PluginConfig writeConfig = JSON.parseObject(def, PluginConfig.class);
				if (writeConfig.get(ElasticSearchProperties.CLUSTER_ADDR) != null) {
					// 如果已经存在es设置，返回job中的信息
					result.setResult(JSON.toJSONString(writeConfig));
				}
			}
			if (result.getResult() == null) {
				PluginConfig writeConfig = new PluginConfig();
				String esAddresses = elasticsearchService.getClusterNodes();
				String[] addrs = esAddresses.split(",");
				String esUrl = addrs[0].split(":")[0] + ":9200";
				writeConfig.put(ElasticSearchProperties.CLUSTER_ADDR, esUrl);
				result.setResult(JSON.toJSONString(writeConfig));
			}
		} catch (

		Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job getwriter：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/getdbs/{type}", method = RequestMethod.POST)
	public ResponseMap getdbs(@PathVariable String type, @RequestBody DataImportJob job) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			String readConfigDef = job.getReadConfig();
			if (JsonStringUtil.isNotEmpty(readConfigDef) && job.getDataSourceType() != null) {
				PluginConfig readerConfig = JSON.parseObject(readConfigDef, PluginConfig.class);
				ISQLBuilder builder = SqlBuilderFactory.getBuilder(type);
				if (builder != null) {
					readerConfig.put("sql", builder.buildShowDbsSql());
					// JDBC数据库查询库,表,表列时去除检查更新列干扰
					removeDisturbWithFilters(readerConfig);
					job.setReadConfig(JSON.toJSONString(readerConfig));
					JobDetail records = pluginService.getData(job);
					result.setResult(records);
				} else {
					throw new Exception("readConfig is null or not datasource type supported");
				}
			} else {
				throw new Exception("datasource can not be null");
			}
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job getdbs：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/getables/{type}/{db}", method = RequestMethod.POST)
	public ResponseMap getables(@PathVariable String type, @PathVariable String db, @RequestBody DataImportJob job)
			throws Exception {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			String readConfigDef = job.getReadConfig();
			if (JsonStringUtil.isNotEmpty(readConfigDef) && job.getDataSourceType() != null) {
				PluginConfig readerConfig = JSON.parseObject(readConfigDef, PluginConfig.class);
				ISQLBuilder builder = SqlBuilderFactory.getBuilder(type);
				if (builder != null) {
					readerConfig.put("sql", builder.buildShowTablesSql(db));
					// JDBC数据库查询库,表,表列时去除检查更新列干扰
					removeDisturbWithFilters(readerConfig);
					job.setReadConfig(JSON.toJSONString(readerConfig));
					JobDetail records = pluginService.getData(job);
					result.setResult(records);
				} else {
					throw new Exception("readConfig is null or not datasource type supported");
				}
			} else {
				throw new Exception("datasource can not be null");
			}
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job getables：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/getcolumns/{type}/{db}/{table}", method = RequestMethod.POST)
	public ResponseMap getcolumns(@PathVariable String type, @PathVariable String db, @PathVariable String table,
			@RequestBody DataImportJob job) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			String readConfigDef = job.getReadConfig();
			if (JsonStringUtil.isNotEmpty(readConfigDef) && job.getDataSourceType() != null) {
				PluginConfig readerConfig = JSON.parseObject(readConfigDef, PluginConfig.class);
				ISQLBuilder builder = SqlBuilderFactory.getBuilder(type);
				if (builder != null) {
					readerConfig.put("sql", builder.buildShowColumnsSql(db, table));
					// JDBC数据库查询库,表,表列时去除检查更新列干扰
					removeDisturbWithFilters(readerConfig);
					job.setReadConfig(JSON.toJSONString(readerConfig));
					JobDetail records = pluginService.getData(job);
					result.setResult(records);
				} else {
					throw new Exception("readConfig is null or not datasource type supported");
				}
			} else {
				throw new Exception("datasource can not be null");
			}
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job getcolumns：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	private void removeDisturb(PluginConfig readerConfig) {
		readerConfig.remove(JDBCReaderProperties.CHECK_COLUMN);
		readerConfig.remove(JDBCReaderProperties.LAST_VAL);
	}

	private void removeDisturbWithFilters(PluginConfig readerConfig) {
		readerConfig.remove(JDBCReaderProperties.CHECK_COLUMN);
		readerConfig.remove(JDBCReaderProperties.LAST_VAL);
		readerConfig.remove(Constants.FILTER_CONFIG);
	}

	@RequestMapping(path = "/start/{jobId}", method = RequestMethod.POST)
	public ResponseMap start(@PathVariable String jobId) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			DataImportJob job = service.findById(jobId);
			pluginService.runSchedule(job);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job start：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/stop/{jobId}", method = RequestMethod.POST)
	public ResponseMap stop(@PathVariable String jobId) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			JobScheduler.getInstance().deleteJob(jobId);
			DataImportJob job = service.findById(jobId);
			// 设置job状态
			if (job != null) {
				job.setStatus(Status.stop);
				result.setResult(job);
			}
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job stop：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(path = "/findrun/{page}/{pageSize}", method = RequestMethod.POST)
	public ResponseMap findrun(@PathVariable int page, @PathVariable int pageSize,
			@RequestBody DataImportSearchCondition jobParam) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {

			Iterable<DataImportJob> jobs = service.findRun(jobParam.getName(), jobParam.getDataSourceType(),
					jobParam.getType(), jobParam.getToken(), page, pageSize);
			if (jobs != null) {
				// 设置job状态
				for (Iterator<DataImportJob> iterator = jobs.iterator(); iterator.hasNext();) {
					DataImportJob job = iterator.next();
					Status status = JobScheduler.getStatus().get(job.getJobId());
					if (status != null) {
						job.setStatus(status);
					}
				}
			}
			result.setResult(jobs);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job findall：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
}
// @RequestMapping(path = "/pause/{jobId}", method = RequestMethod.POST)
// public ResponseMap pause(@PathVariable String jobId){
// try {
// JobScheduler.getInstance().pauseJob(jobId);
// } catch (Exception e) {
// throw e;
// }
// }
//
// @RequestMapping(path = "/resume/{jobId}", method = RequestMethod.POST)
// public ResponseMap resume(@PathVariable String jobId){
// try {
// JobScheduler.getInstance().resumeJob(jobId);
// } catch (Exception e) {
// throw e;
// }
// }