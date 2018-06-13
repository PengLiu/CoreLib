package org.coredata.core.data;

import java.util.LinkedList;
import java.util.List;

import org.coredata.core.data.vo.ColumnMeta;
import org.coredata.core.data.vo.TableMeta;
import org.coredata.core.entities.CommEntity;
import org.coredata.core.util.common.MethodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public abstract class Writer {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	private boolean isGenerateEntity;
	private String token;
	private LinkedList<Integer> idSelects = new LinkedList<Integer>();
	private LinkedList<Integer> nameSelects = new LinkedList<Integer>();

	public void prepare(PluginConfig writerConfig, TableMeta tableMeta, String token) {
		this.token = token;
		hasBusiness(tableMeta);
	}

	public abstract void execute(Record record);

	public abstract void close();

	private boolean hasBusiness(TableMeta tableMeta) {
		if (tableMeta == null) {
			return false;
		}
		List<ColumnMeta> cols = tableMeta.getColumns();
		if (cols != null && !cols.isEmpty()) {
			int temp = 0;
			for (int i = 0; i < cols.size(); i++) {
				ColumnMeta col = cols.get(i);
				if (col.isUse()) {
					if (col.isUseByInstId()) {
						if (!isGenerateEntity) {
							isGenerateEntity = true;
						}
						idSelects.add(i - temp);
					}
					if (col.isUseByInstName()) {
						nameSelects.add(i - temp);
					}
				} else {
					temp++;
				}
			}
		}
		return false;
	}

	public boolean isGenerateEntity() {
		return isGenerateEntity;
	}

	public void setGenerateEntity(boolean isGenerateEntity) {
		this.isGenerateEntity = isGenerateEntity;
	}

	public CommEntity getEntity(Record record) {
		StringBuffer eb = new StringBuffer();
		for (Integer idIndex : idSelects) {
			eb.append(record.get(idIndex));
		}
		eb.append(token);
		String entityId = MethodUtil.md5(eb.toString());

		StringBuffer nb = new StringBuffer();
		for (Integer nameIndex : nameSelects) {
			nb.append(record.get(nameIndex)).append("_");
		}
		String name = nb.toString();
		CommEntity ce = new CommEntity();
		ce.setEntityId(entityId);
		if (!StringUtils.isEmpty(name)) {
			ce.setName(name.substring(0, name.length() - 1));
		}
		ce.setToken(token);
		ce.setType("importor");
		return ce;
	}
}
