package org.coredata.core.util.encryption;

import java.lang.reflect.Field;
import java.util.List;

import org.coredata.core.util.encryption.EncryptionAlgorithm.Method;

/**
 * 加解密工具类，该类可以直接通过字符串进行加/解密工作
 * @author sushi
 *
 */
public class EncryptionUtil {

	public static String deCode(Method algorithm, String source) {
		EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.getAlgorithm(algorithm.toString());
		return encryptionAlgorithm.deCode(source);
	}

	public static String enCode(Method algorithm, String source) {
		EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.getAlgorithm(algorithm.toString());
		return encryptionAlgorithm.enCode(source);
	}

	@SuppressWarnings("rawtypes")
	public static <T> void encrypt(Object source, Class<T> clazz, Method algorithm) {
		if (source == null || clazz == null)
			return;
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (!field.isAnnotationPresent(PersistEncrypted.class))
				continue;
			field.setAccessible(true);
			Class<?> type = field.getType();
			if (type == String.class) {
				try {
					Object srcStr = field.get(source);
					if (srcStr == null)
						return;
					String enCode = enCode(algorithm, srcStr.toString());
					field.set(source, enCode);
				} catch (IllegalArgumentException | IllegalAccessException e) {

				}
			} else if (type == List.class) {
				try {
					List srcStrs = (List) field.get(source);
					if (srcStrs == null)
						return;
					for (int i = 0; i < srcStrs.size(); i++) {
						Object element = srcStrs.get(i);
						encrypt(element, element.getClass(), algorithm);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {

				}
			} else if (type == String[].class) {
				try {
					String[] strs = (String[]) field.get(source);
					if (strs == null)
						return;
					String[] enCodes = new String[strs.length];
					for (int i = 0; i < enCodes.length; i++) {
						enCodes[i] = enCode(algorithm, strs[i]);
					}
					field.set(source, enCodes);
				} catch (IllegalArgumentException | IllegalAccessException e) {

				}
			} else {
				try {
					Object src = field.get(source);
					encrypt(src, src.getClass(), algorithm);
				} catch (IllegalArgumentException | IllegalAccessException e) {

				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static <T> void decrypt(Object source, Class<T> clazz, Method algorithm) {
		if (source == null || clazz == null)
			return;
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (!field.isAnnotationPresent(PersistEncrypted.class))
				continue;
			field.setAccessible(true);
			Class<?> type = field.getType();
			if (type == String.class) {
				try {
					Object srcStr = field.get(source);
					if (srcStr == null)
						return;
					String deCode = deCode(algorithm, srcStr.toString());
					field.set(source, deCode);
				} catch (IllegalArgumentException | IllegalAccessException e) {

				}
			} else if (type == List.class) {
				try {
					List srcStrs = (List) field.get(source);
					if (srcStrs == null)
						return;
					for (int i = 0; i < srcStrs.size(); i++) {
						Object element = srcStrs.get(i);
						decrypt(element, element.getClass(), algorithm);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {

				}
			} else if (type == String[].class) {
				try {
					String[] strs = (String[]) field.get(source);
					if (strs == null)
						return;
					String[] deCodes = new String[strs.length];
					for (int i = 0; i < deCodes.length; i++) {
						deCodes[i] = deCode(algorithm, strs[i]);
					}
					field.set(source, deCodes);
				} catch (IllegalArgumentException | IllegalAccessException e) {

				}
			} else {
				try {
					Object src = field.get(source);
					decrypt(src, src.getClass(), algorithm);
				} catch (IllegalArgumentException | IllegalAccessException e) {

				}
			}
		}
	}

}
