package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.Subscription;

public class SubscriptionDao extends GenericDao<Subscription>{
	
	protected static final Logger log = Logger.getLogger(SubscriptionDao.class.getName());
	
	public SubscriptionDao() {
		super(Subscription.class);
	}
	
	public Subscription save(Subscription sub) {
		if (sub == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(sub);
			sub = pm.detachCopy(sub);
		} finally {
			pm.close();
		}
		return sub;
	}

	public void saveAll(List<Subscription> subs) {
		Date now = new Date();
		for (Subscription s : subs) {
			s.setUpdateDate(now);			
		}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistentAll(subs);
		} finally {
			pm.close();
		}
	}
	
	public void delete(Subscription sub) {
		if (sub != null) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				pm.deletePersistent(sub);
			} finally {
				pm.close();
			}
		}
	}

	public void deleteAll(List<Subscription> list) {
		if (list != null) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				pm.deletePersistentAll(list);
			} finally {
				pm.close();
			}
		}
	}
	
	public Subscription findByUserIdAndSeq(long userId, int seq) {
		Subscription s = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(Subscription.class);
			q.setFilter("userId == userIdParam && seq == seqParam");
			q.declareParameters("long userIdParam, int seqParam");
			q.setOrdering("seq asc");
			@SuppressWarnings("unchecked")
			List<Subscription> subs = (List<Subscription>)q.execute(userId, seq);
			if (subs.size() > 0) {
				s = subs.get(0);
				s = pm.detachCopy(s);
			}
		} finally {
			pm.close();
		}
		return s;
	}	 
	
	public Subscription findByUserIdAndChannelId(long userId, long channelId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Subscription s = null;
		try {
			Query q = pm.newQuery(Subscription.class);
			q.setFilter("userId == userIdParam && channelId== channelIdParam");
			q.setOrdering("seq asc");
			q.declareParameters("long userIdParam, long channelIdParam");
			@SuppressWarnings("unchecked")
			List<Subscription> subs = (List<Subscription>)q.execute(userId, channelId);
			if (subs.size() > 0) {
				s = subs.get(0);
				s = pm.detachCopy(s);
			}
		} finally {
			pm.close();
		}
		return s;
	}
	
	public List<Subscription> findAllByUserId(long userId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Subscription> detached = new ArrayList<Subscription>();
		try {
			Query q = pm.newQuery(Subscription.class);
			q.setFilter("userId == userIdParam");
			q.declareParameters("long userIdParam");
			q.setOrdering("seq asc");
			@SuppressWarnings("unchecked")
			List<Subscription> subscriptions = (List<Subscription>)q.execute(userId);
			detached = (List<Subscription>)pm.detachCopyAll(subscriptions);
		} finally {
			pm.close();
		}
		return detached;
	}	 

	public List<Subscription> findByChannel(long channelId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<Subscription> detached = new ArrayList<Subscription>();
		try {
			Query q = pm.newQuery(Subscription.class);
			q.setFilter("channelId == channelIdParam");
			q.declareParameters("long channelIdParam");
			q.setOrdering("seq asc");
			@SuppressWarnings("unchecked")
			List<Subscription> subscriptions = (List<Subscription>)q.execute(channelId);
			detached = (List<Subscription>)pm.detachCopyAll(subscriptions);
		} finally {
			pm.close();
		}
		return detached;
	}	 
	
	public Subscription findChannelSubscription(long userId, long channelId, int seq) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Subscription detached = null;
		try {
			Query q = pm.newQuery(Subscription.class);
			q.setFilter("userId == userIdParam && channelId == channelIdParam && seq == seqParam");
			q.declareParameters("long userIdParam, long chanenlIdParam, int seqParam");
			q.setOrdering("seq asc");
			@SuppressWarnings("unchecked")
			List<Subscription> subs = (List<Subscription>)q.execute(userId, channelId, seq);
			if (subs.size() > 0) {
				detached = subs.get(0);
				detached = pm.detachCopy(detached);
			}						
		} finally {
			pm.close();
		}
		return detached;
	}
	
}
