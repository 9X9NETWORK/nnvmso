package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.BrandAdminDao;
import com.nnvmso.dao.NnUserDao;
import com.nnvmso.model.BrandAdmin;
import com.nnvmso.model.NnUser;

@Service
public class BrandAdminManager {
	
	protected static final Logger logger = Logger.getLogger(BrandAdmin.class.getName());
	
	private BrandAdminDao adminDao = new BrandAdminDao();
	
	public void create(BrandAdmin admin) {
		admin.setCreateDate(new Date());
		adminDao.save(admin);
	}
	
	public void delete(BrandAdmin admin) {
		adminDao.delete(admin);
	}
	
	public List<NnUser> findAdminUsersByMsoId(long msoId) {
		NnUserDao userDao = new NnUserDao();
		List<BrandAdmin> adminList = adminDao.findByMsoId(msoId);
		List<NnUser> results = new ArrayList<NnUser>();
		
		for (BrandAdmin admin : adminList) {
			NnUser user = userDao.findById(admin.getUserId());
			if (user != null)
				results.add(user);
		}
		return results;
	}
}
