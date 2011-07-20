package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.CategoryDao;
import com.nnvmso.lib.CacheFactory;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.MsoChannel;

@Service
public class CategoryManager {
	
	protected static final Logger log = Logger.getLogger(CategoryManager.class.getName());
	
	private CategoryDao categoryDao = new CategoryDao();
		
	public void create(Category category) {		
		if (this.findByName(category.getName()) == null) {
			Date now = new Date();
			category.setCreateDate(now);
			category.setUpdateDate(now);
			categoryDao.save(category);
			this.cacheRefresh(category);
		}
	}
	
	public Category save(Category category) {
		category.setUpdateDate(new Date());		
		category = categoryDao.save(category);
		this.cacheRefresh(category);
		return category;
	}
	
	public void cacheRefresh(Category category) {
		this.findAllByMsoId(category.getMsoId());
		this.findAllInIpg(category.getMsoId());
	}
	
	public List<Category> findCategoriesByChannelId(long channelId) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findById(channelId);
		if (channel == null) {return new ArrayList<Category>();}
		
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		CategoryManager categoryMngr = new CategoryManager();
		List<CategoryChannel> ccs = ccMngr.findAllByChannelId(channel.getKey().getId());				
		List<Category> categories = new ArrayList<Category>();		
		for (CategoryChannel cc : ccs) {
			Category c = categoryMngr.findById(cc.getCategoryId());
			if (c != null) {categories.add(c);}
		}
		return categories;
	}
	
	public List<Category> findCategoriesByIdStr(String categoryIds) {
		List<Long> categoryIdList = new ArrayList<Long>();	
		String[] arr = categoryIds.split(",");
		for (int i=0; i<arr.length; i++) { categoryIdList.add(Long.parseLong(arr[i])); }
		List<Category> categories = this.findAllByIds(categoryIdList);
		return categories;		
	}
	
	//add only
	public List<Category> changeCategory(long channelId, List<Category>categories) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findById(channelId);
		if (channel == null) {return new ArrayList<Category>();}		
		if (categories.size() == 0) {return new ArrayList<Category>();}		
		
		//update category if necessary
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		// --find existing categories
		List<CategoryChannel> ccs = ccMngr.findAllByChannelId(channel.getKey().getId()); 			
		HashSet<Long> existing = new HashSet<Long>();
		for (CategoryChannel cc : ccs) {
			existing.add(cc.getCategoryId());
		}				
		// --find new categories user defines if there's any	
		List<Long> newCategoryIdList = new ArrayList<Long>();
		for (int i=0; i<categories.size(); i++) {
			if (!existing.contains((categories.get(i).getKey().getId()))) {newCategoryIdList.add(categories.get(i).getKey().getId());}
		}				
		// --add new category		
		if (newCategoryIdList.size() > 0) {
			HashMap<Long, Category> categoryMap = new HashMap<Long, Category>();
			for (Category c : categories) {
				categoryMap.put(c.getKey().getId(), c);
			}
			for (Long id : newCategoryIdList) {
				Category c = categoryMap.get(Long.valueOf(id));
				ccMngr.create(new CategoryChannel(c.getKey().getId(), channel.getKey().getId()));				
			}
		}
		return categories;
	}
	
	public void addChannelCounter(MsoChannel channel) {
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		if (channelMngr.isCounterQualified(channel)) {
			List<CategoryChannel> ccs = new ArrayList<CategoryChannel>();
			ccs = ccMngr.findAllByChannelId(channel.getKey().getId());
			for (CategoryChannel cc : ccs) {
				Category c = this.findById(cc.getCategoryId());
				log.info("count added: category id" + c.getKey().getId() + ";channelId " + channel.getKey().getId() + ";name " + channel.getName());
				c.setChannelCount(c.getChannelCount() + 1);
				this.save(c);					
			}
		}						
	}
	
	public Category findByKey(Key key) {
		return categoryDao.findByKey(key);
	}
	
	public List<Category> list(int page, int limit, String sidx, String sord) {
		return categoryDao.list(page, limit, sidx, sord);
	}
	
	public List<Category> list(int page, int limit, String sidx, String sord, String filter) {
		return categoryDao.list(page, limit, sidx, sord, filter);
	}
	
	public int total() {
		return categoryDao.total();
	}
	
	public int total(String filter) {
		return categoryDao.total(filter);
	}
	
	public List<Category> findAllByMsoIdWithoutCache(long msoId) {
		return categoryDao.findAllByMsoId(msoId);
	}
	
	//result will be cached
	public List<Category> findAllByMsoId(long msoId) {
		Cache cache = CacheFactory.get();
		String key = this.getCacheKey(msoId, true);
		if (cache != null) {
			@SuppressWarnings("unchecked")
			List<Category> categories= (List<Category>) cache.get(key);
			if (categories != null) { 
				log.info("categories from cahce:" + categories.size());
				return categories;
			}
		}		
		List<Category> categories = categoryDao.findAllByMsoId(msoId); //!!!hack
		cache.put(key, categories);
		return categories;
	}

	//result will be cached
	public List<Category> findAllInIpg(long msoId) {
		Cache cache = CacheFactory.get();
		String key = this.getCacheKey(msoId, false);
		if (cache != null) {
			@SuppressWarnings("unchecked")
			List<Category> categories= (List<Category>) cache.get(key);
			if (categories != null) { 
				log.info("categories from cahce:" + categories.size());
				return categories;
			}
		}
		List<Category> categories = categoryDao.findAllInIpg(msoId);//!!!hack
		cache.put(key, categories);
		return categories;
	}
	
	public List<Category> findAll() {
		return categoryDao.findAll();
	}
	
	public Category findByName(String name) {
		return categoryDao.findByName(name);
	}	
	
	public List<Category> findAllByIds(List<Long> ids) {
		 return categoryDao.findAllByIds(ids);
	}

	public Category findById(long id) {
		return categoryDao.findById(id);
	}
		
	//example: mso(1)category(123), returns category
	private String getCacheKey(long msoId, boolean isAll) {
		String option = "all";
		if (!isAll)
			option = "ipg";
		return "mso(" + msoId + ")category(" + option + ")";
	}
	
	public void deleteCache(long msoId) {
		Cache cache = CacheFactory.get();
		if (cache != null) {
			cache.remove(this.getCacheKey(msoId, true));
			cache.remove(this.getCacheKey(msoId, false));
		}
	}		

	public List<Category> findAllByParentId(long parentId) {
		return categoryDao.findAllByParanetId(parentId);
	}
	
}
