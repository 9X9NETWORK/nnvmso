package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.MsoProgramDao;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.ViewLog;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

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
		channelMngr.save(null, channel);
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
	
	public List<MsoProgram> findAllByChannelId(long channelId) {
		return msoProgramDao.findAllByChannelId(channelId);
	}
	
	public List<MsoProgram> findAllByChannelKey(Key channelKey) {
		return msoProgramDao.findAllByChannelKey(channelKey);
	}
	
	public List<MsoProgram> findAllByChannelKeyStr(String channelKey) {
		try {
		  return this.findAllByChannelKey(KeyFactory.stringToKey(channelKey));
		} catch (IllegalArgumentException e) {
			log.info("invalid channel key string");
			return null;
		}
	}
	
	public List<MsoProgram> findAllByChannelIdsAndIsPublic(List<Long>channelIds, boolean isPublic) {
		return msoProgramDao.findAllByChannelIdsAndIsPublic(channelIds, isPublic);
	}
	
	public List<MsoProgram> findPublicPrograms() {
		return msoProgramDao.findPublicPrograms();
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
	
	public MsoProgram findById(long id) {
		return msoProgramDao.findById(id);
	}
	
	public MsoProgram findByKey(Key key) {
		return msoProgramDao.findByKey(key);
	}
	
	public MsoProgram findByKeyStr(String key) {
		try {
		  return this.findByKey(KeyFactory.stringToKey(key));
		} catch (IllegalArgumentException e) {
			log.info("invalid key string");
			return null;
		}
	}
}
