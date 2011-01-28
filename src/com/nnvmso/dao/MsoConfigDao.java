package com.nnvmso.dao;

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoConfig;

public class MsoConfigDao {
	
	public MsoConfig save(MsoConfig config) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		if (config.getCreateDate() == null) {config.setCreateDate(now);};
		config.setUpdateDate(now);
		pm.makePersistent(config);
		config = pm.detachCopy(config);
		pm.close();		
		return config;
	}
	
	public MsoConfig findByMsoAndItem(Mso mso, String item) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(MsoConfig.class);
		query.setFilter("msoKey == msoKeyParam && item == itemParam");		
		query.declareParameters(Key.class.getName() + " msoKeyParam" + ", String itemParam");				
		@SuppressWarnings("unchecked")
		List<MsoConfig> results = (List<MsoConfig>) query.execute(mso.getKey(), item);
		MsoConfig config = null;
		if (results.size() > 0) {
			config = results.get(0);
			System.out.println("found it!");
			config = pm.detachCopy(config);
		}
		pm.close();	
		return config;		
	}
	
	
}
