package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.nnvmso.dao.NnUserChannelSortingDao;
import com.nnvmso.model.NnUserChannelSorting;

public class NnUserChannelSortingManager {

	protected static final Logger log = Logger.getLogger(NnUserChannelSortingManager.class.getName());
	
	private NnUserChannelSortingDao sortingDao = new NnUserChannelSortingDao();	
		
	public NnUserChannelSorting save(NnUserChannelSorting sorting) {
		NnUserChannelSorting existed = this.findByUserAndChannel(sorting.getUserId(), sorting.getChannelId());
		Date now = new Date();
		if (existed == null) {
			existed = sorting;
			existed.setCreateDate(now);
		} else {
			existed.setSort(sorting.getSort());
		}
		existed.setUpdateDate(now);
		return sortingDao.save(existed);		
	}		
	
	public NnUserChannelSorting findByUserAndChannel(long userId, long channelId) {		
		return sortingDao.findByUserAndChannel(userId, channelId);
	}
	
	public List<NnUserChannelSorting> findByUser(long userId) {		
		return sortingDao.findByUser(userId);
	}
	
}
