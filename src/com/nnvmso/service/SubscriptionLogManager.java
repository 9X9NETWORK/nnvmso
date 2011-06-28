package com.nnvmso.service;

import java.util.ArrayList;
import java.util.List;
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
	
	public SubscriptionLog findById(long id) {
		return subDao.findById(id);
	}
	
	public SubscriptionLog findByMsoIdAndChannelId(long msoId, long channelId) {
		return subDao.findByMsoIdAndChannelId(msoId, channelId);
	}

	public SubscriptionLog findByMsoIdAndSetId(long msoId, long setId) {
		return subDao.findByMsoIdAndSetId(msoId, setId);
	}
	
	public int findTotalCountByChannelId(long channelId) {
		return subDao.findTotalCountByChannelId(channelId);
	}
	
	public SubscriptionLog findByChannelId(long channelId) {
		return subDao.findByChannelId(channelId);
	}
	
	public List<SubscriptionLog> findBySetIds(List<Long> setIds) {
		List<SubscriptionLog> logs = new ArrayList<SubscriptionLog>();
		for (Long id : setIds) {
			SubscriptionLog log = this.findById(id);
			if (log!= null) logs.add(log);
		}
		return logs;
	}	
}
