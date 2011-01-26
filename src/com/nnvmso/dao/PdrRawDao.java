package com.nnvmso.dao;

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.PdrRaw;

public class PdrRawDao {
	public void create(PdrRaw pdr) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		pdr.setCreateDate(now);
		pdr.setUpdateDate(now);
		pm.makePersistent(pdr);
		pm.close();		
	}

	public PdrRaw save(PdrRaw pdr) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		pdr.setUpdateDate(now);
		pm.makePersistent(pdr);
		pdr = pm.detachCopy(pdr);
		pm.close();		
		return pdr;
	}
	
	public List<PdrRaw> findByUserKey(Key userKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(PdrRaw.class);
		q.setFilter("userKey == userKeyParam");
		q.declareParameters(Key.class.getName() + " userKeyParam");
		@SuppressWarnings("unchecked")
		List<PdrRaw> results = (List<PdrRaw>)q.execute(userKey);
		results = (List<PdrRaw>)pm.detachCopyAll(results);
		pm.close();
		return results;
		
	}

}
