package com.nnvmso.service;

import java.util.Date;
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
	
	public void delete(CategoryChannelSet ccs) {
		ccsDao.delete(ccs);
	}
}
