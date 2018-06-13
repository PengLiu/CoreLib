package org.coredata.core.model.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.coredata.core.model.converter.DecisionConverter;
import org.coredata.core.model.decision.DecisionModel;

/**
 * 决策模型表对应实体
 * @author sushi
 *
 */
@Entity
@Table(name = "t_decision")
public class DecisionEntity extends BaseEntity {

	private static final long serialVersionUID = 8209784620505691322L;

	@Column(name = "decision_model")
	@Convert(converter = DecisionConverter.class)
	private DecisionModel decisionModel;

	public DecisionModel getDecisionModel() {
		return decisionModel;
	}

	public void setDecisionModel(DecisionModel decisionModel) {
		this.decisionModel = decisionModel;
	}

}
