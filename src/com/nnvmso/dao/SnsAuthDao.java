package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.SnsAuth;

public class SnsAuthDao extends GenericDao<SnsAuth> {
	
	protected static final Logger logger = Logger.getLogger(SnsAuthDao.class.getName());
	
	public SnsAuthDao() {
		super(SnsAuth.class);
	}
	
	public List<SnsAuth> findByMsoId(long msoId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ArrayList<SnsAuth> results = new ArrayList<SnsAuth>();
		
		try {
			Query query = pm.newQuery(SnsAuth.class);
			query.setFilter("msoId == msoIdParam");
			query.declareParameters("long msoIdParam");
			@SuppressWarnings("unchecked")
			List<SnsAuth> tmp = (List<SnsAuth>)query.execute(msoId);
			results = (ArrayList<SnsAuth>) pm.detachCopyAll(tmp);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return results;
	}
	
	public SnsAuth findByMsoIdAndType(long msoId, short type) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ArrayList<SnsAuth> results = new ArrayList<SnsAuth>();
		
		try {
			Query query = pm.newQuery(SnsAuth.class);
			query.setFilter("msoId == msoIdParam");
			query.setFilter("type == typeParam");
			query.declareParameters("long msoIdParam");
			query.declareParameters("short type");
			@SuppressWarnings("unchecked")
			List<SnsAuth> tmp = (List<SnsAuth>)query.execute(msoId, type);
			results = (ArrayList<SnsAuth>) pm.detachCopyAll(tmp);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		if (results.size() > 0)
			return results.get(0);
		else
			return null;
	}

}
