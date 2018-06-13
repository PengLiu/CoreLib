package org.coredata.core.model.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.coredata.core.model.converter.TransformConverter;
import org.coredata.core.model.transform.TransformModel;
import org.coredata.core.util.common.CloneUtil;
import org.coredata.core.util.encryption.EncryptionAlgorithm.Method;
import org.coredata.core.util.encryption.EncryptionUtil;

/**
 * 清洗模型表对应实体
 * @author sushi
 *
 */
@Entity
@Table(name = "t_transform")
public class TransformEntity extends BaseEntity {

	private static final long serialVersionUID = 461316877461688803L;

	@Column(name = "transform_model")
	@Convert(converter = TransformConverter.class)
	private TransformModel transformModel;

	@Transient
	private TransformModel decryptModel;

	public TransformModel getTransformModel() {
		return transformModel;
	}

	public void setTransformModel(TransformModel transformModel) {
		this.transformModel = transformModel;
	}

	@PostLoad
	public void decryptModel() {
		TransformModel cloneModel = CloneUtil.createCloneObj(transformModel);
		EncryptionUtil.decrypt(cloneModel, TransformModel.class, Method.AES);
		this.decryptModel = cloneModel;
	}

	@PrePersist
	@PreUpdate
	public void encryptModel() {
		EncryptionUtil.encrypt(transformModel, TransformModel.class, Method.AES);
	}

	public TransformModel getDecryptModel() {
		return decryptModel;
	}

}
