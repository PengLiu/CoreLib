package org.coredata.core.framework.instance.processer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeNode implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1391431813760494384L;

	private int parentId;
	private int selfId;
	protected String nodeName;

	protected Map<String, Object> obj;
	protected TreeNode parentNode;
	protected List<TreeNode> childList;

	public TreeNode() {
		initChildList();
	}

	public TreeNode(TreeNode parentNode) {
		setParentNode(parentNode);
		initChildList();
	}

	/**  
	 * 是否没有子节点
	 */
	public boolean isLeaf() {
		if (childList == null) {
			return true;
		} else {
			if (childList.isEmpty()) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**  
	 * 插入一个child节点到当前节点中 
	 */
	public void addChildNode(TreeNode treeNode) {
		initChildList();
		childList.add(treeNode);
	}

	public void initChildList() {
		if (childList == null)
			childList = new ArrayList<TreeNode>();
	}

	public boolean isValidTree() {
		return true;
	}

	/**
	 * 返回当前节点的父辈节点集合
	 */
	public List<TreeNode> getElders() {
		List<TreeNode> elderList = new ArrayList<TreeNode>();
		TreeNode parentNode = this.getParentNode();
		if (parentNode == null) {
			return elderList;
		} else {
			elderList.add(parentNode);
			elderList.addAll(parentNode.getElders());
			return elderList;
		}
	}

	/**
	 * 返回当前节点的晚辈集合 
	 */
	public List<TreeNode> getJuniors() {
		List<TreeNode> juniorList = new ArrayList<TreeNode>();
		List<TreeNode> childList = this.getChildList();
		if (childList == null) {
			return juniorList;
		} else {
			int childNumber = childList.size();
			for (int i = 0; i < childNumber; i++) {
				TreeNode junior = childList.get(i);
				juniorList.add(junior);
				juniorList.addAll(junior.getJuniors());
			}
			return juniorList;
		}
	}

	/**
	 * 返回当前节点的晚辈集合 
	 */
	public List<Map<String, Object>> getAllObj() {
		List<Map<String, Object>> objList = new ArrayList<Map<String, Object>>();
		objList.add(obj);
		List<TreeNode> childList = this.getChildList();
		if (childList == null) {
			return objList;
		} else {
			int childNumber = childList.size();
			for (int i = 0; i < childNumber; i++) {
				TreeNode junior = childList.get(i);
				objList.addAll(junior.getAllObj());
			}
			return objList;
		}
	}

	/**
	 * 返回当前节点的晚辈集合 
	 */
	public void getTreeObj() {
		List<Map<String, Object>> objList = new ArrayList<Map<String, Object>>();
		List<TreeNode> childList = this.getChildList();
		if (childList != null) {
			int childNumber = childList.size();
			for (int i = 0; i < childNumber; i++) {
				TreeNode junior = childList.get(i);
				junior.getTreeObj();
				objList.add(junior.getObj());
			}
			obj.put("child", objList);
		}
	}

	/** 
	 * 返回当前节点的孩子集合 
	 */
	public List<TreeNode> getChildList() {
		return childList;
	}

	/** 
	 * 删除节点和它下面的晚辈 
	 */
	public void deleteNode() {
		TreeNode parentNode = this.getParentNode();
		int id = this.getSelfId();
		if (parentNode != null) {
			parentNode.deleteChildNode(id);
		}
	}

	/** 
	 * 删除当前节点的某个子节点
	 */
	public void deleteChildNode(int childId) {
		List<TreeNode> childList = this.getChildList();
		int childNumber = childList.size();
		for (int i = 0; i < childNumber; i++) {
			TreeNode child = childList.get(i);
			if (child.getSelfId() == childId) {
				childList.remove(i);
				return;
			}
		}
	}

	/** 
	 * 动态的插入一个新的节点到当前树中 
	 */
	public boolean insertJuniorNode(TreeNode treeNode) {
		int juniorParentId = treeNode.getParentId();
		if (this.parentId == juniorParentId) {
			parentNode.addChildNode(treeNode);
			return true;
		} else {
			List<TreeNode> childList = this.getChildList();
			int childNumber = childList.size();
			boolean insertFlag;

			for (int i = 0; i < childNumber; i++) {
				TreeNode childNode = childList.get(i);
				insertFlag = childNode.insertJuniorNode(treeNode);
				if (insertFlag == true)
					return true;
			}
			return false;
		}
	}

	/** 
	 * 找到一颗树中某个节点 
	 */
	public TreeNode findTreeNodeById(int id) {
		if (this.selfId == id)
			return this;
		if (childList.isEmpty() || childList == null) {
			return null;
		} else {
			int childNumber = childList.size();
			for (int i = 0; i < childNumber; i++) {
				TreeNode child = childList.get(i);
				TreeNode resultNode = child.findTreeNodeById(id);
				if (resultNode != null) {
					return resultNode;
				}
			}
			return null;
		}
	}

	public void setChildList(List<TreeNode> childList) {
		this.childList = childList;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public int getSelfId() {
		return selfId;
	}

	public void setSelfId(int selfId) {
		this.selfId = selfId;
	}

	public TreeNode getParentNode() {
		return parentNode;
	}

	public void setParentNode(TreeNode parentNode) {
		this.parentNode = parentNode;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	/**
	 * @return the obj
	 */
	public Map<String, Object> getObj() {
		return obj;
	}

	/**
	 * @param obj the obj to set
	 */
	public void setObj(Map<String, Object> obj) {
		this.obj = obj;
	}
}
