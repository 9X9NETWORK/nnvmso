package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.MsoProgramDao;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.Watched;
import com.nnvmso.lib.*;

@Service
public class MsoProgramManager {
	private MsoProgramDao msoProgramDao = new MsoProgramDao();
	
	public void create(MsoChannel channel, MsoProgram program) {		
		program.setChannelKey(channel.getKey());
		msoProgramDao.create(program);
		
		int count = channel.getProgramCount() + 1;
		channel.setProgramCount(count);
		MsoChannelManager channelMngr = new MsoChannelManager();
		channelMngr.save(channel);
	}
	
	public MsoProgram save(MsoProgram program) {
		return msoProgramDao.save(program);
	}
	
	public List<MsoProgram> findNew(String userKey) {
		SubscriptionManager subMngr = new SubscriptionManager();
		WatchedManager watchedMngr = new WatchedManager();
		List<MsoChannel> channels = subMngr.findSubscribedChannels(userKey);
		Key[] channelKeys = new Key[channels.size()];
		for (int i=0; i< channelKeys.length; i++) {
			channelKeys[i] = channels.get(i).getKey();
		}
		List<Watched> watchedList = watchedMngr.findAllByUserKey(channelKeys);
		Hashtable<Key, Watched> watchedTable = new Hashtable<Key, Watched>();
		for (Watched w : watchedList) {
			watchedTable.put(w.getChannelKey(), w);
		}
		List<MsoProgram> programs = msoProgramDao.findNewProgramsByChannels(channels, watchedTable);
		return programs;
	} 

	public List<MsoProgram> findAllByKeys(Key[] channelKeys) {
		return msoProgramDao.findAllByChannelKeys(channelKeys);		
	}
	
	public List<MsoProgram> findAllByChannelId(long id) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findById(id);
		if (channel == null) {return null;}
		return msoProgramDao.findAllByChannel(channel);
	}
	
	public List<MsoProgram> findAllByChannelIdsAndIsPublic(String channelIds, boolean isPublic) {
		 String[] idStrArr = channelIds.split(",");		 
		 long idLongArr[] = new long[idStrArr.length];
		 for (int i=0; i<idLongArr.length; i++) {
			 idLongArr[i] = Long.valueOf(idStrArr[i]);
		 }		
		return msoProgramDao.findAllByChannelIdsAndIsPublic(idLongArr, isPublic);
	}
	
	//!!! cache key and id exchange to save query !!!
	public List<MsoProgram> findAllByUser(NnUser user) {
		SubscriptionManager subService = new SubscriptionManager();			
		List<MsoChannel> channels = subService.findSubscribedChannels(NnStringUtil.getKeyStr(user.getKey()));
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		String channelIds = "";
		for (MsoChannel c : channels) {
			channelIds = channelIds + c.getKey().getId() + ",";
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
	
}