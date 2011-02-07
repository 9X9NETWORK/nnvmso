package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.MsoProgramDao;
import com.nnvmso.lib.CacheFactory;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.ViewLog;

@Service
public class MsoProgramManager {
	
	protected static final Logger log = Logger.getLogger(MsoProgramManager.class.getName());
	
	private MsoProgramDao msoProgramDao = new MsoProgramDao();
	
	public void create(MsoChannel channel, MsoProgram program) {		
		Date now = new Date();
		program.setCreateDate(now);
		program.setUpdateDate(now);
		program.setChannelId(channel.getKey().getId());
		msoProgramDao.save(program);

		//set channel count
		int count = channel.getProgramCount() + 1;
		channel.setProgramCount(count);
		MsoChannelManager channelMngr = new MsoChannelManager();
		channelMngr.save(channel);

		//if the channel's original programCount is zero, its count will not be in the category, adding it now.
		if (count == 1) {
			CategoryManager categoryMngr = new CategoryManager();
			System.out.println("mso program manager, channel create, addChannelCount");
			categoryMngr.addChannelCounter(channel);
		}		
		
		//store in cache
		Cache cache = CacheFactory.get();
		if (cache != null) {
			List<MsoProgram> programs = new ArrayList<MsoProgram>();
			programs.add(program);
			this.storeInCache(cache, programs, program.getChannelId());
		}				
	}
	
	public MsoProgram save(MsoProgram program) {
		program.setUpdateDate(new Date());
		program = msoProgramDao.save(program);
		Cache cache = CacheFactory.get();
		if (cache != null) {
			List<MsoProgram> programs = new ArrayList<MsoProgram>();
			programs.add(program);
			this.storeInCache(cache, programs, program.getChannelId());			
		}
		return program;
	}
	
	public void delete(MsoProgram program) {
		//category channelCount 
		//channel programCount
		//cache
	}
	
	public List<MsoProgram> findNew(long userId) {
		SubscriptionManager subMngr = new SubscriptionManager();
		ViewLogManager watchedMngr = new ViewLogManager();
		List<MsoChannel> channels = subMngr.findSubscribedChannels(userId, 0);
		List<Long> list = new ArrayList<Long>();
		for (int i=0; i< channels.size(); i++) {
			list.add(channels.get(i).getKey().getId());
		}
		List<ViewLog> watchedList = watchedMngr.findAllByUserId(list);
		Hashtable<Long, HashSet<Long>> watchedTable = new Hashtable<Long, HashSet<Long>>();
		for (ViewLog w : watchedList) {
			watchedTable.put(w.getChannelId(), w.getPrograms());
		}
		List<MsoProgram> programs = msoProgramDao.findNewProgramsByChannels(channels, watchedTable);	
		return programs;
	} 
		
	/**	 
	 * @@@ IMPORTANT: does not do isPublic handling yet, currently all programs are public
	 * @@@ Cached
	 */
	public List<MsoProgram> findAllByChannelIdAndIsPublic(long channelId) {
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		Cache cache = CacheFactory.get();
		//find from cache
		if (cache != null) {
			@SuppressWarnings("unchecked")
			List<Long> list = (ArrayList<Long>)cache.get(this.getCacheProgramListKey(channelId));
			//!!! flaw: bad if cache list is not empty but individual program is not in cache  
			if (list != null) {
				for (Long l : list) {
					MsoProgram p =  this.findById(l);
					if (p!= null) {programs.add(p);}
				}
				return programs;
			}
		}
		//find
		programs = msoProgramDao.findAllByChannelId(channelId);
		//store in cache
		if (cache != null) { this.storeInCache(cache, programs, channelId); }				
		return msoProgramDao.findAllByChannelId(channelId);
	}
	
	private void storeInCache(Cache cache, List<MsoProgram>programs, long channelId) {
		@SuppressWarnings("unchecked")
		List<Long> list = (List<Long>)cache.get(this.getCacheProgramListKey(channelId));
		if (list == null) { list = new ArrayList<Long>(); }
		//store individual program
		for (MsoProgram p : programs) {
			cache.put(this.getCacheKey(p.getKey().getId()), p);
			if (!list.contains(p.getKey().getId())) {list.add(p.getKey().getId());}
		}
		//store a channel's program list
		if (list != null) {cache.put(this.getCacheProgramListKey(channelId), list);};
	}
	
