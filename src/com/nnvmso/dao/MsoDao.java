package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.JDOObjectNotFoundException;

import com.google.appengine.api.datastore.Key;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.Mso;

public class MsoDao {
	
	protected static final Logger logger = Logger.getLogger(MsoDao.class.getName());
	
	public Mso save(Mso mso) {
		if (mso == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(mso);
			mso = pm.detachCopy(mso);
		} finally {
			pm.close();
		}
		return mso;
	}
	
	public Mso findByName(String name) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Mso detached = null; 
		try {
			Query query = pm.newQuery(Mso.class);
			name = name.toLowerCase();
			query.setFilter("nameSearch == '" + name + "'");
			@SuppressWarnings("unchecked")
			List<Mso> results = (List<Mso>) query.execute(name);
			if (results.size() > 0) {
				detached = pm.detachCopy(results.get(0));
			}
		} finally {
			pm.close();
		}
		return detached;
	}
	
	public List<Mso> findByType(short type) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Mso> detached = new ArrayList<Mso>();
		try {
			Query query = pm.newQuery(Mso.class);
			query.setFilter("type == " + type);
			@SuppressWarnings("unchecked")
			List<Mso> results = (List<Mso>) query.execute(type);
			detached = (List<Mso>)pm.detachCopyAll(results);
		} finally {
			pm.close();
		}
		return detached;
	}
	
	public Mso findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Mso mso = null;
		logger.info("id == '" + id + "'");
		try {
			mso = pm.getObjectById(Mso.class, id);
			mso = pm.detachCopy(mso);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();			
		}
		return mso;
	}
	
	public List<Mso> findAll() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Mso> detached = new ArrayList<Mso>();
		try {
			Query query = pm.newQuery(Mso.class);
			@SuppressWarnings("unchecked")
			List<Mso> results = (List<Mso>) query.execute();
			detached = (List<Mso>)pm.detachCopyAll(results);
		} finally {
			pm.close();
		}
		return detached;
	}
	
	public Mso findByKey(Key key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Mso mso = null;
		try {
			mso = pm.getObjectById(Mso.class, key);
			mso = pm.detachCopy(mso);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();			
		}
		return mso;
	}
}
