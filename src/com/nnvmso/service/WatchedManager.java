package com.nnvmso.service;

import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.WatchedDao;
import com.nnvmso.model.Watched;

public class WatchedManager {

	protected static final Logger log = Logger.getLogger(NnUserManager.class.getName());
	
	private WatchedDao watchedDao= new WatchedDao();
		
	public void create(Watched watched) {
		watchedDao.create(watched);
	}
	
	public Watched save(Watched watched) {
		return watchedDao.save(watched);
	}
	
	public Watched findByUserKeyAndChannelKey(Key userKey, Key channelKey) {
		return watchedDao.findByUserKeyAndChannelKey(userKey, channelKey);
	}
	
	public List<Watched> findAllByUserKey(Key[] channelKeys) {
		return watchedDao.findAllByUserKey(channelKeys);
	}
	
}
