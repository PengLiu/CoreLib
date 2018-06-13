package org.coredata.core.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.filter.DateFilter;
import org.coredata.core.data.filter.DefaultFilterChain;
import org.coredata.core.data.filter.FilterEnum.FilterName;
import org.coredata.core.data.filter.GrokFilter;
import org.coredata.core.data.filter.IFilter;
import org.coredata.core.data.filter.IFilterChain;
import org.coredata.core.data.filter.IPFilter;
import org.coredata.core.data.filter.JsonFilter;
import org.coredata.core.data.filter.SplitFilter;
import org.coredata.core.data.filter.TransforFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.Gson;

public abstract class Reader {	
	
	protected Logger logger = LoggerFactory.getLogger(getClass());

	private ObjectMapper mapper = new ObjectMapper();

	private List<IFilterChain> chains = new ArrayList<>();

	private Fields fields = new Fields();

	private long recordLimit = -1;

	private Set<Integer> indexSelect;

	public abstract void close();

	public abstract void declareOutputFields(OutputFieldsDeclarer declarer);

	public abstract CompletableFuture<JobDetail> execute(RecordCollector collector);
	
	protected JobDetail jobDetail = new JobDetail();

	private IFilter buildFilter(JsonNode json) {
		String name = json.get("name").asText();
		String config = json.get("config").toString();
		if (FilterName.date.name().equals(name)) {
			return new DateFilter().init(config);
		} else if (FilterName.ip.name().equals(name)) {
			return new IPFilter().init(config);
		} else if (FilterName.split.name().equals(name)) {
			return new SplitFilter().init(config);
		} else if (FilterName.json.name().equals(name)) {
			return new JsonFilter().init(config);
		} else if (FilterName.transfor.name().equals(name)) {
			return new TransforFilter().init(config);
		} else if (FilterName.grok.name().equals(name)) {
			return new GrokFilter().init(config);
		} else {
			return null;
		}
	}

	public void prepare(PluginConfig readerConfig) {

		Object selecteds = readerConfig.get(Constants.INDEX_SELECTED);
		if (selecteds != null && StringUtils.isNotEmpty(selecteds.toString())) {
			Gson gson = new Gson();
			int[] sds = gson.fromJson(selecteds.toString(), int[].class);
			indexSelect = new HashSet<Integer>();
			for (int i = sds.length - 1; i >= 0; i--) {
				indexSelect.add(sds[i]);
			}
		}
		Object limit = readerConfig.get(Constants.RECORD_LIMIT);
		if (limit != null) {
			recordLimit = ((Number) limit).longValue();
		}

		String filterConfig = readerConfig.getString(Constants.FILTER_CONFIG);
		if (!StringUtils.isEmpty(filterConfig)) {
			try {
				final ArrayNode filterChainJson = (ArrayNode) (mapper.readTree(filterConfig));
				filterChainJson.forEach(filterChain -> {
					int column = filterChain.get("column").asInt();
					IFilterChain chain = new DefaultFilterChain();
					chain.setColumnIndex(column);
					ArrayNode filtersJson = (ArrayNode) filterChain.get(Constants.FILTER_CONFIG);
					filtersJson.forEach(filterJson -> {
						IFilter filter = buildFilter(filterJson);
						if (filter != null) {
							chain.registFilter(filter);
						}
					});
					chains.add(chain);
				});

			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

	}

	public void doFilter(Record record) {

		if (CollectionUtils.isEmpty(chains)) {
			return;
		}

		for (IFilterChain chain : chains) {
			chain.doFilter(record);
		}

	}

	public void doSelect(Record record) {
		if (indexSelect == null || indexSelect.size() == 0) {
			return;
		}
		int len = record.size();
		//倒序删除不在保留的列内的数据
		for (int i = len - 1; i >= 0; i--) {
			if (!indexSelect.contains(i)) {
				record.remove(i);
			}
		}
	}

	public List<IFilterChain> getChains() {
		return chains;
	}

	public long getRecordLimit() {
		return recordLimit;
	}

	public void setChains(List<IFilterChain> chains) {
		this.chains = chains;
	}

	public Fields getFields() {
		return fields;
	}

	public void addField(String field) {
		this.fields.add(field);
	}

	public void setFields(Fields fields) {
		this.fields = fields;
	}

}
