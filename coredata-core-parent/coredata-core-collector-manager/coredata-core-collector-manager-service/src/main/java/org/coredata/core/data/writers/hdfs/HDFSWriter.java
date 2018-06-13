package org.coredata.core.data.writers.hdfs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Record;
import org.coredata.core.data.Writer;
import org.coredata.core.data.exception.DataException;
import org.coredata.core.data.vo.TableMeta;
import org.coredata.core.data.writers.WriterProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

@Service(value = "hdfsWriter")
@Scope("prototype")
public class HDFSWriter extends Writer {

	private static final Logger LOGGER = LogManager.getLogger("HDFSWriter");
	private int writerParallelism;
	private String path;
	private String fieldsSeparator;
	private String lineSeparator;
	private String encoding;
	private String compressCodec;
	private BufferedWriter bw;
	private String[] strArray;
	private int fileNum = 1;
	private long maxFileBytesSize;
	private long writenBytesize;
	private long maxRecordSize;
	private long writenRecordSize;
	private long maxWaitTime;
	private long currentTime;
	private boolean appendMode = false;
	private boolean coverMode = false;
	private String partitionStrategy;
	private int currentWriterSequence;
	private String filePathWithoutExtension;
	private String fileExtension = "";
	private int dateIndex;
	private String dateFormat;
	private String lastDate;
	private SimpleDateFormat sdf;
	// 按天拆分文件
	private final SimpleDateFormat defaultdf = new SimpleDateFormat("yyyyMMdd");
	// 按小时拆分文件
	private final SimpleDateFormat hourlydf = new SimpleDateFormat("yyyyMMddHH");
	private Configuration conf;
	private PluginConfig writerConfig;
	private static AtomicInteger sequence = new AtomicInteger(0);
	private static final Pattern REG_FILE_PATH_WITHOUT_EXTENSION = Pattern.compile(".*?(?=(\\.\\w+)?$)");
	private static final Pattern REG_FILE_EXTENSION = Pattern.compile("(\\.\\w+)$");

	@Override
	public void prepare(PluginConfig writerConfig, TableMeta tableMeta, String token) {
		this.writerConfig = writerConfig;
		writerParallelism = writerConfig.getParallelism();
		path = writerConfig.getString(HDFSWriterProperties.PATH);
		Preconditions.checkNotNull(path, "HDFS writer required property: path");

		fieldsSeparator = StringEscapeUtils.unescapeJava(writerConfig.getString(HDFSWriterProperties.FIELDS_SEPARATOR, "\t"));
		lineSeparator = StringEscapeUtils.unescapeJava(writerConfig.getString(HDFSWriterProperties.LINE_SEPARATOR, "\n"));
		encoding = writerConfig.getString(HDFSWriterProperties.ENCODING, "UTF-8");
		compressCodec = writerConfig.getProperty(HDFSWriterProperties.COMPRESS_CODEC);
		maxFileBytesSize = writerConfig.getLong(HDFSWriterProperties.MAX_FILE_SIZE_MB, 0) * 1024 * 1024;
		maxRecordSize = writerConfig.getLong(HDFSWriterProperties.MAX_RECORDS_SIZE, 0);
		maxWaitTime = writerConfig.getLong(HDFSWriterProperties.MAX_WAITTIME, 0);
		dateIndex = writerConfig.getInt(HDFSWriterProperties.PARTITION_DATE_INDEX, -1);
		dateFormat = writerConfig.getString(HDFSWriterProperties.PARTITIONED_DATE_FORMAT);
		appendMode = writerConfig.getBoolean(WriterProperties.WRITE_MODE, false);
		// coverMode = writerConfig.getBoolean(HDFSWriterProperties.COVER_MODE, false);
		coverMode = !appendMode;
		partitionStrategy = writerConfig.getString(HDFSWriterProperties.PARTITIONED_STRATEGY, "");

		String hadoopUser = writerConfig.getString(HDFSWriterProperties.HADOOP_USER);
		if (hadoopUser != null) {
			System.setProperty("HADOOP_USER_NAME", hadoopUser);
		}
		Matcher m1 = REG_FILE_PATH_WITHOUT_EXTENSION.matcher(path.trim());
		if (m1.find()) {
			filePathWithoutExtension = m1.group();
		}
		Matcher m2 = REG_FILE_EXTENSION.matcher(path.trim());
		if (m2.find()) {
			fileExtension = m2.group();
		}
		currentWriterSequence = sequence.getAndIncrement();
	}

