package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ChannelSetChannel;

public class ChannelSetChannelDao extends GenericDao<ChannelSetChannel> {
	
	protected static final Logger logger = Logger.getLogger(ChannelSetChannel.class.getName());
	
	public ChannelSetChannelDao() {
		super(ChannelSetChannel.class);
	}
	
	public List<ChannelSetChannel> findByChannelSet(ChannelSet set) {		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ChannelSetChannel> detached = new ArrayList<ChannelSetChannel>();
		
		try {
			Query query = pm.newQuery(ChannelSetChannel.class);
			query.setFilter("channelSetId == channelSetIdParam");
			query.declareParameters("long channelSetIdParam");
			if (set.isFeatured())
				query.setOrdering("seq");
			else 
				query.setOrdering("updateDate desc");
			@SuppressWarnings("unchecked")
			List<ChannelSetChannel> list = (List<ChannelSetChannel>)query.execute(set.getKey().getId());
			detached = (List<ChannelSetChannel>)pm.detachCopyAll(list);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return detached;
	}

	public ChannelSetChannel findBySetAndChannel(long channelSetId, long channelId) {		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ChannelSetChannel result = null;		
		try {
			Query query = pm.newQuery(ChannelSetChannel.class);
			query.setFilter("channelSetId == channelSetIdParam && channelId == channelIdParam");
			query.declareParameters("long channelSetIdParam, long channelIdParam");
			@SuppressWarnings("unchecked")
			List<ChannelSetChannel> list = (List<ChannelSetChannel>)query.execute(channelSetId, channelId);
			if (list.size() > 0)
				result = pm.detachCopy(list.get(0));
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return result;
	}
	
	public ChannelSetChannel findByChannelSetIdAndSeq(long channelSetId, int seq) {		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ChannelSetChannel result = null;		
		try {
			Query query = pm.newQuery(ChannelSetChannel.class);
			query.setFilter("channelSetId == channelSetIdParam && seq == seqParam");
			query.declareParameters("long channelSetIdParam, int seqParam");
			@SuppressWarnings("unchecked")
			List<ChannelSetChannel> list = (List<ChannelSetChannel>)query.execute(channelSetId, seq);
			if (list.size() > 0)
				result = pm.detachCopy(list.get(0));
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return result;
	}
	
	public List<ChannelSetChannel> findAllByChannelId(long channelId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ChannelSetChannel> detached = new ArrayList<ChannelSetChannel>();
		
		try {
			Query query = pm.newQuery(ChannelSetChannel.class);
			query.setFilter("channelId == channelIdParam");
			query.declareParameters("long channelIdParam");
			@SuppressWarnings("unchecked")
			List<ChannelSetChannel> list = (List<ChannelSetChannel>)query.execute(channelId);
			detached = (List<ChannelSetChannel>)pm.detachCopyAll(list);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return detached;
	}
}
