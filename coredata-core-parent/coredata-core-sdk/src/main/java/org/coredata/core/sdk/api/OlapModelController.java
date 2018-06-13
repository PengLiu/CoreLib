package org.coredata.core.sdk.api;

import java.util.List;

import org.coredata.core.olap.model.entities.OlapModel;
import org.coredata.core.olap.model.services.OlapModelService;
import org.coredata.core.sdk.api.common.ResponseMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/olap/model")
public class OlapModelController {

	@Autowired
	private OlapModelService olapModelService;

	@RequestMapping(value = "/insert", method = RequestMethod.POST)
	public ResponseMap save(@RequestBody OlapModel model) {
		ResponseMap result = ResponseMap.SuccessInstance();
		OlapModel record = null;
		try {
			record = olapModelService.save(model);
			result.setResult(record);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job insert：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public ResponseMap update(@RequestBody OlapModel model) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			OlapModel record = olapModelService.save(model);
			// pluginService.runSchedule(job);
			result.setResult(record);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job update：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/remove/{id}", method = RequestMethod.POST)
	public ResponseMap remove(@PathVariable String id) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			olapModelService.removeJob(id);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job remove：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
	@RequestMapping(value = "/removeall", method = RequestMethod.POST)
	public ResponseMap removeAll(@RequestBody List<OlapModel> models) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			olapModelService.removeAll(models);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job removeAll：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(path = "/findall/{page}/{pageSize}", method = RequestMethod.POST)
	public ResponseMap findAll(@PathVariable int page, @PathVariable int pageSize) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			Page<OlapModel> models = olapModelService.findAll(page, pageSize);
			result.setResult(models);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job findall：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/find/job/{jobId}", method = RequestMethod.GET)
	public ResponseMap findByJobId(@PathVariable String jobId) {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			List<OlapModel> models = olapModelService.findByJobId(jobId);
			result.setResult(models);
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("job findByJobId：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

    
    @RequestMapping(path = "/find/{id}", method = RequestMethod.GET)
    public ResponseMap findById(@PathVariable String id) {
        ResponseMap result = ResponseMap.SuccessInstance();
        try {
            OlapModel model = olapModelService.findById(id);
            result.setResult(model);
        } catch (Throwable e) {
            result = ResponseMap.BadRequestInstance();
            result.setMessage("job findByJobId：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
