package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.PdrRaw;

public class PdrRawDao {

	public PdrRaw save(PdrRaw pdr) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(pdr);
			pdr = pm.detachCopy(pdr);
		} finally {
			pm.close();
		}
		return pdr;
	}

	public List<PdrRaw> findByUser(String token) {
		List<PdrRaw> detached = new ArrayList<PdrRaw>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(PdrRaw.class);
			q.setFilter("userToken == userTokenParam");
			q.declareParameters("String userTokenParam");
			@SuppressWarnings("unchecked")
			List<PdrRaw> results = (List<PdrRaw>)q.execute(token);
			detached = (List<PdrRaw>)pm.detachCopyAll(results);
		} finally {
			pm.close();
		}
		return detached;		
	}
	
	public List<PdrRaw> findByUserAndSession(String token, String session) {
		List<PdrRaw> detached = new ArrayList<PdrRaw>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(PdrRaw.class);
			q.setFilter("userToken == userTokenParam && session == sessionParam");
			q.declareParameters("String userTokenParam, String sessionParam");
			@SuppressWarnings("unchecked")
			List<PdrRaw> results = (List<PdrRaw>)q.execute(token, session);
			detached = (List<PdrRaw>)pm.detachCopyAll(results);
		} finally {
			pm.close();
		}
		return detached;		
	}
	
	public List<PdrRaw> findByUserId(long userId) {
		List<PdrRaw> detached = new ArrayList<PdrRaw>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(PdrRaw.class);
			q.setFilter("userId == userIdParam");
			q.declareParameters(Key.class.getName() + " userIdParam");
			@SuppressWarnings("unchecked")
			List<PdrRaw> results = (List<PdrRaw>)q.execute(userId);
			detached = (List<PdrRaw>)pm.detachCopyAll(results);
		} finally {
			pm.close();
		}
		return detached;		
	}

}
