package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.nnvmso.dao.NnUserWatchedDao;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUserWatched;

public class NnUserWatchedManager {

	protected static final Logger log = Logger.getLogger(NnUserWatchedManager.class.getName());
	
	private NnUserWatchedDao watchedDao= new NnUserWatchedDao();
					
	public NnUserWatched save(NnUserWatched watched) {
		Date now = new Date();
		NnUserWatched existed = this.findByUserIdAndChannelId(watched.getUserId(), watched.getChannelId());
		if (existed != null) {
			existed.setProgram(watched.getProgram());
			watched = existed;
		}
		if (watched.getCreateDate() == null) {watched.setCreateDate(now);}
		watched.setUpdateDate(now);		
		return watchedDao.save(watched);
	}
	
	public NnUserWatched findByUserIdAndChannelId(long userId, long channelId) {
		return watchedDao.findByUserIdAndChannelId(userId, channelId);
	}

	public NnUserWatched findByUserTokenAndChannelId(String token, long channelId) {
		return watchedDao.findByUserTokenAndChannelId(token, channelId);
	}
	
	public List<NnUserWatched> findAllByUserToken(String token) {
		return watchedDao.findAllByUserToken(token);
	}
	
	public void delete(NnUserWatched watched) {
		watchedDao.delete(watched);
	}

}
