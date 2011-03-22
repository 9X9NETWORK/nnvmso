package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.sf.jsr107cache.Cache;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.MsoDao;
import com.nnvmso.lib.CacheFactory;
import com.nnvmso.lib.CookieHelper;
import com.nnvmso.model.Mso;

@Service
public class MsoManager {

	protected static final Logger log = Logger.getLogger(MsoManager.class.getName());
	
	private MsoDao msoDao = new MsoDao();
		
	public void create(Mso mso) {
		if (this.findByName(mso.getName()) == null) {
			this.save(mso);			
		}
		Cache cache = CacheFactory.get();
		if (cache != null) { cache.put(this.getCacheKey(mso.getName()), mso);	}
	}
	
	public Mso save(Mso mso) {
		if (mso.getName() != null) {
			mso.setNameSearch(mso.getName().toLowerCase());
		}
		Date now = new Date();
		if (mso.getCreateDate() == null) {mso.setCreateDate(now);}
		mso.setUpdateDate(now);
		Cache cache = CacheFactory.get();
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
		Cache cache = CacheFactory.get();
		String cacheKey = this.getCacheKey(name);
		if (cache != null) {
			Mso mso = (Mso) cache.get(cacheKey);
			if (mso != null) { return mso;}
		}
		//find
		Mso mso = msoDao.findByName(name);
		//save in cache
		if (cache != null && mso != null) { cache.put(cacheKey, mso);}				
		return mso;
	}	
	
	public String findMsoNameViaHttpReq(HttpServletRequest req) {
		String msoName = req.getParameter("mso"); //?mso=
		if (msoName == null) {
			msoName = CookieHelper.getCookie(req, CookieHelper.MSO);
		}
		if (msoName == null) {
			msoName = "9x9";
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
	
	public List<Mso> list(int page, int limit, String sidx, String sord) {
		return msoDao.list(page, limit, sidx, sord);
	}
	
	public List<Mso> list(int page, int limit, String sidx, String sord, String filter) {
		return msoDao.list(page, limit, sidx, sord, filter);
	}
	
	public int total() {
		return msoDao.total();
	}
	
	public int total(String filter) {
		return msoDao.total(filter);
	}
	
	public Mso findByKey(Key key) {
		return msoDao.findByKey(key);
	}	
	
	//example: mso(1)category(123), returns category
	private String getCacheKey(String name) {
		return "mso(" + name + ")";
	}
	
	public String findCache() {
		String output = "";
		Cache cache = CacheFactory.get();
		if (cache != null) {
			List<Mso> msos = this.findAll();
			for (Mso m : msos) {
				Mso mso = ((Mso)cache.get(this.getCacheKey(m.getName())));
				if (mso != null) {
					output = output + mso.toString();
				}				
			}
		}
		return output;
	}
	
	public void deleteCache() {
		Cache cache = CacheFactory.get();
		List<Mso> msos = this.findAll();
		if (cache != null) {
			for (Mso m : msos) {				
				cache.remove(this.getCacheKey(m.getName()));
			}
		}
	}		
}
