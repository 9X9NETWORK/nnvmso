package com.nnvmso.dao;

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.Subscription;

public class SubscriptionDao {
	public void create(Subscription sub) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		sub.setCreateDate(now);
		sub.setUpdateDate(now);
		pm.makePersistent(sub);
		pm.close();		
	}

	public Subscription save(Subscription sub) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		sub.setUpdateDate(now);
		pm.makePersistent(sub);
		sub = pm.detachCopy(sub);
		pm.close();		
		return sub;
	}

	public void saveAll(List<Subscription> subs) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		for (Subscription s : subs) {
			s.setUpdateDate(now);			
		}
		pm.makePersistentAll(subs);
		pm.close();		
	}
	
	public void delete(Subscription sub) {
		if (sub != null) {
			PersistenceManager pm = PMF.get().getPersistenceManager();		
			pm.deletePersistent(sub);
			pm.close();
		}
	}
	
	public List<Subscription> findAllByUserKeyAndSeq(Key userKey, int seq1, int seq2) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(Subscription.class);
		q.setFilter("userKey == userKeyParam && (seq == seq1Param || seq == seq2Param)");
		q.declareParameters(Key.class.getName() + " userKeyParam, int seq1Param, int seq2Param");
		q.setOrdering("seq asc");
		@SuppressWarnings("unchecked")
		List<Subscription> subscriptions = (List<Subscription>)q.execute(userKey, seq1, seq2);
		subscriptions = (List<Subscription>)pm.detachCopyAll(subscriptions);
		pm.close();
		return subscriptions;
	}	 
	
	public Subscription findByUserKeyAndChannelKey(Key userKey, Key channelKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(Subscription.class);
		q.setFilter("userKey == userKeyParam && channelKey == channelKeyParam");
		q.setOrdering("seq asc");
		q.declareParameters(Key.class.getName() + " userKeyParam, " + Key.class.getName() + " channelKeyParam");
		@SuppressWarnings("unchecked")
		List<Subscription> subs = (List<Subscription>)q.execute(userKey, channelKey);
		Subscription s = null;
		if (subs.size() > 0) {
			s = subs.get(0);
			s = pm.detachCopy(s);
		}		
		pm.close();
		return s;
	}
	
	public List<Subscription> findAllByUserKey(Key userKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(Subscription.class);
		q.setFilter("userKey == userKeyParam");
		q.declareParameters(Key.class.getName() + " userKeyParam");
		q.setOrdering("seq asc");
		@SuppressWarnings("unchecked")
		List<Subscription> subscriptions = (List<Subscription>)q.execute(userKey);
		subscriptions = (List<Subscription>)pm.detachCopyAll(subscriptions);
		pm.close();
		return subscriptions;
	}	 
		
	
}
