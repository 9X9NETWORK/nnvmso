package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.MsoProgramDao;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.ViewLog;

@Service
public class MsoProgramManager {
	
	protected static final Logger log = Logger.getLogger(MsoProgramManager.class.getName());
	
	private MsoProgramDao msoProgramDao = new MsoProgramDao();
	private Cache cache;	
	
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
		channelMngr.save(null, channel);

		//store in cache
		this.setCache();
		if (cache != null) {
			List<MsoProgram> programs = new ArrayList<MsoProgram>();
			programs.add(program);
			this.storeInCache(cache, programs, program.getChannelId());
		}				
	}
	
	public MsoProgram save(MsoProgram program) {
		program.setUpdateDate(new Date());
		return msoProgramDao.save(program);
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
		
	//cached
	public List<MsoProgram> findAllByChannelId(long channelId) {
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		this.setCache();
		//find from cache
		if (cache != null) {
			@SuppressWarnings("unchecked")
			List<Long> list = (ArrayList<Long>)cache.get(this.getCacheProgramListKey(channelId));
			if (list != null) {
				for (Long l : list) {
					MsoProgram p =  this.findById(l);
					if (p!= null) {programs.add(p);}
				}
				log.info("Cache found: channel's programs in cache:" + channelId);
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
		for (MsoProgram p : programs) {
			cache.put(this.getCacheKey(p.getKey().getId()), p);
			list.add(p.getKey().getId());
		}
		if (list != null) {cache.put(this.getCacheProgramListKey(channelId), list);};				
	}
	
	//cached
	public List<MsoProgram> findAllByChannelIdsAndIsPublic(List<Long>channelIds, boolean isPublic) {
		List<MsoProgram> programs = msoProgramDao.findAllByChannelIdsAndIsPublic(channelIds, isPublic);
		/*
		List<MsoProgram> programs = new ArrayList<MsoProgram>(); 
		this.setCache();
		//find from cache
		if (cache != null) {
			for (Long id : channelIds) {
				@SuppressWarnings("unchecked")
				List<Long> list = (ArrayList<Long>)cache.get(this.getCacheProgramListKey(id));
				if (list != null) {
					for (Long l : list) {
						MsoProgram p =  this.findById(l);
						if (p!= null) {programs.add(p);}
					}
					channelIds.remove(id);
					log.info("Cache found: channel's programs in cache:" + id);					
				}
			}
		}
		if (channelIds.size() > 0) {
			//find those not in cache
			List<MsoProgram> list = msoProgramDao.findAllByChannelIdsAndIsPublic(channelIds, isPublic);
			//store in cache
			if (list.size() > 0) {
				long groupId = list.get(0).getKey().getId();
				for (int i=0; i<list.size(); i++) {
					List<MsoProgram> group = new ArrayList<MsoProgram>();
					if (list.get(i).getChannelId() != groupId || (i == list.size()-1)) {
						this.storeInCache(cache, group, groupId);
						groupId = list.get(i).getChannelId();
					} else {
						group.removeAll(group);
					}
				}
				programs.addAll(list);
			}
		}
		*/
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
	
	//cached
	public MsoProgram findById(long id) {
		//find from cache
		String key = this.getCacheKey(id);
		if (cache != null) {
			MsoProgram program = (MsoProgram) cache.get(key);
			if (program != null) {
				log.info("Cache found: program in cache:" + program.getKey().getId());
				return program;
			}
		}
		//find
		MsoProgram program = msoProgramDao.findById(id);
		//save in the cache
		if (cache != null && program != null) { cache.put(key, program);}		
		log.info("Cache NOT found: program is just added:" + program.getKey().getId());		
		return program;
	}
	
	public MsoProgram findByKey(Key key) {
		return msoProgramDao.findByKey(key);
	}
		
	private void setCache() {
	    try {
	        cache = CacheManager.getInstance().getCacheFactory().createCache(
	            Collections.emptyMap());
	      } catch (CacheException e) {}
	}

	//example, program(1)
	private String getCacheKey(long id) {
		return "program(" + id + ")";		
	}

	//example, channel(1)program(1)
	private String getCacheProgramListKey(long channelId) {
		return "channel-programList(" + channelId + ")";		
	}
	
}
