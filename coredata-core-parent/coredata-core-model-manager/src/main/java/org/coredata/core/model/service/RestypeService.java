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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.deta.framework.common.tree.Tree;
import com.deta.framework.common.tree.TreeNode;
import com.deta.framework.common.tree.TreeView;

@Service
@Transactional
public class RestypeService {

	private static final String ROOT_TYPE = "rootrestype";

	private static final String NAME = "name";

	private static final String ID = "id";

	private static final String CHILDS = "childs";

	@Autowired
	private RestypeRepository restypeRepository;

	@Autowired
	private RestypeContentProvider restypeContentProvider;

	@Autowired
	private RestypeListProvider restypeListProvider;

	@Autowired
	private RestypeLabelProvider restypeLabelProvider;

	public void save(Restype restype) {
		if (restype == null)
			return;
		String id = restype.getId();
		RestypeEntity restypeEntity = restypeRepository.findById(id);
		if (restypeEntity != null)
			restypeRepository.delete(restypeEntity);
		restypeEntity = new RestypeEntity();
		String fullPath = "/";
		if (restype.getParentid() != null) {
			String pFullPath = findFullPath(restype.getParentid());
			pFullPath = "/".equals(pFullPath) ? "" : pFullPath;
			fullPath = pFullPath + "/" + restype.getId() + "/";
		} else {
			fullPath = fullPath + restype.getId() + "/";
		}
		restype.setFullPath(fullPath);
		restypeEntity.setRestype(restype);
		restypeRepository.save(restypeEntity);
	}

	public void deleteAll() {
		restypeRepository.deleteAll();
	}

	public Restype findById(String id) {
		RestypeEntity restypeEntity = restypeRepository.findById(id);
		if (restypeEntity == null)
			return null;
		return restypeEntity.getRestype();
	}

	public List<Map<String, Object>> findAllRestype(String token) {
		List<Restype> restypes = findByParentid(ROOT_TYPE, token + "%");
		restypeContentProvider.setCustomerId(token);
		TreeView treeView = new TreeView(restypes, restypeContentProvider,
				restypeLabelProvider);
		Tree tree = treeView.buildTree("resTree");
		return processTreeNode(tree, true);
	}

	private List<Map<String, Object>> processTreeNode(Tree tree,
			boolean ignoreClassify) {
		List<TreeNode> roots = tree.getRoots();
		List<Map<String, Object>> results = new ArrayList<>();
		for (TreeNode r : roots) {
			Map<String, Object> node = new HashMap<>();
			List<Map<String, Object>> resultChilds = new ArrayList<>();
			node.put(ID, r.getId());
			node.put(NAME, r.getName());
			Map<String, String> attributes = r.getExtProperty();
			if (attributes != null) {
				attributes.forEach((k, v) -> {
					node.put(k, v);
				});
			}
			if (CollectionUtils.isEmpty(r.getChildren())) {
				Map<String, String> extProperty = new HashMap<>();
				List<Map<String, String>> childs = new ArrayList<>();
				extProperty.put(ID, r.getId());
				extProperty.put(NAME, r.getName());
				if (attributes != null) {
					attributes.forEach((k, v) -> {
						extProperty.put(k, v);
					});
				}
				childs.add(extProperty);
				node.put(CHILDS, childs);
			} else {
				iteratorChildNode(r, resultChilds, ignoreClassify);
				node.put(CHILDS, resultChilds);
			}
			results.add(node);
		}
		return results;
	}

