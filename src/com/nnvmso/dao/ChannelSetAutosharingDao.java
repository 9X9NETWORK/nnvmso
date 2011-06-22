package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.ChannelSetAutosharing;

public class ChannelSetAutosharingDao extends GenericDao<ChannelSetAutosharing> {
	
	public ChannelSetAutosharingDao() {
		super(ChannelSetAutosharing.class);
	}
	
	public List<ChannelSetAutosharing> findAllByChannelSetId(long channelSetId) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ChannelSetAutosharing> results = new ArrayList<ChannelSetAutosharing>();
		
		try {
			Query query = pm.newQuery(ChannelSetAutosharing.class);
			query.setFilter("channelSetId == channelSetIdParam");
			query.declareParameters("long channelSetIdParam");
			@SuppressWarnings("unchecked")
			List<ChannelSetAutosharing> list = (List<ChannelSetAutosharing>) query.execute(channelSetId);
			if (list.size() > 0)
				results = (List<ChannelSetAutosharing>) pm.detachCopyAll(list);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return results;
	}
	
	public List<ChannelSetAutosharing> findAllByChannelSetIdAndMsoId(long channelSetId, long msoId) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ChannelSetAutosharing> results = new ArrayList<ChannelSetAutosharing>();
		
		try {
			Query query = pm.newQuery(ChannelSetAutosharing.class);
			query.setFilter("channelSetId == channelSetIdParam && msoId == msoIdParam");
			query.declareParameters("long channelSetIdParam, long msoIdParam");
			@SuppressWarnings("unchecked")
			List<ChannelSetAutosharing> list = (List<ChannelSetAutosharing>) query.execute(channelSetId, msoId);
			if (list.size() > 0)
				results = (List<ChannelSetAutosharing>) pm.detachCopyAll(list);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return results;
	}
	
	public ChannelSetAutosharing findChannelSetAutosharing(long msoId, long channelSetId, short type) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ChannelSetAutosharing result = null;
		
		try {
			Query query = pm.newQuery(ChannelSetAutosharing.class);
			query.setFilter("msoId == msoIdParam && channelSetId == channelSetIdParam && type == typeParam");
			query.declareParameters("long msoIdParam, long channelSetIdParam, short typeParam");
			@SuppressWarnings("unchecked")
			List<ChannelSetAutosharing> list = (List<ChannelSetAutosharing>) query.execute(msoId, channelSetId, type);
			if (list.size() > 0)
				result = pm.detachCopy(list.get(0));
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return result;
	}
	
}
