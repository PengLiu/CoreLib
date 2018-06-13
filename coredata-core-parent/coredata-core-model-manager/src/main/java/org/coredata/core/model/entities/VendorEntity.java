package org.coredata.core.model.entities;


import org.coredata.core.model.common.Vendor;
import org.coredata.core.model.converter.VendorConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 厂商模型表对应实体
 * @author sushi
 *
 */
@Entity
@Table(name = "t_vendor")
public class VendorEntity extends BaseEntity {

	private static final long serialVersionUID = -7010064642729263753L;

	@Column(name = "vendor_model")
	@Convert(converter = VendorConverter.class)
	private Vendor vendorModel;

	public Vendor getVendorModel() {
		return vendorModel;
	}

	public void setVendorModel(Vendor vendorModel) {
		this.vendorModel = vendorModel;
	}

}
