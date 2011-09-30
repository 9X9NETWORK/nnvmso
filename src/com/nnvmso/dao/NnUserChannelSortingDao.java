package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.NnUserChannelSorting;

public class NnUserChannelSortingDao extends GenericDao<NnUserChannelSorting>{

	public NnUserChannelSortingDao() {
		super(NnUserChannelSorting.class);
	}

	public NnUserChannelSorting findByUserAndChannel(long userId, long channelId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		NnUserChannelSorting s = null;
		try {
			Query q = pm.newQuery(NnUserChannelSorting.class);
			q.setFilter("userId == userIdParam && channelId== channelIdParam");
			q.declareParameters("long userIdParam, long channelIdParam");
			@SuppressWarnings("unchecked")
			List<NnUserChannelSorting> sorting = (List<NnUserChannelSorting>)q.execute(userId, channelId);
			if (sorting.size() > 0) {
				s = sorting.get(0);
				s = pm.detachCopy(s);
			}
		} finally {
			pm.close();
		}
		return s;		
	}
	
	public List<NnUserChannelSorting> findByUser(long userId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<NnUserChannelSorting> s = new ArrayList<NnUserChannelSorting>();
		try {
			Query q = pm.newQuery(NnUserChannelSorting.class);
			q.setFilter("userId == userIdParam");
			q.declareParameters("long userIdParam");
			@SuppressWarnings("unchecked")
			List<NnUserChannelSorting> sorting = (List<NnUserChannelSorting>)q.execute(userId);
			s = (List<NnUserChannelSorting>) pm.detachCopyAll(sorting);
		} finally {
			pm.close();
		}
		return s;		
	}
	
}
