package org.coredata.core.util.common;

import com.rits.cloning.Cloner;

public class CloneUtil {

	private static Cloner clone = new Cloner();

	@SuppressWarnings("unchecked")
	public static <T> T createCloneObj(Object source) {
		Object deepClone = clone.deepClone(source);
		return (T) deepClone;
	}

}
