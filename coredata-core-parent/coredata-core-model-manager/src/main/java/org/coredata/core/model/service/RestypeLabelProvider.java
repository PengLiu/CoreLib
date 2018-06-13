package org.coredata.core.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.model.common.Restype;
import org.coredata.core.model.entities.RestypeEntity;
import org.coredata.core.model.repositories.RestypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deta.framework.common.tree.ILabelProvider;
import com.deta.framework.common.tree.TreeTypeEnum;

@Component
public class RestypeLabelProvider implements ILabelProvider {

	@Autowired
	private RestypeRepository restypeRepository;

	@Override
	public Map<String, String> getAttributes(final Object curElement, int level,
			int index) {
		Map<String, String> attributes = new HashMap<>();
		Restype restype = (Restype) curElement;
		attributes.put("desc", restype.getDesc());
		attributes.put("isDefault",
				restype.getDefaultType() == null
						? "1"
						: restype.getDefaultType().toString());
		attributes.put("onlyclassify",
				String.valueOf(restype.isOnlyclassify()));
		attributes.put("fullpath", findFullPath(restype.getId()));
		attributes.put("isSystem", String.valueOf(restype.getIsSystem()));
		return attributes;
	}

	@Override
	public String getCheckId(final Object curElement, int level, int index) {
		return null;
	}

	@Override
	public String getIcon(final Object curElement, final int level,
			final int index) {
		return null;
	}

	@Override
	public String getLabelId(final Object curElement, final int level,
			final int index) {
		Restype restype = (Restype) curElement;
		return restype.getId();
	}

	@Override
	public String getLabelName(final Object curElement, final int level,
			final int index) {
		Restype restype = (Restype) curElement;
		return restype.getName();
	}

	@Override
	public TreeTypeEnum getLabelType(final Object curElement, final int level,
			final int index) {
		return TreeTypeEnum.NORMAL;
	}

	@Override
	public boolean isCheck(final Object curElement, int level, int index) {
		return false;
	}

	@Override
	public boolean isClick(final Object curElement, int level, int index) {
		return false;
	}

	@Override
	public boolean isDefaultIcon(final Object curElement, final int level,
			final int index) {
		return false;
	}

	private String findFullPath(String resType) {
		List<String> path = new ArrayList<>();
		calFullPath(path, resType);
		String fullPath = "/";
		for (int i = path.size() - 2; i >= 0; i--) {
			fullPath += path.get(i);
			if (i > 0) {
				fullPath += "/";
			}
		}
		return fullPath;
	}

	private void calFullPath(List<String> path, String restype) {
		RestypeEntity type = RestypeListProvider.cached.get(restype);
		if (type == null) {
			type = restypeRepository.findById(restype);
			if (type == null || type.getRestype() == null) {
				return;
			}
			RestypeListProvider.cached.put(restype, type);
		}
		path.add(type.getRestype().getId());
		String parentid = type.getRestype().getParentid();
		if (StringUtils.isEmpty(parentid)) {
			return;
		} else {
			calFullPath(path, parentid);
		}
	}

}
