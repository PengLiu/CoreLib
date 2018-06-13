package org.coredata.core.model.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.coredata.core.model.converter.DiscoveryConverter;
import org.coredata.core.model.discovery.DiscoveryModel;
import org.coredata.core.util.common.CloneUtil;
import org.coredata.core.util.encryption.EncryptionAlgorithm.Method;
import org.coredata.core.util.encryption.EncryptionUtil;

/**
 * 发现模型表对应实体
 * @author sushi
 *
 */
@Entity
@Table(name = "t_discovery")
public class DiscoveryEntity extends BaseEntity {

	private static final long serialVersionUID = -2914125448347981088L;

	@Column(name = "dis_model")
	@Convert(converter = DiscoveryConverter.class)
	private DiscoveryModel disModel;

	@Transient
	private DiscoveryModel decryptModel;

	public DiscoveryModel getDisModel() {
		return disModel;
	}

	public void setDisModel(DiscoveryModel disModel) {
		this.disModel = disModel;
	}

	@PostLoad
	public void decryptModel() {
		DiscoveryModel cloneModel = CloneUtil.createCloneObj(disModel);
		EncryptionUtil.decrypt(cloneModel, DiscoveryModel.class, Method.AES);
		this.decryptModel = cloneModel;
	}

	@PrePersist
	@PreUpdate
	public void encryptModel() {
		EncryptionUtil.encrypt(disModel, DiscoveryModel.class, Method.AES);
	}

	public DiscoveryModel getDecryptModel() {
		return decryptModel;
	}

}
