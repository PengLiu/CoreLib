package org.coredata.core.data.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class SplitFilter extends BaseFilter {

	private static final Logger logger = LoggerFactory.getLogger(SplitFilter.class);

	@Override
	public void doFilter(int columnIndex, Record record) {
		if (StringUtils.isNoneBlank(actionType) && StringUtils.isNoneBlank(actionRule)) {
			try {
				String oldData = String.valueOf(record.get(columnIndex));
				if (StringUtils.isNotBlank(oldData)) {
					JsonNode actionRuleJson = mapper.readTree(actionRule);
					//切割字符串
					String matchPattern = actionRuleJson.get("splitPattern").textValue();
					//需要增加的index
					List<Integer> indexList = mapper.readValue(actionRuleJson.get("addIndex").toString(), List.class);

					char[] ruleArr = matchPattern.toCharArray();
					List<String> resultList = new ArrayList<>();
					resultList.add((String) record.get(columnIndex));
					for (char rule : ruleArr) {
						resultList = split(resultList, rule);
					}
					//增加数据,涉及动态长度扩展
					for (int i = 0; i < indexList.size(); i++) {
						String str = "";
						if (i < resultList.size()) {
							str = resultList.get(i);
						}
						record.set(indexList.get(i), str);
					}
				}
			} catch (Exception e) {
				logger.info(" transfor error:", e);
			}
		}

	}

	private List<String> split(List<String> dataList, char c) {
		List<String> resultList = new ArrayList<>();
		for (String str : dataList) {
			resultList.addAll(Arrays.asList(str.split(c + "")));
		}
		return resultList;
	}
}
