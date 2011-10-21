package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.ChannelSetDao;
import com.nnvmso.dao.ContentOwnershipDao;
import com.nnvmso.dao.MsoChannelDao;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ContentOwnership;
import com.nnvmso.model.Mso;
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
	
	public void create(ContentOwnership own, Mso mso, MsoChannel channel) {			
		own.setContentType(ContentOwnership.TYPE_CHANNEL);
		own.setContentId(channel.getKey().getId());
		own.setMsoId(mso.getKey().getId());
		own.setCreateDate(new Date());
		own.setCreateMode(ContentOwnership.MODE_UPLOAD);
		
		ownershipDao.save(own);
	}

	public void create(ContentOwnership own, Mso mso, ChannelSet channelSet) {		
		own.setContentType(ContentOwnership.TYPE_CHANNELSET);
		own.setContentId(channelSet.getKey().getId());
		own.setMsoId(mso.getKey().getId());
		own.setCreateDate(new Date());
		own.setCreateMode(ContentOwnership.MODE_CURATE);
		
		ownershipDao.save(own);
	}

	public List<ContentOwnership> findAllByMsoId(long msoId) {
		
		return ownershipDao.findAllByMsoId(msoId);
	}

	public List<MsoChannel> create(Mso mso, List<MsoChannel> channelList) {
		
		List<MsoChannel> results = new ArrayList<MsoChannel>();
		
		for (MsoChannel channel : channelList) {
			ContentOwnership ownership = this.findByMsoIdAndChannelId(mso.getKey().getId(), channel.getKey().getId());
			if (ownership != null) {
				logger.warning("channel " + channel.getKey().getId() + " is already owned by mso " + mso.getKey().getId());
				continue;
			}
			this.create(new ContentOwnership(), mso, channel);
			results.add(channel);
		}
		
		return results;
	}
	
	public List<ContentOwnership> findAll() {
		return ownershipDao.findAll();
	}
	
	public ContentOwnership findByMsoIdAndChannelId(long msoId, long channelId) {
		return ownershipDao.findByMsoIdAndChannelId(msoId, channelId);
	}

	public void delete(ContentOwnership ownership) {
		ownershipDao.delete(ownership);
	}
	
}
