package com.nnvmso.service;

import com.nnvmso.dao.MsoConfigDao;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoConfig;

public class MsoConfigManager {

	private MsoConfigDao configDao = new MsoConfigDao();
	
	public MsoConfig save(MsoConfig config) {
		return configDao.save(config);
	}
		
	public MsoConfig findByMsoAndItem(Mso mso, String item) {
		return configDao.findByMsoAndItem(mso, item);
	}
	
}

