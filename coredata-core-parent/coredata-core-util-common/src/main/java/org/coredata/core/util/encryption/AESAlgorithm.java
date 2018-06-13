package org.coredata.core.util.encryption;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AESAlgorithm extends EncryptionAlgorithm {

	private static final Logger logger = LoggerFactory.getLogger(AESAlgorithm.class);

	private Key key;

	private Cipher enCodeCipher;

	private Cipher deCodeCipher;

	private static final String KEY_WORLD = "datapoint";

	/**
	 * 构造方法，生成算法的key值
	 */
	public AESAlgorithm() {
		KeyGenerator keyGenerator = null;
		//生成Key
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(KEY_WORLD.getBytes());
			keyGenerator.init(128, random);
			enCodeCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			deCodeCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Init KeyGenerator Failed.", e);
		}
		/**
		 * keyGenerator.init(128);
		 * 使用上面这种初始化方法，加载后密文随机
		 * keyGenerator.init(128, new SecureRandom("seedseedseed".getBytes()));
		 * 使用上面这种初始化方法可以特定种子来生成密钥，这样加密后的密文是唯一固定的。
		 */
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] keyBytes = secretKey.getEncoded();
		key = new SecretKeySpec(keyBytes, "AES");
		try {
			enCodeCipher.init(Cipher.ENCRYPT_MODE, key);
			deCodeCipher.init(Cipher.DECRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			logger.error("Init Cipher Failed.", e);
		}
	}

	@Override
	public String enCode(String source) {
		byte[] encodeResult = null;
		try {
			encodeResult = enCodeCipher.doFinal(source.getBytes());
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			logger.error("AES encode error.", e);
		}
		return Hex.encodeHexString(encodeResult);
	}

	@Override
	public String deCode(String encode) {
		if (deCodeCipher == null)
			return null;
		byte[] decodeResult = null;
		try {
			byte[] decodeHex = Hex.decodeHex(encode.toCharArray());
			decodeResult = deCodeCipher.doFinal(decodeHex);
		} catch (IllegalBlockSizeException | BadPaddingException | DecoderException e) {
			logger.error("AES decode error.", e);
			return encode;
		}
		return new String(decodeResult);
	}

}
