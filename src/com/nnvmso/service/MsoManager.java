package com.nnvmso.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.MsoDao;
import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Mso;

@Service
public class MsoManager {

	protected static final Logger log = Logger.getLogger(MsoManager.class.getName());
	
	private MsoDao msoDao = new MsoDao();
	private Cache cache;
		
	public void create(Mso mso) {
		if (this.findByName(mso.getName()) == null) {
			this.save(mso);			
		}
		this.setCache();
		if (cache != null) { cache.put(this.getCacheKey(mso.getName()), mso);	}
	}
	
	public Mso save(Mso mso) {
		if (mso.getName() != null) {
			mso.setNameSearch(mso.getName().toLowerCase());
		}
		Date now = new Date();
		if (mso.getCreateDate() == null) {mso.setUpdateDate(now);}
		mso.setUpdateDate(now);
		this.setCache();
		if (cache != null) { cache.put(this.getCacheKey(mso.getName()), mso);	}
		return msoDao.save(mso);
	}
	
	public List<Mso> findByType(short type) {
		return msoDao.findByType(type);
	}
	
	public Mso findNNMso() {
		List<Mso> msos = this.findByType(Mso.TYPE_NN);
		if (msos.size() > 0) { return msos.get(0); }					
		return null;
	}

	//cached
	public Mso findByName(String name) {
		//find from cache
		if (name == null) {return null;}
		this.setCache();
		String cacheKey = this.getCacheKey(name);
		if (cache != null) {
			Mso mso = (Mso) cache.get(cacheKey);
			if (mso != null) {
				log.info("Cache found: mso in cache:" + mso.getName());
				return mso;	
			}
		}
		//find
		Mso mso = msoDao.findByName(name);
		//save in cache
		if (cache != null && mso != null) { cache.put(cacheKey, mso);}
		log.info("Cache NOT found: mso is just added:" + name);
		return mso;
	}	
	
	public String findMsoNameViaHttpReq(HttpServletRequest req) {
		String url = NnNetUtil.getUrlRoot(req);
		String msoName = "";
		if (url.contains("5f.tv")) {
			msoName = "5f";
		} else if (url.contains("9x9.tv")) {
			msoName = "9x9";
		} else {
			msoName = req.getParameter("mso");
			if (msoName == null) {
				msoName = CookieHelper.getCookie(req, CookieHelper.MSO);
			}
			if (msoName == null) {
				msoName = "9x9";
			}
		}
		return msoName;
	}
	
	public Mso findMsoViaHttpReq(HttpServletRequest req) {
		String msoName = this.findMsoNameViaHttpReq(req);
		Mso mso = this.findByName(msoName);
		if (mso == null) {
			log.info("determine mso failed. use default mso");
			mso = this.findNNMso(); 
		}
		return mso;
	}
	
	public Mso findById(long id) {
		return msoDao.findById(id);
	}
	
	public List<Mso> findAll() {
		return msoDao.findAll();
	}
	
	public Mso findByKey(Key key) {
		return msoDao.findByKey(key);
	}	
	
	//example: mso(1)category(123), returns category
	private String getCacheKey(String name) {
		return "mso(" + name + ")";
	}
	
	private void setCache() {
	    try {
	        cache = CacheManager.getInstance().getCacheFactory().createCache(
	            Collections.emptyMap());
	      } catch (CacheException e) {}	      		
	}
	
	public void deleteCache() {
		this.setCache();
		List<Mso> msos = this.findAll();
		if (cache != null) {
			for (Mso m : msos) {				
				cache.remove(this.getCacheKey(m.getName()));
			}
		}
	}		
}
