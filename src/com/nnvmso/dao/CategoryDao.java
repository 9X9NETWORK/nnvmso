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

	//can be duplicated
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

	public Category findCategory(String categoryName, String subcategoryName) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Category detached = null;
		try {
			Query query = pm.newQuery(Category.class);
			query.setFilter("name == nameParam");
			query.declareParameters(Key.class.getName() + " nameParam");
			@SuppressWarnings("unchecked")
			List<Category> results = (List<Category>) query.execute(subcategoryName);
			results = (List<Category>)pm.detachCopyAll(results);
			if (results.size() == 1) {
				detached = results.get(0);
			} else {
				for (Category c : results) {
					if (c.getParentId() != 0) {
						Category parent = this.findById(c.getParentId());
						if (parent.getName().equals(categoryName))
							detached = parent;
					}
				}				
			}
		} finally {
			pm.close();
		}
		return detached;				
	}
	
	//assuming one mso
	public List<Category> findPlayerCategories(long parentId, String lang) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Category> detached = new ArrayList<Category>();
		System.out.println("watch parent:" + parentId + ";lang:" + lang);
		try {
			Query query = pm.newQuery(Category.class);
			query.setFilter("lang == langParam && parentId == parentIdParam && isPublic == isPublicParam");
			query.declareParameters("String langParam, long parentIdParam, boolean isPublicParam");
			query.setOrdering("seq");
			@SuppressWarnings("unchecked")
			List<Category> results = (List<Category>) query.execute(lang, parentId, true);			
			detached = (List<Category>)pm.detachCopyAll(results);
		} finally {
			pm.close();
		}
		return detached;		
	}
	
	public List<Category> findByParentId(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Category> detached = new ArrayList<Category>();
		try {
			Query query = pm.newQuery(Category.class);
			query.setFilter("parentId == " + id);
			query.setOrdering("name");
			@SuppressWarnings("unchecked")
			List<Category> results = (List<Category>) query.execute();			
			detached = (List<Category>)pm.detachCopyAll(results);
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
	
	public List<Category> findAllByMsoId(long msoId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Category> detached = new ArrayList<Category>();
		try {
			Query q = pm.newQuery(Category.class);
			q.setFilter("msoId == msoIdParam && isPublic == isPublicParam");
			q.declareParameters("long msoIdParam, boolean isPublicParam");
			q.setOrdering("name");
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
			String query = "select from " + Category.class.getName() + " order by createDate";
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
