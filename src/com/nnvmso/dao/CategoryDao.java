package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.JDOFatalException;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.Category;

public class CategoryDao extends GenericDao<Category> {
	
	public CategoryDao() {
		super(Category.class);
	}
	
	public Category save(Category category) {
		if (category == null) {return null;}		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(category);
		} finally {
			pm.close();
		}
		return category;
	}

	public void delete(Category category) {
		throw new JDOFatalException();
	}
	
	public Category findByName(String name) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Category detached = null;
		try {
			Query query = pm.newQuery(Category.class);
			String quotedName = NnStringUtil.escapedQuote(name);
			logger.info("quoted name = " + quotedName);
			query.setFilter("name == " + quotedName);
			@SuppressWarnings("unchecked")
			List<Category> results = (List<Category>) query.execute();			
			if (results.size() > 0) {
				detached = pm.detachCopy(results.get(0));
			}
		} finally {
			pm.close();
		}
		return detached;
	}
	
	public List<Category> findAllByParanetId(long parentId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Category> detached = new ArrayList<Category>();
		try {
			Query q = pm.newQuery(Category.class);
			q.setFilter("parentId == parentIdParam");
			q.declareParameters("long parentIdParam");
			@SuppressWarnings("unchecked")
			List<Category> categories = (List<Category>)q.execute(parentId);
			detached = (List<Category>)pm.detachCopyAll(categories);
		} finally {
			pm.close();
		}
		return detached;
	}
	/*
	public List<Category> findAllByMsoIdAndRestricted(long msoId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Category> detached = new ArrayList<Category>();
		try {
			Query q = pm.newQuery(Category.class);
			q.setFilter("msoId == msoIdParam && type == typeParam");
			q.declareParameters("long msoIdParam, short typeParam");
			q.setOrdering("name");
			@SuppressWarnings("unchecked")
			List<Category> categories = (List<Category>)q.execute(msoId, Category.TYPE_RESTRICTED);
			detached = (List<Category>)pm.detachCopyAll(categories);
		} finally {
			pm.close();			
		}
		return detached;		
	}
	*/

	public List<Category> findAllByMsoId(long msoId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Category> detached = new ArrayList<Category>();
		try {
			Query q = pm.newQuery(Category.class);
			q.setFilter("msoId == msoIdParam");
			q.declareParameters("long msoIdParam");
			q.setOrdering("name");
			@SuppressWarnings("unchecked")
			List<Category> categories = (List<Category>)q.execute(msoId);
			detached = (List<Category>)pm.detachCopyAll(categories);
		} finally {
			pm.close();			
		}
		return detached;		
	}
	
	public List<Category> findAllInIpg(long msoId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Category> detached = new ArrayList<Category>();
		try {
			Query q = pm.newQuery(Category.class);
			q.setFilter("msoId == msoIdParam && isInIpg == isInIpgParam");
			q.declareParameters("long msoIdParam, boolean isInIpgParam");
			q.setOrdering("seq asc");
			@SuppressWarnings("unchecked")
			List<Category> categories = (List<Category>)q.execute(msoId, true);
			detached = (List<Category>)pm.detachCopyAll(categories);
		} finally {
			pm.close();			
		}
		return detached;		
	}
	
	public List<Category> findAll() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Category> detached = new ArrayList<Category>();
		try {
			String query = "select from " + Category.class.getName() + " order by name";
			@SuppressWarnings("unchecked")
			List<Category> categories = (List<Category>) pm.newQuery(query).execute();
			detached = (List<Category>)pm.detachCopyAll(categories);
		} finally {
			pm.close();
		}
		return detached;				
	}
	
	public Category findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Category category = null;
		try { 
			category = pm.getObjectById(Category.class, id);
			category = pm.detachCopy(category);
		} catch (JDOObjectNotFoundException e) {			
		} finally {			
			pm.close();
		}
		return category;		
	}

	public Category findByKey(Key key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Category category = null;
		try { 
			category = pm.getObjectById(Category.class, key);
			category = pm.detachCopy(category);
		} catch (JDOObjectNotFoundException e) {			
		} finally {
			pm.close();			
		}
		return category;		
	}
	
	//!!! contains query
	public List<Category> findAllByIds(List<Long> ids) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Category> categories= new ArrayList<Category>();
		try {
			for (long id : ids) {
				Category c = this.findById(id); 
				if (c != null) {
					categories.add(c);
				}
			}
		} finally {
			pm.close();		
		}
		return categories;
	}
	
}
