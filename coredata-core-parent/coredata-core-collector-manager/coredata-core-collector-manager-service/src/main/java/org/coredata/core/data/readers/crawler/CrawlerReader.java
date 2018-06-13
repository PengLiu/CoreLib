package org.coredata.core.data.readers.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.coredata.core.data.DefaultRecord;
import org.coredata.core.data.OutputFieldsDeclarer;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Reader;
import org.coredata.core.data.Record;
import org.coredata.core.data.RecordCollector;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.debugger.JobStatus;
import org.coredata.core.data.exception.DataException;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service(value = "crawlerReader")
@Scope("prototype")
public class CrawlerReader extends Reader {

	private static CloseableHttpClient client = null;
	private String urlstr = null;
	private String encoding = null;
	private String contentType = null;
	private String httpType = null;
	private String param = null;
	private int startRow = 1;
	private static final Logger LOG = LogManager.getLogger(CrawlerReader.class);

	@Override
	public void prepare(PluginConfig readerConfig) {
		super.prepare(readerConfig);
		urlstr = readerConfig.getString(CrawlerReaderProperties.URL);
		encoding = readerConfig.getString(CrawlerReaderProperties.ENCODING, "UTF-8");
		httpType = readerConfig.getString(CrawlerReaderProperties.HTTPTYPE);
		contentType = readerConfig.getString(CrawlerReaderProperties.CONTENTTYPE);
		param = readerConfig.getString(CrawlerReaderProperties.PARAM);
		startRow = readerConfig.getInt(CrawlerReaderProperties.START_ROW, 1);
	}

	@Async
	@Override
	public CompletableFuture<JobDetail> execute(RecordCollector recordCollector) {
		try {
			if (StringUtils.isNotEmpty(param)) {
				String result = post(urlstr, param, contentType);
				if (httpType.equals(CrawlerReaderProperties.SPIDER)) {
					Gson gson = new Gson();
					List<SpiderResult> srs = gson.fromJson(result, new TypeToken<List<SpiderResult>>() {
					}.getType());
					if (srs != null) {
						for (SpiderResult sr : srs) {
							List<SpiderVal> fields = sr.getFields();
							if (fields != null && !fields.isEmpty()) {
								if (fields.get(0).getValues() != null) {
									int len = fields.get(0).getValues().size();

									if (startRow == 1) {
										Record record = new DefaultRecord(1);
										for (SpiderVal sv : fields) {
											record.add(sv.getName());
										}
										recordCollector.send(record);
									}
									for (int i = 0; i < len; i++) {
										Record record = new DefaultRecord(1);
										for (SpiderVal sv : fields) {
											record.add(sv.getValues().get(i));
										}
										recordCollector.send(record);
									}
								}
							}
						}
					}
				} else {
					Record record = new DefaultRecord(1);
					record.add(result);
					recordCollector.send(record);
				}
			} else {
				List<Record> records = connect();
				for (Record record : records) {
					doFilter(record);
					// get selected columns
					doSelect(record);
					recordCollector.send(record);
				}
			}
			jobDetail.setStatus(JobStatus.Success);
		} catch (Exception e) {
			logger.error("Crawler reader error.", e);
			jobDetail.setStatus(JobStatus.ReaderErr);
		}

		return CompletableFuture.completedFuture(jobDetail);
	}

	public String post(String url, String json, String contentType) {
		HttpPost post = new HttpPost(url);
		CloseableHttpResponse res = null;
		try {
			StringEntity se = new StringEntity(json, Charset.forName("UTF-8"));
			se.setContentType(contentType);
			post.setEntity(se);
			res = client.execute(post);
			if (res != null) {
				if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					LOG.error("no response from url:" + url);
				} else {
					String result = EntityUtils.toString(res.getEntity(), "utf-8");
					return result;
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			LOG.error(String.format("data send error:%s", e.getMessage()));
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

	public List<Record> connect() {
		List<Record> records = new ArrayList<Record>();
		try {
			URL url = new URL(urlstr);
			URLConnection connection = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("offset:")) {
					LOG.info(line);
				} else {
					Record record = new DefaultRecord(1);
					record.add(line);
					records.add(record);
				}
			}
			br.close();
		} catch (IOException e) {
			throw new DataException(e);
		}
		return records;
	}

	static {
		if (client == null) {
			init();
		}
	}

	private static void init() {
		CookieStore cookieStore = new BasicCookieStore();
		// 配置超时时间（连接服务端超时1秒，请求数据返回超时2秒）
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(180000).setConnectionRequestTimeout(180000).build();
		// 设置默认跳转以及存储cookie
		client = HttpClientBuilder.create().setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy()).setRedirectStrategy(new DefaultRedirectStrategy())
				.setDefaultRequestConfig(requestConfig).setDefaultCookieStore(cookieStore).build();
	}

	@Override
	public void close() {

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

	}

}