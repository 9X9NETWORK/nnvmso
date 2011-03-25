package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.PlayerApiService;

public class MsoProgramDao extends GenericDao<MsoProgram> {
	
	protected static final Logger log = Logger.getLogger(PlayerApiService.class.getName());	
		
	public MsoProgramDao() {
		super(MsoProgram.class);
	}
	
	public MsoProgram save(MsoProgram program) {
		if (program == null) {return null;}		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(program);
			program = pm.detachCopy(program);
		} finally {
			pm.close();
		}
		return program;
	}
	
	public void delete(MsoProgram program) {
		if (program == null) return;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.deletePersistent(program);
		} finally {
			pm.close();
		}		
	}
			
	/**
	 * Deprecated!
	 * 
	 * There will be no data in viewLog for now, meaning "the whole channel is always unwatched"
	 * Keep the implementation since the requirement can be changed back again.  
	 */
	public List<MsoProgram> findNewProgramsByChannels(List<MsoChannel> channels, Hashtable<Long, HashSet<Long>> watchedTable) {
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		for (MsoChannel c : channels) {
			Query query = pm.newQuery(MsoProgram.class);
			query.setFilter("channelId == channelIdParam");
			query.declareParameters("long channelIdParam");
	    	query.setOrdering("pubDate desc");	   
	    	@SuppressWarnings("unchecked")
	    	List<MsoProgram> results = (List<MsoProgram>) query.execute(c.getKey().getId());	    	 
			int RECENT_SIZE = 3;	    			
    		int cnt = 0;
	    	if (watchedTable.containsKey(c.getKey().getId())) {
	    		HashSet<Long> watchedPrograms = watchedTable.get(c.getKey());
    			log.info("this channel has been watched:" + c.getKey().getId());
	    		for (int i=0; i<results.size(); i++) {
	    			if (cnt == RECENT_SIZE) {break;}
					if (watchedPrograms != null && !watchedPrograms.contains(results.get(i).getKey().getId())) {
						log.info("unwatched program:" + results.get(i));
						programs.add(results.get(i));
						cnt++;
					}
	    		}
		    	if (cnt < RECENT_SIZE) {
		    		log.info("part of the channel is watched:" + c.getKey().getId());
					for (int i=0; i<results.size(); i++) {
						if (cnt == RECENT_SIZE) {break;}
						if (!programs.contains(results.get(i))) {
							programs.add(results.get(i));
							cnt++;
						}
					}
		    	}	    		
	    	} else {
	    		log.info("the whole channel is unwatched:" + c.getKey().getId());
				for (int i=0; i<results.size(); i++) {
					if (i == RECENT_SIZE) {break;}
					programs.add(results.get(i));
				}	    			    		
	    	}
			programs = (List<MsoProgram>)pm.detachCopyAll(programs);
		}
		pm.close();		
		return programs;
	}

	public MsoProgram findByStorageId(String storageId) {
		MsoProgram detached = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		try {
			Query query = pm.newQuery(MsoProgram.class);
			query.setFilter("storageId == '" + storageId + "'");
			@SuppressWarnings("unchecked")
			List<MsoProgram> results = (List<MsoProgram>) query.execute();
			if (results.size() > 0) {
				detached = pm.detachCopy(results.get(0));
			}
		} finally {
			pm.close();
		}
		return detached;
	}
	
	/**
	 * Good: reference findGoodProgramsByChannelId  
	 */
	public List<MsoProgram> findGoodProgramsByChannelIds(List<Long> channelIds) {
		List<MsoProgram> good = new ArrayList<MsoProgram>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(MsoProgram.class, ":p.contains(channelId)");
			q.setOrdering("channelId asc, pubDate desc");
			@SuppressWarnings("unchecked")
			List<MsoProgram> programs = ((List<MsoProgram>) q.execute(channelIds));		
			good = (List<MsoProgram>) pm.detachCopyAll(programs);
			for (MsoProgram p : programs) {
				  if (p.isPublic() && p.getStatus() != MsoProgram.STATUS_OK && p.getType() == MsoProgram.TYPE_VIDEO) {
					  good.add(p);
				  }			
			}
		} finally {
			pm.close();
		}
		return good;
	}
		
	/**
	 * Good: is Public, is STATUS_OK, is TYPE_VIDEO
	 */
	public List<MsoProgram> findGoodProgramsByChannelId(long channelId) {
		List<MsoProgram> detached = new ArrayList<MsoProgram>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(MsoProgram.class);
			q.setFilter("channelId == channelIdParam && status == statusParam && type == typeParam");
			q.declareParameters("long channelIdParam, short statusParam, short typeParam");
			q.setOrdering("pubDate desc");
			@SuppressWarnings("unchecked")
			List<MsoProgram> programs = (List<MsoProgram>)q.execute(channelId, MsoProgram.STATUS_OK, MsoProgram.TYPE_VIDEO);
			detached = (List<MsoProgram>)pm.detachCopyAll(programs);
		} finally {
			pm.close();
		}
		return detached;
	}
	
	public List<MsoProgram> findAllByChannelId(long channelId) {
		List<MsoProgram> detached = new ArrayList<MsoProgram>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(MsoProgram.class);
			q.setFilter("channelId == channelIdParam");
			q.declareParameters("long channelIdParam");
			q.setOrdering("pubDate desc");
			@SuppressWarnings("unchecked")
			List<MsoProgram> programs = (List<MsoProgram>)q.execute(channelId);		
			detached = (List<MsoProgram>)pm.detachCopyAll(programs);
		} finally {
			pm.close();
		}
		return detached;
	}	
	
	public MsoProgram findById(long id) {
		MsoProgram program = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			program = pm.getObjectById(MsoProgram.class, id);
			program = pm.detachCopy(program);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return program;		
	}	
	
	public MsoProgram findByKey(Key key) {
		MsoProgram program = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			program = pm.getObjectById(MsoProgram.class, key);
			program = pm.detachCopy(program);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();			
		}
		return program;		
	}	

	public int findAndDeleteProgramsOlderThanMax(long channelId) {
		List<MsoProgram> list = new ArrayList<MsoProgram>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(MsoProgram.class);
			q.setOrdering("pubDate asc");
			q.setFilter("channelId == channelIdParam");
			q.declareParameters("long channelIdParam");			
			@SuppressWarnings("unchecked")
			List<MsoProgram> programs = (List<MsoProgram>) q.execute(channelId);			
			if (programs.size() > MsoChannelManager.MAX_CHANNEL_SIZE) {
				int over = programs.size() - MsoChannelManager.MAX_CHANNEL_SIZE;
				list = programs.subList(0, over);				
				log.info("Channel id, original size, delete size:" + channelId + "," + programs.size() + "," + list.size());
				pm.deletePersistentAll(list);
			}
			return programs.size() - list.size(); 
			
		} finally {
			pm.close();
		}		
	}	
	
	public MsoProgram findOldestByChannelId(long channelId) {
		MsoProgram oldest = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(MsoProgram.class);
			q.setFilter("channelId == channelIdParam");
			q.declareParameters("long channelIdParam");			
			q.setOrdering("pubDate asc");
			q.setRange(0, 1);
			@SuppressWarnings("unchecked")
			List<MsoProgram> programs = (List<MsoProgram>) q.execute(channelId);
			if (programs.size() > 0) {
				oldest = programs.get(0);
				oldest = pm.detachCopy(oldest);
			}		
		} finally {
			pm.close();
		}
		return oldest;		
	}	
}
