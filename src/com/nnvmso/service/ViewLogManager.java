package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.nnvmso.dao.ViewLogDao;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.ViewLog;

public class ViewLogManager {

	protected static final Logger log = Logger.getLogger(NnUserManager.class.getName());
	
	private ViewLogDao watchedDao= new ViewLogDao();
					
	public ViewLog save(ViewLog watched) {
		Date now = new Date();
		if (watched.getCreateDate() == null) {watched.setCreateDate(now);}
		watched.setUpdateDate(now);
		return watchedDao.save(watched);
	}
	
	public ViewLog findByUserIdAndChannelId(long userId, long channelId) {
		return watchedDao.findByUserIdAndChannelId(userId, channelId);
	}
	
	public List<ViewLog> findAllByUserId(List<Long> channelIds) {
		return watchedDao.findAllByUserId(channelIds);
	}
	
	public void processPdr(String pdrLine, long userId) {
		if (userId == 0) {return;}
		
		MsoChannelManager channelMngr = new MsoChannelManager();				
		try {
			String[] data = pdrLine.split("\t");		
			long channelId = Long.parseLong(data[2]);
			MsoChannel c = channelMngr.findById(channelId);
			ViewLog watched = this.findByUserIdAndChannelId(userId, c.getKey().getId());
			
			if (c != null) {
				for (int i=3; i< data.length; i++) {
					long programId = Long.parseLong(data[i]);
					if (watched == null) {
						watched = new ViewLog(userId, c.getKey().getId());
						watched.getPrograms().add(programId);					
					} else {							
					    if (!watched.getPrograms().contains(programId)) {
					    	watched.getPrograms().add(programId);
					    	this.save(watched);
					    }
					}
					this.save(watched);					
				}
			}
		} catch (Exception e) {
			log.info("exception catpures: " + e.getClass());
		}
	}
	
}
