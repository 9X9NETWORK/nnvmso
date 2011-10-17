package com.nnvmso.service;

import java.util.Date;
import java.util.List;

import net.sf.jsr107cache.Cache;

import com.nnvmso.dao.MsoConfigDao;
import com.nnvmso.lib.CacheFactory;
import com.nnvmso.model.MsoConfig;

public class MsoConfigManager {

	private MsoConfigDao configDao = new MsoConfigDao();

	public MsoConfig create(MsoConfig config) {
		Date now = new Date();
		config.setCreateDate(now);
		config.setUpdateDate(now);
		return configDao.save(config);
	}
	
	public MsoConfig save(MsoConfig config) {
		config.setUpdateDate(new Date());
		if (config.getKey().equals(MsoConfig.RO)) {
			Cache cache = CacheFactory.get();
			if (cache != null)
				cache.put(MsoConfig.RO, config.getValue());
		}			
		return configDao.save(config);
	}
	
	public boolean isInReadonlyMode() {
		boolean readonly = false;
		MsoConfig config = configDao.findByItem(MsoConfig.RO);
		if (config != null && config.getValue().equals("1")) {
			readonly = true;
		}
		return readonly;
	}
	
	public MsoConfig findByMsoIdAndItem(long msoId, String item) {
		return configDao.findByMsoIdAndItem(msoId, item);
	}

	public List<MsoConfig> findAllByMsoId(long msoId) {
		return configDao.findByMsoId(msoId);
	}
	
	public MsoConfig findByItem(String item) {
		return configDao.findByItem(item);
	}
}

