package com.nnvmso.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.CategoryChannelSet;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;

@Service
public class CmsApiService {
	protected static final Logger logger = Logger.getLogger(CmsApiService.class.getName());
	
	private ChannelSetManager channelSetMngr = new ChannelSetManager();
	private ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
	private MsoManager msoMngr = new MsoManager();
	private CategoryManager catMngr = new CategoryManager();
	private CategoryChannelSetManager ccsMngr = new CategoryChannelSetManager();
	private CategoryChannelManager ccMngr = new CategoryChannelManager();
	
	public ChannelSet getDefaultChannelSet(long msoId) {
		List<ChannelSet> ownedChannelSets = ownershipMngr.findOwnedChannelSetsByMsoId(msoId);
		if (ownedChannelSets.size() > 0) {
			return ownedChannelSets.get(0);
		}
		return null;
	}
	
	public List<MsoChannel> findChannelsByChannelSetId(long channelSetId) {
		return channelSetMngr.findChannelsById(channelSetId);
	}
	
	public List<CategoryChannelSet> whichCCSContainingTheChannelSet(long channelSetId) {
		Mso nnmso = msoMngr.findNNMso();
		List<Category> sysCategories = catMngr.findAllByMsoId(nnmso.getKey().getId());
		List<Long> categoryIds = new ArrayList<Long>();
		for (Category category : sysCategories) {
			categoryIds.add(category.getKey().getId());
		}
		
		return ccsMngr.findByChannelSetIdAndCategoryIds(channelSetId, categoryIds);
	}
	
	public List<Category> whichSystemCategoriesContainingTheChannelSet(long channelSetId) {
		
		List<CategoryChannelSet> ccss = this.whichCCSContainingTheChannelSet(channelSetId);
		
		List<Long> categoryIds = new ArrayList<Long>();
		for (CategoryChannelSet ccs : ccss) {
			categoryIds.add(ccs.getCategoryId());
		}
		
		return catMngr.findAllByIds(categoryIds);
	}
	
	public List<Category> whichSystemCategoriesContainingTheChannel(long channelId) {
		
		List<CategoryChannel> ccs = this.whichCCContainingTheChannel(channelId);
		
		logger.info("ccs count = " + ccs.size());
		
		List<Long> categoryIds = new ArrayList<Long>();
		for (CategoryChannel cc : ccs) {
			categoryIds.add(cc.getCategoryId());
		}
		
		return catMngr.findAllByIds(categoryIds);
	}
	
	public List<CategoryChannel> whichCCContainingTheChannel(Long channelId) {
		Mso nnmso = msoMngr.findNNMso();
		List<Category> sysCategories = catMngr.findAllByMsoId(nnmso.getKey().getId());
		List<Long> categoryIds = new ArrayList<Long>();
		for (Category category : sysCategories) {
			categoryIds.add(category.getKey().getId());
		}
		
		return ccMngr.findByChannelIdAndCategoryIds(channelId, categoryIds);
	}
}
