package org.coredata.core.data.util;

import java.net.MalformedURLException;

public class PluginUtils {

	public static Class<?> loadClass(String pluginName, String className) throws ClassNotFoundException, MalformedURLException {
		return Class.forName(className);
	}

}
