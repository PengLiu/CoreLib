package org.coredata.core.sdk.api;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.entities.CommEntity;
import org.coredata.core.entities.ResEntity;
import org.coredata.core.entities.services.EntityService;
import org.coredata.core.sdk.api.common.ResponseMap;
import org.neo4j.graphdb.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/entities")
public class EntityApi {

	private static final Logger logger = LoggerFactory
			.getLogger(EntityApi.class);

	@Autowired
	private EntityService entityService;

	private ObjectMapper objectMapper = new ObjectMapper();

	@PostMapping(path = "/batchdelete")
	public ResponseMap batchDelete(@RequestBody List<Long> ids) {
		ResponseMap result = ResponseMap.BadRequestInstance();
		try {
			entityService.delete(ids);
			result = ResponseMap.SuccessInstance();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMessage("删除Entity失败:" + e.getMessage());
		}
		return result;
	}

	@RequestMapping(path = "/", method = RequestMethod.POST)
	public ResponseMap save(@RequestBody List<ResEntity> entities) {
		ResponseMap result = ResponseMap.BadRequestInstance();
		try {
			Iterable<ResEntity> entitys = entityService.save(entities);
			result = ResponseMap.SuccessInstance();
			result.setResult(entitys);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMessage("保存Entity失败:" + e.getMessage());
		}
		return result;
	}

	@GetMapping(path = "/{entityId}")
	public ResponseMap getByEntityId(@PathVariable String entityId) {
		ResponseMap result = ResponseMap.BadRequestInstance();
		try {
			Page<CommEntity> findByEntityId = entityService
					.findByEntityId(entityId, PageRequest.of(0, 1));
			result = ResponseMap.SuccessInstance();
			CommEntity entity = findByEntityId != null
					&& findByEntityId.getContent().size() > 0
							? findByEntityId.getContent().get(0)
							: null;
			result.setResult(entity);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMessage("获取Entity失败:" + e.getMessage());
		}
		return result;
	}
	@GetMapping(path = "/getById/{id}")
	public ResponseMap getById(@PathVariable Long id) {
		ResponseMap result = ResponseMap.BadRequestInstance();
		try {
			CommEntity entity = entityService.findById(id);
			result = ResponseMap.SuccessInstance();
			result.setResult(entity);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMessage("获取Entity失败:" + e.getMessage());
		}
		return result;
	}

	@PostMapping(path = "/list")
	public ResponseMap list(@RequestBody String content) {
		ResponseMap result = ResponseMap.BadRequestInstance();
		try {
			Page<CommEntity> findEntitiesByCondition = entityService
					.findEntitiesByCondition(content);
			result = ResponseMap.SuccessInstance();
			result.setResult(findEntitiesByCondition);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMessage("分页查询Entity失败:" + e.getMessage());
		}
		return result;
	}

	/**
	 * TODO:考虑添加查询条件 根据实体间的关系查询实体
	 *
	 * @param condition
	 * @return
	 */
	@PostMapping(path = "/byrelationship")
	public ResponseMap findByRelatedEntity(@RequestBody String condition) {
		ResponseMap badResult = ResponseMap.BadRequestInstance();
		if (StringUtils.isEmpty(condition)) {
			badResult.setMessage("查询条件不能为空");
			return badResult;
		}
		long entityId = 0;
		String relationship = null;
		int depth = 0;
		Direction direct = null;
		try {
			JsonNode json = objectMapper.readTree(condition);
			entityId = json.get("entityId").asLong();
			relationship = json.get("relationship").asText();
			depth = json.get("depth").asInt(1);
			String direction = json.get("direction").asText("INCOMING");
			direct = Direction.valueOf(direction);

		} catch (Exception e) {
			badResult.setMessage("查询条件错误:" + e.getMessage());
			return badResult;
		}
		try {
			Collection<CommEntity> entities = entityService
					.findByRelatedEntity(entityId, relationship, depth, direct);
			ResponseMap result = ResponseMap.SuccessInstance();
			result.setResult(entities);
			return result;
		} catch (Exception e) {
			badResult.setMessage("查询错误:" + e.getMessage());
			return badResult;
		}

	}

	/**
	 * 根据给定的查询条件和count字段,计算数量 countField-count字段 queryJson-查询条件json
	 */
	@PostMapping(path = "/count/bycondition")
	public ResponseMap countByCondition(@RequestBody String condition) {
		ResponseMap badResult = ResponseMap.BadRequestInstance();
		if (StringUtils.isEmpty(condition)) {
			badResult.setMessage("查询条件不能为空");
			return badResult;
		}
		try {
			JsonNode json = objectMapper.readTree(condition);
			String queryJson = json.get("queryJson").asText();
			String countField = json.get("countField").asText();
			Object count;
			if (StringUtils.isNotBlank(countField)) {
				count = entityService.countEntityByPropAndCondition(countField,
						queryJson);
			} else {
				count = entityService.countEntityByCondition(queryJson);
			}
			ResponseMap result = ResponseMap.SuccessInstance();
			result.setResult(count);
			return result;
		} catch (Exception e) {
			badResult.setMessage("查询错误:" + e.getMessage());
			return badResult;
		}

	}
	//
	// @GetMapping(path = "/byprop/{key}/{value}")
	// public ResponseMap getByEntityId(@PathVariable String key,
	// @PathVariable String value) {
	// ResponseMap result = ResponseMap.BadRequestInstance();
	// try {
	// Page<CommEntity> entityPage = entityService.findByProp(key, value,
	// PageRequest.of(0, Integer.MAX_VALUE));
	//
	// result = ResponseMap.SuccessInstance();
	// result.setResult(entityPage.getContent());
	// } catch (Exception e) {
	// logger.error(e.getMessage(), e);
	// result.setMessage("获取Entity失败:" + e.getMessage());
	// }
	// return result;
	// }

}
