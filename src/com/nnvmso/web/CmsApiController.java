package com.nnvmso.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.CategoryChannelManager;
import com.nnvmso.service.CategoryChannelSetManager;
import com.nnvmso.service.CategoryManager;
import com.nnvmso.service.ChannelSetChannelManager;
import com.nnvmso.service.ChannelSetManager;
import com.nnvmso.service.CmsApiService;
import com.nnvmso.service.ContentOwnershipManager;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.MsoProgramManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.TranscodingService;

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
	public @ResponseBody List<MsoChannel> defaultChannelSetChannels(@RequestParam Long msoId) {
		CmsApiService cmsService = new CmsApiService();
		ChannelSet channelSet = cmsService.getDefaultChannelSet(msoId);
		if (channelSet == null)
			return new ArrayList<MsoChannel>();
		return cmsService.findChannelsByChannelSetId(channelSet.getKey().getId());
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
			CategoryChannelSet ccs = new CategoryChannelSet(categoryId, channelSetId);
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
	
	//////////////////// Channel/Program Management ////////////////////
	
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
		return results;
	}
	
	/**
	 * List all channel sets owned by mso
	 */
	@RequestMapping("listOwnedChannelSets")
	public @ResponseBody List<ChannelSet> listOwnedChannelSets(@RequestParam Long msoId) {
		
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		List<ChannelSet> results = new ArrayList<ChannelSet>();
		
		logger.info("msoId = " + msoId);
		
		results = ownershipMngr.findOwnedChannelSetsByMsoId(msoId);
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
			programMngr.delete(program); // NOTE: better way instead of delete ?
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
	}
	
	@RequestMapping("programInfo")
	public @ResponseBody MsoProgram programInfo(@RequestParam Long programId) {
		MsoProgramManager programMngr = new MsoProgramManager();
		return programMngr.findById(programId);
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
		MsoChannel channel = channelMngr.findBySourceUrlSearch(sourceUrl);
		if (channel == null) {
			sourceUrl = channelMngr.verifyUrl(sourceUrl);
			if (sourceUrl == null) {
				logger.warning("invalid source url");
				return null;
			}
			logger.info("new source url");
			channel = channelMngr.initChannelSubmittedFromPlayer(sourceUrl, userMngr.findNNUser()); //!!!
			channelMngr.create(channel, new ArrayList<Category>());
			if (channel.getKey() != null && channel.getContentType() != MsoChannel.CONTENTTYPE_FACEBOOK) { //!!!
				TranscodingService tranService = new TranscodingService();
				tranService.submitToTranscodingService(channel.getKey().getId(), sourceUrl, req);
			}
		}
		
		return channel;
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
	
	@RequestMapping("saveNewProgram")
	public @ResponseBody String saveNewProgram(@RequestParam Long programId,
	                                           @RequestParam Long channelId,
	                                           @RequestParam String sourceUrl,
	                                           @RequestParam String imageUrl,
	                                           @RequestParam String name,
	                                           @RequestParam String intro) {
		
		logger.info("programId = " + programId);
		logger.info("channelId = " + channelId);
		logger.info("sourceUrl = " + sourceUrl);
		logger.info("imageUrl = " + imageUrl);
		logger.info("name = " + name);
		logger.info("intro = " + intro);
		
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		
		MsoProgram program = programMngr.findById(programId);
		if (program == null) {
			return "Invalid programId";
		}
		MsoChannel channel = channelMngr.findById(channelId);
		if (channel == null) {
			return "Invalid channelId";
		}
		
		program.setName(name);
		program.setOtherFileUrl(sourceUrl);
		program.setImageUrl(imageUrl);
		program.setIntro(intro);
		program.setPublic(true);
		programMngr.create(channel, program);
		
		return "OK";
	}
	
	@RequestMapping("saveProgram")
	public @ResponseBody String saveProgram(@RequestParam Long programId,
	                                        @RequestParam String imageUrl,
	                                        @RequestParam String name,
	                                        @RequestParam String intro) {
		logger.info("programId = " + programId);
		logger.info("imageUrl = " + imageUrl);
		logger.info("name = " + name);
		logger.info("intro = " + intro);
		
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoProgram program = programMngr.findById(programId);
		if (program == null) {
			return "Invalid programId";
		}
		program.setName(name);
		program.setImageUrl(imageUrl);
		program.setIntro(intro);
		programMngr.save(program);
		
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
		channel.setUpdateDate(new Date());
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
	public @ResponseBody Long createChannelSkeleton(Long msoId) {
		
		NnUserManager userMngr = new NnUserManager();
		MsoManager msoMngr = new MsoManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		
		Mso mso = msoMngr.findById(msoId);
		if (mso == null)
			return null;
		
		MsoChannel channel = new MsoChannel("New Channel", "New Channel", "/WEB-INF/../images/processing.png", userMngr.findNNUser().getKey().getId());
		channel.setPublic(false);
		channel.setContentType(MsoChannel.CONTENTTYPE_9X9);
		channelMngr.create(channel, new ArrayList<Category>());
		
		//channel1 ownership
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		ownershipMngr.create(new ContentOwnership(), mso, channel);
		
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
	public @ResponseBody List<ChannelSet> listCategoryChannelSets(@RequestParam Long categoryId) {
		CategoryChannelSetManager cscMngr = new CategoryChannelSetManager();
		ChannelSetManager channelSetMngr = new ChannelSetManager();
		
		List<CategoryChannelSet> ccss = cscMngr.findAllByCategoryId(categoryId);
		List<Long> channelSetIdList = new ArrayList<Long>();
		for (CategoryChannelSet ccs : ccss) {
			channelSetIdList.add(ccs.getChannelSetId());
		}
		return channelSetMngr.findAllByChannelSetIds(channelSetIdList);
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
		return catMngr.findAllByMsoId(mso.getKey().getId());
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
		return "OK";
	}
	
}

