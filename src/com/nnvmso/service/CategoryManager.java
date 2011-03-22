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
			//save to cache
			Cache cache = CacheFactory.get();
			if (cache != null) { 
				String key = this.getCacheKey(category.getMsoId(),category.getKey().getId());
				cache.put(key, category);
				String cntKey = this.getCacheCntKey(category.getMsoId());
				@SuppressWarnings("unchecked")
				List<Long> list = (List<Long>)cache.get(cntKey);
				if (list == null) {list = new ArrayList<Long>();}
				list.add(category.getKey().getId());
				cache.put(cntKey, list);
			}			
		}
	}
	
	public Category save(Category category) {
		category.setUpdateDate(new Date());		
		category = categoryDao.save(category);
		//save to cache
		Cache cache = CacheFactory.get();
		String key = this.getCacheKey(category.getMsoId(),category.getKey().getId());
		if (cache != null) { cache.put(key, category);	}				
		return category;
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
	
	//result will be cached
	public List<Category> findAllByMsoId(long msoId) {
		List<Category> categories = new ArrayList<Category>();
		Cache cache = CacheFactory.get();
		List<Long> notCachedIds = new ArrayList<Long>();
		//find from cache
		if (cache != null) {
			@SuppressWarnings("unchecked")
			List<Long> ids = (List<Long>)cache.get(this.getCacheCntKey(msoId));
			if (ids != null) {
				for (int i=0; i < ids.size(); i++) {
					Category c = (Category)cache.get(this.getCacheKey(msoId, ids.get(i)));
					if (c != null) { 
						categories.add(c);
					} else {
						notCachedIds.add(ids.get(i));
					}										
				}
				if (notCachedIds.size() ==0) {return categories;}				
			}
		}
		//find
		if (notCachedIds.size() > 0) {
			categories = this.findAllByIds(notCachedIds);
			log.info("Categories are partial cached.");
		} else {
			categories = categoryDao.findAllByMsoId(msoId);
			log.info("Categories never been cached");
		}
		//save in cache
		if (cache != null) {
			List<Long> ids = new ArrayList<Long>();
			for (int i=0; i < categories.size(); i++) {
				long id = categories.get(i).getKey().getId();
				String key = this.getCacheKey(msoId, id);  
				cache.put(key, categories.get(i));
				ids.add(id);
			}
			String key = this.getCacheCntKey(msoId); 
			cache.put(key, ids);
		}		
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
	private String getCacheKey(long msoId, long categoryId) {
		return "mso(" + msoId + ")category(" + categoryId + ")";
	}
	
	//example: mso(1)categoryCnt, it returns a list of category id
	private String getCacheCntKey(long msoId) {
		return "mso(" + msoId + ")categoryCnt"; 		
	}

	public void deleteCache(long msoId) {
		Cache cache = CacheFactory.get(); 
		List<Category> categories = this.findAllByMsoId(msoId);
		if (cache != null) {			
			for (Category c : categories) {				
				cache.remove(this.getCacheKey(msoId, c.getKey().getId()));
			}
			cache.remove(this.getCacheCntKey(msoId));
		}
	}		

	public String findCache() {
		String output = "";
		String listOutput = "";
		Cache cache = CacheFactory.get();
		if (cache != null) {
			List<Category> categories = this.findAll();
			for (Category c : categories) {
				Category category = (Category)cache.get(this.getCacheKey(c.getMsoId(), c.getKey().getId()));
				 if (category != null) {
					output = output + category.toString() + "\n";
				}
				@SuppressWarnings("unchecked")
				List<Long> list = (List<Long>)cache.get(this.getCacheCntKey(c.getMsoId()));
				if (list != null) {
					listOutput = "\n category id under mso: " + c.getMsoId() + "\n";
					for (Long l : list) {
						listOutput = listOutput + l + "\t";
					}
				}
			}
		}
		return output + listOutput;
	}
	
}
