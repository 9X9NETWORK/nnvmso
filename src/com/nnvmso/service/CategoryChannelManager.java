package com.nnvmso.service;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.CategoryChannelDao;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;

public class CategoryChannelManager {
	private CategoryChannelDao ccDao = new CategoryChannelDao();
	
	public void create(CategoryChannel cc) {
		ccDao.create(cc);
		CategoryManager categoryMngr = new CategoryManager();
		Category category = categoryMngr.findByKey(cc.getCategoryKey());
		category.setChannelCount(category.getChannelCount() + 1);
		categoryMngr.save(category);
	}

	public List<CategoryChannel> findByCategoryKey(Key categoryKey) {
		List<CategoryChannel> ccs = ccDao.findAllByCategoryKey(categoryKey);
		System.out.println("findByCategoryKey(): found " + ccs.size() + " with key " + categoryKey.getId());
		return ccs;
	}
	
	public List<CategoryChannel> findByChannelKey(Key channelKey) {
		List<CategoryChannel> ccs = ccDao.findAllByChannelKey(channelKey);
		System.out.println("CategoryChannelManager: findByChannelKey(): found " + ccs.size() + " with key " + channelKey.getId());
		return ccs;
	}
	
	public CategoryChannel findById(long id) {
		return ccDao.findById(id);
	}
}
