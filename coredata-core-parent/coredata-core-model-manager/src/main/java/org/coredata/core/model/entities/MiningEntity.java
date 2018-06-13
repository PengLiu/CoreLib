package org.coredata.core.model.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.coredata.core.model.converter.MiningConverter;
import org.coredata.core.model.mining.DataminingModel;
import org.coredata.core.util.common.CloneUtil;
import org.coredata.core.util.encryption.EncryptionAlgorithm.Method;
import org.coredata.core.util.encryption.EncryptionUtil;

/**
 * 挖掘模型表对应实体
 * @author sushi
 *
 */
@Entity
@Table(name = "t_mining")
public class MiningEntity extends BaseEntity {

	private static final long serialVersionUID = 5874523764707377795L;

	@Column(name = "mining_model")
	@Convert(converter = MiningConverter.class)
	private DataminingModel miningModel;

	@Transient
	private DataminingModel decryptModel;

	public DataminingModel getMiningModel() {
		return miningModel;
	}

	public void setMiningModel(DataminingModel miningModel) {
		this.miningModel = miningModel;
	}

	@PostLoad
	public void decryptModel() {
		DataminingModel cloneModel = CloneUtil.createCloneObj(miningModel);
		EncryptionUtil.decrypt(cloneModel, DataminingModel.class, Method.AES);
		this.decryptModel = cloneModel;
	}

	@PrePersist
	@PreUpdate
	public void encryptModel() {
		EncryptionUtil.encrypt(miningModel, DataminingModel.class, Method.AES);
	}

	public DataminingModel getDecryptModel() {
		return decryptModel;
	}

}
