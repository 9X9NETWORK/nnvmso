package com.nnvmso.service;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.MsoChannel;

@Service
public class CmsApiService {
	protected static final Logger logger = Logger.getLogger(CmsApiService.class.getName());
	
	private ChannelSetManager channelSetMngr = new ChannelSetManager();
	private ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
	
	public ChannelSet getDefaultChannelSet(long msoId) {
		List<ChannelSet> ownedChannelSets = ownershipMngr.findOwnedChannelSetsByMsoId(msoId);
		if (ownedChannelSets.size() > 0) {
			return ownedChannelSets.get(0);
		}
		return null;
	}
	
	public List<MsoChannel> findChannelsByChannelSetId(long channelSetId) {
		return channelSetMngr.findChannelsById(channelSetId);
	}
	
}
