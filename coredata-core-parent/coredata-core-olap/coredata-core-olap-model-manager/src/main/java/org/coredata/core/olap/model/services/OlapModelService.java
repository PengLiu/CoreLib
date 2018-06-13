package org.coredata.core.olap.model.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.coredata.core.olap.model.entities.OlapDimIndex;
import org.coredata.core.olap.model.entities.OlapFactIndex;
import org.coredata.core.olap.model.entities.OlapFieldDef;
import org.coredata.core.olap.model.entities.OlapModel;
import org.coredata.core.olap.model.repositories.OlapModelResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
@EnableCaching
@Transactional(readOnly = true)
public class OlapModelService {

	private Logger logger = LoggerFactory.getLogger(OlapModelService.class);

	@Autowired
	private OlapModelResp modelResp;

	@Autowired
	private IndexService indexService;

	private ObjectMapper mapper = new ObjectMapper();

	@Transactional
	public void updateModelTemplates() {

		List<OlapModel> models = modelResp.findAll();

		for (OlapModel model : models) {
			OlapFactIndex factIndex = model.getFactIndex();
			try {
				ObjectNode template = (ObjectNode) mapper
						.readTree(OlapModelService.class.getResourceAsStream("/template.json"));
				template.put("template", factIndex.getIndexName() + "_*");
				ObjectNode props = (ObjectNode) template.get("mappings").get("prop_def").get("properties");
				// define fact index fields
				for (OlapFieldDef fieldDef : factIndex.getFactFields()) {
					ObjectNode prop = mapper.createObjectNode();
					prop.put("type", fieldDef.getFieldType());
					props.set(fieldDef.getName(), prop);
				}
				// define dimension nested objects
				for (OlapDimIndex dim : factIndex.getDimensions()) {
					String nestedId = dim.getRefName();
					ObjectNode prop = mapper.createObjectNode();
					prop.put("type", "nested");
					ObjectNode dimProps = mapper.createObjectNode();
					for (OlapFieldDef fieldDef : dim.getDeminsionFields()) {
						ObjectNode tmp = mapper.createObjectNode();
						tmp.put("type", fieldDef.getFieldType());
						dimProps.set(fieldDef.getName(), tmp);
					}
					prop.set("properties", dimProps);
					props.set(nestedId, prop);
				}
				indexService.updateTemplate(factIndex.getIndexName() + "_template",
						mapper.writeValueAsString(template));
			} catch (IOException e) {
				logger.error("Create template error.", e);
			}

		}

	}

	public List<OlapModel> findByJobId(String jobId) {
		return modelResp.findByJobId(jobId);
	}

	@Transactional
	public OlapModel save(OlapModel model) {
		return modelResp.save(model);
	}

	public OlapModel findByName(String name) {
		return modelResp.findByName(name);
	}

	public Page<OlapModel> findAll(int page, int pageSize) {
		Sort sort = new Sort(Direction.ASC, "name");
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		return modelResp.findAll(pageable);
	}

	@Transactional
	public void removeJob(String id) {
		modelResp.deleteById(id);
	}

	@Transactional
	public void removeAll(List<OlapModel> entities) {
		modelResp.deleteAll(entities);
	}

	public OlapModel findById(String id) {
		Optional<OlapModel> op = modelResp.findById(id);
		if (op.isPresent()) {
			return (OlapModel) op.get();
		} else {
			return null;
		}
	}

}