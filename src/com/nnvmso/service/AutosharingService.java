package com.nnvmso.service;

import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.ChannelAutosharingDao;
import com.nnvmso.dao.ChannelSetAutosharingDao;
import com.nnvmso.model.SnsAuth;

@Service
public class AutosharingService {
	
	protected static final Logger logger = Logger.getLogger(AutosharingService.class.getName());
	
	private ChannelAutosharingDao channelAutosharingDao = new ChannelAutosharingDao();
	private ChannelSetAutosharingDao channelSetAutosharingDao = new ChannelSetAutosharingDao();
	
	public boolean isChannelAutosharedToFacebook(long channelId) {
		return channelAutosharingDao.isChannelAutosharedTo(channelId, SnsAuth.TYPE_FACEBOOK);
	}
	
	public boolean isChannelAutosharedToTwitter(long channelId) {
		return channelAutosharingDao.isChannelAutosharedTo(channelId, SnsAuth.TYPE_TWITTER);
	}
	
	public boolean isChannelAutosharedToPlurk(long channelId) {
		return channelAutosharingDao.isChannelAutosharedTo(channelId, SnsAuth.TYPE_PLURK);
	}
	
	public boolean isChannelAutosharedToSina(long channelId) {
		return channelAutosharingDao.isChannelAutosharedTo(channelId, SnsAuth.TYPE_SINA);
	}
	
	public boolean isChannelSetAutosharedToFacebook(long channelSetId) {
		return channelSetAutosharingDao.isChannelSetAutosharedTo(channelSetId, SnsAuth.TYPE_FACEBOOK);
	}
	
	public boolean isChannelSetAutosharedToTwitter(long channelSetId) {
		return channelSetAutosharingDao.isChannelSetAutosharedTo(channelSetId, SnsAuth.TYPE_TWITTER);
	}
	
	public boolean isChannelSetAutosharedToPlurk(long channelSetId) {
		return channelSetAutosharingDao.isChannelSetAutosharedTo(channelSetId, SnsAuth.TYPE_PLURK);
	}
	
	public boolean isChannelSetAutosharedToSina(long channelSetId) {
		return channelSetAutosharingDao.isChannelSetAutosharedTo(channelSetId, SnsAuth.TYPE_SINA);
	}
	
}
