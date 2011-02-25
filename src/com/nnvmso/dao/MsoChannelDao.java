package com.nnvmso.dao;

import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.MsoChannel;

public class MsoChannelDao {
		
	public MsoChannel save(MsoChannel channel) {
		if (channel == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(channel);
		pm.close();		
		return channel;
	}
	
	public MsoChannel findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoChannel channel = null;
		try {
			channel = pm.getObjectById(MsoChannel.class, id);
			channel = pm.detachCopy(channel);
		} catch (JDOObjectNotFoundException e) {
		}		
		pm.close();
		return channel;		
	}	

	public MsoChannel findByKey(Key key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoChannel channel = null;
		try {
			channel = pm.getObjectById(MsoChannel.class, key);
			channel = pm.detachCopy(channel);
		} catch (JDOObjectNotFoundException e) {
		}		
		pm.close();
		return channel;		
	}	

	//!!!
	public List<MsoChannel> findAll() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(MsoChannel.class);
		@SuppressWarnings("unchecked")
		List<MsoChannel> results = (List<MsoChannel>) query.execute();
		results = (List<MsoChannel>)pm.detachCopyAll(results);
		pm.close();
		return results;
	}
	
	public MsoChannel findByName(String name) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		    	
		Query q = pm.newQuery(MsoChannel.class);
		q.setFilter("name == nameParam");
		q.declareParameters(Key.class.getName() + " nameParam");
		@SuppressWarnings("unchecked")
		List<MsoChannel> channels = (List<MsoChannel>) q.execute(name);
		MsoChannel channel = null;
		if (channels.size() > 0) {
			channel = pm.detachCopy(channels.get(0));
		}
		pm.close();
		return channel;				
	}	
	
	public MsoChannel findBySourceUrlSearch(String url) {
		if (url == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();		    	
		Query q = pm.newQuery(MsoChannel.class);
		q.setFilter("sourceUrlSearch == sourceUrlSearchParam");
		q.declareParameters(Key.class.getName() + " sourceUrlSearchParam");
		@SuppressWarnings("unchecked")
		List<MsoChannel> channels = (List<MsoChannel>) q.execute(url.toLowerCase());
		MsoChannel channel = null;
		if (channels.size() > 0) {
			channel = pm.detachCopy(channels.get(0));
		}
		pm.close();
		return channel;				
	}		
	
	//!!! probably not used, otherwise need to add index
	public List<MsoChannel> findPublicChannels() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoChannel.class);
		q.setFilter("isPublic == isPublicParam");
		q.declareParameters("boolean isPublicParam");
		q.setOrdering("nameSearch asc");
		@SuppressWarnings("unchecked")
		List<MsoChannel> channels = (List<MsoChannel>) q.execute(true);
		channels = (List<MsoChannel>)pm.detachCopyAll(channels);
		pm.close();		
		return channels;
	}	
}
