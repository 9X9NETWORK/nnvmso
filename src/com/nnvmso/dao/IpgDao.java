package com.nnvmso.dao;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.NnUser;

public class IpgDao {

	protected static final Logger log = Logger.getLogger(IpgDao.class.getName());
		
	public Ipg save(Ipg ipg) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(ipg);
		ipg = pm.detachCopy(ipg);
		pm.close();
		return ipg;
	}	
	
	public Ipg findById(Long id) {
		log.info("IpgManager.findById(" + id + ")");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Ipg ipg = null, detached = null;
		try {
			DateFormat df = DateFormat.getDateInstance();
			ipg = (Ipg)pm.getObjectById(Ipg.class, id);
			log.info("ipg channel count: " + ipg.getChannels().size() + ";owner: " + ipg.getUserId() + ";ipg date: " + df.format(ipg.getCreateDate())); 
			detached = (Ipg)pm.detachCopy(ipg);
		} catch (JDOObjectNotFoundException e) {
		}
		pm.close();
		return detached;
	}	
	
	public List<Ipg> findByUserId(long userId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Ipg.class);
		query.setFilter("userId == userIdParam");
		query.declareParameters("long userIdParam");
		@SuppressWarnings("unchecked")
		List<Ipg> results = (List<Ipg>)query.execute(userId);
		log.info("ipg count = " + results.size());
		results = (List<Ipg>)pm.detachCopyAll(results);
		pm.close();
		return results;
	}	
		
}
