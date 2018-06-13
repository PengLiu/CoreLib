package org.coredata.core.data.readers.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.coredata.core.data.OutputFieldsDeclarer;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Reader;
import org.coredata.core.data.RecordCollector;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.debugger.JobStatus;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service(value = "binaryReader")
@Scope("prototype")
public class BinaryReader extends Reader {

	private String host;

	private int port;
	private int minPort;
	private int maxPort;
	private String userName;

	private String password;

	private String dir;

	private boolean removeFile;

	private String hdfsPath;

	private String hdfsUser;

	private String hdfsUri;

	@Override
	public void prepare(PluginConfig readerConfig) {
		// binary no filter need
		host = readerConfig.getString(BinaryReaderProperties.HOST);
		port = readerConfig.getInt(BinaryReaderProperties.PORT, 21);
		minPort = readerConfig.getInt(BinaryReaderProperties.MINPORT, 8011);
		maxPort = readerConfig.getInt(BinaryReaderProperties.MAXPORT, 8050);
		userName = readerConfig.getString(BinaryReaderProperties.USER);
		password = readerConfig.getString(BinaryReaderProperties.PASSWORD);
		dir = readerConfig.getString(BinaryReaderProperties.DIR);
		if (StringUtils.isNotEmpty(dir)) {
			if (!dir.endsWith("/") && !dir.endsWith("\\")) {
				dir = dir + "/";
			}
		}
		removeFile = readerConfig.getBoolean(BinaryReaderProperties.REMOVE_FILE, false);
		hdfsUri = readerConfig.getString(BinaryReaderProperties.HDFS_URI);
		hdfsPath = readerConfig.getString(BinaryReaderProperties.HDFS_DIR);
		hdfsUser = readerConfig.getString(BinaryReaderProperties.HDFS_USER);
	}

	@Async
	@Override
	public CompletableFuture<JobDetail> execute(RecordCollector recordCollector) {

		FileSystem fs = null;
		FTPClient client = null;
		FTPFile[] files = null;
		StringBuffer sb = new StringBuffer();
		sb.append("BinaryReader host:").append(host).append(" port:").append(port).append(" username:").append(userName).append(" password:").append(password)
				.append(" dir:").append(dir).append(" hdfsUri:").append(hdfsUri);

		try {
			Configuration conf = new Configuration();
			conf.set("fs.default.name", hdfsUri);
			fs = FileSystem.get(new URI(hdfsUri), conf, hdfsUser);
			client = FTPUtils.getFtpClient(host, port, userName, password, minPort, maxPort);
			client.enterLocalActiveMode();
			files = client.listFiles(dir);

			if (files != null && files.length > 0) {
				sb.append(" files.length:").append(files.length);
				for (FTPFile file : files) {
					if (file.isDirectory()) {
						System.out.println(file.getName());
					}
					OutputStream os = null;
					InputStream is = null;
					try {
						is = client.retrieveFileStream(dir + file.getName());
						if (is != null) {
							Path path = new Path(hdfsPath + "/" + file.getName());
							os = fs.create(path);
							IOUtils.copy(is, os);
						}
						if (removeFile) {
							// 不保留源文件
							client.deleteFile(dir + file.getName());
						}
					} catch (IllegalArgumentException | IOException e) {
						throw e;
					} finally {
						try {
							if (os != null) {
								os.close();
							}
						} catch (Exception e) {
							;
						}
						try {
							if (os != null) {
								os.close();
							}
						} catch (Exception e) {
							;
						}
					}
				}
			}
			jobDetail.setStatus(JobStatus.Success);
		} catch (Exception e1) {
			sb.append(" binary error:");
			logger.error(sb.toString(), e1);
			jobDetail.setStatus(JobStatus.ReaderErr);
		} finally {
			sb.append(" finished");
			logger.error(sb.toString());
			try {
				if (client != null) {
					client.logout();
					client.disconnect();
				}
			} catch (Exception e) {
				;
			}
		}

		return CompletableFuture.completedFuture(jobDetail);

	}

	@Override
	public void close() {

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

	}

}