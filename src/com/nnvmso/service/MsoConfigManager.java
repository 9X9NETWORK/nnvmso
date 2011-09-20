package com.nnvmso.service;

import java.util.Date;
import java.util.List;

import com.nnvmso.dao.MsoConfigDao;
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
		return configDao.save(config);
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

