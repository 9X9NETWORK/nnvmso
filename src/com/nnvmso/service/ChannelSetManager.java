package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.ChannelSetDao;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ChannelSetChannel;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;

@Service
public class ChannelSetManager {
	
	protected static final Logger logger = Logger.getLogger(ChannelSetManager.class.getName());
	
	private ChannelSetDao channelSetDao = new ChannelSetDao();
	
	public void create(ChannelSet channelSet, List<MsoChannel> channels) {
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		
		if (this.findByName(channelSet.getName()) != null) {
			logger.warning("channelSet already exists, name: " + channelSet.getName());
			//return;
		}
		channelSet.setNameSearch(channelSet.getName().trim().toLowerCase());
		Date now = new Date();
		channelSet.setCreateDate(now);
		channelSet.setUpdateDate(now);
		channelSet.setChannelCount(channels.size());
		channelSetDao.save(channelSet);
		
		for (MsoChannel channel : channels) {
			ChannelSetChannel csc = new ChannelSetChannel(channelSet.getKey().getId(), channel.getKey().getId(), channel.getSeq());
			cscMngr.create(csc);
		}
	}
	
	public ChannelSet findByBeautifulUrl(String url) {
		return channelSetDao.findByBeautifulUrl(url);
	}

	public ChannelSet findBybeautifulUrl(String url) {
		return channelSetDao.findByBeautifulUrl(url);
	}
	
	public List<ChannelSet> findFeaturedSetsByMso(Mso mso) {
		return channelSetDao.findFeaturedSetsByMso(mso);
	}

	public List<ChannelSet> findFeaturedSets() {
		return channelSetDao.findFeaturedSets();
	}
	
	public ChannelSet save(ChannelSet channelSet) {	
		//NOTE check name existence if needed
		channelSet.setUpdateDate(new Date());
		//shouldn't be too many, should be ok
		List<MsoChannel> channels = this.findChannelsById(channelSet.getKey().getId());
		channelSet.setChannelCount(channels.size());
		channelSetDao.save(channelSet);
		
		return channelSet;
	}
	
	private Object findByName(String name) {
		return channelSetDao.findByNameSearch(name.trim().toLowerCase());
	}
	
	public List<MsoChannel> findChannelsById(long channelSetId) {
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		MsoChannelManager channelMngr = new MsoChannelManager();		
		List<ChannelSetChannel> cscs = cscMngr.findByChannelSetId(channelSetId); 
		ArrayList<MsoChannel> results = new ArrayList<MsoChannel>();
		
		for (ChannelSetChannel csc : cscs) {
			MsoChannel channel = channelMngr.findById(csc.getChannelId());
			if (channel != null) {
				channel.setSeq(csc.getSeq());
				results.add(channel);
			}
		}
		return results;
	}
	
	public ChannelSet findById(long channelSetId) {
		return channelSetDao.findById(channelSetId);
	}

	public List<ChannelSet> findAllByChannelSetIds(List<Long> channelSetIdList) {
		List<ChannelSet> results = new ArrayList<ChannelSet>();
		for (Long channelSetId : channelSetIdList) {
			ChannelSet channelSet = this.findById(channelSetId);
			if (channelSet != null) results.add(channelSet);
		}
		return results;
	}
	
}
