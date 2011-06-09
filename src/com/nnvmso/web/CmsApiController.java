package com.nnvmso.web;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.CategoryChannelSet;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ContentOwnership;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.CategoryChannelManager;
import com.nnvmso.service.CategoryChannelSetManager;
import com.nnvmso.service.CategoryManager;
import com.nnvmso.service.ChannelSetChannelManager;
import com.nnvmso.service.ChannelSetManager;
import com.nnvmso.service.CmsApiService;
import com.nnvmso.service.ContentOwnershipManager;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.TranscodingService;

@Controller
@RequestMapping("CMSAPI")
public class CmsApiController {
	protected static final Logger logger = Logger.getLogger(CmsApiController.class.getName());
	
	//private final CmsApiService cmsApiService = new CmsApiService();
	//private static MessageSource messageSource = new ClassPathXmlApplicationContext("locale.xml");
	//private Locale locale = Locale.TRADITIONAL_CHINESE; // NOTE hard-coded
	
	//private void prepService(HttpServletRequest req) {
	//}
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/blank";
	}
	
	/**
	 * List all channels owned by mso
	 */
	@RequestMapping("listOwnedChannels")
	public @ResponseBody List<MsoChannel> listOwnedChannels(@RequestParam Long msoId) {
		
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		
		logger.info("msoId = " + msoId);
		
		return ownershipMngr.findOwnedChannelsByMsoId(msoId);
	}
	
	/**
	 * List all channel in mso default channel set
	 */
	@RequestMapping("defaultChannelSetChannels")
	public @ResponseBody List<MsoChannel> defaultChannelSetChannels(@RequestParam Long msoId) {
		CmsApiService cmsService = new CmsApiService();
		ChannelSet channelSet = cmsService.getDefaultChannelSet(msoId);
		if (channelSet == null)
			return new ArrayList<MsoChannel>();
		return cmsService.findChannelsByChannelSetId(channelSet.getKey().getId());
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
	 * List all system categories (mso in TYPE_NN)
	 */
	@RequestMapping("systemCategories")
	public @ResponseBody List<Category> systemCategories() {
		CategoryManager catMngr = new CategoryManager();
		MsoManager msoMngr = new MsoManager();
		Mso nnmso = msoMngr.findNNMso();
		return catMngr.findAllByMsoId(nnmso.getKey().getId());
	}
	
	@RequestMapping("saveChannelSet")
	public @ResponseBody String saveChannelSet(@RequestParam Long channelSetId,
	                                           @RequestParam String imageUrl,
	                                           @RequestParam String name,
	                                           @RequestParam String intro,
	                                           @RequestParam String tag,
	                                           @RequestParam Long categoryId) {
		
		logger.info("channelSetId = " + channelSetId);
		logger.info("imageUrl = " + imageUrl);
		logger.info("name = " + name);
		logger.info("intro = " + intro);
		logger.info("tag = " + tag);
		logger.info("categoryId = " + categoryId);
		
		CmsApiService cmsApiService = new CmsApiService();
		ChannelSetManager channelSetMngr = new ChannelSetManager();
		ChannelSet channelSet = channelSetMngr.findById(channelSetId);
		CategoryChannelSetManager ccsMngr = new CategoryChannelSetManager();
		
		if (channelSet == null)
			return "Invalid ChannelSetId";
		
		channelSet.setName(name);
		channelSet.setTag(tag);
		channelSet.setImageUrl(imageUrl);
		channelSet.setIntro(intro);
		channelSetMngr.save(channelSet);
		
		List<CategoryChannelSet> ccss = cmsApiService.whichCCSContainingTheChannelSet(channelSetId);
		
		// NOTE: channel set can only in one system category
		for (CategoryChannelSet ccs : ccss) {
			if (ccs.getCategoryId() != categoryId) {
				ccsMngr.delete(ccs);
				ccss.remove(ccs);
			}
		}
		
		logger.info("ccss size = " + ccss.size());
		
		if (ccss.isEmpty()) {
			// create a new CategoryChannelSet
			CategoryChannelSet ccs = new CategoryChannelSet(channelSetId, categoryId);
			ccsMngr.create(ccs);
			logger.info("create new CategoryChannelSet channelSetId = " + channelSetId + ", categoryId = " + categoryId);
		}
		
		return "OK";
	}
	
	@RequestMapping("changeChannelSetChannel")
	public void changeChannelSetChannel(@RequestParam Long  channelSetId,
	                                    @RequestParam Short from,
	                                    @RequestParam Short to) {
		
		logger.info("channelSetId = " + channelSetId + ", from = " + from + ", to = " + to);
		
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		cscMngr.moveSeq(channelSetId, from, to);
	}
	
	@RequestMapping("addChannelSetChannel")
	public void addChannelSetChannel(@RequestParam Long  channelSetId,
	                                 @RequestParam Long  channelId,
	                                 @RequestParam Short seq) {
		
		logger.info("channelSetId = " + channelSetId + ", channelId = " + channelId + ", seq = " + seq);
		
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		
		MsoChannel channel = channelMngr.findById(channelId);
		if (channel == null) {
			logger.warning("Invalid channelId");
			return;
		}
		channel.setSeq(seq);
		cscMngr.addChannel(channelSetId, channel);
		
	}
	
	@RequestMapping("removeChannelSetChannel")
	public void removeChannelSetChannel(@RequestParam Long  channelSetId,
	                                    @RequestParam Short seq) {
		
		logger.info("channelSetId = " + channelSetId + ", seq = " + seq);
		
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		cscMngr.removeChannel(channelSetId, seq);
		
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
	
	@RequestMapping("removeChannelFromList")
	public void removeChannelFromList(@RequestParam Long channelId, @RequestParam Long msoId) {
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		ContentOwnership ownership = ownershipMngr.findByMsoIdAndChannelId(msoId, channelId);
		if (ownership != null)
			ownershipMngr.delete(ownership);
	}
	
	@RequestMapping("channelInfo")
	public MsoChannel channelInfo(@RequestParam Long channelId) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		return channelMngr.findById(channelId);
	}
	
	@RequestMapping("importChannelByUrl")
	public @ResponseBody String importChannelByUrl(HttpServletRequest req, @RequestParam String sourceUrl) {
		
		MsoChannelManager channelMngr = new MsoChannelManager();
		NnUserManager userMngr = new NnUserManager();
		
		sourceUrl = sourceUrl.trim();
		logger.info("import " + sourceUrl);
		MsoChannel channel = channelMngr.findBySourceUrlSearch(sourceUrl);
		if (channel == null) {
			sourceUrl = channelMngr.verifyUrl(sourceUrl);
			if (sourceUrl == null)
				return null;
			logger.info("new source url");
			channel = channelMngr.initChannelSubmittedFromPlayer(sourceUrl, userMngr.findNNUser()); //!!!
			channelMngr.create(channel, new ArrayList<Category>());
			if (channel.getKey() != null && channel.getContentType() != MsoChannel.CONTENTTYPE_FACEBOOK) { //!!!
				TranscodingService tranService = new TranscodingService();
				tranService.submitToTranscodingService(channel.getKey().getId(), sourceUrl, req);
			}
		}
		
		return sourceUrl;
	}
	
	@RequestMapping("addChannelByUrl")
	public @ResponseBody String addChannelByUrl(@RequestParam String sourceUrl,
	                                            @RequestParam String imageUrl,
	                                            @RequestParam String name,
	                                            @RequestParam String intro,
	                                            @RequestParam String tag,
	                                            @RequestParam Long categoryId,
	                                            @RequestParam Long msoId) {
		
		logger.info("sourceUrl = " + sourceUrl);
		logger.info("imageUrl = " + imageUrl);
		logger.info("name = " + name);
		logger.info("intro = " + intro);
		logger.info("tag = " + tag);
		logger.info("categoryId = " + categoryId);
		logger.info("msoId = " + msoId);
		
		CmsApiService cmsApiService = new CmsApiService();
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findBySourceUrlSearch(sourceUrl);
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findById(msoId);
		
		if (channel == null)
			return "Invalid Source Url";
		if (mso == null)
			return "Invalid msoId";
		
		channel.setName(name);
		//channel.setTag(tag); MsoChannel needs a tag property
		channel.setImageUrl(imageUrl);
		channel.setIntro(intro);
		channelMngr.save(channel);
		
		List<CategoryChannel> ccs = cmsApiService.whichCCContainingTheChannel(channel.getKey().getId());
		
		// NOTE: channel can only in one system category
		for (CategoryChannel cc : ccs) {
			if (cc.getCategoryId() != categoryId) {
				ccMngr.delete(cc);
				ccs.remove(cc);
			}
		}
		
		logger.info("ccs size = " + ccs.size());
		
		if (ccs.isEmpty()) {
			// create a new CategoryChannelSet
			CategoryChannel cc = new CategoryChannel(categoryId, channel.getKey().getId());
			ccMngr.create(cc);
			logger.info("create new CategoryChannel channelId = " + channel.getKey().getId() + ", categoryId = " + categoryId);
		}
		
		// create ownership
		ContentOwnership ownership = ownershipMngr.findByMsoIdAndChannelId(msoId, channel.getKey().getId());
		if (ownership == null)
			ownershipMngr.create(new ContentOwnership(), mso, channel);
		
		return "OK";
	}
	
	@RequestMapping("saveChannel")
	public @ResponseBody String saveChannel(@RequestParam Long channelId,
	                                        @RequestParam String imageUrl,
	                                        @RequestParam String name,
	                                        @RequestParam String intro,
	                                        @RequestParam String tag,
	                                        @RequestParam Long categoryId) {
		
		logger.info("channelId = " + channelId);
		logger.info("imageUrl = " + imageUrl);
		logger.info("name = " + name);
		logger.info("intro = " + intro);
		logger.info("tag = " + tag);
		logger.info("categoryId = " + categoryId);
		
		CmsApiService cmsApiService = new CmsApiService();
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findById(channelId);
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		
		if (channel == null)
			return "Invalid ChannelId";
		
		channel.setName(name);
		//channel.setTag(tag); MsoChannel needs a tag property
		channel.setImageUrl(imageUrl);
		channel.setIntro(intro);
		channelMngr.save(channel);
		
		List<CategoryChannel> ccs = cmsApiService.whichCCContainingTheChannel(channelId);
		
		// NOTE: channel can only in one system category
		for (CategoryChannel cc : ccs) {
			if (cc.getCategoryId() != categoryId) {
				ccMngr.delete(cc);
				ccs.remove(cc);
			}
		}
		
		logger.info("ccs size = " + ccs.size());
		
		if (ccs.isEmpty()) {
			// create a new CategoryChannelSet
			CategoryChannel cc = new CategoryChannel(categoryId, channelId);
			ccMngr.create(cc);
			logger.info("create new CategoryChannel channelId = " + channelId + ", categoryId = " + categoryId);
		}
		
		return "OK";
	}

}
