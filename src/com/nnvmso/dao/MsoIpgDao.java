package com.nnvmso.dao;

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.MsoIpg;

public class MsoIpgDao {
	
	public void create(MsoIpg msoIpg) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		msoIpg.setCreateDate(now);
		msoIpg.setUpdateDate(now);
		pm.makePersistent(msoIpg);
		pm.close();		
	}
	
	public MsoIpg save(MsoIpg msoIpg) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		msoIpg.setUpdateDate(new Date());
		pm.makePersistent(msoIpg);
		msoIpg = pm.detachCopy(msoIpg);
		pm.close();		
		return msoIpg;
	}

	public List<MsoIpg> findByMsoKey(Key msoKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(MsoIpg.class);
		q.setFilter("msoKey == msoKeyParam");
		q.declareParameters(Key.class.getName() + " msoKeyParam");
		@SuppressWarnings("unchecked")
		List<MsoIpg> ipg = (List<MsoIpg>)q.execute(msoKey);
		ipg = (List<MsoIpg>)pm.detachCopyAll(ipg);
		pm.close();
		return ipg;
	}
	
}
