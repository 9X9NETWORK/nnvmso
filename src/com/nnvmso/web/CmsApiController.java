package com.nnvmso.web;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jsr107cache.Cache;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import twitter4j.TwitterException;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.nnvmso.lib.CacheFactory;
import com.nnvmso.lib.FacebookLib;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.lib.PiwikLib;
import com.nnvmso.lib.YouTubeLib;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.CategoryChannelSet;
import com.nnvmso.model.ChannelAutosharing;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ChannelSetAutosharing;
import com.nnvmso.model.ChannelSetChannel;
import com.nnvmso.model.ContentOwnership;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.SnsAuth;
import com.nnvmso.service.AreaOwnershipManager;
import com.nnvmso.service.AutosharingService;
import com.nnvmso.service.CategoryChannelManager;
import com.nnvmso.service.CategoryChannelSetManager;
import com.nnvmso.service.CategoryManager;
import com.nnvmso.service.ChannelSetChannelManager;
import com.nnvmso.service.ChannelSetManager;
import com.nnvmso.service.CmsApiService;
import com.nnvmso.service.ContentOwnershipManager;
import com.nnvmso.service.ContentWorkerService;
import com.nnvmso.service.IpgManager;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.MsoProgramManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.SnsAuthManager;
import com.nnvmso.service.SubscriptionLogManager;
import com.nnvmso.service.TranscodingService;
import com.nnvmso.web.json.facebook.FBPost;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

