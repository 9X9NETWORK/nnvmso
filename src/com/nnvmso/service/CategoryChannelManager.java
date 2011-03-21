package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.nnvmso.dao.CategoryChannelDao;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.MsoChannel;

public class CategoryChannelManager {
	
	protected static final Logger log = Logger.getLogger(CategoryChannelManager.class.getName());
	
	private CategoryChannelDao ccDao = new CategoryChannelDao();		
	
	public void create(CategoryChannel cc) {
		Date now = new Date();
		cc.setCreateDate(now);
		cc.setUpdateDate(now);
		ccDao.save(cc);
	}	

	public void delete(CategoryChannel cc) {
		CategoryManager categoryMngr = new CategoryManager();
		Category category = categoryMngr.findById(cc.getCategoryId()); 
		ccDao.delete(cc);
		category.setChannelCount(category.getChannelCount()-1);
		categoryMngr.save(category);
	}
	
	public void deleteChannelCategory(MsoChannel channel, List<Category> categories) {
		if (channel == null) {return;}
		if (categories.size() == 0) {return;}
				
		for (Category c : categories) {	
			CategoryChannel cc = this.findByCategoryIdAndChanelId(c.getKey().getId(), channel.getKey().getId());
			if (cc != null) {
				this.delete(cc);
			}
		}		
	}
	
	public List<CategoryChannel> findAllByCategoryId(long categoryId) {
		List<CategoryChannel> ccs = ccDao.findAllByCategoryId(categoryId);
		log.info("findByCategoryKey(): found " + ccs.size() + " with id " + categoryId);
		return ccs;
	}
	
	public List<CategoryChannel> findAllByChannelId(long channelId) {
		List<CategoryChannel> ccs = ccDao.findAllByChannelId(channelId);
		return ccs;
	}

	public CategoryChannel findByCategoryIdAndChanelId(long categoryId, long channelId) {
		return ccDao.findByCategoryIdAndChannelId(categoryId, channelId);
	}
	
	public CategoryChannel findById(long id) {
		return ccDao.findById(id);
	}
}
