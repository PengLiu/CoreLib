package org.coredata.core.model.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.coredata.core.model.common.Restype;
import org.coredata.core.model.converter.RestypeConverter;

/**
 * 资产类型表对应的实体
 * @author sushi
 *
 */
@Entity
@Table(name = "t_restype")
public class RestypeEntity extends BaseEntity {

	private static final long serialVersionUID = -2368041670014076353L;

	@Column(name = "restype_model")
	@Convert(converter = RestypeConverter.class)
	private Restype restype;

	public Restype getRestype() {
		return restype;
	}

	public void setRestype(Restype restype) {
		this.restype = restype;
	}

}
