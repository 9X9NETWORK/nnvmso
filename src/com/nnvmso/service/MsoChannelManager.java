package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.nnvmso.dao.MsoChannelDao;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoIpg;
import com.nnvmso.model.NnUser;

@Service
public class MsoChannelManager {

	protected static final Logger log = Logger.getLogger(MsoChannelManager.class.getName());
	
	public static short MAX_CHANNEL_SIZE = 50;
	
	private MsoChannelDao msoChannelDao = new MsoChannelDao();
		
	/**
	 * @@@IMPORTANT 
	 * setProgramCount will be done automatically in MsoProgramManager when a program is added.
	 * If necessary to manually change programCount, please do with caution.    	
	 */
	public void create(MsoChannel channel, List<Category> categories) {
		Date now = new Date();
		channel.setCreateDate(now);
		channel.setUpdateDate(now);
		msoChannelDao.save(channel);
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		if (channel.getStatus() == MsoChannel.STATUS_SUCCESS && channel.isPublic() == true) {
			for (Category c : categories) {
				ccMngr.create(new CategoryChannel(c.getKey().getId(), channel.getKey().getId()));
			}
		}
	}
	
	public MsoChannel save(MsoChannel channel) {
		return msoChannelDao.save(channel);
	}		
	
	public MsoChannel initChannelSubmittedFromPlayer(String sourceUrl, NnUser user) {
		MsoChannel channel = new MsoChannel(sourceUrl, user.getKey().getId());
		channel.setContentType(this.getContentTypeByUrl(sourceUrl));		
		channel.setImageUrl("/WEB-INF/../images/processing.png");
		if (channel.getContentType() == MsoChannel.CONTENTTYPE_PODCAST) {
			channel.setName("Podcast Processing");
		} else if (channel.getType() == MsoChannel.CONTENTTYPE_YOUTUBE) {
			channel.setName("Youtube Processing");
		}
		channel.setStatus(MsoChannel.STATUS_PROCESSING);
		channel.setUserId(user.getKey().getId());
		channel.setPublic(false);
		return channel;
	}
				
	public short getContentTypeByUrl(String url) {
		short type = MsoChannel.CONTENTTYPE_PODCAST;
		//!!!
		if (url.contains("http://www.youtube.com") || url.contains("https://www.youtube.com") || 
			url.contains("http://youtube.com") || url.contains("https://youtube.com"))
		{			
			type = MsoChannel.CONTENTTYPE_YOUTUBE;
		}
		return type;
	}		
		
	public boolean verifyPodcastUrl(MsoChannel channel) {
		boolean valid = true;		
		TranscodingService tranService = new TranscodingService();
		if (channel.getContentType() == MsoChannel.CONTENTTYPE_PODCAST) {
			String podcastInfo[] = tranService.getPodcastInfo(channel.getSourceUrl());			
			if (!podcastInfo[0].equals("200") || !podcastInfo[1].contains("xml")) {
				valid = false;				
			}
		}
		return valid;
	}

	public List<MsoChannel> findMsoDefaultChannels(long msoId) {		
		//find msoIpg
		MsoIpgManager msoIpgMngr = new MsoIpgManager();
		List<MsoIpg>msoIpg = msoIpgMngr.findAllByMsoId(msoId);				
		
		//retrieve channels
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		for (MsoIpg i : msoIpg) {
			MsoChannel channel = msoChannelDao.findById(i.getChannelId());
			if (channel != null) {
				channel.setType(i.getType());
				channel.setSeq(i.getSeq());
				channels.add(channel);
			}
		}
		return channels;
	}	

	public MsoChannel findById(long id) {
		return msoChannelDao.findById(id);
	}
	
	public List<MsoChannel> findPublicChannels() {
		return msoChannelDao.findPublicChannels();				
	}	

	public MsoChannel findBySourceUrl(String url) {
		return msoChannelDao.findBySourceUrl(url);
	}

	public MsoChannel findByName(String name) {
		return msoChannelDao.findByName(name);
	}
	
	public List<MsoChannel> findPublicChannelsByCategoryId(long categoryId) {
		//channels within a category
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		List<CategoryChannel> ccs = (List<CategoryChannel>) ccMngr.findAllByCategoryId(categoryId);

		//retrieve channels
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		for (CategoryChannel cc : ccs) {
			MsoChannel channel = msoChannelDao.findById(cc.getChannelId());
			if (channel != null && channel.getStatus() != MsoChannel.STATUS_ERROR && channel.getProgramCount() > 0) {
				channels.add(channel);
			}
		}
		return channels;
	}
						
	public MsoChannel findByKey(Key key) {
		return msoChannelDao.findByKey(key);
	}
	
	public MsoChannel findByKeyStr(String key) {		
		try {
		  return this.findByKey(KeyFactory.stringToKey(key));
		} catch (IllegalArgumentException e) {
			log.info("invalid key string");
			return null;
		}				
	}
	
}
