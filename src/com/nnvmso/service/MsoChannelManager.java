package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
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
	private Cache cache;
	
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
		
		//create CategoryChannel
		CategoryChannelManager ccMngr = new CategoryChannelManager();		
		for (Category c : categories) {
			ccMngr.create(new CategoryChannel(c.getKey().getId(), channel.getKey().getId()));
		}
		
		//set category channelCount if necessary
		CategoryManager categoryMngr = new CategoryManager();
		if (channel.getStatus() == MsoChannel.STATUS_SUCCESS && channel.isPublic()) {
			for (Category c : categories) {			
				c.setChannelCount(c.getChannelCount() + 1);
				categoryMngr.save(c);
			}
		}
		
		//save to cache
		this.setCache();		
		String key = this.getCacheKey(channel.getKey().getId());
		if (cache != null) { cache.put(key, channel); }		
	}

	/** 
	 * @param originalState used to calculate category counter, pass null if not interested
	 */
	public MsoChannel save(MsoChannel originalState, MsoChannel channel) {
		//change category's channelCount() !!! minus scenario is missing
		if (originalState != null &&
			channel.getStatus() == MsoChannel.STATUS_SUCCESS && 
			channel.isPublic() == true && 
			(originalState.getStatus() != MsoChannel.STATUS_SUCCESS || !originalState.isPublic()) ) {
			log.info("add category counter");
			CategoryChannelManager ccMngr = new CategoryChannelManager();
			CategoryManager categoryManager = new CategoryManager();
			List<CategoryChannel> ccs = ccMngr.findAllByChannelId(channel.getKey().getId());
			List<Long> categoryIds = new ArrayList<Long>(); 
			for (CategoryChannel cc : ccs) {
				categoryIds.add(cc.getCategoryId());
			}
			List<Category> categories = categoryManager.findAllByIds(categoryIds);
			for (Category c : categories) {
				c.setChannelCount(c.getChannelCount()+1);
			}
		}
		channel = msoChannelDao.save(channel);
		//save to cache
		this.setCache();		
		String key = this.getCacheKey(channel.getKey().getId());
		if (cache != null) { cache.put(key, channel); }
		return channel;
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
			MsoChannel channel = this.findById(i.getChannelId());
			if (channel != null) {
				channel.setType(i.getType());
				channel.setSeq(i.getSeq());
				channels.add(channel);
			}
		}
		return channels;
	}	

	//cached
	public MsoChannel findById(long id) {
		//find from cache
		this.setCache();
		String key = this.getCacheKey(id);
		if (cache != null) {
			MsoChannel channel = (MsoChannel) cache.get(key);
			if (channel != null) {
				log.info("Cache found: channel in cache:" + channel.getKey().getId());
				return channel;
			}
		}
		//find
		MsoChannel channel = msoChannelDao.findById(id);
		//save in cache
		if (cache != null && channel != null) { cache.put(key, channel);}		
		log.info("Cache NOT found: channel is just added:" + channel.getKey().getId());
		return channel;
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
			//!!! fix query
			MsoChannel channel = this.findById(cc.getChannelId());
			if (channel != null && channel.getStatus() == MsoChannel.STATUS_SUCCESS && channel.getProgramCount() > 0) {
				channels.add(channel);
			}
		}
		return channels;
	}
						
	public MsoChannel findByKey(Key key) {
		return msoChannelDao.findByKey(key);
	}
	
	private void setCache() {
	    try {
	        cache = CacheManager.getInstance().getCacheFactory().createCache(
	            Collections.emptyMap());
	      } catch (CacheException e) {}	      		
	}

	private String getCacheKey(long id) {
		return "channel(" + id + ")";		
	}
}
