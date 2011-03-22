package com.nnvmso.dao;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;

public class GenericDao<T> {
	
	protected static final Logger log = Logger.getLogger(GenericDao.class.getName());
	private Class<T> daoClass;
	
	public GenericDao(Class<T> daoClass) {
		this.daoClass = daoClass;
	}
	
	/**
	 * Get total number of objects
	 */
	public int total() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		int result = 0;
		try {
			Query query = pm.newQuery(daoClass);
			@SuppressWarnings("unchecked")
			int total = ((List<T>)query.execute()).size();
			result = total;
		} finally {
			pm.close();
		}
		return result;
	}
	
	public int total(String filter) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		int result = 0;
		try {
			Query query = pm.newQuery(daoClass);
			if (filter != null && filter != "")
				query.setFilter(filter);
			@SuppressWarnings("unchecked")
			int total = ((List<T>)query.execute()).size();
			result = total;
		} finally {
			pm.close();
		}
		return result;
	}
	
	/**
	 * List objects by specified criteria
	 *
	 * @param page   the page number (start from 1)
	 * @param limit  number of items per page
	 * @param sidx   sorting field
	 * @param sord   sorting direction (asc, desc)
	 */
	public List<T> list(int page, int limit, String sidx, String sord) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<T> results;
		try {
			Query query = pm.newQuery(daoClass);
			query.setOrdering(sidx + " " + sord);
			query.setRange((page - 1) * limit, page * limit);
			@SuppressWarnings("unchecked")
			List<T> tmp = (List<T>)query.execute();
			results = (List<T>)pm.detachCopyAll(tmp);
		} finally {
			pm.close();
		}
		return results;
	}
	
	public List<T> list(long page, long limit, String sidx, String sord, String filter) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<T> results;
		try {
			Query query = pm.newQuery(daoClass);
			if (filter != null && filter != "")
				query.setFilter(filter);
			query.setOrdering(sidx + " " + sord);
			query.setRange((page - 1) * limit, page * limit);
			@SuppressWarnings("unchecked")
			List<T> tmp = (List<T>)query.execute();
			results = (List<T>)pm.detachCopyAll(tmp);
		} finally {
			pm.close();
		}
		return results;
	}
	
	public T findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		T dao = null;
		try {
			@SuppressWarnings("unchecked")
			T tmp = (T)pm.getObjectById(daoClass, id);
			dao = (T)pm.detachCopy(tmp);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return dao;		
	}	
	
	public T findByKey(Key key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		T dao = null;
		try {
			@SuppressWarnings("unchecked")
			T tmp = (T)pm.getObjectById(daoClass, key);
			dao = (T)pm.detachCopy(tmp);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return dao;		
	}
	
}
