package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.CategoryChannelSet;

public class CategoryChannelSetDao extends GenericDao<CategoryChannelSet> {
	
	protected static final Logger logger = Logger.getLogger(CategoryChannelSetDao.class.getName());
	
	public CategoryChannelSetDao() {
		super(CategoryChannelSet.class);
	}
	
	public List<CategoryChannelSet> findAllByCategoryId(long categoryId) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<CategoryChannelSet> detached = new ArrayList<CategoryChannelSet>();
		try {
			Query query = pm.newQuery(CategoryChannelSet.class);
			query.setFilter("categoryId == categoryIdParam");
			query.declareParameters("long categoryIdParam");
			@SuppressWarnings("unchecked")
			List<CategoryChannelSet> ccs = (List<CategoryChannelSet>)query.execute(categoryId);
			detached = (List<CategoryChannelSet>)pm.detachCopyAll(ccs);
		} finally {
			pm.close();
		}
		return detached;
	}
}
