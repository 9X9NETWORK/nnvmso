package com.nnvmso.dao;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.NnGuest;

public class NnGuestDao extends GenericDao<NnGuest> {

	public NnGuestDao() {
		super(NnGuest.class);
	}
	
	public NnGuest findByToken(String token) {
		NnGuest guest = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(NnGuest.class);
			query.setFilter("token == tokenParam");
			query.declareParameters("String tokenParam");		
			@SuppressWarnings("unchecked")
			List<NnGuest> results = (List<NnGuest>) query.execute(token);
			if (results.size() > 0) {
				guest = results.get(0);			
			}
			guest = pm.detachCopy(guest);
		} finally {
			pm.close();
		}
		return guest;				
	}

	public void delete(NnGuest guest) {
		if (guest == null) return;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.deletePersistent(guest);
		} finally {
			pm.close();
		}		
	}	
}
