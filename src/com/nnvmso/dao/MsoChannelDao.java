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
		System.out.println("id:" + id);
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
	
	public MsoChannel findBySourceUrl(String url) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		    	
		Query q = pm.newQuery(MsoChannel.class);
		q.setFilter("sourceUrl == sourceUrlParam");
		q.declareParameters(Key.class.getName() + " sourceUrlParam");
		@SuppressWarnings("unchecked")
		List<MsoChannel> channels = (List<MsoChannel>) q.execute(url);
		MsoChannel channel = null;
		if (channels.size() > 0) {
			channel = pm.detachCopy(channels.get(0));
		}
		pm.close();
		return channel;				
	}		
	
	public List<MsoChannel> findPublicChannels() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoChannel.class);
		q.setFilter("isPublic == isPublicParam");
		q.declareParameters("boolean isPublicParam");
		q.setOrdering("name asc");
		@SuppressWarnings("unchecked")
		List<MsoChannel> channels = (List<MsoChannel>) q.execute(true);
		channels = (List<MsoChannel>)pm.detachCopyAll(channels);
		pm.close();		
		return channels;
	}	
}
