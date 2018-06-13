package org.coredata.core.olap.model.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.coredata.core.olap.model.services.vo.FieldMeta;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;

@Service
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "dimensionCache", keyGenerator = "dimKeyGen")
public class IndexService {

	private Logger logger = Logger.getLogger(IndexService.class);

	private boolean runLoop = true;

	private String PROPERTIES = "properties";

	private String TYPE = "type";

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Cacheable
	public String queryByCondition(String index, String field, Object val) {

		NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder().withIndices(index + "*").withQuery(QueryBuilders.termQuery(field, val));
		return elasticsearchTemplate.query(searchQuery.build(), new ResultsExtractor<String>() {
			@Override
			public String extract(SearchResponse response) {
				if (response.getHits().totalHits > 0) {
					SearchHit searchHit = response.getHits().getAt(0);
					return searchHit.getSourceAsString();
				}
				return null;
			}
		});

	}

	@SuppressWarnings("unchecked")
	private List<FieldMeta> createMetaInfoRes(String parent, Map<String, Object> map) {
		List<FieldMeta> meta = new ArrayList<>();
		for (Entry<String, Object> entry : map.entrySet()) {
			Map<String, Object> tmp = (Map<String, Object>) entry.getValue();
			if (FieldType.Nested.toString().equalsIgnoreCase((String) tmp.get(TYPE))) {
				meta.addAll(createMetaInfoRes(entry.getKey(), (Map<String, Object>) tmp.get(PROPERTIES)));
			} else {
				String key = parent == null ? entry.getKey() : parent + "." + entry.getKey();
				FieldMeta fieldMeta = new FieldMeta();
				fieldMeta.setFieldName(key);
				fieldMeta.setType((String) tmp.get(TYPE));
				switch (fieldMeta.getType()) {
				case "integer":
				case "long":
				case "date":
				case "float":
				case "double":
				case "boolean":
				case "ip":
				case "keyword":
					fieldMeta.setCanAggregation(true);
					break;
				default:
					fieldMeta.setCanAggregation(false);
				}
				meta.add(fieldMeta);
			}
		}
		return meta;
	}

	@SuppressWarnings("unchecked")
	public Collection<FieldMeta> loadIndexMeta(String index) {

		Collection<FieldMeta> meta = new ArrayList<>();

		try {
			String indexName = index + "_*";
			GetIndexResponse resp = elasticsearchTemplate.getClient().admin().indices().getIndex(new GetIndexRequest().indices(indexName)).get();
			ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = resp.getMappings();
			ObjectObjectCursor<String, MappingMetaData> metas = mappings.values().iterator().next().value.iterator().next();
			Map<String, Object> map = (Map<String, Object>) metas.value.getSourceAsMap().get(PROPERTIES);
			meta.addAll(createMetaInfoRes(null, map));
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Get " + index + " mapping error.", e);
		}
		return meta;
	}

	public void deleteIndex(String index) {
		DeleteIndexRequest request = new DeleteIndexRequest();
		request.indices(index + "_*");
		elasticsearchTemplate.getClient().admin().indices().delete(request);
	}

	public long count(String index) {
		NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder().withIndices(index + "_*");
		return elasticsearchTemplate.query(searchQuery.build(), new ResultsExtractor<Long>() {
			@Override
			public Long extract(SearchResponse response) {
				return response.getHits().totalHits;
			}
		});
	}

	public PutIndexTemplateResponse updateTemplate(String templateName, String templateJson) {

		try {
			return elasticsearchTemplate.getClient().admin().indices()
					.putTemplate(new PutIndexTemplateRequest(templateName).source(templateJson, XContentType.JSON)).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;

	}

}