	/**
	 * 该方法用于迭代获取子节点，直到无法获取为止
	 * 
	 * @param r
	 * @return
	 */
	private List<Map<String, Object>> iteratorChildNode(TreeNode r,
			List<Map<String, Object>> resultChilds, boolean ignoreClassify) {
		List<TreeNode> childs = r.getChildren();
		for (TreeNode c : childs) {
			Map<String, Object> child = new HashMap<>();
			if (CollectionUtils.isEmpty(c.getChildren())) {
				child.put(ID, c.getId());
				child.put(NAME, c.getName());
				Map<String, String> extProperty = c.getExtProperty();
				if (extProperty != null) {
					if (!ignoreClassify) {
						String onlyclassify = extProperty.get("onlyclassify");
						if ("true".equals(onlyclassify))
							continue;
					}
					extProperty.forEach((k, v) -> child.put(k, v));
				}
				resultChilds.add(child);
			} else {// 如果下级不为空也需要判定一下
				if (!ignoreClassify) {// 如果忽略classify属性
					Map<String, String> extProperty = c.getExtProperty();
					if (extProperty != null) {
						String onlyclassify = extProperty.get("onlyclassify");
						if ("false".equals(onlyclassify)) {
							child.put(ID, c.getId());
							child.put(NAME, c.getName());
							extProperty.forEach((k, v) -> child.put(k, v));
							resultChilds.add(child);
						}
					}
				}
				iteratorChildNode(c, resultChilds, ignoreClassify);
			}
		}
		return resultChilds;
	}

	public String findFullPath(String resType) {
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
		Restype type = findById(restype);
		if (type == null) {
			return;
		}
		path.add(type.getId());
		if (StringUtils.isEmpty(type.getParentid())) {
			return;
		} else {
			calFullPath(path, type.getParentid());
		}
	}

	/**
	 * 该方法用于返回数据集成资源列表
	 */
	public List<Map<String, Object>> findAllRestypeByType(String type,
			String customerId) {
		List<Map<String, Object>> results = new ArrayList<>();
		List<Restype> roots = new ArrayList<>();
		Restype rootType = findById(type);
		if (rootType == null)
			return results;
		roots.add(rootType);
		restypeContentProvider.setCustomerId(customerId);
		TreeView treeView = new TreeView(roots, restypeContentProvider,
				restypeLabelProvider);
		Tree tree = treeView.buildTree("resTree");
		results = processTreeNode(tree, false);
		return results;
	}

	public Tree findAllResTree(String customerId) {
		List<Restype> restypes = findByParentid(ROOT_TYPE, customerId + "%");
		restypeContentProvider.setCustomerId(customerId);
		TreeView treeView = new TreeView(restypes, restypeContentProvider,
				restypeLabelProvider);
		Tree tree = treeView.buildTree("resTree");
		return tree;
	}

	public List<TreeNode> findAllChildRestype(String customerId) {
		List<TreeNode> results = new ArrayList<>();
		List<Restype> roots = new ArrayList<>();
		Restype rootType = findById("itandiot");
		if (rootType == null)
			return results;
		roots.add(rootType);
		restypeListProvider.setCustomerId(customerId);
		TreeView treeView = new TreeView(roots, restypeListProvider,
				restypeLabelProvider);
		Tree tree = treeView.buildTree("resTree");
		results = processAllChildsNode(tree);
		return results;
	}

	/**
	 * 用于处理相关子资源类型结果
	 * 
	 * @param tree
	 * @return
	 */
	private List<TreeNode> processAllChildsNode(Tree tree) {
		List<TreeNode> roots = tree.getRoots();
		List<TreeNode> results = new ArrayList<>();
		for (TreeNode r : roots) {
			if (CollectionUtils.isEmpty(r.getChildren())) {
				continue;
			}
			List<TreeNode> children = r.getChildren();
			for (TreeNode treeNode : children) {// 循环第一层结果
				results.add(treeNode);
			}
		}
		return results;
	}

	public long findAllResTypeCount() {
		return restypeRepository.count();
	}

	public List<Restype> findByParentid(String id, String token) {
		List<Restype> restypes = new ArrayList<>();
		List<RestypeEntity> entitys = restypeRepository.findByParentid(id,
				token + "%");
		if (CollectionUtils.isEmpty(entitys))
			return restypes;
		entitys.forEach(entity -> restypes.add(entity.getRestype()));
		return restypes;
	}

	public void delete(String id) {
		RestypeEntity entity = restypeRepository.findById(id);
		if (entity == null)
			return;

		Restype restype = entity.getRestype();
		if (restype.getIsSystem() == 0) {
			restypeRepository.deleteById(entity.getId());
		}
	}

	public void save(List<Restype> restypes) {
		for (Restype restype : restypes) {
			save(restype);
		}
	}
}
