package org.coredata.core.util.encryption;

import java.util.HashMap;
import java.util.Map;

public abstract class EncryptionAlgorithm {

	public static enum Method {
		AES
	}

	protected static final Map<String, EncryptionAlgorithm> algorithms = new HashMap<>();

	public abstract String enCode(String source);

	public abstract String deCode(String encode);

	public static void init() {
		algorithms.put(Method.AES.toString(), new AESAlgorithm());
	}

	public static EncryptionAlgorithm getAlgorithm(String method) {
		return algorithms.get(method);
	}

}
