package com.nnvmso.dao;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.NnDevice;
import com.nnvmso.model.NnUser;

public class NnDeviceDao extends GenericDao<NnDevice> {

	protected static final Logger logger = Logger.getLogger(NnDeviceDao.class.getName());
	
	public NnDeviceDao() {
		super(NnDevice.class);
	}
	
	public NnDevice findByToken(String token) {
		NnDevice device = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(NnDevice.class);
			query.setFilter("token == tokenParam");
			query.declareParameters("String tokenParam");		
			@SuppressWarnings("unchecked")
			List<NnDevice> results = (List<NnDevice>) query.execute(token);
			if (results.size() > 0) {
				device = results.get(0);			
			}
			device = pm.detachCopy(device);
		} finally {
			pm.close();
		}
		return device;
	}
	
}
