package com.nnvmso.service;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.nnvmso.dao.NnUserDao;
import com.nnvmso.model.*;

@Service
public class NnUserManager {
	
	protected static final Logger log = Logger.getLogger(NnUserManager.class.getName());
		
	private NnUserDao nnUserDao = new NnUserDao();
	
	//@@@IMPORTANT email duplication is your responsibility
	public void create(NnUser user) {
		user.setName(user.getName().replaceAll("\\s", " "));
		user.setEmail(user.getEmail().toLowerCase());
		nnUserDao.create(user);
	}

	public NnUser save(NnUser user) {
		return nnUserDao.save(user);
	}
	
	public List<NnUser> findByType(short type) {
		return nnUserDao.findByType(type);
	}
	
	public NnUser findNNUser() {
		List<NnUser> users = nnUserDao.findByType(NnUser.TYPE_NN);
		if (users.size() > 0) {return nnUserDao.findByType(NnUser.TYPE_NN).get(0); }
		return null;
	}
	
	public NnUser findAuthenticatedUser(String email, String password, Mso mso) {
		if (mso == null) {return null;}
		return nnUserDao.findAuthenticatedUser(email, password, mso.getKey());
	}
	
	public void subscibeDefaultChannels(NnUser user) {
		MsoChannelManager channelMngr = new MsoChannelManager();		
		List<MsoChannel> channels = channelMngr.findMsoDefaultChannels(user.getMsoKey());		
		SubscriptionManager subManager = new SubscriptionManager();
		for (MsoChannel c : channels) {
			subManager.channelSubscribe(user, c, c.getSeq(), c.getType());
		}
		log.info("subscribe to " + channels.size() + " of channels by user:" + user.getKey() + "(mso is " + user.getMsoKey() + ")");
	}
			
	public NnUser findByEmailAndMso(String email, Mso mso) {
		return nnUserDao.findByEmailAndMsoKey(email.toLowerCase(), mso.getKey());
	}
	
	public NnUser findByKey(Key key) {
		return nnUserDao.findByKey(key);
	}

	public NnUser findByKeyStr(String key) {
		try {
			return this.findByKey(KeyFactory.stringToKey(key));
		} catch (IllegalArgumentException e) {
			log.info("invalid key string");
			return null;
		}
	}
	
}