	@Override
	public void execute(Record record) {
		try {
			if (StringUtils.isNotEmpty(dateFormat) && dateIndex >= 0) {

				if (sdf == null) {
					sdf = new SimpleDateFormat(dateFormat);
				}
				try {
					String currentDate = defaultdf.format(sdf.parse(record.get(dateIndex).toString()));
					if (lastDate == null) {
						lastDate = currentDate;
					} else if (!currentDate.equals(lastDate)) {
						bw.close();
						fileNum = 0;
						writenBytesize = 0;
						lastDate = currentDate;
						// 时间间隔已过，新建文件无需append
						bw = createBufferedWriter(createFilePath(fileNum), false);
					}
				} catch (ParseException e) {
					throw new DataException(e);
				} catch (IOException e) {
					throw new DataException(e);
				}
			} else if (!StringUtils.isEmpty(partitionStrategy)) {
				// 默认是每天一个文件
				String currentDate = null;
				switch (partitionStrategy.toLowerCase()) {
				case "hourly":
					currentDate = hourlydf.format(new Date(System.currentTimeMillis()));
					break;
				default:
					currentDate = defaultdf.format(new Date(System.currentTimeMillis()));
					break;
				}
				try {
					if (lastDate == null) {
						lastDate = currentDate;
					} else if (!currentDate.equals(lastDate)) {
						bw.close();
						fileNum = 0;
						writenBytesize = 0;
						lastDate = currentDate;
						// 时间间隔已过，新建文件无需append
						bw = createBufferedWriter(createFilePath(fileNum), false);
					}
				} catch (IOException e) {
					throw new DataException(e);
				}
			}
			if (bw == null) {
				conf = new Configuration();
				// 第一次写文件
				Path hdfsPath = createFilePath(fileNum);
				// file num 移动到最后
				try {
					moveToLastFileNum(hdfsPath);
					hdfsPath = createFilePath(fileNum);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if (writerConfig.containsKey(HDFSWriterProperties.HDFS_CONF_PATH)) {
					for (String path : writerConfig.getString(HDFSWriterProperties.HDFS_CONF_PATH).split(",")) {
						conf.addResource(new Path("file://" + path));
					}
				}
				if (coverMode) {
					// 覆盖现有文件
					try {
						Path existPath = new Path(filePathWithoutExtension);
						FileSystem fs = existPath.getFileSystem(conf);
						fs.delete(existPath, true);
					} catch (IllegalArgumentException | IOException e) {
						throw new DataException(e);
					}
				}
				try {
					bw = createBufferedWriter(hdfsPath, appendMode);
				} catch (Exception e) {
					// handler冲突,需要关闭fs重新建立连接
					close();
					try {
						FileSystem fs = hdfsPath.getFileSystem(conf);
						if (fs != null) {
							fs.close();
						}
					} catch (Exception e1) {
						;
					}
					throw new DataException(e);
				}
			}
			if (strArray == null) {
				strArray = new String[record.size()];
			}

			for (int i = 0, len = record.size(); i < len; i++) {
				Object o = record.get(i);
				if (o == null) {
					strArray[i] = "NULL";
				} else {
					strArray[i] = o.toString();
				}
			}
			try {
				String line = Joiner.on(fieldsSeparator).join(strArray) + lineSeparator;
				if (maxWaitTime > 0 && ((System.currentTimeMillis() - currentTime) / 1000 >= maxWaitTime)) {
					bw.close();
					bw = createBufferedWriter(createFilePath(fileNum), appendMode);
					writenBytesize = 0;
					writenRecordSize = 0;
				}
				if (maxFileBytesSize > 0) {
					writenBytesize += line.getBytes(encoding).length;
					if (writenBytesize >= maxFileBytesSize) {
						bw.close();
						bw = createBufferedWriter(createFilePath(fileNum), appendMode);
						writenBytesize = 0;
						writenRecordSize = 0;
					}
				}
				if (maxRecordSize > 0) {
					writenRecordSize++;
					if (writenRecordSize > maxRecordSize) {
						bw.close();
						bw = createBufferedWriter(createFilePath(fileNum), appendMode);
						writenBytesize = 0;
						writenRecordSize = 0;
					}
				}
				bw.write(line);
			} catch (IOException e) {
				throw new DataException(e);
			}
		} catch (Exception e) {
			StringBuffer sb = new StringBuffer();
			sb.append(this.path).append("has error:");
			//			if(e!=null) {
			//				if(e.getCause()!=null) {
			//					sb.append(e.getCause().getCause().toString());
			//				}
			//			}
			LOGGER.error(sb.toString());
			throw new DataException(e);
		}
	}

	@Override
	public void close() {
		if (bw != null) {
			try {
				bw.close();
			} catch (IOException e) {
				//				throw new HDataException(e);
				e.printStackTrace();
			}
		}
		// if (fs != null) {
		// try {
		// fs.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
	}

	// private FileSystem fs;

	private Path createFilePath(int fileNum) {
		String path = null;
		if (dateIndex >= 0 && StringUtils.isNotEmpty(dateFormat) && writerParallelism > 1 && maxFileBytesSize > 0) {
			path = String.format("%s%s_%05d_%05d%s", filePathWithoutExtension, lastDate, currentWriterSequence, fileNum, fileExtension);
		} else if (dateIndex >= 0 && StringUtils.isNotEmpty(dateFormat) && writerParallelism > 1) {
			path = String.format("%s%s_%05d%s", filePathWithoutExtension, lastDate, currentWriterSequence, fileExtension);
		} else if (dateIndex >= 0 && StringUtils.isNotEmpty(dateFormat) && maxFileBytesSize > 0) {
			path = String.format("%s%s_%05d%s", filePathWithoutExtension, lastDate, fileNum, fileExtension);
		} else if (dateIndex >= 0 && StringUtils.isNotEmpty(dateFormat)) {
			path = String.format("%s%s%s", filePathWithoutExtension, lastDate, fileNum, fileExtension);
		} else if (dateIndex < 0 && StringUtils.isNotEmpty(dateFormat)) {
			path = String.format("%s%s_%05d%s", filePathWithoutExtension, lastDate, fileNum, fileExtension);
		} else if (!StringUtils.isEmpty(partitionStrategy)) {
			path = String.format("%s%s_%05d%s", filePathWithoutExtension, lastDate, fileNum, fileExtension);
		} else if (writerParallelism > 1 && maxFileBytesSize > 0) {
			path = String.format("%s%05d_%05d%s", filePathWithoutExtension, currentWriterSequence, fileNum, fileExtension);
		} else if (writerParallelism > 1) {
			path = String.format("%s%05d%s", filePathWithoutExtension, currentWriterSequence, fileExtension);
		} else if (maxFileBytesSize > 0) {
			path = String.format("%s%05d%s", filePathWithoutExtension, fileNum, fileExtension);
		} else {
			path = filePathWithoutExtension + fileExtension;
		}
		return new Path(path);
	}

	private void moveToLastFileNum(Path hdfsPath) throws IOException {
		FileSystem fs = hdfsPath.getFileSystem(conf);
		if (fs.exists(hdfsPath)) {
			fileNum++;
			hdfsPath = createFilePath(fileNum);
			moveToLastFileNum(hdfsPath);
		} else {
			fileNum--;
		}
	}

	private BufferedWriter createBufferedWriter(Path hdfsPath, boolean appendMode) throws IOException {
		FileSystem fs = hdfsPath.getFileSystem(conf);
		FSDataOutputStream output = null;
		try {
			if (appendMode) {
				if (!fs.exists(hdfsPath)) {
					hdfsPath = createFilePath(fileNum);
					fs.createNewFile(hdfsPath);
				}
				output = fs.append(hdfsPath);
			} else {
				if (fs.exists(hdfsPath)) {
					fileNum++;
					hdfsPath = createFilePath(fileNum);
					output = fs.create(hdfsPath);
				} else {
					hdfsPath = createFilePath(fileNum);
					output = fs.create(hdfsPath);
				}
			}
			// writer 计时器
			currentTime = System.currentTimeMillis();

			if (compressCodec == null) {
				return new BufferedWriter(new OutputStreamWriter(output, encoding));
			} else {
				CompressionCodecFactory factory = new CompressionCodecFactory(conf);
				CompressionCodec codec = factory.getCodecByClassName(compressCodec);
				return new BufferedWriter(new OutputStreamWriter(codec.createOutputStream(output), encoding));
			}
		} catch (IOException e) {
			throw new DataException(e);
		}
	}

}
