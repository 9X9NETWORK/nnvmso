package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.CategoryDao;
import com.nnvmso.lib.CacheFactory;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;

@Service
public class CategoryManager {
	
	protected static final Logger log = Logger.getLogger(CategoryManager.class.getName());
	private static MessageSource messageSource = new ClassPathXmlApplicationContext("locale.xml");
	
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
	
	public void cacheRefresh(Category category) {
		this.findAllByMsoId(category.getMsoId());
	}
	
	public String translate(String name) {
		if (name == null) return null;
		Locale locale = Locale.TRADITIONAL_CHINESE;
		if (name.equals("News & Politics")) 
			return messageSource.getMessage("category.news", null, locale);
		if (name.equals("Finance & Management")) 
			return messageSource.getMessage("category.finance", null, locale);
		if (name.equals("Entertainment")) 
			return messageSource.getMessage("category.entertainment", null, locale);
		if (name.equals("Music")) 
			return messageSource.getMessage("category.music", null, locale);
		if (name.equals("Sports & Outdoors")) 
			return messageSource.getMessage("category.sports", null, locale);
		if (name.equals("Tech & Science")) 
			return messageSource.getMessage("category.tech", null, locale);
		if (name.equals("Gaming")) 
			return messageSource.getMessage("category.gaming", null, locale);
		if (name.equals("Lifestyle & Hobbies")) 
			return messageSource.getMessage("category.lifestyle", null, locale);
		if (name.equals("Travel & Living")) 
			return messageSource.getMessage("category.travel", null, locale);
		if (name.equals("Arts & Creative")) 
			return messageSource.getMessage("category.arts", null, locale);
		if (name.equals("Society & Organizations")) 
			return messageSource.getMessage("category.org", null, locale);
		if (name.equals("Education & How to")) 
			return messageSource.getMessage("category.education", null, locale);
		if (name.equals("Nature & Animals")) 
			return messageSource.getMessage("category.nature", null, locale);
		if (name.equals("People")) 
			return messageSource.getMessage("category.people", null, locale);
		if (name.equals("Religion & Spirituality")) 
			return messageSource.getMessage("category.religion", null, locale);
		if (name.equals("Others")) 
			return messageSource.getMessage("category.others", null, locale);
		return "其他類";
	}			
	
	public Category save(Category category) {
		category.setUpdateDate(new Date());		
		category = categoryDao.save(category);
		this.cacheRefresh(category);
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
	
	public List<Category> findPlayerCategories(long parentId, long msoId) {
		return categoryDao.findPlayerCategories(parentId, msoId);
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
			//find all the category a channel belongs to
			ccs = ccMngr.findAllByChannelId(channel.getKey().getId());						
			for (CategoryChannel cc : ccs) {
				Category c = this.findById(cc.getCategoryId());
				log.info("count added: category id" + c.getKey().getId() + ";channelId " + channel.getKey().getId() + ";name " + channel.getName());
				if (channel.getLangCode().equals(Mso.LANG_ZH))
					c.setChnChannelCount(c.getChnChannelCount()+1);
				else
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
		String key = this.getCacheKey(msoId);
		if (cache != null) {
			@SuppressWarnings("unchecked")
			List<Category> categories= (List<Category>) cache.get(key);
			if (categories != null) { 
				log.info("categories from cahce:" + categories.size());
				return categories;
			}
		}		
		List<Category> categories = categoryDao.findAllByMsoId(msoId); //!!!hack
		List<Category> sequence = new ArrayList<Category>();
		Category others = null;
		for (Category c : categories) {
			if (!c.getName().equals("Others"))
				sequence.add(c);
			else
				others = c;
				
		}
		if (others != null)
			sequence.add(others);
		cache.put(key, sequence);
		return sequence;
	}
	
	public List<Category> findAll() {
		return categoryDao.findAll();
	}

	public List<Category> findByParentId(long id) {
		return categoryDao.findByParentId(id);
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
			
	private String getCacheKey(long msoId) {
		return "mso(" + msoId + ")category(all)";
	}
	
	public void deleteCache(long msoId) {
		Cache cache = CacheFactory.get();
		if (cache != null) {
			cache.remove(this.getCacheKey(msoId));
		}
	}		

	public List<Category> findAllByParentId(long parentId) {
		return categoryDao.findAllByParanetId(parentId);
	}
	
}
