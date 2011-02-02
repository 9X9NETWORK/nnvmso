package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.CategoryDao;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.Category;

@Service
public class CategoryManager {
	
	protected static final Logger log = Logger.getLogger(CategoryManager.class.getName());
	
	private CategoryDao categoryDao = new CategoryDao();
	private Cache cache;
		
	public void create(Category category) {		
		if (this.findByName(category.getName()) == null) {
			Date now = new Date();
			category.setName(NnStringUtil.capitalize(category.getName()));
			category.setCreateDate(now);
			category.setUpdateDate(now);
			categoryDao.save(category);
			//save to cache
			this.setCache();
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
		category.setName(NnStringUtil.capitalize(category.getName()));		
		category = categoryDao.save(category);
		//save to cache
		this.setCache();
		String key = this.getCacheKey(category.getMsoId(),category.getKey().getId());
		if (cache != null) { cache.put(key, category);	}				
		return category;
	}
	
	public Category findByKey(Key key) {
		return categoryDao.findByKey(key);
	}

	//result will be cached
	public List<Category> findAllByMsoId(long msoId) {
		List<Category> categories = new ArrayList<Category>();
		this.setCache();
		//find from cache
		if (cache != null) {
			@SuppressWarnings("unchecked")
			List<Long> ids = (List<Long>)cache.get(this.getCacheCntKey(msoId));
			if (ids != null) {
				//!!! add if individual cache is kicked out handling
				log.info("Cache found: categories in cache, count=" + ids.size());
				for (int i=0; i < ids.size(); i++) {
					categories.add((Category)cache.get(this.getCacheKey(msoId, ids.get(i))));
				}
				return categories;
			}
		}
		//find
		categories = categoryDao.findAllByMsoId(msoId);
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
			log.info("Cache NOT found: categories is just added, count=" + categories.size());
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
	
	private void setCache() {
	    try {
	        cache = CacheManager.getInstance().getCacheFactory().createCache(
	            Collections.emptyMap());
	      } catch (CacheException e) {}	      		
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
		this.setCache();
		List<Category> categories = this.findAllByMsoId(msoId);
		if (cache != null) {			
			for (Category c : categories) {				
				cache.remove(this.getCacheKey(msoId, c.getKey().getId()));
			}
			cache.remove(this.getCacheCntKey(msoId));
		}
	}		
	
}
