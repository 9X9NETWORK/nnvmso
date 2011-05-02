package com.nnvmso.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.ChannelSetDao;
import com.nnvmso.dao.ContentOwnershipDao;
import com.nnvmso.dao.MsoChannelDao;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ContentOwnership;
import com.nnvmso.model.MsoChannel;

@Service
public class ContentOwnershipManager {
	
	protected static final Logger logger = Logger.getLogger(ContentOwnershipManager.class.getName());
	
	private ContentOwnershipDao ownershipDao = new ContentOwnershipDao();
	private ChannelSetDao channelSetDao = new ChannelSetDao();
	private MsoChannelDao channelDao = new MsoChannelDao();
	
	public List<ChannelSet> findOwnedChannelSetsByMsoId(long msoId) {
		
		List<ContentOwnership> ownershipList = ownershipDao.findByMsoIdAndContentType(msoId, ContentOwnership.TYPE_CHANNELSET);
		
		ArrayList<Long> channelSetIds = new ArrayList<Long>();
		for (ContentOwnership ownership : ownershipList) {
			channelSetIds.add(ownership.getContentId());
		}
		
		return channelSetDao.findAllByIds(channelSetIds);
		
	}
	
	public List<MsoChannel> findOwnedChannelsByMsoId(long msoId) {
		
		List<ContentOwnership> ownershipList = ownershipDao.findByMsoIdAndContentType(msoId, ContentOwnership.TYPE_CHANNEL);
		
		ArrayList<Long> channelIds = new ArrayList<Long>();
		for (ContentOwnership ownership : ownershipList) {
			channelIds.add(ownership.getContentId());
		}
		return channelDao.findAllByIds(channelIds);
	}
}
