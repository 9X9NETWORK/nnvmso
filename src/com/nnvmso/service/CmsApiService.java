package com.nnvmso.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.model.Category;
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
	
	public List<Category> whichSystemCategoriesContainingTheChannelSet(long channelSetId) {
		Mso nnmso = msoMngr.findNNMso();
		List<Category> sysCategories = catMngr.findAllByMsoId(nnmso.getKey().getId());
		List<Long> categoryIds = new ArrayList<Long>();
		for (Category category : sysCategories) {
			categoryIds.add(category.getKey().getId());
		}
		
		List<CategoryChannelSet> ccss = ccsMngr.findByChannelSetIdAndCategoryIds(channelSetId, categoryIds);
		
		categoryIds.clear();
		for (CategoryChannelSet ccs : ccss) {
			categoryIds.add(ccs.getCategoryId());
		}
		
		return catMngr.findAllByIds(categoryIds);
	}
}
