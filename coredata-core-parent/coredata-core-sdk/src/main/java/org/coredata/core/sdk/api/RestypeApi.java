package org.coredata.core.sdk.api;

import java.util.List;

import org.coredata.core.model.common.Restype;
import org.coredata.core.model.service.RestypeService;
import org.coredata.core.sdk.api.common.ResponseMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/restype")
public class RestypeApi {

	private static Logger logger = LoggerFactory.getLogger(RestypeApi.class);

	@Autowired
	private RestypeService service;

	private ObjectMapper objectMapper = new ObjectMapper();

	@RequestMapping(path = "/", method = RequestMethod.POST)
	public ResponseMap save(@RequestBody List<Restype> restypes) {
		ResponseMap result = ResponseMap.BadRequestInstance();
		try {
			service.save(restypes);
			result = ResponseMap.SuccessInstance();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMessage("保存失败:" + e.getMessage());
		}
		return result;
	}

	@GetMapping(path = "/{id}")
	public ResponseMap getById(@PathVariable String id) {
		ResponseMap result = ResponseMap.BadRequestInstance();
		try {
			Restype restype = service.findById(id);
			result = ResponseMap.SuccessInstance();
			result.setResult(restype);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMessage("获取Entity失败:" + e.getMessage());
		}
		return result;
	}
	@GetMapping(path = "/findfullpath/{id}")
	public ResponseMap findfullpath(@PathVariable String id) {
		ResponseMap result = ResponseMap.BadRequestInstance();
		try {
			String fullpath = service.findFullPath(id);
			result = ResponseMap.SuccessInstance();
			result.setResult(fullpath);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMessage("获取ResType FullPath失败:" + e.getMessage());
		}
		return result;
	}
	@GetMapping(path = "/findbyparent/{token}/{id}")
	public ResponseMap findByParentId(@PathVariable String id,
			@PathVariable String token) {
		ResponseMap result = ResponseMap.BadRequestInstance();
		try {
			List<Restype> findByParentid = service.findByParentid(id, token);
			result = ResponseMap.SuccessInstance();
			result.setResult(findByParentid);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMessage("获取父ResType列表失败:" + e.getMessage());
		}
		return result;
	}

}
