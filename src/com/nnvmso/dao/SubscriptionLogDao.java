package com.nnvmso.dao;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.SubscriptionLog;

public class SubscriptionLogDao {
	public SubscriptionLog save(SubscriptionLog log) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(log);
		log = pm.detachCopy(log);
		pm.close();		
		return log;
	}
	
	public SubscriptionLog findByMsoIdAndChannelId(long msoId, long channelId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(SubscriptionLog.class);
		q.setFilter("msoId == msoIdParam && channelId== channelIdParam");
		q.declareParameters("long msoIdParam, long channelIdParam");
		@SuppressWarnings("unchecked")
		List<SubscriptionLog> subs = (List<SubscriptionLog>)q.execute(msoId, channelId);
		SubscriptionLog s = null;
		if (subs.size() > 0) {
			s = subs.get(0);
			s = pm.detachCopy(s);
		}		
		pm.close();
		return s;		
	}
	
	
}
