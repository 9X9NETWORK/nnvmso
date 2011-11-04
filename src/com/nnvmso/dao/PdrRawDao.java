package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.NnDevice;
import com.nnvmso.model.NnUser;
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
	
	@SuppressWarnings("unchecked")
	public List<PdrRaw> findDebugging(
			NnUser user, NnDevice device, String session,
			String ip, Date since) {
		List<PdrRaw> detached = new ArrayList<PdrRaw>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<PdrRaw> results = new ArrayList<PdrRaw>();
		try {
			Query q = pm.newQuery(PdrRaw.class);
			if (user != null && session != null) {
				q.setFilter("userId == userIdParam && session == sessionParam");
				q.declareParameters("long userIdParam, String sessionParam");
				results = (List<PdrRaw>)q.execute(user.getKey().getId(), session);
			} else if (device != null && session != null) {
				q.setFilter("deviceToken == deviceIdParam && session == sessionParam");
				q.declareParameters("long userIdParam, String sessionParam");
				results = (List<PdrRaw>)q.execute(device.getKey().getId(), session);
			} else if (user != null) {
				q.setFilter("userId == userIdParam");
				q.declareParameters("long userIdParam");
				results = (List<PdrRaw>)q.execute(user.getKey().getId());				
			} else if (device != null) {
				q.setFilter("deviceId == deviceIdParam");
				q.declareParameters("long deviceIdParam");
				results = (List<PdrRaw>)q.execute(device.getKey().getId());				
			} else if (ip != null) {
				q.declareImports("import java.util.Date");
				q.setFilter("ip == ipParam && createDate > createDateParam");
				q.declareParameters("String ipParam, Date createDateParam");
				results = (List<PdrRaw>)q.execute(ip, since);
			}
			q.setRange(0,199);
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
