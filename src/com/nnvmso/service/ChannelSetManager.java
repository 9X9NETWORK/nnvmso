package com.nnvmso.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.ChannelSetChannelDao;
import com.nnvmso.dao.MsoChannelDao;
import com.nnvmso.model.ChannelSetChannel;
import com.nnvmso.model.MsoChannel;

@Service
public class ChannelSetManager {
	
	protected static final Logger logger = Logger.getLogger(ChannelSetManager.class.getName());
	
	private MsoChannelDao channelDao = new MsoChannelDao();
	private ChannelSetChannelDao cscDao = new ChannelSetChannelDao();
	
	public List<MsoChannel> findChannelsById(long channelSetId) {
		
		List<ChannelSetChannel> channelSets = cscDao.findByChannelSetId(channelSetId);
		ArrayList<MsoChannel> results = new ArrayList<MsoChannel>();
		
		for (ChannelSetChannel csc : channelSets) {
			MsoChannel channel = channelDao.findById(csc.getChannelId());
			if (channel != null) {
				channel.setSeq(csc.getSeq());
				results.add(channel);
			}
		}
		return results;
	}
}
