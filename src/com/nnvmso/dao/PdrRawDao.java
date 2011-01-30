package com.nnvmso.dao;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.PdrRaw;

public class PdrRawDao {

	public PdrRaw save(PdrRaw pdr) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(pdr);
		pdr = pm.detachCopy(pdr);
		pm.close();		
		return pdr;
	}
	
	public List<PdrRaw> findByUserId(long userId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(PdrRaw.class);
		q.setFilter("userId == userIdParam");
		q.declareParameters(Key.class.getName() + " userIdParam");
		@SuppressWarnings("unchecked")
		List<PdrRaw> results = (List<PdrRaw>)q.execute(userId);
		results = (List<PdrRaw>)pm.detachCopyAll(results);
		pm.close();
		return results;
		
	}

}
