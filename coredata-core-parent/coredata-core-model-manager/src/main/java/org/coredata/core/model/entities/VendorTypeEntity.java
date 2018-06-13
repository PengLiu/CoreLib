package org.coredata.core.model.entities;


import org.coredata.core.model.common.VendorType;
import org.coredata.core.model.converter.VendorTypeConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 厂商型号模型表对应实体
 * @author sushi
 *
 */
@Entity
@Table(name = "t_vendortype")
public class VendorTypeEntity extends BaseEntity {

	private static final long serialVersionUID = 1009346729212959190L;

	@Column(name = "vendortype_model")
	@Convert(converter = VendorTypeConverter.class)
	private VendorType vendorType;

	public VendorType getVendorType() {
		return vendorType;
	}

	public void setVendorType(VendorType vendorType) {
		this.vendorType = vendorType;
	}

}
