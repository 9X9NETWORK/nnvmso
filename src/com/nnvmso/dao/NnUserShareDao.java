package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.NnUserShare;

public class NnUserShareDao {

	protected static final Logger log = Logger.getLogger(NnUserShare.class.getName());
		
	public NnUserShare save(NnUserShare share) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(share);
			share= pm.detachCopy(share);
		} finally {
			pm.close();
		}
		return share;
	}	
	
	public NnUserShare findById(Long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		NnUserShare share= null, detached = null;
		try {
			share = (NnUserShare)pm.getObjectById(NnUserShare.class, id); 
			detached = (NnUserShare)pm.detachCopy(share);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();			
		}
		return detached;
	}	
	
	// count the number that program be shared
	public List<NnUserShare> findByProgramId(long programId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<NnUserShare> detached = new ArrayList<NnUserShare>();
		try {
			Query query = pm.newQuery(Ipg.class);
			query.setFilter("programId == programIdParam");
			query.declareParameters("long programIdParam");
			@SuppressWarnings("unchecked")
			List<NnUserShare> results = (List<NnUserShare>)query.execute(programId);
			log.info("ipg count = " + results.size());
			detached = (List<NnUserShare>)pm.detachCopyAll(results);
		} finally {
			pm.close();
		}
		return detached;
	}
	
	public List<NnUserShare> findByUserId(long userId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<NnUserShare> detached = new ArrayList<NnUserShare>();
		try {
			Query query = pm.newQuery(NnUserShare.class);
			query.setFilter("userId == userIdParam");
			query.declareParameters("long userIdParam");
			@SuppressWarnings("unchecked")
			List<NnUserShare> results = (List<NnUserShare>)query.execute(userId);
			log.info("ipg count = " + results.size());
			detached = (List<NnUserShare>)pm.detachCopyAll(results);
		} finally {
			pm.close();
		}
		return detached;
	}	
		
}
