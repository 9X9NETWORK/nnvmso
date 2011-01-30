package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.nnvmso.dao.MsoIpgDao;
import com.nnvmso.model.MsoIpg;

public class MsoIpgManager {
	protected static final Logger log = Logger.getLogger(MsoIpgManager.class.getName());
	
	private MsoIpgDao msoIpgDao = new MsoIpgDao();
 
	public void create(MsoIpg msoIpg) {
		Date now = new Date();
		msoIpg.setCreateDate(now);
		msoIpg.setUpdateDate(now);
		this.save(msoIpg);
	}
	
	public void save(MsoIpg msoIpg) {
		msoIpg.setUpdateDate(new Date());
		msoIpgDao.save(msoIpg);
	}
	
	public List<MsoIpg> findAllByMsoId(long msoId) {
		return msoIpgDao.findAllByMsoId(msoId);
	}

}
