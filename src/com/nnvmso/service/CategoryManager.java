package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.nnvmso.dao.CategoryDao;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.Category;

@Service
public class CategoryManager {
	
	protected static final Logger logger = Logger.getLogger(CategoryManager.class.getName());
	
	private CategoryDao categoryDao = new CategoryDao();
	
	public void create(Category category) {		
		if (this.findByName(category.getName()) == null) {
			this.save(category);
		}
	}

	public Category save(Category category) {
		Date now = new Date();
		if (category.getCreateDate() == null) {category.setUpdateDate(now);}
		category.setUpdateDate(new Date());
		category.setName(NnStringUtil.capitalize(category.getName()));
		return categoryDao.save(category);
	}
	
	public Category findByKey(Key key) {
		return categoryDao.findByKey(key);
	}
	
	public Category findByKeyStr(String key) {
		try {
			return this.findByKey(KeyFactory.stringToKey(key));
		} catch (IllegalArgumentException e) {
			logger.info("invalid key string");
			return null;
		}
	}
	
	public List<Category> findAllByMsoId(long msoId) {
		return categoryDao.findAllByMsoId(msoId);
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
}
