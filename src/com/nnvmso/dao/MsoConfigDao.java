package com.nnvmso.dao;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.MsoConfig;

public class MsoConfigDao {
	
	public MsoConfig save(MsoConfig config) {
		if (config == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(config);
		config = pm.detachCopy(config);
		pm.close();		
		return config;
	}
	
	public MsoConfig findByMsoIdAndItem(long msoId, String item) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(MsoConfig.class);
		query.setFilter("msoId == msoIdParam && item == itemParam");		
		query.declareParameters("long msoIdParam" + ", String itemParam");				
		@SuppressWarnings("unchecked")
		List<MsoConfig> results = (List<MsoConfig>) query.execute(msoId, item);
		MsoConfig config = null;
		if (results.size() > 0) {
			config = results.get(0);
			config = pm.detachCopy(config);
		}
		pm.close();	
		return config;		
	}
	
	
}