@Controller
@RequestMapping("CMSAPI")
public class CmsApiController {
	protected static final Logger logger = Logger.getLogger(CmsApiController.class.getName());
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/blank";
	}
	
	//////////////////// ChannelSet Management ////////////////////
	
	@RequestMapping("searchChannel")
	public @ResponseBody List<MsoChannel> searchChannel(@RequestParam String text) {
		logger.info("search: " + text);
		if (text == null || text.length() == 0) {
			logger.warning("no query string");
			return new ArrayList<MsoChannel>();
		}
		return MsoChannelManager.searchChannelEntries(text);
	}
	
	@RequestMapping("defaultChannelSetInfo")
	public @ResponseBody ChannelSet defaultChannelSetInfo(@RequestParam Long msoId) {
		CmsApiService cmsService = new CmsApiService();
		return cmsService.getDefaultChannelSet(msoId);
	}
	
	/**
	 * Which system category is default channel set in
	 * 
	 * @param msoId
	 * @return Category or null (if more than one categories found, return the first one)
	 */
	@RequestMapping("defaultChannelSetCategory")
	public @ResponseBody Category defaultChannelSetCategory(@RequestParam Long msoId) {
		CmsApiService cmsService = new CmsApiService();
		ChannelSet channelSet = cmsService.getDefaultChannelSet(msoId);
		if (channelSet == null)
			return null;
		List<Category> categoryList = cmsService.whichSystemCategoriesContainingTheChannelSet(channelSet.getKey().getId());
		if (categoryList.size() > 0)
			return categoryList.get(0);
		else
			return null;
	}
	
	/**
	 * List all channel in mso default channel set
	 */
	@RequestMapping("defaultChannelSetChannels")
	public @ResponseBody List<MsoChannel> defaultChannelSetChannels(@RequestParam(required=false) Long msoId,
	                                                                 @RequestParam(required=false) Boolean isGood,
	                                                                 @RequestParam(required=false) Long channelSetId) {
		CmsApiService cmsService = new CmsApiService();
		ChannelSetManager setMngr = new ChannelSetManager();
		ChannelSet channelSet = null;
		if (msoId != null) {
			channelSet = cmsService.getDefaultChannelSet(msoId);
		} else if (channelSetId != null) {
			channelSet = setMngr.findById(channelSetId);
		}
		if (channelSet == null)
			return new ArrayList<MsoChannel>();
		List<MsoChannel> cadidate = cmsService.findChannelsByChannelSetId(channelSet.getKey().getId());
		List<MsoChannel> results = new ArrayList<MsoChannel>();
		SubscriptionLogManager subLogMngr = new SubscriptionLogManager();
		for (MsoChannel channel : cadidate) {
			if (isGood == null || !isGood || channel.getStatus() == MsoChannel.STATUS_SUCCESS) {
				channel.setSubscriptionCount(subLogMngr.findTotalCountByChannelId(channel.getKey().getId()));
				results.add(channel);
			}
		}
		return results;
	}
	
	@RequestMapping("listOwnedAndDefaultSetChannels")
	public @ResponseBody List<MsoChannel> listOwnedAndDefaultSetChannels(@RequestParam Long msoId) {
		
		SubscriptionLogManager subLogMngr = new SubscriptionLogManager();
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		List<MsoChannel> results = new ArrayList<MsoChannel>();
		CmsApiService cmsService = new CmsApiService();
		
		logger.info("msoId = " + msoId);
		
		class MsoChannelComparator implements Comparator<MsoChannel> {  // yes, I know, its a little dirty
			@Override
			public int compare(MsoChannel channel1, MsoChannel channel2) {
				Date date1 = channel1.getUpdateDate();
				Date date2 = channel2.getUpdateDate();
				return date2.compareTo(date1);
			}
		}
		
		results = ownershipMngr.findOwnedChannelsByMsoId(msoId);
		Collections.sort(results, new MsoChannelComparator());
		
		ChannelSet channelSet = cmsService.getDefaultChannelSet(msoId);
		if (channelSet == null) {
			for (MsoChannel channel : results) {
				channel.setSubscriptionCount(subLogMngr.findTotalCountByChannelId(channel.getKey().getId()));
			}
			return results;
		}
		
		HashSet<Long> set = new HashSet<Long>();
		for (MsoChannel channel : results) {
			set.add(channel.getKey().getId());
		}
		
		List<MsoChannel> results2 = cmsService.findChannelsByChannelSetId(channelSet.getKey().getId());
		for (MsoChannel channel : results2) {
			Long channelId = channel.getKey().getId();
			if (!set.contains(channelId)) {
				set.add(channelId);
				channel.setSubscriptionCount(subLogMngr.findTotalCountByChannelId(channelId));
				results.add(channel);
			}
		}
		
		return results;
	}
	
	@RequestMapping("saveChannelSet")
	public @ResponseBody String saveChannelSet(HttpServletRequest req,
	                                           @RequestParam Long channelSetId,
	                                           @RequestParam(required = false) String channelIds,
	                                           @RequestParam(required = false) String imageUrl,
	                                           @RequestParam String name,
	                                           @RequestParam String intro,
	                                           @RequestParam String tag,
	                                           @RequestParam String lang,
	                                           @RequestParam Long categoryId) {
		
		logger.info("channelSetId = " + channelSetId);
		logger.info("channelIds = " + channelIds);
		logger.info("imageUrl = " + imageUrl);
		logger.info("name = " + name);
		logger.info("intro = " + intro);
		logger.info("tag = " + tag);
		logger.info("lang = " + lang);
		logger.info("categoryId = " + categoryId);
		
		CmsApiService cmsApiService = new CmsApiService();
		ChannelSetManager channelSetMngr = new ChannelSetManager();
		ChannelSet channelSet = channelSetMngr.findById(channelSetId);
		CategoryChannelSetManager ccsMngr = new CategoryChannelSetManager();
		
		if (channelSet == null)
			return "Invalid ChannelSetId";
		
		channelSet.setName(name);
		channelSet.setTag(tag);
		channelSet.setLang(lang);
		if (imageUrl != null) {
			channelSet.setImageUrl(imageUrl);
			// TODO: channel set also needs to be processed
		}
		channelSet.setIntro(intro);
		channelSetMngr.save(channelSet);
		
		List<CategoryChannelSet> ccss = cmsApiService.whichCCSContainingTheChannelSet(channelSetId);
		List<CategoryChannelSet> removable = new ArrayList<CategoryChannelSet>();
		
		// NOTE: channel set can only in one system category
		for (CategoryChannelSet ccs : ccss) {
			if (ccs.getCategoryId() != categoryId) {
				removable.add(ccs);
			}
		}
		for (CategoryChannelSet ccs : removable) {
			ccsMngr.delete(ccs);
			ccss.remove(ccs);
		}
		
		logger.info("ccss size = " + ccss.size());
		
		if (ccss.isEmpty()) {
			// create a new CategoryChannelSet
			CategoryChannelSet ccs = new CategoryChannelSet(categoryId, channelSetId);
			ccsMngr.create(ccs);
			// TODO: dealing with channelCount
			logger.info("create new CategoryChannelSet channelSetId = " + channelSetId + ", categoryId = " + categoryId);
		}
		
		if (channelIds != null) {
			ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
			MsoChannelManager channelMngr = new MsoChannelManager();
			List<ChannelSetChannel> cscs = cscMngr.findByChannelSetId(channelSetId);
			for (ChannelSetChannel csc : cscs) {
				cscMngr.delete(csc);
			}
			String[] split = channelIds.split(",");
			for (int i = 0; i < split.length; i++) {
				MsoChannel channel = channelMngr.findById(Long.valueOf(split[i]));
				if (channel == null) {
					logger.warning("channel id does not exist: " + split[i]);
					continue;
				}
				cscMngr.create(new ChannelSetChannel(channelSetId, channel.getKey().getId(), i + 1));
			}
		}
		// piwik
		PiwikLib.createPiwikSite(channelSetId, 0, req);
		return "OK";
	}
	
	@RequestMapping("changeChannelSetChannel")
	public @ResponseBody String changeChannelSetChannel(@RequestParam Long  channelSetId,
	                                    @RequestParam Short from,
	                                    @RequestParam Short to) {
		
		logger.info("channelSetId = " + channelSetId + ", from = " + from + ", to = " + to);
		
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		if (cscMngr.moveSeq(channelSetId, from, to)) {
			return "OK";
		}
		return "Failed";
	}
	
	@RequestMapping("addChannelSetChannel")
	public @ResponseBody String addChannelSetChannel(@RequestParam Long  channelSetId,
	                                 @RequestParam Long  channelId,
	                                 @RequestParam Short seq) {
		
		logger.info("channelSetId = " + channelSetId + ", channelId = " + channelId + ", seq = " + seq);
		
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		
		MsoChannel channel = channelMngr.findById(channelId);
		if (channel == null) {
			return "Invalid channelId";
		}
		channel.setSeq(seq);
		cscMngr.addChannel(channelSetId, channel);
		return "OK";
	}
	
	@RequestMapping("removeChannelSetChannel")
	public @ResponseBody String removeChannelSetChannel(@RequestParam Long  channelSetId,
	                                    @RequestParam Short seq) {
		
		logger.info("channelSetId = " + channelSetId + ", seq = " + seq);
		
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		cscMngr.removeChannel(channelSetId, seq);
		return "OK";
	}
	
	//////////////////// Channel/Program Management ////////////////////
	
	@RequestMapping("getPodcastInfo")
	public @ResponseBody Map<String, String> getPodcastInfo(@RequestParam String url) throws IllegalArgumentException, FeedException, IOException {
		//URL feedUrl = new URL(url);
		Map<String, String> result = new HashMap<String, String>();
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(new URL(url)));
		String title, description, thumbnail;
		title = feed.getTitle();
		description = feed.getDescription();
		thumbnail = (feed.getImage() != null) ? feed.getImage().getUrl() : null;
		if (title != null) {
			result.put("title", title);
			logger.info("title = " + title);
		}
		if (description != null) {
			result.put("description", description);
			logger.info("description = " + description);
		}
		if (thumbnail != null) {
			result.put("thumbnail", thumbnail);
			logger.info("thumbnail = " + thumbnail);
		} else {
			//List<SyndEntry> entries = feed.getEntries();
		}
		return result;
	}
	
	// not in used
	@RequestMapping("getYouTubeVideoInfo")
	public @ResponseBody Map<String, String> getYouTubeVideoInfo(@RequestParam String videoIdStr) {
		return YouTubeLib.getYouTubeVideoEntry(videoIdStr);
	}
	
	/**
	 * List all channels owned by mso
	 */
	@RequestMapping("listOwnedChannels")
	public @ResponseBody List<MsoChannel> listOwnedChannels(@RequestParam Long msoId) {
		
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		List<MsoChannel> results = new ArrayList<MsoChannel>();
		
		logger.info("msoId = " + msoId);
		
		class MsoChannelComparator implements Comparator<MsoChannel> {  // yes, I know, its a little dirty
			@Override
			public int compare(MsoChannel channel1, MsoChannel channel2) {
				Date date1 = channel1.getUpdateDate();
				Date date2 = channel2.getUpdateDate();
				return date2.compareTo(date1);
			}
		}
		
		results = ownershipMngr.findOwnedChannelsByMsoId(msoId);
		Collections.sort(results, new MsoChannelComparator());
		SubscriptionLogManager subLogMngr = new SubscriptionLogManager();
		for (MsoChannel channel : results) {
			channel.setSubscriptionCount(subLogMngr.findTotalCountByChannelId(channel.getKey().getId()));
		}
		return results;
	}
	
	/**
	 * List all channel sets owned by mso
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("listOwnedChannelSets")
	public @ResponseBody List<ChannelSet> listOwnedChannelSets(
			HttpServletResponse response,
			@RequestParam(required=false) Long msoId,
			@RequestParam(required=false) String sortby) {
		
		response.addDateHeader("Expires", System.currentTimeMillis() + 3600000);
		
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		List<ChannelSet> results = new ArrayList<ChannelSet>();
		MsoManager msoMngr = new MsoManager();
		Mso nn = msoMngr.findNNMso();
		String cacheIdString = "System.ChannelSets(sortby=lang)";
		
		if (msoId == null) {
			
			logger.info("system channel sets");
			msoId = nn.getKey().getId();
			
			Cache cache = CacheFactory.get();
			if (sortby != null && cache != null) {
				if (sortby.equalsIgnoreCase("lang")) {
					// get from cache
					results = (List<ChannelSet>)cache.get(cacheIdString);
					if (results != null) {
						logger.info("get from cache");
						return results;
					}
				} else if (sortby.equalsIgnoreCase("reset")) {
					// hack
					logger.info("remove from cache");
					cache.remove(cacheIdString);
				}
			}
		}
		logger.info("msoId = " + msoId);
		results = ownershipMngr.findOwnedChannelSetsByMsoId(msoId);
		for (ChannelSet channelSet : results) {
			CmsApiService cmsService = new CmsApiService();
			AreaOwnershipManager areaMngr = new AreaOwnershipManager();
			channelSet.setSubscriptionCount(areaMngr.findTotalCountBySetId(channelSet.getKey().getId()));
			List<Category> sysCategories = cmsService.whichSystemCategoriesContainingTheChannelSet(channelSet.getKey().getId());
			if (sysCategories.size() > 0) {
				channelSet.setLang(sysCategories.get(0).getLang());
			}
		}
		class ChannelSetComparator implements Comparator<ChannelSet> {  // yes, I know, its a little dirty
			@Override
			public int compare(ChannelSet set1, ChannelSet set2) {
				String lang1 = set1.getLang();
				String lang2 = set2.getLang();
				if (lang1.equalsIgnoreCase(lang2))
					return 0;
				if (lang1.equalsIgnoreCase("en"))
					return -1;
				else
					return 1;
			}
		}
		if (sortby != null && sortby.equalsIgnoreCase("lang")) {
			Collections.sort(results, new ChannelSetComparator());
			if (msoId == nn.getKey().getId()) {
				// put to cache
				Cache cache = CacheFactory.get();
				if (cache != null) {
					logger.info("put to cache");
					cache.put(cacheIdString, results);
				}
			}
		}
		return results;
	}
	
	@RequestMapping("switchProgramPublicity")
	public @ResponseBody Boolean switchProgramPublicity(@RequestParam Long programId) {
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoProgram program = programMngr.findById(programId);
		if (program.isPublic())
			program.setPublic(false);
		else
			program.setPublic(true);
		programMngr.save(program);
		return program.isPublic();
	}
	
	@RequestMapping("switchChannelPublicity")
	public @ResponseBody Boolean switchChannelPublicity(@RequestParam Long channelId) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findById(channelId);
		if (channel.isPublic())
			channel.setPublic(false);
		else
			channel.setPublic(true);
		channelMngr.save(channel);
		return channel.isPublic();
	}
	
	@RequestMapping("removeProgram")
	public @ResponseBody void removeProgram(@RequestParam Long programId) {
		logger.info("programId = " + programId);
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoProgram program = programMngr.findById(programId);
		if (program != null) {
			programMngr.delete(program);
		}
	}
	
	@RequestMapping("removeChannelFromList")
	public @ResponseBody void removeChannelFromList(@RequestParam Long channelId, @RequestParam Long msoId) {
		
		logger.info("msoId = " + msoId + ", channelId = " + channelId);
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		ContentOwnership ownership = ownershipMngr.findByMsoIdAndChannelId(msoId, channelId);
		if (ownership != null) {
			ownershipMngr.delete(ownership);
			logger.info("remove ownership");
		}
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		// remove channels in set
		List<ChannelSet> channelSets = ownershipMngr.findOwnedChannelSetsByMsoId(msoId);
		for (ChannelSet channelSet : channelSets) {
			List<ChannelSetChannel> cscs = cscMngr.findByChannelSetId(channelSet.getKey().getId());
			for (ChannelSetChannel csc : cscs) {
				if (csc.getChannelId() == channelId) {
					cscMngr.removeChannel(channelSet.getKey().getId(), csc.getSeq());
				}
			}
		}
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		CategoryManager catMngr = new CategoryManager();
		// remove channels in directory
		List<Category> categories = catMngr.findAllByMsoIdWithoutCache(msoId);
		for (Category category : categories) {
			CategoryChannel cc = ccMngr.findByCategoryIdAndChannelId(category.getKey().getId(), channelId);
			if (cc != null) {
				QueueFactory.getDefaultQueue().add(
						TaskOptions.Builder.withUrl("/CMSAPI/removeCategoryChannel")
						.param("categoryId", String.valueOf(cc.getCategoryId()))
						.param("channelId", String.valueOf(cc.getChannelId())));
			}
		}
	}
	
	@RequestMapping("programInfo")
	public @ResponseBody MsoProgram programInfo(@RequestParam Long programId) {
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoProgram program = programMngr.findByIdCacheless(programId);
		program.setName(NnStringUtil.revertHtml(program.getName()));
		program.setIntro(NnStringUtil.revertHtml(program.getIntro()));
		program.setComment(NnStringUtil.revertHtml(program.getComment()));
		return program;
	}
	
	@RequestMapping("channelInfo")
	public @ResponseBody MsoChannel channelInfo(@RequestParam Long channelId) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		return channelMngr.findById(channelId);
	}
	
	@RequestMapping("importChannelByUrl")
	public @ResponseBody MsoChannel importChannelByUrl(HttpServletRequest req, @RequestParam String sourceUrl) {
		
		MsoChannelManager channelMngr = new MsoChannelManager();
		NnUserManager userMngr = new NnUserManager();
		
		sourceUrl = sourceUrl.trim();
		logger.info("import " + sourceUrl);
		sourceUrl = channelMngr.verifyUrl(sourceUrl);
		if (sourceUrl == null) {
			logger.warning("invalid source url");
			return null;
		}
		logger.info("normalized " + sourceUrl);
		MsoChannel channel = channelMngr.findBySourceUrlSearch(sourceUrl);
		if (channel == null) {
			logger.info("new source url");
			channel = channelMngr.initChannelSubmittedFromPlayer(sourceUrl, userMngr.findNNUser()); //!!!
			if (channel == null) {
				logger.warning("invalid source url");
				return null;
			}
			channelMngr.create(channel, new ArrayList<Category>());
			if (channel.getKey() != null && channel.getContentType() != MsoChannel.CONTENTTYPE_FACEBOOK) { //!!!
				TranscodingService tranService = new TranscodingService();
				tranService.submitToTranscodingService(channel.getKey().getId(), sourceUrl, req);
				// piwik
				PiwikLib.createPiwikSite(0, channel.getKey().getId(), req);
			}
		} else {
			// piwik
			PiwikLib.createPiwikSite(0, channel.getKey().getId(), req);
		}
		
		return channel;
	}
	
	@RequestMapping("addChannelByUrl")
	public @ResponseBody String addChannelByUrl(HttpServletRequest req,
	                                            @RequestParam String sourceUrl,
	                                            @RequestParam(required = false) String imageUrl,
	                                            @RequestParam String name,
	                                            @RequestParam String intro,
	                                            @RequestParam String tag,
	                                            @RequestParam String langCode,
	                                            @RequestParam Long channelSetId,
	                                            @RequestParam Long msoId) throws NoSuchAlgorithmException {
		
		logger.info("sourceUrl = " + sourceUrl);
		logger.info("imageUrl = " + imageUrl);
		logger.info("name = " + name);
		logger.info("intro = " + intro);
		logger.info("tag = " + tag);
		logger.info("langCode = " + langCode);
		logger.info("categoryId = " + channelSetId);
		logger.info("msoId = " + msoId);
		
		CmsApiService cmsApiService = new CmsApiService();
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findBySourceUrlSearch(sourceUrl);
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		MsoManager msoMngr = new MsoManager();
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		ChannelSetManager csMngr = new ChannelSetManager();
		
		Mso mso = msoMngr.findById(msoId);
		
		if (channel == null)
			return "Invalid Source Url";
		if (mso == null)
			return "Invalid msoId";
		
		channel.setName(name);
		channel.setTags(tag);
		channel.setLangCode(langCode);
		if (imageUrl != null) {
			ContentWorkerService workerService = new ContentWorkerService();
			Long timestamp = System.currentTimeMillis() / 1000L;
			
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			String sudoChannelSource = "http://9x9.tv/channel/" + channel.getKey().getId();
			sha1.update(sudoChannelSource.getBytes());
			String prefix = NnStringUtil.bytesToHex(sha1.digest()) + "_" + timestamp + "_";
			
			logger.info("prefix = " + prefix);
			
			channel.setImageUrl(imageUrl);
			workerService.channelLogoProcess(channel.getKey().getId(), imageUrl, prefix, req);
		}
		channel.setIntro(intro);
		// channel.setStatus(MsoChannel.STATUS_SUCCESS); // default import status
		channelMngr.save(channel);
		
		// submit to transcoding server again
		if (channel.getKey() != null && channel.getContentType() != MsoChannel.CONTENTTYPE_FACEBOOK) { //!!!
			TranscodingService tranService = new TranscodingService();
			tranService.submitToTranscodingService(channel.getKey().getId(), sourceUrl, req);
		}
		
		ChannelSet channelSet = csMngr.findById(channelSetId);
		if (channelSet == null)
			return "Invalid channelSetId";
		
		// NOTE: channel can only in one system Channel Set
		List<ChannelSetChannel> removable = new ArrayList<ChannelSetChannel>();
		List<ChannelSetChannel> sysCSCs = cmsApiService.whichSystemCSCContainingThisChannel(channel.getKey().getId());
		for (ChannelSetChannel csc : sysCSCs) {
			if (csc.getChannelSetId() != channelSetId) {
				removable.add(csc);
			}
		}
		for (ChannelSetChannel csc : removable) {
			sysCSCs.remove(csc);
			cscMngr.delete(csc);
		}
		logger.info("sysCSCs size = " + sysCSCs.size());
		if (sysCSCs.isEmpty()) {
			// create a new ChannelSetChannel
			cscMngr.appendChannel(channelSetId, channel);
			logger.info("create new ChannelSetChannel channelId = " + channel.getKey().getId() + ", channelSetId = " + channelSetId);
			//csMngr.save(channelSet);
		}
		
		// create ownership
		ContentOwnership ownership = ownershipMngr.findByMsoIdAndChannelId(msoId, channel.getKey().getId());
		if (ownership == null)
			ownershipMngr.create(new ContentOwnership(), mso, channel);
		
		return "OK";
	}
	
	@RequestMapping("saveNewProgram")
	public @ResponseBody String saveNewProgram(HttpServletRequest req,
	                                           @RequestParam Long programId,
	                                           @RequestParam Long channelId,
	                                           @RequestParam String sourceUrl,
	                                           @RequestParam(required = false) String imageUrl,
	                                           @RequestParam(required = false) String name,
	                                           @RequestParam(required = false) String comment,
	                                           @RequestParam(required = false) String intro) throws NoSuchAlgorithmException {
		
		logger.info("programId = " + programId);
		logger.info("channelId = " + channelId);
		logger.info("sourceUrl = " + sourceUrl);
		logger.info("imageUrl = " + imageUrl);
		logger.info("name = " + name);
		logger.info("intro = " + intro);
		logger.info("comment = " + comment);
		
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		ContentWorkerService workerService = new ContentWorkerService();
		
		MsoProgram program = programMngr.findById(programId);
		if (program == null) {
			return "Invalid programId";
		}
		MsoChannel channel = channelMngr.findById(channelId);
		if (channel == null) {
			return "Invalid channelId";
		}		
		Long timestamp = System.currentTimeMillis() / 1000L;
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		sha1.update(sourceUrl.getBytes());
		String prefix = NnStringUtil.bytesToHex(sha1.digest()) + "_" + timestamp + "_";
		logger.info("prefix = " + prefix);
		
		program.setOtherFileUrl(sourceUrl);
		// TODO: ProgramManager.getContentTypeByUrl()
		if (sourceUrl.indexOf("youtube.com") == -1) {
			// TODO: check source url is valid
			boolean autoGeneratedLogo = (imageUrl == null) ? true : false;
			workerService.programVideoProcess(programId, sourceUrl, prefix, autoGeneratedLogo, req);
			program.setContentType(MsoProgram.CONTENTTYPE_DIRECTLINK);
			logger.info("direct link");
		} else {
			// TODO: check if youtube url is valid
			program.setContentType(MsoProgram.CONTENTTYPE_YOUTUBE);
			logger.info("youtube link");
		}
		if (imageUrl != null) {
			program.setImageUrl(imageUrl);
			workerService.programLogoProcess(program.getKey().getId(), imageUrl, prefix, req);
		}
		if (name != null) {
			program.setName(NnStringUtil.htmlSafeAndTrucated(name));
		}
		if (intro != null) {
			program.setIntro(NnStringUtil.htmlSafeAndTrucated(intro));
		}
		if (comment != null) {
			program.setComment(NnStringUtil.htmlSafeAndTrucated(comment));
		}
		program.setPublic(true);
		programMngr.create(channel, program);
		
		return "OK";
	}
	
	@RequestMapping("saveProgram")
	public @ResponseBody String saveProgram(HttpServletRequest req,
	                                        @RequestParam Long programId,
	                                        @RequestParam(required = false) String imageUrl,
	                                        @RequestParam(required = false) String name,
	                                        @RequestParam(required = false) String intro,
	                                        @RequestParam(required = false) String comment) throws NoSuchAlgorithmException {
		logger.info("programId = " + programId);
		logger.info("imageUrl = " + imageUrl);
		logger.info("name = " + name);
		logger.info("intro = " + intro);
		logger.info("comment = " + comment);
		
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoProgram program = programMngr.findById(programId);
		if (program == null) {
			return "Invalid programId";
		}
		if (name != null)
			program.setName(NnStringUtil.htmlSafeAndTrucated(name));
		if (intro != null)
			program.setIntro(NnStringUtil.htmlSafeAndTrucated(intro));
		if (comment != null)
			program.setComment(NnStringUtil.htmlSafeAndTrucated(comment));
		if (imageUrl != null) {
			ContentWorkerService workerService = new ContentWorkerService();
			Long timestamp = System.currentTimeMillis() / 1000L;
			
			String sourceUrl;
			if (program.getMpeg4FileUrl() != null)
				sourceUrl = program.getMpeg4FileUrl();
			else if (program.getWebMFileUrl() != null)
				sourceUrl = program.getWebMFileUrl();
			else if (program.getOtherFileUrl() != null)
				sourceUrl = program.getOtherFileUrl();
			else if (program.getAudioFileUrl() != null)
				sourceUrl = program.getAudioFileUrl();
			else
				sourceUrl = "http://9x9.tv/episode/" + program.getKey().getId();
			
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			sha1.update(sourceUrl.getBytes());
			String prefix = NnStringUtil.bytesToHex(sha1.digest()) + "_" + timestamp + "_";
			
			logger.info("prefix = " + prefix);
			
			program.setImageUrl(imageUrl);
			workerService.programLogoProcess(program.getKey().getId(), imageUrl, prefix, req);
		}
		programMngr.save(program);
		
		return "OK";
	}
	
	@RequestMapping("saveChannel")
	public @ResponseBody String saveChannel(HttpServletRequest req,
	                                        @RequestParam Long channelId,
	                                        @RequestParam(required = false) Long   msoId,
	                                        @RequestParam(required = false) String imageUrl,
	                                        @RequestParam(required = false) String name,
	                                        @RequestParam(required = false) String intro,
	                                        @RequestParam(required = false) String tag,
	                                        @RequestParam(required = false) String langCode,
	                                        @RequestParam Long channelSetId) throws NoSuchAlgorithmException {
		
		logger.info("channelId = " + channelId);
		logger.info("imageUrl = " + imageUrl);
		logger.info("name = " + name);
		logger.info("intro = " + intro);
		logger.info("tag = " + tag);
		logger.info("langCode = " + langCode);
		logger.info("categoryId = " + channelSetId);
		logger.info("msoId = " + msoId);
		
		CmsApiService cmsApiService = new CmsApiService();
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findById(channelId);
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		ChannelSetManager csMngr = new ChannelSetManager();
		
		if (channel == null)
			return "Invalid ChannelId";
		
		if (tag != null)
			channel.setTags(tag);
		if (langCode != null)
			channel.setLangCode(langCode);
		if (imageUrl != null) {
			ContentWorkerService workerService = new ContentWorkerService();
			Long timestamp = System.currentTimeMillis() / 1000L;
			
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			String sudoChannelSource = "http://9x9.tv/channel/" + channel.getKey().getId();
			sha1.update(sudoChannelSource.getBytes());
			String prefix = NnStringUtil.bytesToHex(sha1.digest()) + "_" + timestamp + "_";
			
			logger.info("prefix = " + prefix);
			
			channel.setImageUrl(imageUrl);
			workerService.channelLogoProcess(channelId, imageUrl, prefix, req);
		}
		
		if (name != null)
			channel.setName(name);
		if (intro != null)
			channel.setIntro(intro);
		if (msoId != null) { // first time the channel be saved
			channel.setPublic(true);
		}
		channel.setUpdateDate(new Date());
		channelMngr.save(channel);
		
		ChannelSet channelSet = csMngr.findById(channelSetId);
		if (channelSet == null)
			return "Invalid channelSetId";
		
		// NOTE: channel can only in one system Channel Set
		List<ChannelSetChannel> removable = new ArrayList<ChannelSetChannel>();
		List<ChannelSetChannel> sysCSCs = cmsApiService.whichSystemCSCContainingThisChannel(channel.getKey().getId());
		for (ChannelSetChannel csc : sysCSCs) {
			if (csc.getChannelSetId() != channelSetId) {
				removable.add(csc);
			}
		}
		for (ChannelSetChannel csc : removable) {
			sysCSCs.remove(csc);
			cscMngr.delete(csc);
		}
		logger.info("sysCSCs size = " + sysCSCs.size());
		if (sysCSCs.isEmpty()) {
			// create a new ChannelSetChannel
			cscMngr.appendChannel(channelSetId, channel);
			logger.info("create new ChannelSetChannel channelId = " + channel.getKey().getId() + ", channelSetId = " + channelSetId);
			//csMngr.save(channelSet); // update channelCount
		}
		
		//channel1 ownership
		if (msoId != null) {
			MsoManager msoMngr = new MsoManager();
			Mso mso = msoMngr.findById(msoId);
			if (mso != null) {
				ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
				if (ownershipMngr.findByMsoIdAndChannelId(msoId, channelId) == null) {
					logger.info("create ownership");
					ownershipMngr.create(new ContentOwnership(), mso, channel);
				}
			} else {
				logger.warning("invalid msoId");
			}
		}
		// piwik
		PiwikLib.createPiwikSite(0, channelId, req);
		return "OK";
	}
	
	@RequestMapping("channelSystemChannelSet")
	public @ResponseBody ChannelSet channelSystemChannelSet(@RequestParam Long channelId) {
		CmsApiService cmsService = new CmsApiService();
		if (channelId == null)
			return null;
		List<ChannelSet> channelSetList = cmsService.whichSystemChannelSetsContainingThisChannel(channelId);
		if (channelSetList.size() > 0)
			return channelSetList.get(0);
		else
			return null;
	}
	
	@RequestMapping("channelCategory")
	public @ResponseBody Category channelCategory(@RequestParam Long channelId) {
		CmsApiService cmsService = new CmsApiService();
		if (channelId == null)
			return null;
		List<Category> categoryList = cmsService.whichSystemCategoriesContainingTheChannel(channelId);
		if (categoryList.size() > 0)
			return categoryList.get(0);
		else
			return null;
	}
	
	@RequestMapping("createProgramSkeleton")
	public @ResponseBody Long createProgramSkeleton() {
		MsoProgramManager programMngr = new MsoProgramManager();
		
		logger.info("create program skeleton");
		MsoProgram program = new MsoProgram("New Program", "New Program", "/WEB-INF/../images/processing.png", MsoProgram.TYPE_VIDEO);
		program.setPublic(false);
		program.setType(MsoProgram.TYPE_VIDEO);
		programMngr.create(program);
		
		return program.getKey().getId();
	}
	
	@RequestMapping("createChannelSkeleton")
	public @ResponseBody Long createChannelSkeleton() {
		
		NnUserManager userMngr = new NnUserManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		
		MsoChannel channel = new MsoChannel("New Channel", "New Channel", "/WEB-INF/../images/processing.png", userMngr.findNNUser().getKey().getId());
		channel.setPublic(false);
		channel.setStatus(MsoChannel.STATUS_WAIT_FOR_APPROVAL);
		channel.setContentType(MsoChannel.CONTENTTYPE_MIXED); // a channel type in podcast does not allow user to add program in it, so change to mixed type
		channelMngr.create(channel, new ArrayList<Category>());
		
		return channel.getKey().getId();
	}
	
	@RequestMapping("programList")
	public @ResponseBody List<MsoProgram> programList(Long channelId) {
		MsoProgramManager programMngr = new MsoProgramManager();
		return programMngr.findAllByChannelId(channelId);
	}
	
	//////////////////// Directory Management ////////////////////
	
	@RequestMapping("listCategoryChannels")
	public @ResponseBody List<MsoChannel> listCategoryChannels(@RequestParam Long categoryId) {
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		
		List<CategoryChannel> ccs = ccMngr.findAllByCategoryId(categoryId);
		List<Long> channelIdList = new ArrayList<Long>();
		for (CategoryChannel cc : ccs) {
			channelIdList.add(cc.getChannelId());
		}
		return channelMngr.findAllByChannelIds(channelIdList);
	}
	
	@RequestMapping("listCategoryChannelSets")
	public @ResponseBody List<ChannelSet> listCategoryChannelSets(@RequestParam Long categoryId, @RequestParam(required=false) Boolean isPublic) {
		CategoryChannelSetManager cscMngr = new CategoryChannelSetManager();
		ChannelSetManager channelSetMngr = new ChannelSetManager();
		
		List<CategoryChannelSet> ccss = cscMngr.findAllByCategoryId(categoryId);
		List<Long> channelSetIdList = new ArrayList<Long>();
		for (CategoryChannelSet ccs : ccss) {
			channelSetIdList.add(ccs.getChannelSetId());
		}
		if (isPublic != null && isPublic) {
			return channelSetMngr.findAllPublicByChannelSetIds(channelSetIdList);
		} else {
			return channelSetMngr.findAllByChannelSetIds(channelSetIdList);
		}
	}
	
	/**
	 * List all mso categories
	 */
	@RequestMapping("listCategories")
	public @ResponseBody List<Category> listCategories(@RequestParam Long msoId) {
		CategoryManager catMngr = new CategoryManager();
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findById(msoId);
		if (mso == null)
			return new ArrayList<Category>();
		return catMngr.findAllByMsoIdWithoutCache(mso.getKey().getId());  // accuracy
	}
	
	/**
	 * List all system categories (mso in TYPE_NN)
	 */
	@RequestMapping("systemCategories")
	public @ResponseBody List<Category> systemCategories(@RequestParam(required=false) Long parentId,
	                                                      @RequestParam(required=false) String lang,
	                                                     HttpServletRequest request,
	                                                     HttpServletResponse response) {
		response.addDateHeader("Expires", System.currentTimeMillis() + 3600000);
		CategoryManager catMngr = new CategoryManager();
		MsoManager msoMngr = new MsoManager();
		Mso nnmso = msoMngr.findNNMso();
		List<Category> categories = catMngr.findAllByMsoId(nnmso.getKey().getId());
		List<Category> results = new ArrayList<Category>();
		long parentIdValue;
		if (parentId == null) {
			parentIdValue = 0;
		} else {
			parentIdValue = parentId.longValue();
		}
		for (Category category : categories) {
			if (category.getParentId() == parentIdValue) {
				if (lang == null)
					results.add(category);
				else if (lang != null && lang.equalsIgnoreCase(category.getLang())) {
					results.add(category);
				}
			}
		}
		class CategoryComparator implements Comparator<Category> {
			@Override
			public int compare(Category category1, Category category2) {
				int seq1 = category1.getSeq();
				if (category1.getLang() != null && category1.getLang().equalsIgnoreCase("en")) {
					seq1 -= 100;
				}
				int seq2 = category2.getSeq();
				if (category2.getLang() != null && category2.getLang().equalsIgnoreCase("en")) {
					seq2 -= 100;
				}
				return (seq1 - seq2);
			}
		}
		Collections.sort(results, new CategoryComparator());
		return results;
	}
	
	@RequestMapping("renameCategory")
	public @ResponseBody String renameCategory(@RequestParam Long categoryId,
	                                           @RequestParam String name) {
		logger.info("categoryId = " + categoryId);
		logger.info("name = " + name);
		
		CategoryManager catMngr = new CategoryManager();
		Category category = catMngr.findById(categoryId);
		if (category == null)
			return "Invalid categoryId";
		category.setName(name);
		catMngr.save(category);
		return "OK";
	}
	
	@RequestMapping("createCategory")
	public @ResponseBody Long createCategory(@RequestParam Long msoId,
	                                         @RequestParam Long parentId,
	                                         @RequestParam String name) {
		logger.info("msoId = " + msoId);
		logger.info("parentId = " + parentId);
		logger.info("name = " + name);
		
		CategoryManager catMngr = new CategoryManager();
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findById(msoId);
		if (mso == null)
			return null;
		Category category = new Category(name, true, msoId);
		category.setParentId(parentId);
		catMngr.create(category);
		logger.info("newCategoryId = " + category.getKey().getId());
		if (category.getKey() == null)
			return null;
		return category.getKey().getId();
	}
	
	@RequestMapping("createCategoryChannelSet")
	public @ResponseBody String createCategoryChannelSet(@RequestParam Long categoryId,
	                                                     @RequestParam Long channelSetId) {
		logger.info("categoryId = " + categoryId);
		logger.info("channelSetId = " + channelSetId);
		
		CategoryChannelSetManager ccsMngr = new CategoryChannelSetManager();
		CategoryManager catMngr = new CategoryManager();
		ChannelSetManager setMngr = new ChannelSetManager();
		
		Category category = catMngr.findById(categoryId);
		if (category == null)
			return "Invalid categoryId";
		ChannelSet channelSet = setMngr.findById(channelSetId);
		if (channelSet == null)
			return "Invalid channelSetId";
		
		CategoryChannelSet found = ccsMngr.findByCategoryIdAndChannelSetId(categoryId, channelSetId);
		if (found == null) {
			ccsMngr.create(new CategoryChannelSet(categoryId, channelSetId));
			// TODO: dealing with channelCount
			return "OK";
		}
		return "CategoryChannelSet Exists";
	}
	
	@RequestMapping("createCategoryChannel")
	public @ResponseBody String createCategoryChannel(@RequestParam Long categoryId,
	                                                  @RequestParam Long channelId) {
		logger.info("categoryId = " + categoryId);
		logger.info("channelId = " + channelId);
		
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		CategoryManager catMngr = new CategoryManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		
		Category category = catMngr.findById(categoryId);
		if (category == null)
			return "Invalid categoryId";
		MsoChannel channel = channelMngr.findById(channelId);
		if(channel == null)
			return "Invalid channelId";
		
		CategoryChannel found = ccMngr.findByCategoryIdAndChannelId(categoryId, channelId);
		if (found == null) {
			ccMngr.create(new CategoryChannel(categoryId, channelId));
			// TODO: dealing with channelCount
			return "OK";
		}
		return "CategoryChannel Exists";
	}
	
	@RequestMapping("removeCategoryChannel")
	public @ResponseBody String removeCategoryChannel(@RequestParam Long categoryId,
	                                                  @RequestParam Long channelId) {
		logger.info("categoryId = " + categoryId);
		logger.info("channelId = " + channelId);
		
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		CategoryChannel found = ccMngr.findByCategoryIdAndChannelId(categoryId, channelId);
		if (found == null)
			return "Not Found";
		ccMngr.delete(found);
		// TODO: dealing with channelCount
		return "OK";
	}
	
	@RequestMapping("removeCategoryChannelSet")
	public @ResponseBody String removeCategoryChannelSet(@RequestParam Long categoryId,
	                                                     @RequestParam Long channelSetId) {
		logger.info("categoryId = " + categoryId);
		logger.info("channelSetId = " + channelSetId);
		
		CategoryChannelSetManager ccsMngr = new CategoryChannelSetManager();
		CategoryChannelSet found = ccsMngr.findByCategoryIdAndChannelSetId(categoryId, channelSetId);
		if (found == null)
			return "Not Found";
		ccsMngr.delete(found);
		// TODO: dealing with channelCount
		return "OK";
	}
	
	@RequestMapping("removeCategory")
	public @ResponseBody String removeCategory(@RequestParam Long categoryId) {
		
		CategoryManager catMngr = new CategoryManager();
		CategoryChannelSetManager ccsMngr = new CategoryChannelSetManager();
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		
		logger.info("categoryId = " + categoryId);
		
		Category category = catMngr.findById(categoryId);
		if (category == null)
			return "Invalid categoryId";
		if (category.getParentId() == 0)
			return "Can Not Remove Root";
		
		// remove channel sets
		List<CategoryChannelSet> ccss = ccsMngr.findAllByCategoryId(categoryId);
		for (CategoryChannelSet ccs : ccss) {
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/CMSAPI/removeCategoryChannelSet")
					.param("categoryId", String.valueOf(ccs.getCategoryId()))
					.param("channelSetId", String.valueOf(ccs.getChannelSetId())));
		}
		// remove channels
		List<CategoryChannel> ccs = ccMngr.findAllByCategoryId(categoryId);
		for (CategoryChannel cc : ccs) {
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/CMSAPI/removeCategoryChannel")
					.param("categoryId", String.valueOf(cc.getCategoryId()))
					.param("channelId", String.valueOf(cc.getChannelId())));
		}
		// remove sub-categories
		List<Category> subCategories = catMngr.findAllByParentId(categoryId);
		for (Category sub : subCategories) {
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/CMSAPI/removeCategory")
					.param("categoryId", String.valueOf(sub.getKey().getId())));
		}
		
		// dealing with category cache
		catMngr.deleteCache(category.getMsoId());
		
		category.setMsoId(0);
		category.setParentId(0);
		catMngr.save(category);
		return "OK";
	}
	
	@RequestMapping("moveCategory")
	public @ResponseBody String moveCategory(@RequestParam Long toCategoryId,
	                                         @RequestParam Long fromCategoryId,
	                                         @RequestParam Long categoryId) {
		logger.info("toCategoryId = " + toCategoryId);
		logger.info("fromCategoryId = " + fromCategoryId);
		logger.info("categoryId = " + categoryId);
		
		CategoryManager catMngr = new CategoryManager();
		Category category = catMngr.findById(categoryId);
		if (category == null)
			return "Category Not Found";
		Category parent = catMngr.findById(toCategoryId);
		if (parent == null)
			return "Parent Not Found";
		if (category.getParentId() != fromCategoryId)
			return "Parent Not Matched";
		category.setParentId(toCategoryId);
		catMngr.save(category);
		return "OK";
	}
	
	@RequestMapping("moveCategoryChannelSet")
	public @ResponseBody String moveCategoryChannelSet(@RequestParam Long toCategoryId,
	                                                   @RequestParam Long fromCategoryId,
	                                                   @RequestParam Long channelSetId) {
		logger.info("toCategoryId = " + toCategoryId);
		logger.info("fromCategoryId = " + fromCategoryId);
		logger.info("channelSetId = " + channelSetId);
		
		// TODO: check fromCategoryId
		// TODO: check toCategoryId
		CategoryChannelSetManager ccsMngr = new CategoryChannelSetManager();
		CategoryChannelSet ccs = ccsMngr.findByCategoryIdAndChannelSetId(fromCategoryId, channelSetId);
		if (ccs == null)
			return "Not Found";
		ccs.setCategoryId(toCategoryId);
		ccsMngr.save(ccs);
		// TODO: dealing with channelCount
		return "OK";
	}
	
	@RequestMapping("moveCategoryChannel")
	public @ResponseBody String moveCategoryChannel(@RequestParam Long toCategoryId,
	                                                @RequestParam Long fromCategoryId,
	                                                @RequestParam Long channelId) {
		logger.info("toCategoryId = " + toCategoryId);
		logger.info("fromCategoryId = " + fromCategoryId);
		logger.info("channelId = " + channelId);
		
		// TODO: check fromCategoryId
		// TODO: check toCategoryId
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		CategoryChannel cc = ccMngr.findByCategoryIdAndChannelId(fromCategoryId, channelId);
		if (cc == null)
			return "Not Found";
		cc.setCategoryId(toCategoryId);
		ccMngr.save(cc);
		// TODO: dealing with channelCount
		return "OK";
	}
	
	//////////////////// Promotion Tools ////////////////////
	
	@RequestMapping("setSnsAuth")
	public @ResponseBody String setSnsAuth(@RequestParam Long msoId,
	                                        @RequestParam Short type,
	                                        @RequestParam Boolean enabled) {
		SnsAuthManager snsMngr = new SnsAuthManager();
		SnsAuth snsAuth = snsMngr.findMsoIdAndType(msoId, type);
		if (snsAuth != null) {
			snsAuth.setEnabled(enabled);
			snsMngr.save(snsAuth);
			return "OK";
		} else {
			return "NotFound";
		}
	}
	@RequestMapping("removeSnsAuth")
	public @ResponseBody String removeSnsAuth(@RequestParam Long msoId,
	                                           @RequestParam Short type) {
		SnsAuthManager snsMngr = new SnsAuthManager();
		AutosharingService shareService = new AutosharingService();
		
		SnsAuth snsAuth = snsMngr.findMsoIdAndType(msoId, type);
		if (snsAuth != null) {
			snsMngr.delete(snsAuth);
		}
		List<ChannelAutosharing> autoshareList = shareService.findAllChannelsByMsoIdAndType(msoId, type);
		for (ChannelAutosharing autoshare : autoshareList) {
			shareService.delete(autoshare);
		}
		return "OK";
	}
	
	@RequestMapping("createSnsAuth")
	public @ResponseBody String createSnsAuth(@RequestParam Long msoId,
	                                           @RequestParam Short type,
	                                           @RequestParam String token,
	                                           @RequestParam(required=false) String secrete) {
		
		SnsAuthManager snsMngr = new SnsAuthManager();
		SnsAuth snsAuth = snsMngr.findMsoIdAndType(msoId, type);
		if (snsAuth == null) {
			snsAuth = new SnsAuth(msoId, type, token);
			if (secrete != null)
				snsAuth.setSecrete(secrete);
			snsAuth.setEnabled(true);
			snsMngr.create(snsAuth);
		} else {
			snsAuth.setToken(token);
			if (secrete != null)
				snsAuth.setSecrete(secrete);
			snsAuth.setEnabled(true);
			snsMngr.save(snsAuth);
		}
		return "OK";
	}
	
	@RequestMapping("listSnsAuth")
	public @ResponseBody List<SnsAuth> listSnsAuth(@RequestParam Long msoId) {
		logger.info("msoId = " + msoId);
		SnsAuthManager snsMngr = new SnsAuthManager();
		List<SnsAuth> list = snsMngr.findAllByMsoId(msoId);
		for (SnsAuth sns : list) {
			if (sns.getType() == SnsAuth.TYPE_FACEBOOK) {
				FacebookLib.populatePageList(sns);
			}
		}
		return list;
	}
	
	@RequestMapping("listChannelAutosharing")
	public @ResponseBody List<ChannelAutosharing> listChannelAutosharing(@RequestParam Long msoId, @RequestParam Long channelId) {
		logger.info("msoId = " + msoId);
		logger.info("channelId = " + channelId);
		AutosharingService shareService = new AutosharingService();
		return shareService.findAllByChannelIdAndMsoId(channelId, msoId);
	}
	
	@RequestMapping("listChannelSetAutosharing")
	public @ResponseBody List<ChannelSetAutosharing> listChannelSetAutosharing(@RequestParam Long msoId, @RequestParam Long channelSetId) {
		logger.info("msoId = " + msoId);
		logger.info("channelSetId = " + channelSetId);
		AutosharingService shareService = new AutosharingService();
		return shareService.findAllByChannelSetIdAndMsoId(channelSetId, msoId);
	}
	
	@RequestMapping("createChannelAutosharing")
	public @ResponseBody String createChannelAutosharing(@RequestParam Long msoId,
	                                                   @RequestParam Long channelId,
	                                                   @RequestParam(required=false) String parameter,
	                                                   @RequestParam(required=false) String target,
	                                                   @RequestParam Short type) {
		logger.info("msoId = " + msoId);
		logger.info("channelId = " + channelId);
		logger.info("type = " + type);
		logger.info("parameter = " + parameter);
		logger.info("target = " + target);
		
		AutosharingService shareService = new AutosharingService();
		ChannelAutosharing autosharing = shareService.findChannelAutosharing(msoId, channelId, type);
		if (autosharing == null) {
			autosharing = new ChannelAutosharing(msoId, channelId, type);
			if (parameter != null) {
				autosharing.setParameter(parameter);
			}
			if (target != null) {
				autosharing.setTarget(target);
			}
			shareService.create(autosharing);
		} else {
			autosharing.setParameter(parameter);
			autosharing.setTarget(target);
			shareService.save(autosharing);
		}
		return "OK";
	}
	
	@RequestMapping("createChannelSetAutosharing")
	public @ResponseBody void createChannelSetAutosharing(@RequestParam Long msoId,
	                                                      @RequestParam Long channelSetId,
	                                                      @RequestParam Short type) {
		logger.info("msoId == " + msoId);
		logger.info("channelSetId = " + channelSetId);
		logger.info("type = " + type);
		
		AutosharingService shareService = new AutosharingService();
		if (shareService.findChannelSetAutosharing(msoId, channelSetId, type) == null) {
			shareService.create(new ChannelSetAutosharing(msoId, channelSetId, type));
		}
	}
	
	@RequestMapping("removeChannelAutosharing")
	public @ResponseBody String removeChannelAutosharing(@RequestParam Long msoId,
	                                                   @RequestParam Long channelId,
	                                                   @RequestParam Short type) {
		logger.info("msoId = " + msoId);
		logger.info("channelId = " + channelId);
		logger.info("type = " + type);
		
		AutosharingService shareService = new AutosharingService();
		ChannelAutosharing autosharing = shareService.findChannelAutosharing(msoId, channelId, type);
		if (autosharing != null) {
			shareService.delete(autosharing);
		}
		return "OK";
	}
	
	@RequestMapping("removeChannelSetAutosharing")
	public @ResponseBody void removeChannelSetAutosharing(@RequestParam Long msoId,
	                                                      @RequestParam Long channelSetId,
	                                                      @RequestParam Short type) {
		logger.info("msoId = " + msoId);
		logger.info("channelSetId = " + channelSetId);
		logger.info("type = " + type);
		
		AutosharingService shareService = new AutosharingService();
		ChannelSetAutosharing autosharing = shareService.findChannelSetAutosharing(msoId, channelSetId, type);
		if (autosharing != null) {
			shareService.delete(autosharing);
		}
	}
	
	@RequestMapping("postToFacebook")
	public @ResponseBody void postToFacebook(@RequestBody FBPost fbPost, HttpServletRequest req) {
		try {
			fbPost.setLink(fbPost.getLink().replaceFirst("localhost", req.getServerName()));
			logger.info(fbPost.toString());
			FacebookLib.postToFacebook(fbPost);
		} catch (IOException e) {
			exception(e);
		}
	}
	
	@RequestMapping("postToTwitter")
	public @ResponseBody void postToTwitter(@RequestBody FBPost fbPost, HttpServletRequest req) {
		try {
			fbPost.setLink(fbPost.getLink().replaceFirst("localhost", req.getServerName()));
			logger.info(fbPost.toString());
			FacebookLib.postToTwitter(fbPost);
		} catch (IOException e) {
			exception(e);
		} catch (TwitterException e) {
			logger.info("post to twitter operation terminated : "+e.getErrorMessage());
		}
	}
	
	//////////////////// statistics ////////////////////
	
	@RequestMapping("channelStatisticsInfo")
	public @ResponseBody Map<String, Integer> channelStatisticsInfo(@RequestParam Long channelId) {
		logger.info("channelId = " + channelId);
		SubscriptionLogManager subLogMngr = new SubscriptionLogManager();
		Map<String, Integer> result = new HashMap<String, Integer>();
		result.put("subscriptionCount", subLogMngr.findTotalCountByChannelId(channelId));
		return result;
	}
	
	@RequestMapping("channelSetStatisticsInfo")
	public @ResponseBody Map<String, Integer> channelSetStatisticsInfo(@RequestParam Long channelSetId) {
		logger.info("channelSetId = " + channelSetId);
		AreaOwnershipManager areaMngr = new AreaOwnershipManager();
		Map<String, Integer> result = new HashMap<String, Integer>();
		result.put("subscriptionCount", areaMngr.findTotalCountBySetId(channelSetId));
		return result;
	}
	
	@RequestMapping("programStatisticsInfo")
	public @ResponseBody Map<String, Integer> programStatisticsInfo(@RequestParam Long programId) {
		logger.info("programId = " + programId);
		IpgManager ipgMngr = new IpgManager();
		Map<String, Integer> result = new HashMap<String, Integer>();
		List<Ipg> ipgs = ipgMngr.findByProgramId(programId);
		result.put("shareCount", ipgs.size());
		return result;
	}
	
	//////////////////// others ////////////////////
	
	@RequestMapping("changePassword")
	public @ResponseBody String changePassword(@RequestParam Long msoId, @RequestParam String newPassword) {
		
		MsoManager msoMngr = new MsoManager();
		NnUserManager userMngr = new NnUserManager();
		Mso mso = msoMngr.findById(msoId);
		
		NnUser user = null;
		switch (mso.getType()) {
		case Mso.TYPE_NN:
		case Mso.TYPE_3X3:
		case Mso.TYPE_ENTERPRISE:
		case Mso.TYPE_MSO:
		case Mso.TYPE_TCO:
			user = userMngr.findMsoUser(mso);
		default:
		}
		if (user == null)
			return "Invalid msoType";
		user.setPassword(newPassword);
		userMngr.resetPassword(user);
		
		return "OK";
	}
}

