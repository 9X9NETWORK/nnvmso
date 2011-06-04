package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.ContentOwnershipDao;
import com.nncloudtv.dao.NnChannelDao;
import com.nncloudtv.dao.NnSetDao;
import com.nncloudtv.model.ContentOwnership;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnSet;

@Service
public class ContentOwnershipManager {
	
	protected static final Logger logger = Logger.getLogger(ContentOwnershipManager.class.getName());
	
	private ContentOwnershipDao ownershipDao = new ContentOwnershipDao();
	private NnSetDao setDao = new NnSetDao();
	private NnChannelDao channelDao = new NnChannelDao();
	
	public List<NnSet> findOwnedSetsByMsoId(long msoId) {
		
		List<ContentOwnership> ownershipList = ownershipDao.findByMsoIdAndContentType(msoId, ContentOwnership.TYPE_CHANNELSET);
		
		ArrayList<Long> setIds = new ArrayList<Long>();
		for (ContentOwnership ownership : ownershipList) {
			setIds.add(ownership.getContentId());
		}
		
		return setDao.findAllByIds(setIds);
		
	}
	
	public List<NnChannel> findOwnedChannelsByMsoId(long msoId) {		
		List<ContentOwnership> ownershipList = ownershipDao.findByMsoIdAndContentType(msoId, ContentOwnership.TYPE_CHANNEL);
		
		ArrayList<Long> channelIds = new ArrayList<Long>();
		for (ContentOwnership ownership : ownershipList) {
			channelIds.add(ownership.getContentId());
		}
		return channelDao.findAllByIds(channelIds);
	}
	
	public void create(ContentOwnership own, Mso mso, NnChannel channel) {
		
		own.setContentType(ContentOwnership.TYPE_CHANNEL);
		own.setContentId(channel.getId());
		own.setMsoId(mso.getId());
		own.setCreateDate(new Date());
		own.setCreateMode(ContentOwnership.MODE_UPLOAD);
		
		ownershipDao.save(own);
	}

	public void create(ContentOwnership own, Mso mso, NnSet set) {
		
		own.setContentType(ContentOwnership.TYPE_CHANNELSET);
		own.setContentId(set.getId());
		own.setMsoId(mso.getId());
		own.setCreateDate(new Date());
		own.setCreateMode(ContentOwnership.MODE_CURATE);
		
		ownershipDao.save(own);
	}

	public List<ContentOwnership> findAllByMsoId(long msoId) {
		
		return ownershipDao.findAllByMsoId(msoId);
	}

	public List<NnChannel> create(Mso mso, List<NnChannel> channelList) {
		
		List<NnChannel> results = new ArrayList<NnChannel>();
		
		for (NnChannel channel : channelList) {
			ContentOwnership ownership = this.findByMsoIdAndChannelId(mso.getId(), channel.getId());
			if (ownership != null) {
				logger.warning("channel " + channel.getId() + " is already owned by mso " + mso.getId());
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
