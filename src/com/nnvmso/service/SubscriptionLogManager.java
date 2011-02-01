package com.nnvmso.service;

import java.util.logging.Logger;

import com.nnvmso.dao.SubscriptionLogDao;
import com.nnvmso.model.SubscriptionLog;

public class SubscriptionLogManager {
	
	protected static final Logger log = Logger.getLogger(SubscriptionManager.class.getName());
	
	SubscriptionLogDao subDao = new SubscriptionLogDao(); 
	
	public void create(SubscriptionLog log) {
		this.save(log);
	}
	public SubscriptionLog save(SubscriptionLog log) {
		return subDao.save(log);
	}
	
	public SubscriptionLog findByMsoIdAndChannelId(long msoId, long channelId) {
		return subDao.findByMsoIdAndChannelId(msoId, channelId);
	}

}
