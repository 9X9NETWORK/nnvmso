package com.nnvmso.service;

import java.util.ArrayList;
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
		
	public void create(MsoChannel channel, List<Category> categories) {		
		msoChannelDao.create(channel);
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		for (Category c : categories) {
			ccMngr.create(new CategoryChannel(c.getKey(), channel.getKey()));
		}
	}
		
	public MsoChannel initChannelSubmittedFromPlayer(String sourceUrl, NnUser user) {
		MsoChannel channel = new MsoChannel(sourceUrl, user.getKey());
		channel.setContentType(this.getContentTypeByUrl(sourceUrl));		
		if (channel.getContentType() == MsoChannel.CONTENTTYPE_PODCAST) {
			channel.setName("Podcast Processing");
			channel.setImageUrl("/WEB-INF/../images/podcastDefault.jpg");
		} else if (channel.getType() == MsoChannel.CONTENTTYPE_YOUTUBE) {
			channel.setName("Youtube Processing");
			channel.setImageUrl("/WEB-INF/../images/youtube_videos.gif");
		}
		channel.setNnUserKey(user.getKey());
		channel.setPublic(false);
		return channel;
	}
	
	public MsoChannel save(MsoChannel channel) {
		return msoChannelDao.save(channel);
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

	public List<MsoChannel> findMsoDefaultChannels(Key msoKey) {		
		//find msoIpg
		MsoIpgManager msoIpgMngr = new MsoIpgManager();
		List<MsoIpg>msoIpg = msoIpgMngr.findByMsoKey(msoKey);				
		
		//retrieve channels
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		for (MsoIpg i : msoIpg) {
			MsoChannel channel = msoChannelDao.findByKey(i.getChannelKey());
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
		
	//!!! return null a good idea? or category should be handled by outside.
	public List<MsoChannel> findPublicChannelsByCategoryId(long id) {
		//get category
		CategoryManager categoryMngr= new CategoryManager();
		Category category = categoryMngr.findById(id);
		if (category == null) {return null;}
		
		//channels within a category
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		List<CategoryChannel> ccs = (List<CategoryChannel>) ccMngr.findByCategoryKey(category.getKey());

		//retrieve channels
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		for (CategoryChannel cc : ccs) {
			MsoChannel channel = msoChannelDao.findByKey(cc.getChannelKey());
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
