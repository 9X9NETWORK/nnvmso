package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.NnContentDao;
import com.nnvmso.model.NnContent;

@Service
public class NnContentManager {

	protected static final Logger logger = Logger.getLogger(NnContentManager.class.getName());
	
	private NnContentDao nnContentDao = new NnContentDao();
	
	public NnContent create(NnContent content) {
		NnContent existed = this.findByItemAndLang(content.getItem(), content.getLang());
		if (existed != null) {
			content.setContent(content.getContent());
			this.save(existed);
			return existed;
		}
		content.setCreateDate(new Date());
		this.save(content);
		return content;
	}
	
	public NnContent save(NnContent content) {		
		content.setUpdateDate(new Date());
		content = nnContentDao.save(content);
		return content;
	}

	public NnContent findByItemAndLang(String item, String lang) {		
		return nnContentDao.findByItemAndLang(item, lang);
	}

	public List<NnContent> findAll() {		
		return nnContentDao.findAll();
	}
	
}
