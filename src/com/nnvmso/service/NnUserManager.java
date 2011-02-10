package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.NnUserDao;
import com.nnvmso.lib.AuthLib;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;

@Service
public class NnUserManager {
	
	protected static final Logger log = Logger.getLogger(NnUserManager.class.getName());
		
	private NnUserDao nnUserDao = new NnUserDao();
	
	//@@@IMPORTANT email duplication is your responsibility
	public void create(NnUser user) {
		user.setName(user.getName().replaceAll("\\s", " "));
		user.setEmail(user.getEmail().toLowerCase());
		user.setToken(this.generateToken());
		Date now = new Date();
		user.setCreateDate(now);
		user.setUpdateDate(now);
		nnUserDao.save(user);
	}

	public NnUser save(NnUser user) {
		if (user.getPassword() != null) {
			user.setSalt(AuthLib.generateSalt());
			user.setCryptedPassword(AuthLib.encryptPassword(user.getPassword(), user.getSalt()));
		}
		user.setEmail(user.getEmail().toLowerCase());
		user.setUpdateDate(new Date());
		return nnUserDao.save(user);
	}

	/**
	 * GAE can only write 5 records a sec, maybe safe enough to do so w/out DB retrieving.
	 * taking the chance to speed up signin (meaning not to consult DB before creating the account).
	 */
	private String generateToken() {
		String time = String.valueOf(new Date().getTime());
		String random = RandomStringUtils.randomAlphabetic(10);
		String result = time + random;
		result = RandomStringUtils.random(20, 0, 20, true, true, result.toCharArray());
		return result;
	}	
	
	public List<NnUser> findByType(short type) {
		return nnUserDao.findByType(type);
	}
	
	public NnUser findNNUser() {
		List<NnUser> users = nnUserDao.findByType(NnUser.TYPE_NN);
		if (users.size() > 0) {return nnUserDao.findByType(NnUser.TYPE_NN).get(0); }
		return null;
	}
	
	public NnUser findAuthenticatedUser(String email, String password, long msoId) {
		return nnUserDao.findAuthenticatedUser(email.toLowerCase(), password, msoId);
	}
	
	public void subscibeDefaultChannels(NnUser user) {
		MsoChannelManager channelMngr = new MsoChannelManager();		
		List<MsoChannel> channels = channelMngr.findMsoDefaultChannels(user.getMsoId());		
		SubscriptionManager subManager = new SubscriptionManager();
		for (MsoChannel c : channels) {
			subManager.subscribeChannel(user.getKey().getId(), c.getKey().getId(), c.getSeq(), c.getType(), user.getMsoId());
		}
		log.info("user " +  user.getKey().getId() + "(" + user.getToken() + ") subscribe " + channels.size() + " channels (mso:" + user.getMsoId() + ")");
	}
			
	public NnUser findByEmailAndMso(String email, Mso mso) {
		return nnUserDao.findByEmailAndMsoId(email.toLowerCase(), mso.getKey().getId());
	}

	public NnUser findByToken(String token) {
		return nnUserDao.findByToken(token);
	}
	
	public NnUser findByKey(Key key) {
		return nnUserDao.findByKey(key);
	}
	
}
