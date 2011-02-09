package com.nnvmso.dao;

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.Subscription;

public class SubscriptionDao {
	public Subscription save(Subscription sub) {
		if (sub == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
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
	
	public Subscription findByUserIdAndSeq(long userId, int seq) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(Subscription.class);
		q.setFilter("userId == userIdParam && seq == seqParam");
		q.declareParameters("long userIdParam, int seqParam");
		q.setOrdering("seq asc");
		@SuppressWarnings("unchecked")
		List<Subscription> subs = (List<Subscription>)q.execute(userId, seq);
		Subscription s = null;
		if (subs.size() > 0) {
			s = subs.get(0);
			s = pm.detachCopy(s);
		}
		pm.close();
		return s;
	}	 
	
	public Subscription findByUserIdAndChannelId(long userId, long channelId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(Subscription.class);
		q.setFilter("userId == userIdParam && channelId== channelIdParam");
		q.setOrdering("seq asc");
		q.declareParameters("long userIdParam, long channelIdParam");
		@SuppressWarnings("unchecked")
		List<Subscription> subs = (List<Subscription>)q.execute(userId, channelId);
		Subscription s = null;
		if (subs.size() > 0) {
			s = subs.get(0);
			s = pm.detachCopy(s);
		}		
		pm.close();
		return s;
	}
	
	public List<Subscription> findAllByUserId(long userId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(Subscription.class);
		q.setFilter("userId == userIdParam");
		q.declareParameters("long userIdParam");
		q.setOrdering("seq asc");
		@SuppressWarnings("unchecked")
		List<Subscription> subscriptions = (List<Subscription>)q.execute(userId);
		subscriptions = (List<Subscription>)pm.detachCopyAll(subscriptions);
		pm.close();
		return subscriptions;
	}	 
		
	
}
