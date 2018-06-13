package org.coredata.core.stream.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class LookupTool implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(LookupTool.class);

	private Map<String, String> ouiTable = new HashMap<String, String>(32 * 1024);

//	private Map<String, String> i18n = new HashMap<String, String>(16 * 1024);
	private Map<String, String> i18n = new HashMap<String, String>(0);

	/**
	 * the standard (IEEE 802) format for printing MAC-48 addresses
	 * 3D:F2:C9:A6:B3:4F //<-- standard
	 * 3D-F2-C9-A6-B3-4F //<-- standard
	 * 3D-F2-C9-A6-B3:4F //<-- standard
	 * @param mac, 只接受第一种格式
	 * @return 厂商公司名称
	 */
	public String getVendor(String mac) {
		if (mac == null || mac.length() == 0) {
			return "";
		}

		if (mac.length() != 17) {
			return "";
		}

		return ouiTable.get(mac.toUpperCase().substring(0, 8));
	}

	/**
	 * Beta API
	 * @param vendor 
	 * @return 厂商中文名称
	 */
	public String i18n(String vendor) {
		if (vendor == null) {
			return null;
		}
		
		String zh = i18n.get(vendor);
		if (zh == null) {
			return vendor;
		} else {
			return zh;
		}
	}

	@Override
	public void afterPropertiesSet() {
		try {
			ResourceLoader loader = new FileSystemResourceLoader();
			Resource resource = loader.getResource("classpath:/oui.txt");

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					resource.getInputStream(), StandardCharsets.UTF_8));

			String line = reader.readLine();
			while (line != null) {
				if (line.contains("(hex)")) {
					//MAC前24位
					String macPre = line.substring(0, 8).replace('-', ':');
					String vendor = line.substring(18, line.length());
					ouiTable.put(macPre, vendor);
				}

				line = reader.readLine();
			}

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		
//		try {
//			ResourceLoader loader = new FileSystemResourceLoader();
//			Resource resource = loader.getResource("classpath:/i18n.csv");
//
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					resource.getInputStream(), StandardCharsets.UTF_8));
//
//			String line = reader.readLine();
//			while (line != null) {
//				String[] raw = line.split("\t", 2);
//				i18n.put(raw[0], raw[1]);
//
//				line = reader.readLine();
//			}
//
//		} catch (Throwable e) {
//			logger.error(e.getMessage(), e);
//		}
	}
}
