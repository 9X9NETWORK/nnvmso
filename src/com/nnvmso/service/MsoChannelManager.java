package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.MsoChannelDao;
import com.nnvmso.lib.CacheFactory;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoIpg;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.SubscriptionLog;

@Service
public class MsoChannelManager {

	protected static final Logger log = Logger.getLogger(MsoChannelManager.class.getName());
	
	public static short MAX_CHANNEL_SIZE = 50;
	
	private MsoChannelDao msoChannelDao = new MsoChannelDao();
	
	/**
	 * @@@IMPORTANT 
	 * setProgramCount will be done automatically in MsoProgramManager when a program is added.
	 * If necessary to manually change programCount, please do with caution.
	 * 
	 * @@@IMPORTANT
	 * sourceURL is not supposed to be duplicated. Duplication check is your responsibility.   
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
		if (this.isCounterQualified(channel)) {
		    System.out.println("channel manager, channel create, addChannelCount");
			categoryMngr.addChannelCounter(channel);
		}
		
		//save to cache
		Cache cache = CacheFactory.get();		
		String key = this.getCacheKey(channel.getKey().getId());
		if (cache != null) { cache.put(key, channel); }		
	}

	/**
	 * There's chance category's channelCounter is wrong, but so far the chance is small: 
	 * 1. when channel from public to non-public
	 * 2. when channel from status success to non-success
	 * Currently the counter is mainly dealt in MsoProgramManager.create() 
	 * Will need to fix it in transaction     
	 */
	public MsoChannel save(MsoChannel channel) {
		channel = msoChannelDao.save(channel);
		//save to cache
		Cache cache = CacheFactory.get();		
		String key = this.getCacheKey(channel.getKey().getId());
		if (cache != null) { cache.put(key, channel); }
		return channel;
	}		
	
	public void delete(MsoChannel channel) {
		//category channelCount
		//cache
	}
	
	//!!! model?
	public boolean isCounterQualified(MsoChannel channel) {
		boolean qualified = false;
		if (channel.getStatus() == MsoChannel.STATUS_SUCCESS &&
			channel.getProgramCount() > 0 &&
			channel.isPublic()) {
			qualified = true;
		}
		return qualified;
	}
	
	public MsoChannel initChannelSubmittedFromPlayer(String sourceUrl, NnUser user) {
		MsoChannel channel = new MsoChannel(sourceUrl, user.getKey().getId());
		channel.setContentType(this.getContentTypeByUrl(sourceUrl));
		channel.setImageUrl("/WEB-INF/../images/processing.png");
		if (channel.getContentType() == MsoChannel.CONTENTTYPE_PODCAST) {
			channel.setName("Podcast Processing");
		} else if (channel.getContentType() == MsoChannel.CONTENTTYPE_YOUTUBE) {
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

	/**
	 * @@@ Cached 
	 */
	public MsoChannel findById(long id) {
		//find from cache
		Cache cache = CacheFactory.get();
		String key = this.getCacheKey(id);
		if (cache != null) {
			MsoChannel channel = (MsoChannel) cache.get(key);
			if (channel != null) { return channel;}
		}
		//find
		MsoChannel channel = msoChannelDao.findById(id);
		//save in cache
		if (cache != null && channel != null) { cache.put(key, channel);}		
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
	
	//!!! fix query
	public List<MsoChannel> findPublicChannelsByCategoryId(long categoryId) {
		//channels within a category
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		CategoryManager categoryMngr = new CategoryManager();
		SubscriptionLogManager sublogMngr = new SubscriptionLogManager();
		List<CategoryChannel> ccs = (List<CategoryChannel>) ccMngr.findAllByCategoryId(categoryId);

		//retrieve channels
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		for (CategoryChannel cc : ccs) {
			MsoChannel channel = this.findById(cc.getChannelId());
			if (channel != null && channel.getStatus() == MsoChannel.STATUS_SUCCESS && channel.getProgramCount() > 0) {
				Category category  = categoryMngr.findById(cc.getCategoryId());
				if (category != null) {
					SubscriptionLog sublog = sublogMngr.findByMsoIdAndChannelId(category.getMsoId(), channel.getKey().getId());			
				    if (sublog != null) {channel.setSubscriptionCount(sublog.getCount());}
				}
				channels.add(channel);
			}
		}				
		
		return channels;
	}
		
	//!!! limit
	public List<MsoChannel> findAll() {
		return msoChannelDao.findAll();
	}
	
	public MsoChannel findByKey(Key key) {
		return msoChannelDao.findByKey(key);
	}
	
	private String getCacheKey(long id) {
		return "channel(" + id + ")";		
	}

	public List<MsoChannel> findCache() {
		List<MsoChannel> channels = this.findAll();
		List<MsoChannel> cachedChannels = new ArrayList<MsoChannel>();
		Cache cache = CacheFactory.get();
		if (cache != null) {
			for (MsoChannel c : channels) {
				if (cache.get(this.getCacheKey(c.getKey().getId())) != null) {
					cachedChannels.add(c);
				}
			}			
		}
		return cachedChannels;
	}
	
	//!!! timeout
	public void cacheAll() {
		List<MsoChannel> channels = this.findAll();
		Cache cache = CacheFactory.get();
		if (cache != null) {
			for (MsoChannel c : channels) {
				cache.put(this.getCacheKey(c.getKey().getId()), c);
			}			
		}		
	}
	
	public void deleteCache() {
		Cache cache = CacheFactory.get();
		List<MsoChannel> channels = this.findAll();
		if (cache != null) {
			for (MsoChannel c : channels) {
				cache.remove(this.getCacheKey(c.getKey().getId()));
			}						
		}
	}
	
}
