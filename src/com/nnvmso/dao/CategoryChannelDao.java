package com.nnvmso.dao;

import java.util.Date;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.Subscription;

public class CategoryChannelDao {

	public void create(CategoryChannel cc) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		cc.setCreateDate(now);
		cc.setUpdateDate(now);
		pm.makePersistent(cc);
		pm.close();		
	}

	public CategoryChannel save(CategoryChannel cc) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		cc.setUpdateDate(now);
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
	
	public List<CategoryChannel> findAllByCategoryKey(Key categoryKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(CategoryChannel.class);
		q.setFilter("categoryKey == categoryKeyParam");
		q.declareParameters(Key.class.getName() + " categoryKeyParam");
		@SuppressWarnings("unchecked")
		List<CategoryChannel> ccs = (List<CategoryChannel>)q.execute(categoryKey);
		ccs = (List<CategoryChannel>)pm.detachCopyAll(ccs);		
		pm.close();
		return ccs;
	}	

	public List<CategoryChannel> findAllByChannelKey(Key channelKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(CategoryChannel.class);
		q.setFilter("channelKey == channelKeyParam");
		q.declareParameters(Key.class.getName() + " channelKeyParam");
		@SuppressWarnings("unchecked")
		List<CategoryChannel> ccs = (List<CategoryChannel>)q.execute(channelKey);
		ccs = (List<CategoryChannel>)pm.detachCopyAll(ccs);		
		pm.close();
		return ccs;
	}	
	
	public CategoryChannel findByCategoryKeyAndChannelKey(Key categoryKey, Key channelKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(Subscription.class);
		q.setFilter("categoryKey == categoryKeyParam && channelKey == channelKeyParam");
		q.declareParameters(Key.class.getName() + " categoryKeyParam, " + Key.class.getName() + " channelKeyParam");
		@SuppressWarnings("unchecked")
		List<CategoryChannel> ccs= (List<CategoryChannel>)q.execute(categoryKey, channelKey);
		CategoryChannel cc = null;
		if (ccs.size() > 0) {
			cc = ccs.get(0);
			cc = pm.detachCopy(cc);
		}		
		pm.close();
		return cc;
	}
		
}
