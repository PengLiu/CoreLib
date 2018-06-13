package org.coredata.core.model.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.coredata.core.model.action.model.ActionModel;
import org.coredata.core.model.converter.ActionConverter;

/**
 * 动作模型表对应实体
 * @author sushi
 *
 */
@Entity
@Table(name = "t_action")
public class ActionEntity extends BaseEntity {

	private static final long serialVersionUID = 7662331274415154589L;

	@Column(name = "action_model")
	@Convert(converter = ActionConverter.class)
	private ActionModel actionModel;

	public ActionModel getActionModel() {
		return actionModel;
	}

	public void setActionModel(ActionModel actionModel) {
		this.actionModel = actionModel;
	}

}
