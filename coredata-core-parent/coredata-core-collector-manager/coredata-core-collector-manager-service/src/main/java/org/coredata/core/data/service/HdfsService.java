package org.coredata.core.data.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.zookeeper.common.IOUtils;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.writers.WriterProperties;
import org.coredata.core.data.writers.hdfs.HDFSWriterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * operate hdfs file or directory util class
 * 
 */
@Service
public class HdfsService {

	private Logger logger = LoggerFactory.getLogger(HdfsService.class);

	@Value(value = "${hdfs.uri}")
	private String uri;

	@Value(value = "${hdfs.user}")
	private String user;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String createHomeDir(String currentUser) {

		Configuration conf = new Configuration();
		conf.set("fs.default.name", uri);

		try (FileSystem fs = FileSystem.get(new URI(uri), conf, user)) {
			Path home = new Path(getHdfsHome(currentUser));
			fs.delete(home, true);
			fs.mkdirs(home);
			fs.setOwner(home, currentUser, "supergroup");
			return home.toString();
		} catch (IllegalArgumentException | IOException | InterruptedException | URISyntaxException e) {
			logger.error("Create user home " + currentUser + " error", e);
			return null;
		}
	}

	/**
	 * make a new dir in the hdfs
	 * 
	 * @param dir
	 *            the dir may like '/tmp/testdir'
	 * @return boolean true-success, false-failed
	 * @exception IOException
	 *                something wrong happends when operating files
	 */
	public boolean mkdir(String dir) throws IOException {
		if (StringUtils.isEmpty(dir)) {
			return false;
		}
		dir = getUri() + dir;
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(URI.create(dir), conf);
		if (!fs.exists(new Path(dir))) {
			fs.mkdirs(new Path(dir));
		}

		fs.close();
		return true;
	}

	/**
	 * delete a dir in the hdfs. if dir not exists, it will throw
	 * FileNotFoundException
	 * 
	 * @param dir
	 *            the dir may like '/tmp/testdir'
	 * @return boolean true-success, false-failed
	 * @exception IOException
	 *                something wrong happends when operating files
	 * 
	 */
	public boolean deleteDir(String dir) throws IOException {
		if (StringUtils.isEmpty(dir)) {
			return false;
		}
		dir = getUri() + dir;
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(URI.create(dir), conf);
		fs.delete(new Path(dir), true);
		fs.close();
		return true;
	}

