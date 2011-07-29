package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.NnUserPref;

public class NnUserPrefDao extends GenericDao<NnUserPref> {

	protected static final Logger log = Logger.getLogger(NnUserPref.class.getName());
	
	public NnUserPrefDao() {
		super(NnUserPref.class);
	}	
	
	public NnUserPref save(NnUserPref pref) {
		if (pref == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(pref);
			pref = pm.detachCopy(pref);
		} finally {
			pm.close();
		}
		return pref;
	}

	public List<NnUserPref> findByUserId(long userId) {
		List<NnUserPref> pref = new ArrayList<NnUserPref>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(NnUserPref.class);
			query.setFilter("userId == userIdParam");
			query.declareParameters("long userIdParam");		
			@SuppressWarnings("unchecked")
			List<NnUserPref> results = (List<NnUserPref>) query.execute(userId);
			pref = (List<NnUserPref>) pm.detachCopyAll(results);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return pref;		
	}	

	public NnUserPref findByUserIdAndItem(long userId, String item) {
		NnUserPref pref = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(NnUserPref.class);
			query.setFilter("userId == userIdParam && item == itemParam");
			query.declareParameters("long userIdParam, String itemParam");
			@SuppressWarnings("unchecked")
			List<NnUserPref> results = (List<NnUserPref>) query.execute(userId, item);
			if (results.size() > 0) {		
				pref = (NnUserPref) pm.detachCopy(results.get(0));
			}
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return pref;
	}
	
}
