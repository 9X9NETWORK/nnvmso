package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.sf.jsr107cache.Cache;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.MsoChannelDao;
import com.nnvmso.lib.CacheFactory;
import com.nnvmso.lib.FacebookLib;
import com.nnvmso.lib.PiwikLib;
import com.nnvmso.lib.SearchJanitorUtils;
import com.nnvmso.lib.YouTubeLib;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.ContentOwnership;
import com.nnvmso.model.LangTable;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoIpg;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.NnUserReport;
import com.nnvmso.model.SubscriptionLog;

@Service
public class MsoChannelManager {

	protected static final Logger log = Logger.getLogger(MsoChannelManager.class.getName());
	
	public static short MAX_CHANNEL_SIZE = 50;
	public static final int MAX_NUMBER_OF_WORDS_TO_PUT_IN_INDEX = 200;
	
	private MsoChannelDao msoChannelDao = new MsoChannelDao();

	public List<MsoChannel> findSince(Date since) {
		return msoChannelDao.findSince(since);
	}
	
	public boolean create(String url, String name, boolean devel, HttpServletRequest req) {
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findByEmail(Mso.NNEMAIL);		
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findNNMso();		
		boolean piwik = true;
		
		if (!url.contains("maplestage")) {  
    		String checkedUrl = YouTubeLib.formatCheck(url);
    		if (checkedUrl == null) {
    			log.info("bad url:" + url);
    			return false;
    		} else {
    			url = checkedUrl;
    		}
    	}
		
		MsoChannel c = this.findBySourceUrlSearch(url);
		TranscodingService tranService = new TranscodingService();
		if (c == null) {					
			c = new MsoChannel(url, user.getKey().getId());
			c.setStatus(MsoChannel.STATUS_PROCESSING);
			c.setContentType(this.getContentTypeByUrl(url));
			this.create(c);
			ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
			ownershipMngr.create(new ContentOwnership(), mso, c);
			if (!devel) {
				tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);
				this.save(c);
			} else {
				piwik = false; //local testing, no piwik creation
			}
		} else {		
			if (c.getContentType() == MsoChannel.CONTENTTYPE_YOUTUBE_CHANNEL || 
				c.getContentType() == MsoChannel.CONTENTTYPE_YOUTUBE_PLAYLIST) {
				if (c.getOriName() == null) {
					log.info("re-submit youtube channel:" + c.getSourceUrl());
					if (!devel)
						tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);						
				}
			}				
			//log.info("this channel existed:" + url);
			if (c.getStatus() == MsoChannel.STATUS_WAIT_FOR_APPROVAL) {
				log.info("mark the channel from waiting to approval to success");
				c.setStatus(MsoChannel.STATUS_SUCCESS);
			} else if (c.getStatus() == MsoChannel.STATUS_PROCESSING){
				if (!devel)
					tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);
				log.info("was in processing mode, going to submit again");
			} else if (c.getContentType() == MsoChannel.CONTENTTYPE_MAPLE_SOAP && c.getProgramCount() < 5) {
				log.info("maple soap program count < 5; re-send:" + c.getSourceUrlSearch());
				if (!devel)
					tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);
			} else if (c.getContentType() == MsoChannel.CONTENTTYPE_MAPLE_VARIETY && c.getProgramCount() < 5) {
				log.info("maple variety program count < 5; re-send:" + c.getSourceUrlSearch());
				if (!devel)
					tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);
			} else if (c.getStatus() != MsoChannel.STATUS_SUCCESS){
				log.info("wanted channel but not success");					
			}				
		}
		if (piwik) {
			String piwikId = PiwikLib.createPiwikSite(0, c.getKey().getId(), req);
			c.setPiwik(piwikId);
		}
		c.setName(name);		
		this.save(c);
		return true;
	}
	
	public void create(MsoChannel channel) {
		Date now = new Date();
		if (channel.getSourceUrl() != null) {
			channel.setSourceUrl(channel.getSourceUrl().trim()); //remove the trailing slash
			channel.setSourceUrlSearch(channel.getSourceUrl().toLowerCase());
		}
		if (channel.getLangCode() == null || channel.getLangCode().length() == 0) {
			channel.setLangCode(LangTable.LANG_EN);
		}
		channel.setCreateDate(now);
		channel.setUpdateDate(now);
		msoChannelDao.save(channel);

		//save to cache
		Cache cache = CacheFactory.get();		
		String key = this.getCacheKey(channel.getKey().getId());
		if (cache != null) { cache.put(key, channel); }				
	}
	
	/**
	 * @@@IMPORTANT 
	 * setProgramCount will be done automatically in MsoProgramManager when a program is added.
	 * If necessary to manually change programCount, please do with caution.
	 * 
	 * @@@IMPORTANT
	 * sourceURL is not supposed to be duplicated. Duplication check is your responsibility.   
	 */
	public void create(MsoChannel channel, List<Category> categories) {
		this.create(channel);
		//create CategoryChannel
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		for (Category c : categories) {
			ccMngr.create(new CategoryChannel(c.getKey().getId(), channel.getKey().getId()));
		}		
		//set category channelCount if necessary
		CategoryManager categoryMngr = new CategoryManager();
		if (this.isCounterQualified(channel)) {
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
	 *       
	 */
	public MsoChannel save(MsoChannel channel) {
		if (channel.getSourceUrl() != null) {
			channel.setSourceUrl(channel.getSourceUrl().trim());
			channel.setSourceUrlSearch(channel.getSourceUrl().toLowerCase());
		}
		
		channel.setUpdateDate(new Date());
		if (channel.getStatus() == MsoChannel.STATUS_SUCCESS)
			MsoChannelManager.updateFTSStuffForMsoChannel(channel);
		channel = msoChannelDao.save(channel);
		//save to cache
		Cache cache = CacheFactory.get();		
		String key = this.getCacheKey(channel.getKey().getId());
		if (cache != null) { cache.put(key, channel); }
		return channel;
	}		
	
	/**
	 * No deletion so we can keep track of blacklist urls 
	 */
	public void delete(MsoChannel channel) {
		msoChannelDao.delete(channel);
		//delete categories
		//delete channel
		//delete programs
		//change category channelCount
		//check cache
	}
	
	public void deletePrograms(MsoChannel channel) {
		MsoProgramManager programMngr = new MsoProgramManager();
		List<MsoProgram> programs = programMngr.findAllByChannelId(channel.getKey().getId());
		programMngr.deleteAll(programs);		
	}

	public void calculateAndSaveChannelCount(long channelId) {
		MsoProgramManager programMngr = new MsoProgramManager();
		List<MsoProgram> programs = programMngr.findGoodProgramsByChannelId(channelId);
		MsoChannel channel = this.findById(channelId);
		if (channel != null) 
			channel.setProgramCount(programs.size());		
		this.save(channel);		
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
	
	
	public List<MsoChannel> findUnUniqueSourceUrl() {
		List<MsoChannel> channels = this.findAll();
		HashSet<String> set = new HashSet<String>();
		List<MsoChannel> bad = new ArrayList<MsoChannel>();
		for (MsoChannel c : channels) {
			if (!set.contains(c.getSourceUrl())) {
				set.add(c.getSourceUrl());
			} else {
				log.info("duplicate source url:" + c.getSourceUrl());
				bad.add(c);
			}
		}
		return bad;
	}
		
	public MsoChannel initChannelSubmittedFromPlayer(String sourceUrl, NnUser user) {
		if (sourceUrl == null) {return null;}
		MsoChannel channel = new MsoChannel(sourceUrl, user.getKey().getId());
		channel.setContentType(this.getContentTypeByUrl(sourceUrl));
		if (channel.getContentType() == MsoChannel.CONTENTTYPE_FACEBOOK) {
			FacebookLib lib = new FacebookLib();
			String[] info = lib.getFanpageInfo(sourceUrl);			
			channel.setName(info[0]);
			if (info[1] == null) {
				channel.setImageUrl("/WEB-INF/../images/facebook-icon.gif");			
			} else {
				channel.setImageUrl(info[1]);
			}
			channel.setStatus(MsoChannel.STATUS_SUCCESS);
		} else {
			channel.setImageUrl("/WEB-INF/../images/processing.png");
			channel.setName("Processing");
			channel.setStatus(MsoChannel.STATUS_PROCESSING);
			if (channel.getContentType() == MsoChannel.CONTENTTYPE_YOUTUBE_CHANNEL) {
				String url = channel.getSourceUrl();
				String name = YouTubeLib.getYouTubeChannelName(url);
				log.info("youtube: " + name);
				Map<String, String> info = YouTubeLib.getYouTubeChannelEntry(name);
				if (!info.get("status").equals(String.valueOf(NnStatusCode.SUCCESS)))
					return null;
				if (info.get("title") != null)
					channel.setName(info.get("title"));
				if (info.get("description") != null)
					channel.setIntro(info.get("description"));
				if (info.get("thumbnail") != null)
					channel.setImageUrl(info.get("thumbnail"));
			} else if (channel.getContentType() == MsoChannel.CONTENTTYPE_YOUTUBE_PLAYLIST) {
				String url = channel.getSourceUrl();
				String name = YouTubeLib.getYouTubeChannelName(url);
				log.info("playlist: " + name);
				Map<String, String> info = YouTubeLib.getYouTubePlaylistEntry(name);
				if (!info.get("status").equals(String.valueOf(NnStatusCode.SUCCESS)))
					return null;
				if (info.get("title") != null)
					channel.setName(info.get("title"));
				if (info.get("description") != null)
					channel.setIntro(info.get("description"));
				if (info.get("thumbnail") != null)
					channel.setImageUrl(info.get("thumbnail"));
			}
		}
		channel.setSourceUrlSearch(sourceUrl.toLowerCase());
		channel.setUserId(user.getKey().getId());
		channel.setPublic(false);
		return channel;
	}
	
	//the url has to be verified(verifyUrl) first
	public short getContentTypeByUrl(String url) {
		short type = MsoChannel.CONTENTTYPE_PODCAST;
		if (url.contains("http://www.youtube.com"))			
			type = MsoChannel.CONTENTTYPE_YOUTUBE_CHANNEL;
		if (url.contains("http://www.youtube.com/view_play_list?p="))
			type = MsoChannel.CONTENTTYPE_YOUTUBE_PLAYLIST;
		if (url.contains("facebook.com")) 
			type = MsoChannel.CONTENTTYPE_FACEBOOK;
		/*
		if (url.contains("maplestage"))
			type = MsoChannel.CONTENTTYPE_MAPLE_VARIETY; //or soap
			*/
		return type;
	}		
		
	public String verifyUrl(String url) {
		if (url == null) return null;
		if (!url.contains("http://") && !url.contains("https://"))
			return null;
		if (!url.contains("youtube.com") && !url.contains("facebook.com"))
			return null;
		if (url.contains("youtube.com"))
			url = YouTubeLib.formatCheck(url);
		
		/*
		if (!url.contains("youtube.com")) {
			if (url.contains("deimos3.apple.com")) { //temp fix for demo
				return url;
			}
			if (url.contains("facebook.com")) {
				return url;
			}
			String podcastInfo[] = tranService.getPodcastInfo(url);			
			if (!podcastInfo[0].equals("200") || !podcastInfo[1].contains("xml")) {
				log.info("invalid url:" + url);		
				return null;
			}
		} else {
			url = YouTubeLib.formatCheck(url);
		}
		*/
		return url;
	}

	public List<MsoChannel> findMaples() {
		return msoChannelDao.findMaples();
	}
	
	public List<MsoChannel> findMsoDefaultChannels(long msoId, boolean needSubscriptionCnt) {		
		//find msoIpg
		MsoIpgManager msoIpgMngr = new MsoIpgManager();
		SubscriptionLogManager sublogMngr = new SubscriptionLogManager();		
		List<MsoIpg>msoIpg = msoIpgMngr.findAllByMsoId(msoId);						
		//retrieve channels
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		for (MsoIpg i : msoIpg) {
			MsoChannel channel = this.findById(i.getChannelId());
			if (channel != null) {
				channel.setType(i.getType());
				channel.setSeq(i.getSeq());
				if (needSubscriptionCnt) {
					SubscriptionLog sublog = sublogMngr.findByMsoIdAndChannelId(msoId, channel.getKey().getId());
					channel.setSubscriptionCount(sublog.getCount());
				}
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
	
	public List<MsoChannel> findAllByChannelIds(List<Long> channelIds) {
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		for (Long id : channelIds) {
			MsoChannel channel = this.findById(id);
			if (channel != null) channels.add(channel);
		}
		return channels;		
	}
	
	public List<MsoChannel> findPublicChannels(boolean needSubscriptionCnt) {
		List<MsoChannel> channels = msoChannelDao.findPublicChannels();
		SubscriptionLogManager sublogMngr = new SubscriptionLogManager();
		//currently the counter is brand-unaware
		if (needSubscriptionCnt) {
			for (MsoChannel c : channels) {
				SubscriptionLog sublog = sublogMngr.findByChannelId(c.getKey().getId());							
				if (sublog != null) {c.setSubscriptionCount(sublog.getCount());}			
			}
		}
		return channels;
	}
	
	public List<MsoChannel> findAllByStatus(short status) {
		List<MsoChannel> channels = msoChannelDao.findAllByStatus(status);		
		return channels;
	}	

	public List<MsoChannel> findFeaturedChannelsByMso(NnUser user) {
		List<MsoChannel> channels = msoChannelDao.findFeaturedChannelsByMso(user);
		return channels;
	}		

	public List<MsoChannel> findFeaturedChannels() {
		List<MsoChannel> channels = msoChannelDao.findFeaturedChannels();
		return channels;
	}		
	
	//!!! here or dao
	public MsoChannel findBySourceUrlSearch(String url) {
		if (url == null) {return null;}
		return msoChannelDao.findBySourceUrlSearch(url.trim().toLowerCase());
	}

	public MsoChannel findByName(String name) {
		return msoChannelDao.findByName(name);
	}
	
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
			if (channel != null && channel.getStatus() == MsoChannel.STATUS_SUCCESS && channel.getProgramCount() > 0 && channel.isPublic()) { 
				//category is used to find this channel's mso, then find corresponding subscription count
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
		
	public List<MsoChannel> findfindAllAfterTheDate(Date since) {
		return msoChannelDao.findAllAfterTheDate(since);
	}
	
	//!!! limit
	public List<MsoChannel> findAll() {
		return msoChannelDao.findAll();
	}

	public List<MsoChannel> findProgramsMoreThanMax() {
		return msoChannelDao.findProgramMoreThanMax();
	}
	
	public List<MsoChannel> list(int page, int limit, String sidx, String sord) {
		return msoChannelDao.list(page, limit, sidx, sord);
	}
	
	public List<MsoChannel> list(int page, int limit, String sidx, String sord, String filter) {
		return msoChannelDao.list(page, limit, sidx, sord, filter);
	}
	
	public int total() {
		return msoChannelDao.total();
	}
	
	public int total(String filter) {
		return msoChannelDao.total(filter);
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

	public List<MsoChannel> findPublicChannelsByCategoryIdAndLang(long categoryId, String lang) {
		//channels within a category
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		CategoryManager categoryMngr = new CategoryManager();
		SubscriptionLogManager sublogMngr = new SubscriptionLogManager();
		List<CategoryChannel> ccs = (List<CategoryChannel>) ccMngr.findAllByCategoryId(categoryId);

		//retrieve channels
		List<MsoChannel> chineseChannels = new ArrayList<MsoChannel>();
		List<MsoChannel> englishChannels = new ArrayList<MsoChannel>();
		for (CategoryChannel cc : ccs) {
			MsoChannel channel = this.findById(cc.getChannelId());
			if (channel != null && channel.getStatus() == MsoChannel.STATUS_SUCCESS && channel.getProgramCount() > 0 && channel.isPublic()) { 
				//category is used to find this channel's mso, then find corresponding subscription count
				Category category  = categoryMngr.findById(cc.getCategoryId());
				if (category != null) {
					SubscriptionLog sublog = sublogMngr.findByMsoIdAndChannelId(category.getMsoId(), channel.getKey().getId());			
				    if (sublog != null) {channel.setSubscriptionCount(sublog.getCount());}
				}
				if (channel.getLangCode() != null && channel.getLangCode().equals(Mso.LANG_ZH))
					chineseChannels.add(channel);
				else 
					englishChannels.add(channel);
			}
		}				
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		if (lang != null) {
			if (lang.equals(Mso.LANG_ZH))					
				channels.addAll(chineseChannels);
			else
				channels.addAll(englishChannels);
		} else { 
			channels.addAll(chineseChannels);
			channels.addAll(englishChannels);
		}
		return channels;
	}
	
	public List<MsoChannel> findChannelsByIdStr(String channelIds) {
		List<Long> channelIdList = new ArrayList<Long>();	
		String[] arr = channelIds.split(",");
		for (int i = 0; i < arr.length; i++) {
			channelIdList.add(Long.parseLong(arr[i]));
		}
		List<MsoChannel> channels = msoChannelDao.findAllByIds(channelIdList);
		return channels;
	}

	public static void updateFTSStuffForMsoChannel(MsoChannel channel) {			
		StringBuffer sb = new StringBuffer();		
		sb.append(channel.getName() + " " + channel.getIntro());			
		Set<String> new_ftsTokens = SearchJanitorUtils.getTokensForIndexingOrQuery(
				sb.toString(),
				MAX_NUMBER_OF_WORDS_TO_PUT_IN_INDEX);				
		Set<String> ftsTokens = channel.getFts();
		if (ftsTokens != null)
			ftsTokens.clear();
		else
			ftsTokens = new HashSet<String>();
		for (String token : new_ftsTokens) {
			ftsTokens.add(token);
		}		
	}
	
	public static Set<String> getFtsTokens(String name, String intro) {			
		StringBuffer sb = new StringBuffer();		
		sb.append(name + " " + intro);			
		Set<String> new_ftsTokens = SearchJanitorUtils.getTokensForIndexingOrQuery(
				sb.toString(),
				MAX_NUMBER_OF_WORDS_TO_PUT_IN_INDEX);
		Set<String> ftsTokens = new HashSet<String>();
		ftsTokens.clear();
		for (String token : new_ftsTokens) {
			ftsTokens.add(token);
		}
		return ftsTokens;
	}   
	public List<MsoChannel> findChannelsByType(short type) {
		return msoChannelDao.findChannelsByType(type);		
	}
	
	public static List<MsoChannel> searchChannelEntries(String queryString) {
		return MsoChannelDao.searchChannelEntries(queryString);		
	}
	
	public static short getDefaultSorting(MsoChannel c) {
		short sorting = MsoChannel.SORT_NEWEST_TO_OLDEST; 
		if (c.getContentType() == MsoChannel.CONTENTTYPE_MAPLE_SOAP || 
			c.getContentType() == MsoChannel.CONTENTTYPE_MAPLE_VARIETY)
			sorting = MsoChannel.SORT_MAPEL;
		return sorting;
	}
}
