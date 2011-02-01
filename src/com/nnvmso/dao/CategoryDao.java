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

public class CategoryDao {
	
	public Category save(Category category) {
		if (category == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(category);
		pm.close();
		return category;
	}

	public void delete(Category category) {
		throw new JDOFatalException();
	}
	
	public Category findByName(String name) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Category.class);
		query.setFilter("name == '" + NnStringUtil.capitalize(name) + "'");		
		@SuppressWarnings("unchecked")
		List<Category> results = (List<Category>) query.execute();
		Category detached = null;
		if (results.size() > 0) {
			detached = pm.detachCopy(results.get(0));
		}
		pm.close();
		return detached;
	}
		
	public List<Category> findAllByMsoId(long msoId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(Category.class);
		q.setFilter("msoId == msoIdParam");
		q.declareParameters("long msoIdParam");
		q.setOrdering("name");
		@SuppressWarnings("unchecked")
		List<Category> categories = (List<Category>)q.execute(msoId);
		categories = (List<Category>)pm.detachCopyAll(categories);
		pm.close();
		return categories;		
	}
	
	public List<Category> findAll() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		String query = "select from " + Category.class.getName() + " order by createDate";
		@SuppressWarnings("unchecked")
		List<Category> categories = (List<Category>) pm.newQuery(query).execute();
		categories = (List<Category>)pm.detachCopyAll(categories);
		pm.close();
		return categories;				
	}
	
	public Category findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Category category = null;
		try { 
			category = pm.getObjectById(Category.class, id);
			category = pm.detachCopy(category);
		} catch (JDOObjectNotFoundException e) {			
		}
		pm.close();
		return category;		
	}

	public Category findByKey(Key key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Category category = null;
		try { 
			category = pm.getObjectById(Category.class, key);
			category = pm.detachCopy(category);
		} catch (JDOObjectNotFoundException e) {			
		}
		pm.close();
		return category;		
	}
	
	//!!! contains query
	public List<Category> findAllByIds(List<Long> ids) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Category> categories= new ArrayList<Category>();
		for (long id : ids) {
			Category c = this.findById(id); 
			if (c != null) {
				categories.add(c);
			}
		}
		pm.close();
		return categories;
	}
			
}
