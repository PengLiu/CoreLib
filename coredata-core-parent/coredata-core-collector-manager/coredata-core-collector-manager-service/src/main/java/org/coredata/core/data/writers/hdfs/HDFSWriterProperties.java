package org.coredata.core.data.writers.hdfs;

public class HDFSWriterProperties {
	public static final String PATH = "path";										//HDFS文件路径，如：hdfs://192.168.1.1:8020/user/1.txt
	public static final String FIELDS_SEPARATOR = "fields.separator";				//字段分隔符，默认：\t
	public static final String LINE_SEPARATOR = "line.separator";					//行分隔符，默认：\n
	public static final String ENCODING = "encoding";								//文件编码，默认：UTF-8
	public static final String COMPRESS_CODEC = "compress.codec";					//压缩编码，如：org.apache.hadoop.io.compress.GzipCodec，默认：空
	public static final String HADOOP_USER = "hadoop.user";							//具有HDFS写权限的用户名，不能为空
	public static final String MAX_FILE_SIZE_MB = "max.file.size.mb";				//单个文件最大大小限制（单位：MB），默认：0
	public static final String MAX_RECORDS_SIZE = "max.record.size";				//单次写入最大记录数，默认：0
	public static final String MAX_WAITTIME = "max.wait.time";						//是否追加，默认：false
	public static final String COVER_MODE = "cover.mode";							//是否覆盖，默认：false
	public static final String HDFS_CONF_PATH = "hdfs.conf.path";					//hdfs-site.xml配置文件路径，默认：空
	public static final String PARTITION_DATE_INDEX = "partition.date.index";		//日期字段索引值，起始值为0，默认：-1
	public static final String PARTITIONED_DATE_FORMAT = "partition.date.format";	//日期格式，如：yyyy-MM-dd，默认：空
	public static final String PARTITIONED_STRATEGY = "partition.strategy";			//文件生成策略 hourly/daily 每小时/每天，默认：daily
	public static final String HDFS_URI = "hdfs.uri";


}
