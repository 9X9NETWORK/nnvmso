package com.nnvmso.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.CategoryDao;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.Category;

@Service
public class CategoryManager {
	
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
	
	public List<Category> findAllByMsoId(long msoId) {
		return categoryDao.findAllByMsoId(msoId);
	}
	
	public List<Category> findAll() {
		return categoryDao.findAll();
	}
	
	public Category findByName(String name) {
		return categoryDao.findByName(name);
	}	
	
	public List<Category> findAllByIds(String ids) {
		 String[] idStrArr = ids.split(",");		 
		 long idLongArr[] = new long[idStrArr.length];
		 for (int i=0; i<idLongArr.length; i++) {
			 idLongArr[i] = Long.valueOf(idStrArr[i]);
		 }
		 return categoryDao.findAllByIds(idLongArr);
	}

	public Category findById(long id) {
		return categoryDao.findById(id);
	}		
}