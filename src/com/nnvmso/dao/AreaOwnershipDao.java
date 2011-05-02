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
	
}
