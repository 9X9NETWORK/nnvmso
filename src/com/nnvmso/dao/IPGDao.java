package com.nnvmso.dao;

import java.util.Date;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.IPG;

public class IPGDao {

	public void create(IPG ipg) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
//		ipg.setCreateDate(new Date());
//		ipg.setUpdateDate(new Date());
		pm.makePersistent(ipg);
		pm.close();
	}
	
	public IPG findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		IPG detached = null;
		try { 
			IPG ipg = pm.getObjectById(IPG.class, id);
			detached = pm.detachCopy(ipg);
		} catch (JDOObjectNotFoundException e) {			
		}
		pm.close();
		return detached;				
	}
}
