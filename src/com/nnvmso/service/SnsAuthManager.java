package com.nnvmso.service;

import java.util.Date;
import java.util.List;
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
		auth.setEnabled(true);
		authDao.save(auth);
	}
	
	public void save(SnsAuth auth) {
		authDao.save(auth);
	}
	
	public void delete(SnsAuth auth) {
		authDao.delete(auth);
	}
	
	public List<SnsAuth> findAllByMsoId(long msoId) {
		return authDao.findAllByMsoId(msoId);
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
	
	public SnsAuth findMsoIdAndType(Long msoId, Short type) {
		return authDao.findByMsoIdAndType(msoId, type);
	}
}
