package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.NnChannel;

public class NnChannelDao extends GenericDao<NnChannel> {
	
	protected static final Logger log = Logger.getLogger(MsoDao.class.getName());
	
	public NnChannelDao() {
		super(NnChannel.class);
	}	
	
	public NnChannel save(NnChannel channel) {
		if (channel == null) {return null;}
		PersistenceManager pm = PMF.getContent().getPersistenceManager();
		try {
			pm.makePersistent(channel);			
			channel = pm.detachCopy(channel);
		} finally {
			pm.close();
		}
		return channel;
	}
	
	public NnChannel findById(long id) {
		PersistenceManager pm = PMF.getContent().getPersistenceManager();		
		NnChannel channel = null;
		try {
			channel = pm.getObjectById(NnChannel.class, id);
			channel = pm.detachCopy(channel);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();			
		}
		return channel;		
	}	

	@SuppressWarnings("unchecked")
	public static List<NnChannel> searchChannelEntries(String queryStr) {		
	    PersistenceManager pm = PMF.getContent().getPersistenceManager();
	    String sql = "select * from nnchannel " +
	    		      "where name like '%" + queryStr + "%'" + 
	    		         "|| intro like '%" + queryStr + "%'";	 	   
	    log.info("Sql=" + sql);
	    Query q= pm.newQuery("javax.jdo.query.SQL", sql);
	    q.setClass(NnChannel.class);
	    List<NnChannel> results = (List<NnChannel>) q.execute();
	    
		return results; 	
	}
	
	public List<NnChannel> findAll() {
		PersistenceManager pm = PMF.getContent().getPersistenceManager();
		List<NnChannel> detached = new ArrayList<NnChannel>();
		try {
			Query query = pm.newQuery(NnChannel.class);
			@SuppressWarnings("unchecked")
			List<NnChannel> results = (List<NnChannel>) query.execute();
			detached = (List<NnChannel>)pm.detachCopyAll(results);
		} finally {
			pm.close();
		}
		return detached;
	}
		
	public List<NnChannel> findAllByStatus(short status) {
		PersistenceManager pm = PMF.getContent().getPersistenceManager();
		List<NnChannel> detached = new ArrayList<NnChannel>(); 
		try {
			Query q = pm.newQuery(NnChannel.class);
			q.setFilter("status == statusParam");
			q.declareParameters("short statusParam");
			q.setOrdering("createDate asc");
			@SuppressWarnings("unchecked")
			List<NnChannel> channels = (List<NnChannel>) q.execute(status);
			detached = (List<NnChannel>)pm.detachCopyAll(channels);
		} finally {
			pm.close();
		}
		return detached;
	}	

	public NnChannel findBySourceUrl(String url) {
		if (url == null) {return null;}
		PersistenceManager pm = PMF.getContent().getPersistenceManager();
		NnChannel channel = null;
		try {
			Query q = pm.newQuery(NnChannel.class);
			q.setFilter("sourceUrl== sourceUrlParam");
			q.declareParameters("String sourceUrlParam");
			@SuppressWarnings("unchecked")
			//List<NnChannel> channels = (List<NnChannel>) q.execute(url.toLowerCase());
			List<NnChannel> channels = (List<NnChannel>) q.execute(url);
			if (channels.size() > 0) {
				channel = pm.detachCopy(channels.get(0));
			}
		} finally {
			pm.close();
		}
		return channel;				
	}		
	
}
