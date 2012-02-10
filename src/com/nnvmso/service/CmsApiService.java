package com.nnvmso.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.CategoryChannelSet;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ChannelSetChannel;
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
		List<Category> sysCategories = catMngr.findAllSystemCategories();
		List<Long> categoryIds = new ArrayList<Long>();
		for (Category category : sysCategories) {
			categoryIds.add(category.getKey().getId());
		}
		
		return ccsMngr.findByChannelSetIdAndCategoryIds(channelSetId, categoryIds);
	}
	
	public Category whichSystemCategoryContainingTheChannelSet(long channelSetId) {
		
		List<Category> sysCategories = catMngr.findAllSystemCategories();
		
		for (Category category : sysCategories) {
			CategoryChannelSet ccs = ccsMngr.findByCategoryIdAndChannelSetId(category.getKey().getId(), channelSetId);
			if (ccs != null) {
				return catMngr.findById(ccs.getCategoryId());
			}
		}
		
		return null;
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
		List<Category> sysCategories = catMngr.findAllSystemCategories();
		List<Long> categoryIds = new ArrayList<Long>();
		for (Category category : sysCategories) {
			categoryIds.add(category.getKey().getId());
		}
		
		return ccMngr.findByChannelIdAndCategoryIds(channelId, categoryIds);
	}
	
	public List<ChannelSet> whichSystemChannelSetsContainingThisChannel(
			Long channelId) {
		ChannelSetManager setMngr = new ChannelSetManager();
		List<ChannelSetChannel> cscs = this.whichSystemCSCContainingThisChannel(channelId);
		List<Long> channelSetIds = new ArrayList<Long>();
		for (ChannelSetChannel csc : cscs) {
			channelSetIds.add(csc.getChannelSetId());
		}
		return setMngr.findAllByChannelSetIds(channelSetIds);
	}
	
	public List<ChannelSetChannel> whichSystemCSCContainingThisChannel(long channelId) {
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		MsoManager msoMngr = new MsoManager();
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		List<ChannelSetChannel> results = new ArrayList<ChannelSetChannel>();
		
		List<ChannelSetChannel> cscs = cscMngr.findAllByChannelId(channelId);
		List<ChannelSet> systemChannelSets = ownershipMngr.findOwnedChannelSetsByMsoId(msoMngr.findNNMso().getKey().getId());
		List<Long> systemChannelSetIds = new ArrayList<Long>();
		for (ChannelSet set : systemChannelSets) {
			systemChannelSetIds.add(set.getKey().getId());
		}
		for (ChannelSetChannel csc : cscs) {
			if (systemChannelSetIds.contains(csc.getChannelSetId())) {
				results.add(csc);
			}
		}
		
		return results;
	}
}
