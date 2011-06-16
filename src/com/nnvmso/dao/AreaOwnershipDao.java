package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.AreaOwnership;

public class AreaOwnershipDao extends GenericDao<AreaOwnership> {
	
	protected static final Logger logger = Logger.getLogger(AreaOwnershipDao.class.getName());
	
	public AreaOwnershipDao() {
		super(AreaOwnership.class);
	}
	
	public List<AreaOwnership> findByUserId(long userId) {		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ArrayList<AreaOwnership> detached = new ArrayList<AreaOwnership>();
		
		try {
			Query query = pm.newQuery(AreaOwnership.class);
			query.setFilter("userId == userIdParam");
			query.declareParameters("long userIdParam");
			@SuppressWarnings("unchecked")
			List<AreaOwnership> tmp = (List<AreaOwnership>)query.execute(userId);
			detached = (ArrayList<AreaOwnership>) pm.detachCopyAll(tmp);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return detached;
	}

	public AreaOwnership findByUserIdAndAreaNo(long userId, short areaNo) {		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		AreaOwnership detached = null;		
		try {
			Query query = pm.newQuery(AreaOwnership.class);
			query.setFilter("userId == userIdParam && areaNo == areaNoParam");
			query.declareParameters("long userIdParam, short areaNoParam");
			@SuppressWarnings("unchecked")
			List<AreaOwnership> result = (List<AreaOwnership>)query.execute(userId, areaNo);
			if (result.size() > 0) {
				detached = pm.detachCopy(result.get(0));
			} 
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return detached;
	}

	public AreaOwnership findByUserIdAndSetId(long userId, long setId) {		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		AreaOwnership detached = null;		
		try {
			Query query = pm.newQuery(AreaOwnership.class);
			query.setFilter("userId == userIdParam && setId == setIdParam");
			query.declareParameters("long userIdParam, short setIdParam");
			@SuppressWarnings("unchecked")
			List<AreaOwnership> result = (List<AreaOwnership>)query.execute(userId, setId);
			if (result.size() > 0) {
				detached = pm.detachCopy(result.get(0));
			} 
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return detached;
	}
	
}
