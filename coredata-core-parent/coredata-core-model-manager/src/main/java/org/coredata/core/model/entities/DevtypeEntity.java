package org.coredata.core.model.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.coredata.core.model.common.DevtypeModel;
import org.coredata.core.model.converter.DevtypeConverter;

/**
 * 厂商类型模型表对应实体
 * @author sushi
 *
 */
@Entity
@Table(name = "t_devtype")
public class DevtypeEntity extends BaseEntity {

	private static final long serialVersionUID = -1360494252040975590L;

	@Column(name = "dev_model")
	@Convert(converter = DevtypeConverter.class)
	private DevtypeModel devModel;

	public DevtypeModel getDevModel() {
		return devModel;
	}

	public void setDevModel(DevtypeModel devModel) {
		this.devModel = devModel;
	}

}