	/**
	 * @@@ Cached 
	 */
	public List<MsoProgram> findAllByChannelIdsAndIsPublic(List<Long>channelIds, boolean isPublic) {
		//List<MsoProgram> programs = msoProgramDao.findAllByChannelIdsAndIsPublic(channelIds, isPublic);

		System.out.println("original channel size:" + channelIds.size());
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		List<Long> test = new ArrayList<Long>();
		test.addAll(channelIds);
		Cache cache = CacheFactory.get();
		//find from cache
		if (cache != null) {
			for (Long id : test) {
				@SuppressWarnings("unchecked")
				List<Long> list = (ArrayList<Long>)cache.get(this.getCacheProgramListKey(id));
				if (list != null) {
					for (Long l : list) {
						MsoProgram p =  this.findById(l);
						if (p!= null) {programs.add(p);}
					}
					channelIds.remove(id);					
				}
			}
		}
		System.out.println("remaining channel size:" + channelIds.size());
		if (channelIds.size() > 0) {
			//find those not in cache
			List<MsoProgram> list = msoProgramDao.findAllByChannelIdsAndIsPublic(channelIds, isPublic);
			//store in cache
			if (list.size() > 0) {
				long groupId = list.get(0).getChannelId();
				for (int i=0; i<list.size(); i++) {
					List<MsoProgram> group = new ArrayList<MsoProgram>();
					if (list.get(i).getChannelId() != groupId || (i == list.size()-1)) {
						this.storeInCache(cache, group, groupId);
						groupId = list.get(i).getChannelId();
						group.removeAll(group);
						System.out.println("store this channel in cache:" + groupId);
					} else {
						group.add(list.get(i));
					}
				}
				programs.addAll(list);
			}
		}

		return programs;
	}

	public List<MsoProgram> findSubscribedPrograms(long userId) {
		SubscriptionManager subService = new SubscriptionManager();			
		List<MsoChannel> channels = subService.findSubscribedChannels(userId, 0);
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		List<Long> channelIds = new ArrayList<Long>();
		for (MsoChannel c : channels) {
			channelIds.add(c.getKey().getId());
		}		
		programs = this.findAllByChannelIdsAndIsPublic(channelIds, true);
		return programs;
	}
	
	public MsoProgram findByStorageId(String storageId) {
		return msoProgramDao.findByStorageId(storageId);
	}
	
	/**
	 * @@@ Cached 
	 */
	public MsoProgram findById(long id) {
		//find from cache
		Cache cache = CacheFactory.get();
		String key = this.getCacheKey(id);
		if (cache != null) {
			MsoProgram program = (MsoProgram) cache.get(key);
			if (program != null) {
				return program;
			}
		}
		//find
		MsoProgram program = msoProgramDao.findById(id);
		//save in the cache
		if (cache != null && program != null) { cache.put(key, program);}
		return program;
	}
	
	public MsoProgram findByKey(Key key) {
		return msoProgramDao.findByKey(key);
	}
		
	//example, program(1)
	private String getCacheKey(long id) {
		return "program(" + id + ")";		
	}

	//example, channel(1)program(1)
	private String getCacheProgramListKey(long channelId) {
		return "channel-programList(" + channelId + ")";		
	}

	public String findCacheByChannel(long channelId) {
		Cache cache = CacheFactory.get();
		String listStr = "";
		String programStr = "";
		if (cache != null) {
			@SuppressWarnings("unchecked")
			List<Long> list = (ArrayList<Long>)cache.get(this.getCacheProgramListKey(channelId));
			if (list != null) {
				for (Long l : list) { listStr = listStr + l + "\t"; }
			}
			List<MsoProgram> programs = msoProgramDao.findAllByChannelId(channelId);
			for (MsoProgram p : programs) {	
				MsoProgram cacheP = (MsoProgram) cache.get(this.getCacheKey(p.getKey().getId())); 
				if (cacheP != null) {
					programStr = programStr + cacheP.getKey().getId() + "\t" + cacheP.getChannelId() + "\t" + cacheP.getName() + "\n";
				}
			}			
		}	 		
		return "program list:\n" + listStr + "\nprogram cached:\n" + programStr; 		
	}
	
	public void cacheByChannelId(long channelId) {
		Cache cache = CacheFactory.get();
		if (cache != null) {
			this.findAllByChannelIdAndIsPublic(channelId);
		}
	}
	
	public void deleteCacheByChannel(long channelId) {
		Cache cache = CacheFactory.get();
		List<MsoProgram> programs = this.findAllByChannelIdAndIsPublic(channelId);
		if (cache != null) {
			for (MsoProgram c : programs) {				
				cache.remove(this.getCacheKey(c.getKey().getId()));
			}
			cache.remove(this.getCacheProgramListKey(channelId));
		}
	}
	
}
