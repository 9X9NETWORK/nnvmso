package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.CategoryChannelSetDao;
import com.nnvmso.model.CategoryChannelSet;

@Service
public class CategoryChannelSetManager {
	
	private CategoryChannelSetDao ccsDao = new CategoryChannelSetDao();
	
	protected static final Logger log = Logger.getLogger(CategoryChannelSetManager.class.getName());
	
	public void create(CategoryChannelSet ccs) {
		Date now = new Date();
		ccs.setUpdateDate(now);
		ccs.setCreateDate(now);
		ccsDao.save(ccs);
	}
	
	public void save(CategoryChannelSet ccs) {
		Date now = new Date();
		ccs.setUpdateDate(now);
		ccsDao.save(ccs);
	}
	
	public void delete(CategoryChannelSet ccs) {
		ccsDao.delete(ccs);
	}
	
	public List<CategoryChannelSet> findByChannelSetIdAndCategoryIds(long channelSetId, List<Long> categoryIds) {
		List<CategoryChannelSet> results = new ArrayList<CategoryChannelSet>();
		for (Long categoryId : categoryIds) {
			CategoryChannelSet ccs = ccsDao.findByChannelSetIdAndCategoryId(channelSetId, categoryId);
			if (ccs != null)
				results.add(ccs);
		}
		return results;
	}

	public List<CategoryChannelSet> findAllBySetId(Long setId) {
		return ccsDao.findAllBySetId(setId);
	}
	
	public List<CategoryChannelSet> findAllByCategoryId(Long categoryId) {
		return ccsDao.findAllByCategoryId(categoryId);
	}

	public CategoryChannelSet findByCategoryIdAndChannelSetId(Long categoryId, Long channelSetId) {
		return ccsDao.findByChannelSetIdAndCategoryId(channelSetId, categoryId);
	}
	
}
