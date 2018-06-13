package org.coredata.core.model.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.coredata.core.model.collection.CollectionModel;
import org.coredata.core.model.converter.CollectionConverter;
import org.coredata.core.util.common.CloneUtil;
import org.coredata.core.util.encryption.EncryptionAlgorithm.Method;
import org.coredata.core.util.encryption.EncryptionUtil;

/**
 * 采集模型表对应实体
 * @author sushi
 *
 */
@Entity
@Table(name = "t_collection")
public class CollectionEntity extends BaseEntity {

	private static final long serialVersionUID = 4443965814420263176L;

	@Column(name = "col_model")
	@Convert(converter = CollectionConverter.class)
	private CollectionModel colModel;

	@Transient
	private CollectionModel decryptModel;

	public CollectionModel getColModel() {
		return colModel;
	}

	public void setColModel(CollectionModel colModel) {
		this.colModel = colModel;
	}

	@PostLoad
	public void decryptModel() {
		CollectionModel cloneModel = CloneUtil.createCloneObj(colModel);
		EncryptionUtil.decrypt(cloneModel, CollectionModel.class, Method.AES);
		this.decryptModel = cloneModel;
	}

	@PrePersist
	@PreUpdate
	public void encryptModel() {
		EncryptionUtil.encrypt(colModel, CollectionModel.class, Method.AES);
	}

	public CollectionModel getDecryptModel() {
		return decryptModel;
	}

}
