package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.ChannelSetChannelDao;
import com.nnvmso.model.ChannelSetChannel;

@Service
public class ChannelSetChannelManager {
	protected static final Logger logger = Logger.getLogger(ChannelSetChannelManager.class.getName());
	
	ChannelSetChannelDao cscDao = new ChannelSetChannelDao();
	
	public void create(ChannelSetChannel csc) {
		Date now = new Date();
		csc.setCreateDate(now);
		csc.setUpdateDate(now);
		cscDao.save(csc);
	}
	
	public List<ChannelSetChannel> findByChannelSetId(long channelSetId) {
		return cscDao.findByChannelSetId(channelSetId);
	}
	
}
