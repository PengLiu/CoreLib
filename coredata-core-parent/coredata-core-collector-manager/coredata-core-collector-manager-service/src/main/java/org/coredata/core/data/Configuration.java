package org.coredata.core.data;

import java.util.Properties;

public abstract class Configuration extends Properties {

	private static final long serialVersionUID = 8606831740240321865L;

	public String getString(String key, String defaultValue) {
		Object value = get(key);
		return value != null ? value.toString() : defaultValue; 
	}

	public String getString(String key) {
		Object value = get(key);
		return value != null ? value.toString() :""; 
	}

	public void setString(String key, String value) {
		setProperty(key, value);
	}

	public int getInt(String key, int defaultValue) {
		Object value = get(key);
		return value != null ? Integer.parseInt(value.toString()) : defaultValue;
	}

	public void setInt(String key, int value) {
		setString(key, Integer.toString(value));
	}

	public long getLong(String key, long defaultValue) {
		Object value = get(key);
		return value != null ? Long.parseLong(value.toString()) : defaultValue;
	}

	public void setLong(String key, long value) {
		setString(key, Long.toString(value));
	}

	public double getDouble(String key, double defaultValue) {
		Object value = get(key);
		return value != null ? Double.parseDouble(value.toString()) : defaultValue;
	}

	public void setDouble(String key, double value) {
		setString(key, Double.toString(value));
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		Object value = get(key);
		return value != null ? Boolean.parseBoolean(value.toString()) : defaultValue;
	}

	public void setBoolean(String key, boolean value) {
		setString(key, Boolean.toString(value));
	}

	public float getFloat(String key, float defaultValue) {
		Object value = get(key);
		return value != null ? Float.parseFloat(value.toString()) : defaultValue;
	}

	public void setFloat(String key, float value) {
		setString(key, Float.toString(value));
	}
}
