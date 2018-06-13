package org.coredata.core.stream.mining.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.util.redis.service.RedisService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class OperationSystemFunction extends AbsFunction {

	private static final String S_WINDOWS_WORKSTATION_SYSOID = "1.3.6.1.4.1.311.1.1.3.1.1";

	private static final String S_WINDOWS_SERVER_SYSOID = "1.3.6.1.4.1.311.1.1.3.1.2";

	private static final String S_WINDOWS_DATACENTER_SYSOID = "1.3.6.1.4.1.311.1.1.3.1.3";

	private static final Map<String, String> S_NOVERSIONMAPPING = new HashMap<String, String>();

	private static final Map<String, String> S_WORKSTATION = new HashMap<String, String>();

	private static final Map<String, String> S_SERVER = new HashMap<String, String>();

	private static final Map<String, String> S_DATACENTER = new HashMap<String, String>();

	static {
		S_NOVERSIONMAPPING.put("Windows 2000 Server", "Microsoft Windows 2000 Server");
		S_NOVERSIONMAPPING.put("Windows 2000 Professional", "Microsoft Windows 2000 Professional");
		S_NOVERSIONMAPPING.put("XP Professional", "Microsoft Windows XP Professional");
		S_NOVERSIONMAPPING.put("Windows Server 2003", "Microsoft Windows Server 2003");
		S_NOVERSIONMAPPING.put("Windows Server 2008", "Microsoft Windows Server 2008");
		S_NOVERSIONMAPPING.put("Windows 7 Ultimate", "Microsoft Windows 7 Ultimate");

		S_WORKSTATION.put("1057", "Microsoft Windows NT 3.51 Workstation");
		S_WORKSTATION.put("1381", "Microsoft Windows NT 4.0 Workstation");
		S_WORKSTATION.put("2195", "Microsoft Windows 2000");
		S_WORKSTATION.put("2600", "Microsoft Windows XP");
		S_WORKSTATION.put("3790", "Microsoft Windows XP x64");
		S_WORKSTATION.put("6000", "Microsoft Windows Vista");
		S_WORKSTATION.put("6001", "Microsoft Windows Vista SP1");
		S_WORKSTATION.put("6002", "Microsoft Windows Vista SP2");
		S_WORKSTATION.put("7600", "Microsoft Windows 7");
		S_WORKSTATION.put("7601", "Microsoft Windows 7 SP1");

		S_SERVER.put("1057", "Microsoft Windows NT Server 3.51");
		S_SERVER.put("1381", "Microsoft Windows NT Server 4.0");
		S_SERVER.put("2195", "Microsoft Windows 2000 Server");
		S_SERVER.put("3790", "Microsoft Windows Server 2003");
		S_SERVER.put("6001", "Microsoft Windows Server 2008");
		S_SERVER.put("6002", "Microsoft Windows Server 2008 SP2");
		S_SERVER.put("7600", "Microsoft Windows Server 2008 R2");
		S_SERVER.put("7601", "Microsoft Windows Server 2008 R2 SP1");
		S_SERVER.put("9200", "Microsoft Windows Server 2012");
		S_SERVER.put("9600", "Microsoft Windows Server 2012 R2 Standard");

		S_DATACENTER.put("1057", "NT Datacenter 3.51");
		S_DATACENTER.put("1381", "NT Datacenter 4.0");
		S_DATACENTER.put("2195", "2000 Datacenter Server");
		S_DATACENTER.put("3790", "Server 2003 Datacenter");
		S_DATACENTER.put("6001", "Server 2008 Datacenter");
		S_DATACENTER.put("6002", "Server 2008 Datacenter SP2");
		S_DATACENTER.put("7600", "Server 2008 Datacenter R2");
		S_DATACENTER.put("7601", "Server 2008 Datacenter R2 SP1");
		S_DATACENTER.put("9600", "Server 2012 Datacenter");
	}

	@Override
	public String getName() {
		return "operSystem";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		Object expObj = FunctionUtils.getJavaObject(exp, env);//获取表达式的值
		String instId = env.get(INSTANCE_ID).toString();
		String result = "Microsoft Windows";
		String expStr = "";
		try {
			if (expObj instanceof List) {
				List results = (List) expObj;
				Object r = results.get(0);
				expStr = r.toString();
			} else {
				expStr = expObj.toString();
			}
			//替换变量后，进行操作系统处理
			Object instance = redisService.loadDataByTableAndKey(RedisService.INSTANCE, instId);//同步获取资产信息
			if (instance == null)
				return new AviatorString(result);
			String instanceStr = JSON.toJSONString(instance);
			Map<String, Object> insObj = JSON.parseObject(instanceStr, new TypeReference<Map<String, Object>>() {
			});
			String systemoid = insObj.get("props.systemoid") == null ? null : insObj.get("props.systemoid").toString();//获取对应属性信息，寻找systemOid属性
			if (StringUtils.isEmpty(systemoid))
				return new AviatorString(result);
			String osBuild = getOsBuild(expStr);
			String osBuildNmuber = getOSBuildNmuber(expStr);
			String sys = null;
			switch (systemoid) {
			case S_WINDOWS_WORKSTATION_SYSOID:
				sys = S_WORKSTATION.get(osBuildNmuber);
				break;
			case S_WINDOWS_SERVER_SYSOID:
				sys = S_SERVER.get(osBuildNmuber);
				break;
			case S_WINDOWS_DATACENTER_SYSOID:
				sys = S_DATACENTER.get(osBuildNmuber);
				break;
			}
			if ("Unknown OS".equals(osBuild))
				sys = getSystemInfo(osBuild);
			if (sys != null)
				result = sys;
		} catch (Exception e) {
			logError(e);
		}
		return new AviatorString(result);
	}

	/**
	 * {获取windows主机的buildNumber信息，以判断操作系统版本}.
	 *
	 * @param desc String windows主机描述信息
	 * @return String buildNumber 信息
	 */
	private static String getOSBuildNmuber(final String desc) {
		String t_result = null;
		if (desc == null) {
			return t_result;
		}

		int t_buildNumber = desc.indexOf("Build Number: ");
		int t_build = desc.indexOf("Build ");

		if (t_buildNumber > 0) {
			t_result = desc.substring(t_buildNumber, t_buildNumber + 10);
			t_result = StringUtils.remove(t_result, "Build Number: ");
		} else if (t_build > 0) {
			t_result = desc.substring(t_build, t_build + 10);
			t_result = StringUtils.remove(t_result, "Build ");
		}
		if (StringUtils.isNotBlank(t_result)) {
			return t_result.trim();
		}

		return null;
	}

	/**
	 * {method description}.
	 *
	 * @param desc String
	 * @return String
	 */
	private static String getOsBuild(final String desc) {
		if (desc == null) {
			return null;
		}
		if (desc.indexOf("Version") < 0) {
			return null;
		}
		return StringUtils.substringAfterLast(desc, "Version").trim();
	}

	/**
	 * {获取操作系统信息}.
	 *
	 * @param str String
	 * @return String
	 */
	private static String getSystemInfo(final String str) {
		String t_result;
		if (str.indexOf("Microsoft Windows XP Professional") >= 0) {
			t_result = S_NOVERSIONMAPPING.get("Microsoft Windows XP Professional");
		} else if (str.indexOf("2000 Server") >= 0) {
			t_result = S_NOVERSIONMAPPING.get("Windows 2000 Server");
		} else if (str.indexOf("2000 Professional") >= 0) {
			t_result = S_NOVERSIONMAPPING.get("Windows 2000 Professional");
		} else if (str.indexOf("Server 2003") >= 0) {
			t_result = S_NOVERSIONMAPPING.get("Windows Server 2003");
		} else if (str.indexOf("Server 2008") >= 0) {
			t_result = S_NOVERSIONMAPPING.get("Microsoft Windows Server 2008");
		} else if (str.indexOf("6.0.6001") >= 0) {
			t_result = S_NOVERSIONMAPPING.get("Microsoft Windows Server 2008");
		} else if (str.indexOf("6.0.6002") >= 0) {
			t_result = S_NOVERSIONMAPPING.get("Microsoft Windows Server 2008");
		} else {
			t_result = "Microsoft Windows";
		}
		return t_result;
	}
}
