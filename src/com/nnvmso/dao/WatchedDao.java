package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.Watched;

public class WatchedDao {
	public void create(Watched watched) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		watched.setCreateDate(now);
		watched.setUpdateDate(now);
		pm.makePersistent(watched);
		pm.close();		
	}

	public Watched save(Watched watched) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		watched.setUpdateDate(now);
		pm.makePersistent(watched);
		watched = pm.detachCopy(watched);
		pm.close();		
		return watched;
	}
	
	public List<Watched> findAllByUserKey(Key[] channelKeys) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(Watched.class, ":p.contains(channelKey)");
		@SuppressWarnings("unchecked")
		List<Watched> watched = new ArrayList<Watched>((List<Watched>) q.execute(Arrays.asList(channelKeys)));
		watched = (List<Watched>)pm.detachCopyAll(watched);
		pm.close();
		return watched;
	}
	
	public Watched findByUserKeyAndChannelKey(Key userKey, Key channelKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(Watched.class);
		q.setFilter("userKey == userKeyParam && channelKey == channelKeyParam");
		q.declareParameters(Key.class.getName() + " userKeyParam, " + Key.class.getName() + " channelKeyParam");
		@SuppressWarnings("unchecked")
		List<Watched> watches = (List<Watched>)q.execute(userKey, channelKey);
		Watched w = null;
		if (watches.size() > 0) {
			w = watches.get(0);
			w = pm.detachCopy(w);
		}		
		pm.close();
		return w;
		
	}

}
