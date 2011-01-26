package com.nnvmso.service;

import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.MsoIpgDao;
import com.nnvmso.model.MsoIpg;

public class MsoIpgManager {
	protected static final Logger log = Logger.getLogger(MsoIpgManager.class.getName());
	
	private MsoIpgDao msoIpgDao = new MsoIpgDao();
 
	public void create(MsoIpg msoIpg) {
		msoIpgDao.create(msoIpg);
	}
	
	public void save(MsoIpg msoIpg) {
		msoIpgDao.save(msoIpg);
	}
	
	public List<MsoIpg> findByMsoKey(Key msoKey) {
		return msoIpgDao.findByMsoKey(msoKey);
	}

}
