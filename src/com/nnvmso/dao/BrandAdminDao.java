package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.BrandAdmin;

public class BrandAdminDao extends GenericDao<BrandAdmin> {
	
	protected static final Logger logger = Logger.getLogger(BrandAdminDao.class.getName());
	
	public BrandAdminDao() {
		super(BrandAdmin.class);
	}
	
	public List<BrandAdmin> findByMsoId(long msoId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ArrayList<BrandAdmin> results = new ArrayList<BrandAdmin>();
		
		try {
			Query query = pm.newQuery(BrandAdmin.class);
			query.setFilter("msoId == msoIdParam");
			query.declareParameters("long msoIdParam");
			@SuppressWarnings("unchecked")
			List<BrandAdmin> tmp = (List<BrandAdmin>)query.execute(msoId);
			results = (ArrayList<BrandAdmin>) pm.detachCopyAll(tmp);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return results;
	}
}
