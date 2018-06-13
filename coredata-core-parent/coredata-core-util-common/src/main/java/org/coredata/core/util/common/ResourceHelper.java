package org.coredata.core.util.common;

import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceHelper {

	private String properFile = null;

	private ResourceBundle bundle = null;

	public ResourceHelper(String properFile) {
		this.properFile = properFile;
	}

	public void init() {
		bundle = ResourceBundle.getBundle(properFile, Locale.getDefault());
	}

	public void init(Locale locale) {
		bundle = ResourceBundle.getBundle(properFile, locale);
	}

	public String getMsg(String key) {
		if (bundle != null && key != null) {
			return bundle.getString(key);
		}
		return "";
	}

}
