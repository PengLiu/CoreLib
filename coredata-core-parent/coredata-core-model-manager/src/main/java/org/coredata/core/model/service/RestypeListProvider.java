package org.coredata.core.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.model.common.Restype;
import org.coredata.core.model.entities.RestypeEntity;
import org.coredata.core.model.repositories.RestypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.deta.framework.common.tree.IContentProvider;

@Component
public class RestypeListProvider implements IContentProvider {

	@Autowired
	private RestypeRepository restypeRepository;

	public static final Map<String, RestypeEntity> cached = new HashMap<>();

	private String customerId;

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	@Override
	public List<? extends Object> getChildren(Object curElement,
			final int level, final int index) {
		List<Restype> results = new ArrayList<>();
		Restype restype = (Restype) curElement;
		List<RestypeEntity> entitys = restypeRepository
				.findByParentid(restype.getId(), customerId);
		List<Restype> childs = new ArrayList<>();
		if (!CollectionUtils.isEmpty(entitys))
			entitys.forEach(entity -> {
				childs.add(entity.getRestype());
				cached.put(entity.getRestype().getId(), entity);
			});
		if (!CollectionUtils.isEmpty(childs)) {// 如果子不为空，循环判定
			for (Restype c : childs) {
				boolean isAsset = c.getIsAsset();
				if (isAsset)
					results.add(c);
				else {
					String isroot = c.getIsroot();
					if ("true".equals(isroot))
						results.add(c);
				}
			}
		}
		return results;
	}

	@Override
	public List<? extends Object> getRootElement(
			List<? extends Object> treeData, int level, int index) {
		List<Restype> root = new ArrayList<>();
		for (Object obj : treeData) {
			Restype restype = (Restype) obj;
			root.add(restype);
		}
		return root;
	}

	@Override
	public boolean hasChild(Object curElement, final int level,
			final int index) {
		return false;
	}

}
