package org.coredata.core.model.service;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.model.common.DevtypeModel;
import org.coredata.core.model.common.Vendor;
import org.coredata.core.model.common.VendorFirm;
import org.coredata.core.model.common.VendorType;
import org.coredata.core.model.discovery.DiscoveryModel;
import org.coredata.core.model.entities.DevtypeEntity;
import org.coredata.core.model.entities.VendorEntity;
import org.coredata.core.model.entities.VendorTypeEntity;
import org.coredata.core.model.repositories.DevtypeModelRepository;
import org.coredata.core.model.repositories.VendorRepository;
import org.coredata.core.model.repositories.VendorTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class VendorService {

	private static final Logger logger = LoggerFactory.getLogger(VendorService.class);

	@Autowired
	private VendorRepository vendorRepository;

	@Autowired
	private DevtypeModelRepository devtypeModelRepository;

	@Autowired
	private VendorTypeRepository vendorTypeRepository;

	
	public void save(Vendor vendor) {
		if (vendor == null)
			return;
		String id = vendor.getId();
		VendorEntity vendorEntity = vendorRepository.findById(id);
		if (vendorEntity != null)
			vendorRepository.delete(vendorEntity);
		vendorEntity = new VendorEntity();
		vendorEntity.setVendorModel(vendor);
		vendorRepository.save(vendorEntity);
	}

	
	public void saveDevtypeModel(DevtypeModel model) {
		if (model == null)
			return;
		String sysoid = model.getSysobjectid();
		DevtypeEntity devtypeEntity = null;
		if (!StringUtils.isEmpty(sysoid)) {
			devtypeEntity = devtypeModelRepository.findBySysobjectid(sysoid);
			if (devtypeEntity != null) {
				logger.error("Has the same sysoid.Sysoid is : " + sysoid + ".");
				devtypeModelRepository.delete(devtypeEntity);
			}
		}
		devtypeEntity = new DevtypeEntity();
		devtypeEntity.setDevModel(model);
		devtypeModelRepository.save(devtypeEntity);
	}

	
	public void deleteAll() {
		vendorRepository.deleteAll();
	}

	
	public DevtypeModel findDevtypeModelBySysoid(String sysoid) {
		DevtypeEntity devtypeEntity = devtypeModelRepository.findBySysobjectid(sysoid);
		if (devtypeEntity == null)
			return null;
		return devtypeEntity.getDevModel();
	}

	
	public void saveVendorType(Map<String, Set<DiscoveryModel>> models) {
		//保存之前，先清空原有内容
		vendorTypeRepository.deleteAll();
		if (models == null || models.size() <= 0)
			return;
		models.forEach((k, v) -> {
			VendorType type = new VendorType();
			type.setRestype(k);
			List<VendorFirm> firms = new ArrayList<>();
			v.forEach(d -> {
				VendorFirm firm = new VendorFirm();
				firm.setId(d.getId());
				firm.setName(d.getName());
				firms.add(firm);
			});
			type.getFirms().addAll(firms);
			VendorTypeEntity entity = new VendorTypeEntity();
			entity.setVendorType(type);
			vendorTypeRepository.save(entity);
		});
	}

	
	public long findAllVendorTypeCount() {
		return vendorTypeRepository.count();
	}

	
	public long findAllVendorCount() {
		return vendorRepository.count();
	}

	
	public long findAllDevtypeCount() {
		return devtypeModelRepository.count();
	}

	
	public VendorTypeEntity findByRestype(String restype) {
		List<VendorTypeEntity> vendortypes = vendorTypeRepository.findByRestype(restype);
		return CollectionUtils.isEmpty(vendortypes) ? null : vendortypes.get(0);
	}

	
	public void onlySaveVendorType(VendorTypeEntity vendorType) {
		vendorTypeRepository.save(vendorType);
	}

	
	public List<VendorType> findVendorTypeEntityByRestype(String restype) {
		List<VendorType> vendortypes = new ArrayList<>();
		List<VendorTypeEntity> entitys = vendorTypeRepository.findByRestype(restype);
		if (CollectionUtils.isEmpty(entitys))
			return vendortypes;
		entitys.forEach(entity -> vendortypes.add(entity.getVendorType()));
		return vendortypes;
	}

}
