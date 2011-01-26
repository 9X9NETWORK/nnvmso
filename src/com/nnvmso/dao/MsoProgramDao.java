package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import com.nnvmso.model.Watched;
import com.nnvmso.service.PlayerApiService;

public class MsoProgramDao {
	
	protected static final Logger log = Logger.getLogger(PlayerApiService.class.getName());	
	
	public void create(MsoProgram program) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		program.setCreateDate(now);
		if (program.getUpdateDate() == null) {
			program.setUpdateDate(now);
		}
		pm.makePersistent(program);
		pm.close();		
	}
	
	public MsoProgram save(MsoProgram program) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		if (program.getUpdateDate() == null) {
			program.setUpdateDate(new Date());
		}
		pm.makePersistent(program);
		program = pm.detachCopy(program);
		pm.close();		
		return program;
	}
	
	public List<MsoProgram> findNewProgramsByChannels(List<MsoChannel> channels, Hashtable<Key, Watched> watchedTable) {
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		for (MsoChannel c : channels) {
			Query query = pm.newQuery(MsoProgram.class);
			query.setFilter("channelKey == channelKeyParam");
			query.declareParameters(Key.class.getName() + " channelKeyParam");
	    	query.setOrdering("updateDate desc");	   
	    	@SuppressWarnings("unchecked")
	    	List<MsoProgram> results = (List<MsoProgram>) query.execute(c.getKey());	    	
			int RECENT_SIZE = 3;	    			
    		int cnt = 0;
	    	if (watchedTable.containsKey(c.getKey())) {
	    		HashSet<Long> watchedPrograms = watchedTable.get(c.getKey()).getPrograms();
    			log.info("this channel has been watched:" + c.getKey().getId());
	    		for (int i=0; i<results.size(); i++) {
	    			if (cnt == RECENT_SIZE) {break;}
					if (!watchedPrograms.contains(results.get(i).getKey().getId())) {
						log.info("unwatched program:" + results.get(i).getKey().getId());
						programs.add(results.get(i));
						cnt++;
					}
	    		}
		    	if (cnt < RECENT_SIZE) {
		    		log.info("part of the channel is watched:" + c.getKey());
					for (int i=0; i<results.size(); i++) {
						if (cnt == RECENT_SIZE) {break;}
						if (!programs.contains(results.get(i))) {
							programs.add(results.get(i));
							cnt++;
						}
					}
		    	}	    		
	    	}
	    	if (!watchedTable.containsKey(c.getKey())) {
	    		log.info("the whole channel is unwatched:" + c.getKey());
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

	public List<MsoProgram> findAllByChannelKeys(Key[] keys) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoProgram.class, ":p.contains(channelKey)"); 
		@SuppressWarnings("unchecked")
		List<MsoProgram> programs = (List<MsoProgram>) q.execute(Arrays.asList(keys));
		List<MsoProgram> results = new ArrayList<MsoProgram>();
		results.addAll(programs);
		Iterator<MsoProgram> iter = results.iterator();
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
	
	public List<MsoProgram> findAllByChannelIdsAndIsPublic(long[] channelIds, boolean isPublic) { 			
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Key[] channelKeys = new Key[channelIds.length];
		for (int i=0; i<channelIds.length; i++) {
			try {
				MsoChannel c = pm.getObjectById(MsoChannel.class, channelIds[i]);
				channelKeys[i] = c.getKey();
			} catch (JDOObjectNotFoundException e) { }			
		}
		Query q = pm.newQuery(MsoProgram.class, ":p.contains(channelKey)");
		//q.setOrdering("updateDate desc");
		@SuppressWarnings("unchecked")
		List<MsoProgram> programs = new ArrayList<MsoProgram>((List<MsoProgram>) q.execute(Arrays.asList(channelKeys)));
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
	
	public List<MsoProgram> findAllByChannel(MsoChannel channel) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoProgram.class);
		q.setFilter("channelKey == channelKeyParam");
		q.declareParameters(Key.class.getName() + " channelKeyParam");
		@SuppressWarnings("unchecked")
		List<MsoProgram> programs = (List<MsoProgram>) q.execute(channel.getKey());		
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
	
}
