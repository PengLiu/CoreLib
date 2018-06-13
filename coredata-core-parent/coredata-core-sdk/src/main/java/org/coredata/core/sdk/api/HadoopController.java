package org.coredata.core.sdk.api;

import org.coredata.core.data.service.HdfsService;
import org.coredata.core.sdk.api.common.ResponseMap;
import org.coredata.core.vo.HadoopUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 封装 Hive 相关操作
 * 
 * @author liupeng
 *
 */
@RestController
@RequestMapping("/api/v1/hadoop")
public class HadoopController {

	@Autowired
	private HdfsService hdfsService;

	@RequestMapping(value = "/user/init/{user}/{group}", method = RequestMethod.PUT)
	public ResponseMap initUser(@PathVariable String user, @PathVariable String group) {
		ResponseMap result = ResponseMap.SuccessInstance();

		if(StringUtils.isEmpty(user)) {
			return null;
		}
		try {// 创建hdfs home 目录
			boolean exist = hdfsService.existUser(user);
			String home = "";
			if (!exist) {
				home = hdfsService.createHomeDir(user);
			}else {
				home = hdfsService.getHdfsHome(user);
			}
			HadoopUserInfo userInfo = new HadoopUserInfo();
			userInfo.setHome(home);
			userInfo.setName(user);
			userInfo.setGroup(group);
			result.setResult(userInfo);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("showdbs：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
}
