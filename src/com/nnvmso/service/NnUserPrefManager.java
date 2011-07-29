package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.NnUserPrefDao;
import com.nnvmso.model.NnUserPref;

@Service
public class NnUserPrefManager {
	
	protected static final Logger log = Logger.getLogger(NnUserPrefManager.class.getName());
		
	private NnUserPrefDao nnUserPrefDao = new NnUserPrefDao();
	
	public void create(NnUserPref pref) { 
		pref.setCreateDate(new Date());
		nnUserPrefDao.save(pref);
	}

	public NnUserPref save(NnUserPref pref) {
		pref.setUpdateDate(new Date());
		return nnUserPrefDao.save(pref);
	}

	public List<NnUserPref> findByUserId(long userId) {
		return nnUserPrefDao.findByUserId(userId);
	}

	public NnUserPref findByUserIdAndItem(long userId, String item) {		
		return nnUserPrefDao.findByUserIdAndItem(userId, item);
	}
	
}
