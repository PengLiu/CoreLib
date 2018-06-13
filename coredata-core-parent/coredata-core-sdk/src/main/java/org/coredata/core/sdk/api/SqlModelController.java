package org.coredata.core.sdk.api;

import java.util.List;

import org.coredata.core.data.entities.SqlModel;
import org.coredata.core.data.service.SqlModelService;
import org.coredata.core.sdk.api.common.ResponseMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sqlmodel")
public class SqlModelController {

	@Autowired
	private SqlModelService service;
	// private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping(value = "/insert", method = RequestMethod.POST)
	public ResponseMap create(@RequestBody SqlModel job) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			SqlModel record = service.save(job);
			result.setResult(record);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("model insert：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public ResponseMap update(@RequestBody SqlModel job) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			SqlModel record = service.save(job);
			result.setResult(record);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("model update：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(value = "/count", method = RequestMethod.POST)
	public ResponseMap findAll() {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			long k = service.count();
			result.setResult(k);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("model count：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/find/{modelId}", method = RequestMethod.POST)
	public ResponseMap find(@PathVariable String modelId) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			SqlModel record = service.findById(modelId);
			result.setResult(record);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("model find：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/findall/{page}/{pageSize}", method = RequestMethod.POST)
	public ResponseMap findAll(@PathVariable int page, @PathVariable int pageSize) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			result.setResult(service.findAll(page, pageSize));
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("model findall：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/remove/{modelId}", method = RequestMethod.POST)
	public ResponseMap remove(@PathVariable String modelId) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			service.removeById(modelId);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("model remove：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(value = "/removeall", method = RequestMethod.POST)
	public ResponseMap removeAll(@RequestBody List<SqlModel> models) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			service.removeAll(models);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("model removeall：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
}
