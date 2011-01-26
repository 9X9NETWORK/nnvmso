package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.MsoChannel;

public class MsoChannelDao {
	
	public void create(MsoChannel channel) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		channel.setCreateDate(now);
		channel.setUpdateDate(now);
		pm.makePersistent(channel);
		pm.close();	
	}
	
	public MsoChannel save(MsoChannel channel) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		channel.setUpdateDate(new Date());
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
		
	public List<MsoChannel> findPublicChannelsByCategoryIds(long[] categoryIds) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Key[] categoryKeys = new Key[categoryIds.length];
		for (int i=0; i<categoryIds.length; i++) {
			MsoChannel c = pm.getObjectById(MsoChannel.class, categoryIds[i]);
			categoryKeys[i] = c.getKey();
		}
		Query q = pm.newQuery(MsoChannel.class, ":p.contains(categoryKey)");
		@SuppressWarnings("unchecked")
		List<MsoChannel> channels = new ArrayList<MsoChannel>((List<MsoChannel>) q.execute(Arrays.asList(categoryKeys)));
		Iterator<MsoChannel> iter = channels.iterator();
		while(iter.hasNext()) {
		  MsoChannel c = iter.next();
		  if (!c.isPublic()) {
			  iter.remove();
		  }
		}
		channels = (List<MsoChannel>)pm.detachCopyAll(channels);
		pm.close();
		return channels;
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
