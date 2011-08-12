package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.ChannelAutosharing;

public class ChannelAutosharingDao extends GenericDao<ChannelAutosharing> {
	
	public ChannelAutosharingDao() {
		super(ChannelAutosharing.class);
	}
	
	public boolean isChannelAutosharedByMso(long msoId, long channelId, short type) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		boolean result = false;
		
		try {
			Query query = pm.newQuery(ChannelAutosharing.class);
			query.setFilter("msoId == msoIdParam");
			query.setFilter("channelId == channelIdParam");
			query.setFilter("type == typeParam");
			query.declareParameters("long msoIdParam");
			query.declareParameters("long channelIdParam");
			query.declareParameters("short typeParam");
			@SuppressWarnings("unchecked")
			List<ChannelAutosharing> list = (List<ChannelAutosharing>) query.execute(msoId, channelId, type);
			if (list.size() > 0)
				result = true;
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return result;
	}
	
	public List<ChannelAutosharing> findAllByChannelId(long channelId) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ChannelAutosharing> results = new ArrayList<ChannelAutosharing>();
		
		try {
			Query query = pm.newQuery(ChannelAutosharing.class);
			query.setFilter("channelId == channelIdParam");
			query.declareParameters("long channelIdParam");
			@SuppressWarnings("unchecked")
			List<ChannelAutosharing> list = (List<ChannelAutosharing>) query.execute(channelId);
			if (list.size() > 0)
				results = (List<ChannelAutosharing>) pm.detachCopyAll(list);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return results;
	}
	
	public List<ChannelAutosharing> findAllByChannelIdAndMsoId(long channelId, long msoId) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ChannelAutosharing> results = new ArrayList<ChannelAutosharing>();
		
		try {
			Query query = pm.newQuery(ChannelAutosharing.class);
			query.setFilter("channelId == channelIdParam && msoId == msoIdParam");
			query.declareParameters("long channelIdParam, long msoIdParam");
			@SuppressWarnings("unchecked")
			List<ChannelAutosharing> list = (List<ChannelAutosharing>) query.execute(channelId, msoId);
			if (list.size() > 0)
				results = (List<ChannelAutosharing>) pm.detachCopyAll(list);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return results;
	}

	public ChannelAutosharing findChannelAutosharing(long msoId, long channelId, short type) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ChannelAutosharing result = null;
		
		try {
			Query query = pm.newQuery(ChannelAutosharing.class);
			query.setFilter("msoId == msoIdParam && channelId == channelIdParam && type == typeParam");
			query.declareParameters("long msoIdParam, long channelIdParam, short typeParam");
			@SuppressWarnings("unchecked")
			List<ChannelAutosharing> list = (List<ChannelAutosharing>) query.execute(msoId, channelId, type);
			if (list.size() > 0)
				result = pm.detachCopy(list.get(0));
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return result;
	}

	public List<ChannelAutosharing> findAllByChannelIdAndType(long channelId, short type) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ChannelAutosharing> results = new ArrayList<ChannelAutosharing>();
		
		try {
			Query query = pm.newQuery(ChannelAutosharing.class);
			query.setFilter("channelId == channelIdParam && type == typeParam");
			query.declareParameters("long channelIdParam, short typeParam");
			@SuppressWarnings("unchecked")
			List<ChannelAutosharing> list = (List<ChannelAutosharing>) query.execute(channelId, type);
			if (list.size() > 0)
				results = (List<ChannelAutosharing>) pm.detachCopyAll(list);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return results;
	}
}
