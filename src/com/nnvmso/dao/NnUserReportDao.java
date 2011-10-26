package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.NnUserReport;

public class NnUserReportDao extends GenericDao<NnUserReport>{
	protected static final Logger logger = Logger.getLogger(NnUserReportDao.class.getName());
	
	public NnUserReportDao() {
		super(NnUserReport.class);
	}

	public List<NnUserReport> findSince(Date since) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<NnUserReport> detached = new ArrayList<NnUserReport>();
		try {
			Query query = pm.newQuery(NnUserReport.class);
			query.setFilter("createDate > createDateParam");
			query.declareImports("import java.util.Date");
			query.declareParameters("Date createDateParam");			 
			 
			@SuppressWarnings("unchecked")
			List<NnUserReport> results = (List<NnUserReport>) query.execute(since);
			detached = (List<NnUserReport>)pm.detachCopyAll(results);
		} finally {
			pm.close();
		}
		return detached;		
	}
	
	public List<NnUserReport> findByUser(String token) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<NnUserReport> detached = new ArrayList<NnUserReport>();
		try {
			Query query = pm.newQuery(NnUserReport.class);
			query.setFilter("userToken == tokenParam");
			query.declareParameters("String tokenParam");				
			@SuppressWarnings("unchecked")
			List<NnUserReport> results = (List<NnUserReport>) query.execute(token);
			detached = (List<NnUserReport>)pm.detachCopyAll(results);
		} finally {
			pm.close();
		}
		return detached;		
	}

	public List<NnUserReport> findByUserSince(String token, Date since) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<NnUserReport> detached = new ArrayList<NnUserReport>();
		try {
			Query query = pm.newQuery(NnUserReport.class);
			query.declareImports("import java.util.Date");
			query.setFilter("userToken == tokenParam && createDate > dateParam");
			query.declareParameters("String tokenParam, Date dateParam");				
			@SuppressWarnings("unchecked")
			List<NnUserReport> results = (List<NnUserReport>) query.execute(token, since);
			detached = (List<NnUserReport>)pm.detachCopyAll(results);
		} finally {
			pm.close();
		}
		return detached;		
	}
	
}

