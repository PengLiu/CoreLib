package org.coredata.core.sdk.api.util;

import java.util.LinkedHashMap;
import java.util.List;

import org.coredata.core.data.vo.ColumnMeta;
import org.coredata.core.data.vo.TableMeta;

import com.alibaba.fastjson.JSON;

public class ElasticsearchTemplateUtil {

	public static String getEsTempalte(final TableMeta tableMeta) {
		List<ColumnMeta> cols = tableMeta.getColumns();
		int[] selects = tableMeta.getIndexSelected();
		LinkedHashMap<String, ElasticsearchTempateType> map = new LinkedHashMap<String, ElasticsearchTempateType>();
		for (int i : selects) {
			String name = cols.get(i).getName();
			ElasticsearchTempateType et = new ElasticsearchTempateType();
			String type = cols.get(i).getType();
			et.setType(type);
			map.put(name, et);
		}
		String template = JSON.toJSONString(map);
		return template;
	}
}
