package org.coredata.core.model.service;

import java.util.ArrayList;
import java.util.List;

import org.coredata.core.model.common.Restype;
import org.coredata.core.model.entities.RestypeEntity;
import org.coredata.core.model.repositories.RestypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.deta.framework.common.tree.IContentProvider;

@Component
public class RestypeContentProvider implements IContentProvider {

	@Autowired
	private RestypeRepository restypeRepository;

	private String customerId;

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	@Override
	public List<? extends Object> getChildren(Object curElement,
			final int level, final int index) {
		Restype restype = (Restype) curElement;
		List<RestypeEntity> entitys = restypeRepository
				.findByParentidAndIsroot(restype.getId(), "true", customerId);
		List<Restype> restypes = new ArrayList<>();
		if (CollectionUtils.isEmpty(entitys))
			return null;
		entitys.forEach(e -> restypes.add(e.getRestype()));
		return restypes;
	}

	@Override
	public List<? extends Object> getRootElement(
			List<? extends Object> treeData, final int level, final int index) {
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
