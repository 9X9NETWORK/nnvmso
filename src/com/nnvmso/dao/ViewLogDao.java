package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.ViewLog;

public class ViewLogDao {
	public ViewLog save(ViewLog watched) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(watched);
			watched = pm.detachCopy(watched);
		} finally {
			pm.close();
		}
		return watched;
	}
	
	public List<ViewLog> findAllByUserId(List<Long> channelIds) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ViewLog> detached = new ArrayList<ViewLog>();
		try {
			Query q = pm.newQuery(ViewLog.class, ":p.contains(channelId)");
			@SuppressWarnings("unchecked")
			List<ViewLog> watched = new ArrayList<ViewLog>((List<ViewLog>) q.execute(channelIds));
			detached = (List<ViewLog>)pm.detachCopyAll(watched);
		} finally {
			pm.close();
		}
		return detached;
	}
	
	public ViewLog findByUserIdAndChannelId(long userId, long channelId) {
		ViewLog w = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(ViewLog.class);
			q.setFilter("userId == userIdParam && channelId== channelIdParam");
			q.declareParameters("long userIdParam, long channelIdParam");
			@SuppressWarnings("unchecked")
			List<ViewLog> watches = (List<ViewLog>)q.execute(userId, channelId);
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