	public boolean deleteWithFullPath(String dir) {
		FileSystem fs = null;
		try {
			if (StringUtils.isEmpty(dir)) {
				return false;
			}
			Configuration conf = new Configuration();
			fs = FileSystem.get(URI.create(dir), conf);
			Path path = new Path(dir);
			if (fs.exists(path)) {
				fs.delete(new Path(dir), true);
			}
			fs.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					;
				}
			}
		}
		return false;
	}

	/**
	 * list files/directories/links names under a directory, not include embed
	 * objects
	 * 
	 * @param dir
	 *            a folder path may like '/tmp/testdir'
	 * @return List<String> list of file names
	 * @throws IOException
	 *             file io exception
	 */
	public List<String> listAll(String dir) throws IOException {
		if (StringUtils.isEmpty(dir)) {
			return new ArrayList<String>();
		}
		dir = getUri() + dir;
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(URI.create(dir), conf);
		FileStatus[] stats = fs.listStatus(new Path(dir));
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < stats.length; ++i) {
			if (stats[i].isFile()) {
				// regular file
				names.add(stats[i].getPath().toString());
			} else if (stats[i].isDirectory()) {
				// dir
				names.add(stats[i].getPath().toString());
			} else if (stats[i].isSymlink()) {
				// is s symlink in linux
				names.add(stats[i].getPath().toString());
			}
		}

		fs.close();
		return names;
	}

	/*
	 * upload the local file to the hds, notice that the path is full like
	 * /tmp/test.txt if local file not exists, it will throw a FileNotFoundException
	 * 
	 * @param localFile local file path, may like F:/test.txt or /usr/local/test.txt
	 * 
	 * @param hdfsFile hdfs file path, may like /tmp/dir
	 * 
	 * @return boolean true-success, false-failed
	 * 
	 * @throws IOException file io exception
	 */
	public boolean uploadLocalFile2HDFS(String localFile, String hdfsFile) throws IOException {
		if (StringUtils.isEmpty(localFile) || StringUtils.isEmpty(hdfsFile)) {
			return false;
		}
		hdfsFile = getUri() + hdfsFile;
		Configuration config = new Configuration();
		FileSystem hdfs = FileSystem.get(URI.create(uri), config);
		Path src = new Path(localFile);
		Path dst = new Path(hdfsFile);
		hdfs.copyFromLocalFile(src, dst);
		hdfs.close();
		return true;
	}

	/*
	 * create a new file in the hdfs.
	 * 
	 * notice that the toCreateFilePath is the full path
	 * 
	 * and write the content to the hdfs file.
	 */
	/**
	 * create a new file in the hdfs. if dir not exists, it will create one
	 * 
	 * @param newFile
	 *            new file path, a full path name, may like '/tmp/test.txt'
	 * @param content
	 *            file content
	 * @return boolean true-success, false-failed
	 * @throws IOException
	 *             file io exception
	 */
	public boolean createNewHDFSFile(String newFile, String content) throws IOException {
		if (StringUtils.isEmpty(newFile) || null == content) {
			return false;
		}
		newFile = getUri() + newFile;
		Configuration config = new Configuration();
		FileSystem hdfs = FileSystem.get(URI.create(newFile), config);
		FSDataOutputStream os = hdfs.create(new Path(newFile));
		os.write(content.getBytes("UTF-8"));
		os.close();
		hdfs.close();
		return true;
	}

	/**
	 * delete the hdfs file
	 * 
	 * @param hdfsFile
	 *            a full path name, may like '/tmp/test.txt'
	 * @return boolean true-success, false-failed
	 * @throws IOException
	 *             file io exception
	 */
	public boolean deleteHDFSFile(String hdfsFile) throws IOException {
		if (StringUtils.isEmpty(hdfsFile)) {
			return false;
		}
		hdfsFile = getUri() + hdfsFile;
		Configuration config = new Configuration();
		FileSystem hdfs = FileSystem.get(URI.create(hdfsFile), config);
		Path path = new Path(hdfsFile);
		boolean isDeleted = hdfs.delete(path, true);
		hdfs.close();
		return isDeleted;
	}

	/**
	 * read the hdfs file content
	 * 
	 * @param hdfsFile
	 *            a full path name, may like '/tmp/test.txt'
	 * @return byte[] file content
	 * @throws IOException
	 *             file io exception
	 */
	public byte[] readHDFSFile(String hdfsFile) throws Exception {
		if (StringUtils.isEmpty(hdfsFile)) {
			return null;
		}
		hdfsFile = getUri() + hdfsFile;
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(URI.create(hdfsFile), conf);
		// check if the file exists
		Path path = new Path(hdfsFile);
		if (fs.exists(path)) {
			FSDataInputStream is = fs.open(path);
			// get the file info to create the buffer
			FileStatus stat = fs.getFileStatus(path);
			// create the buffer
			byte[] buffer = new byte[Integer.parseInt(String.valueOf(stat.getLen()))];
			is.readFully(0, buffer);
			is.close();
			fs.close();
			return buffer;
		} else {
			throw new Exception("the file is not found .");
		}
	}

	/**
	 * append something to file dst
	 * 
	 * @param hdfsFile
	 *            a full path name, may like '/tmp/test.txt'
	 * @param content
	 *            string
	 * @return boolean true-success, false-failed
	 * @throws Exception
	 *             something wrong
	 */
	public boolean append(String hdfsFile, String content) throws Exception {
		if (StringUtils.isEmpty(hdfsFile)) {
			return false;
		}
		if (StringUtils.isEmpty(content)) {
			return true;
		}

		hdfsFile = getUri() + hdfsFile;
		Configuration conf = new Configuration();
		// solve the problem when appending at single datanode hadoop env
		conf.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER");
		conf.set("dfs.client.block.write.replace-datanode-on-failure.enable", "true");
		FileSystem fs = FileSystem.get(URI.create(hdfsFile), conf);
		// check if the file exists
		Path path = new Path(hdfsFile);
		if (fs.exists(path)) {
			try {
				InputStream in = new ByteArrayInputStream(content.getBytes());
				OutputStream out = fs.append(new Path(hdfsFile));
				IOUtils.copyBytes(in, out, 4096, true);
				out.close();
				in.close();
				fs.close();
			} catch (Exception ex) {
				fs.close();
				throw ex;
			}
		} else {
			createNewHDFSFile(hdfsFile, content);
		}
		return true;
	}

	public String getHdfsHome(String user) {
		StringBuffer sb = new StringBuffer();
		sb.append("/home/").append(user).append("/");
		return sb.toString();
	}

	public String getHdfsPath(String user, String directory) {
		StringBuffer sb = new StringBuffer();
		sb.append("/home/").append(user).append("/").append(directory).append("/");
		return sb.toString();
	}

	public String getHdfsPathWithUri(String user, String directory) {
		StringBuffer sb = new StringBuffer();
		sb.append(uri).append(getHdfsPath(user, directory));
		return sb.toString();
	}

	public PluginConfig getDefaultHdfsPath(String user, String jobId) {
		if(StringUtils.isEmpty(user)) {
			return null;
		}
		try {
			boolean exist = existUser(user);
			if (!exist) {
				createHomeDir(user);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String path = getHdfsPathWithUri(user, jobId);
		PluginConfig writeConfig = new PluginConfig();
		writeConfig.put(HDFSWriterProperties.PATH, path);
		writeConfig.put(HDFSWriterProperties.HADOOP_USER, this.user);
		writeConfig.put(HDFSWriterProperties.HDFS_URI, uri);
		writeConfig.put(WriterProperties.WRITE_MODE, true);
		writeConfig.put(HDFSWriterProperties.MAX_WAITTIME, 60);
		writeConfig.put(HDFSWriterProperties.MAX_FILE_SIZE_MB, 100);
		writeConfig.put(HDFSWriterProperties.MAX_RECORDS_SIZE, 100000);
		return writeConfig;
	}

	/**
	 * make a new dir in the hdfs
	 * 
	 * @param dir
	 *            the dir may like '/tmp/testdir'
	 * @return boolean true-success, false-failed
	 * @exception IOException
	 *                something wrong happends when operating files
	 */
	public boolean existUser(String user) throws IOException {
		if (StringUtils.isEmpty(user)) {
			return false;
		}
		Configuration conf = new Configuration();
		conf.set("fs.default.name", uri);
		try (FileSystem fs = FileSystem.get(new URI(uri), conf, user)) {
			Path home = new Path(getHdfsHome(user));
			return fs.exists(home);
		} catch (IllegalArgumentException | IOException | InterruptedException | URISyntaxException e) {
			logger.error("Create home folder error", e);
			return false;
		}

	}
}