package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.NnUserWatched;

public class NnUserWatchedDao extends GenericDao<NnUserWatched>{

	protected static final Logger logger = Logger.getLogger(NnUserWatchedDao.class.getName());
	
	public NnUserWatchedDao() {
		super(NnUserWatched.class);
	}

	@SuppressWarnings("unchecked")
	public List<NnUserWatched> findAllByUser(long userId, String userToken) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ArrayList<NnUserWatched> results = new ArrayList<NnUserWatched>();
		
		try {
			Query query = pm.newQuery(NnUserWatched.class);
			if (userId != 0) {
				query.setFilter("userId == userIdParam");
				query.declareParameters("long userIdParam");
				query.setOrdering("createDate DESC");
				results.addAll((List<NnUserWatched>)query.execute(userId));
			} else {
				query.setFilter("userToken == userTokenParam");
				query.declareParameters("String userTokenParam");				
				results.addAll((List<NnUserWatched>)query.execute(userToken));
			}
			results = (ArrayList<NnUserWatched>) pm.detachCopyAll(results);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return results;	
	}

	@SuppressWarnings("unchecked")
	public List<NnUserWatched> findAllByUserToken(String token) {
		NnUserWatched w = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ArrayList<NnUserWatched> results = new ArrayList<NnUserWatched>();
		try {
			Query q = pm.newQuery(NnUserWatched.class);
			q.setFilter("userToken == userTokenParam");
			q.declareParameters("String userTokenParam");
			results.addAll((List<NnUserWatched>)q.execute(token));
		} finally {
			pm.close();
		}
		return results;		
	}

	public NnUserWatched findByUserTokenAndChannelId(String token, long channelId) {
		NnUserWatched w = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(NnUserWatched.class);
			q.setFilter("userToken == userTokenParam && channelId== channelIdParam");
			q.declareParameters("String userTokenParam, long channelIdParam");
			@SuppressWarnings("unchecked")
			List<NnUserWatched> watches = (List<NnUserWatched>)q.execute(token, channelId);
			if (watches.size() > 0) {
				w = watches.get(0);
				w = pm.detachCopy(w);
			}
		} finally {
			pm.close();
		}
		return w;		
	}
	
	public NnUserWatched findByUserIdAndChannelId(long userId, long channelId) {
		NnUserWatched w = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(NnUserWatched.class);
			q.setFilter("userId == userIdParam && channelId== channelIdParam");
			q.declareParameters("long userIdParam, long channelIdParam");
			@SuppressWarnings("unchecked")
			List<NnUserWatched> watches = (List<NnUserWatched>)q.execute(userId, channelId);
			if (watches.size() > 0) {
				w = watches.get(0);
				w = pm.detachCopy(w);
			}
		} finally {
			pm.close();
		}
		return w;		
	}
	
}