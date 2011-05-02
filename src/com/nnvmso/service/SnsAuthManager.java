package com.nnvmso.service;

import java.util.Date;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.SnsAuthDao;
import com.nnvmso.model.SnsAuth;

@Service
public class SnsAuthManager {
	
	protected static final Logger logger = Logger.getLogger(SnsAuthManager.class.getName());
	
	private SnsAuthDao authDao = new SnsAuthDao();
	
	public void create(SnsAuth auth) {
		auth.setCreateDate(new Date());
		authDao.save(auth);
	}
	
	public void delete(SnsAuth auth) {
		authDao.delete(auth);
	}
	
	public SnsAuth findFacebookAuthByMsoId(long msoId) {
		return authDao.findByMsoIdAndType(msoId, SnsAuth.TYPE_FACEBOOK);
	}
	
	public SnsAuth findTitterAuthByMsoId(long msoId) {
		return authDao.findByMsoIdAndType(msoId, SnsAuth.TYPE_TWITTER);
	}
	
	public SnsAuth findPlurkAuthByMsoId(long msoId) {
		return authDao.findByMsoIdAndType(msoId, SnsAuth.TYPE_PLURK);
	}
	
	public SnsAuth findSinaAuthByMsoId(long msoId) {
		return authDao.findByMsoIdAndType(msoId, SnsAuth.TYPE_SINA);
	}
}
