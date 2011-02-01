package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.MsoDao;
import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Mso;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@Service
public class MsoManager {

	protected static final Logger log = Logger.getLogger(MsoManager.class.getName());
	
	private MsoDao msoDao = new MsoDao();
 
	public void create(Mso mso) {
		if (this.findByName(mso.getName()) == null) {
			this.save(mso);			
		}		
	}
	
	public Mso save(Mso mso) {
		if (mso.getName() != null) {
			mso.setNameSearch(mso.getName().toLowerCase());
		}
		Date now = new Date();
		if (mso.getCreateDate() == null) {mso.setUpdateDate(now);}
		mso.setUpdateDate(now);
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

	public Mso findByName(String name) {
		if (name == null) {return null;}
		Mso mso = msoDao.findByName(name);
		return mso;
	}	

	//to be removed in the future, for multi-language
	public short findMsoTypeViaHttpReq(HttpServletRequest req) {
		String msoName = this.findMsoNameViaHttpReq(req);
		if (!msoName.equals("5f")) {
			return Mso.TYPE_NN;
		}
		return Mso.TYPE_MSO;
	}

	public String findMsoNameViaHttpReq(HttpServletRequest req) {
		String url = NnNetUtil.getUrlRoot(req);
		String msoName = "";
		if (url.contains("5f.tv")) {
			msoName = "5f";
			log.info("determine mso name by url:" + msoName);
		} else if (url.contains("9x9.tv")) {
			msoName = "9x9";
			log.info("determine mso name by url:" + msoName);
		} else {
			msoName = req.getParameter("mso");
			log.info("determine mso name by header:" + msoName);
			if (msoName == null) {
				msoName = CookieHelper.getCookie(req, CookieHelper.MSO);
				log.info("determine mso name by cookie:" + msoName);
			}
			if (msoName == null) {
				msoName = "9x9";
				log.info("determine mso name by default:" + msoName);
			}
		}
		return msoName;
	}
	
	public Mso findMsoViaHttpReq(HttpServletRequest req) {
		String msoName = this.findMsoNameViaHttpReq(req);
		Mso mso = new MsoManager().findByName(msoName);
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
	
	public Mso findByKeyStr(String key) {
		try {
			return this.findByKey(KeyFactory.stringToKey(key));
		} catch (IllegalArgumentException e) {
			log.info("invalid key string");
			return null;
		}
	}
}
