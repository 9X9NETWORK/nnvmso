package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.nnvmso.dao.NnUserShareDao;
import com.nnvmso.model.NnUserShare;

public class NnUserShareManager {

	protected static final Logger log = Logger.getLogger(NnUserShareManager.class.getName());
	
	private NnUserShareDao shareDao = new NnUserShareDao();	
	
	public void create(NnUserShare share, long userId) {
		Date now = new Date();
		share.setUpdateDate(now);
		share.setCreateDate(now);
		shareDao.save(share);
	}	
	
	public NnUserShare save(NnUserShare share) {
		return shareDao.save(share);
	}	
	
	public NnUserShare findById(Long id) {
		return shareDao.findById(id);
	}
	
	public List<NnUserShare> findByUserId(long userId) {
		return shareDao.findByUserId(userId);
	}
			
}