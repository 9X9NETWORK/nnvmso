package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.PlayerApiService;

public class MsoProgramDao {
	
	protected static final Logger log = Logger.getLogger(PlayerApiService.class.getName());	
		
	public MsoProgram save(MsoProgram program) {
		if (program == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(program);
		program = pm.detachCopy(program);
		pm.close();		
		return program;
	}
	
	public List<MsoProgram> findNewProgramsByChannels(List<MsoChannel> channels, Hashtable<Long, HashSet<Long>> watchedTable) {
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		System.out.println("channels size:" + channels.size());
		for (MsoChannel c : channels) {
			Query query = pm.newQuery(MsoProgram.class);
			query.setFilter("channelId == channelIdParam");
			query.declareParameters("long channelIdParam");
	    	query.setOrdering("updateDate desc");	   
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
					System.out.println(results.get(i).getKey().getId());
					programs.add(results.get(i));
				}	    			    		
	    	}
			programs = (List<MsoProgram>)pm.detachCopyAll(programs);
		}
		pm.close();		
		return programs;
	}

	public MsoProgram findByStorageId(String storageId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(MsoProgram.class);
		query.setFilter("storageId == '" + storageId + "'");
		@SuppressWarnings("unchecked")
		List<MsoProgram> results = (List<MsoProgram>) query.execute();
		MsoProgram detached = null;
		if (results.size() > 0) {
			detached = pm.detachCopy(results.get(0));
		}
		pm.close();
		return detached;
	}
	
	public List<MsoProgram> findAllByChannelIdsAndIsPublic(List<Long> channelIds, boolean isPublic) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoProgram.class, ":p.contains(channelId)");
		@SuppressWarnings("unchecked")
		List<MsoProgram> programs = ((List<MsoProgram>) q.execute(channelIds));
		Iterator<MsoProgram> iter = programs.iterator();
		while(iter.hasNext()) {
		  MsoProgram p = iter.next();
		  if (!p.isPublic()) {
			  iter.remove();
		  }
		}
		programs = (List<MsoProgram>) pm.detachCopyAll(programs);
		pm.close();
		return programs;
	}
		
	public List<MsoProgram> findAllByChannelId(long channelId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoProgram.class);
		q.setFilter("channelId == channelIdParam");
		q.declareParameters("long channelIdParam");
		@SuppressWarnings("unchecked")
		List<MsoProgram> programs = (List<MsoProgram>) q.execute(channelId);		
		List<MsoProgram> detached = (List<MsoProgram>)pm.detachCopyAll(programs);
		pm.close();
		return detached;
	}
	
	public MsoProgram findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoProgram program = null;
		try {
			program = pm.getObjectById(MsoProgram.class, id);
			program = pm.detachCopy(program);
		} catch (JDOObjectNotFoundException e) {
		}		
		pm.close();
		return program;		
	}	
	
	public MsoProgram findByKey(Key key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoProgram program = null;
		try {
			program = pm.getObjectById(MsoProgram.class, key);
			program = pm.detachCopy(program);
		} catch (JDOObjectNotFoundException e) {
		}		
		pm.close();
		return program;		
	}	
	
	public List<MsoProgram> findPublicPrograms() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoProgram.class);
		q.setFilter("isPublic == isPublicParam");
		q.declareParameters("boolean isPublicParam");
		q.setOrdering("name asc");
		@SuppressWarnings("unchecked")
		List<MsoProgram> programs = (List<MsoProgram>) q.execute(true);
		programs = (List<MsoProgram>)pm.detachCopyAll(programs);
		pm.close();
		return programs;
	}
}
