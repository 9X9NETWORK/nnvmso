package com.nnvmso.dao;

import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.CategoryChannel;

public class CategoryChannelDao {

	public CategoryChannel save(CategoryChannel cc) {
		if (cc == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		pm.makePersistent(cc);
		cc= pm.detachCopy(cc);
		pm.close();		
		return cc;
	}
	
	public void delete(CategoryChannel cc) {
		if (cc!= null) {
			PersistenceManager pm = PMF.get().getPersistenceManager();		
			pm.deletePersistent(cc);
			pm.close();
		}
	}
	
	public CategoryChannel findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		CategoryChannel cc = null;
		try {
			cc = pm.getObjectById(CategoryChannel.class, id);
			cc = pm.detachCopy(cc);
		} catch (JDOObjectNotFoundException e) {
		}		
		pm.close();
		return cc;		
	}	
	
	public List<CategoryChannel> findAllByCategoryId(long categoryId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(CategoryChannel.class);
		q.setFilter("categoryId == categoryIdParam");
		q.declareParameters("long categoryIdParam");
		@SuppressWarnings("unchecked")
		List<CategoryChannel> ccs = (List<CategoryChannel>)q.execute(categoryId);
		ccs = (List<CategoryChannel>)pm.detachCopyAll(ccs);		
		pm.close();
		return ccs;
	}	

	public List<CategoryChannel> findAllByChannelId(long channelId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(CategoryChannel.class);
		q.setFilter("channelId == channelIdParam");
		q.declareParameters("long channelIdParam");
		@SuppressWarnings("unchecked")
		List<CategoryChannel> ccs = (List<CategoryChannel>)q.execute(channelId);
		ccs = (List<CategoryChannel>)pm.detachCopyAll(ccs);		
		pm.close();
		return ccs;
	}	
	
	public CategoryChannel findByCategoryIdAndChannelId(long categoryId, long channelId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(CategoryChannel.class);
		q.setFilter("categoryId == categoryIdParam && channelId == channelIdParam");
		q.declareParameters("long categoryIdParam, long channelIdParam");
		@SuppressWarnings("unchecked")
		List<CategoryChannel> ccs= (List<CategoryChannel>)q.execute(categoryId, channelId);
		CategoryChannel cc = null;
		if (ccs.size() > 0) {
			cc = ccs.get(0);
			cc = pm.detachCopy(cc);
		}		
		pm.close();
		return cc;
	}
		
}
