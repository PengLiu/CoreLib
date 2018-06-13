package org.coredata.core.sdk.api.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class FileTool {

	private static RandomAccessFile randomAccessFile;

	public static void writeFile(final String filePath, final byte[] data) throws Exception {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filePath);
			fos.write(data);
			fos.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}
	
	
//	public static void largeFileIO(String inputFile) {
//	        try {
//	            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(inputFile)));
//	            BufferedReader in = new BufferedReader(new InputStreamReader(bis, "utf-8"), 1 * 1024 * 1024);// 10M缓存
//	            while (in.ready()) {
//	                String line = in.readLine();
//	            }
//	            in.close();
//	        } catch (IOException ex) {
//	            ex.printStackTrace();
//	        }
//	    }
	  
	public static byte[] readFile(String filePath) throws Exception {
		FileChannel fc = null;
		try {
			randomAccessFile = new RandomAccessFile(filePath, "r");
			fc = randomAccessFile.getChannel();
			MappedByteBuffer byteBuffer = fc.map(MapMode.READ_ONLY, 0, fc.size()).load();
			System.out.println(byteBuffer.isLoaded());
			byte[] result = new byte[(int) fc.size()];
			if (byteBuffer.remaining() > 0) {
				byteBuffer.get(result, 0, byteBuffer.remaining());
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				fc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
