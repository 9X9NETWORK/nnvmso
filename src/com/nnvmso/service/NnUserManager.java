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
	public void create(NnUser user, String token) {
		user.setName(user.getName().replaceAll("\\s", " "));
		user.setEmail(user.getEmail().toLowerCase());
		if (token == null)
			user.setToken(NnUserManager.generateToken());
		else 
			user.setToken(token);
		if (user.getPassword() != null) {
			user.setSalt(AuthLib.generateSalt());
			user.setCryptedPassword(AuthLib.encryptPassword(user.getPassword(), user.getSalt()));
		}		
		Date now = new Date();
		user.setCreateDate(now);
		user.setUpdateDate(now);
		nnUserDao.save(user);
	}

	public NnUser resetPassword(NnUser user) {
		user.setPassword(user.getPassword());
		user.setSalt(AuthLib.generateSalt());
		user.setCryptedPassword(AuthLib.encryptPassword(user.getPassword(), user.getSalt()));
		this.save(user);
		return user;
	}
	
	public NnUser save(NnUser user) {		
		user.setUpdateDate(new Date());
		return nnUserDao.save(user);
	}

	public void delete(NnUser user) {
		nnUserDao.delete(user);
	}
	
	/**
	 * GAE can only write 5 records a sec, maybe safe enough to do so w/out DB retrieving.
	 * taking the chance to speed up signin (meaning not to consult DB before creating the account).
	 */
	public static String generateToken() {
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
	
	public NnUser findMsoUser(Mso mso) {
		
		if (mso.getType() == Mso.TYPE_NN) {
			return this.findNNUser();
		} else if (mso.getType() == Mso.TYPE_MSO) {
			List<NnUser> users = nnUserDao.findByType(NnUser.TYPE_TBC);
			for (NnUser user : users) {
				if (user.getMsoId() == mso.getKey().getId()) {
					log.info("found TYPE_MSO");
					return user;
				}
			}
		} else if (mso.getType() == Mso.TYPE_3X3) {
			List<NnUser> users = nnUserDao.findByType(NnUser.TYPE_3X3);
			for (NnUser user : users) {
				if (user.getMsoId() == mso.getKey().getId()) {
					log.info("found TYPE_3X3");
					return user;
				}
			}
		} else if (mso.getType() == Mso.TYPE_TCO) {
			List<NnUser> users = nnUserDao.findByTypeAndMsoId(NnUser.TYPE_TCO, mso.getKey().getId());
			if (users.size() > 0)
				log.info("found TYPE_TCO");
				return users.get(0);
		}
		return null;
	}
	
	public NnUser findAuthenticatedUser(String email, String password) {
		return nnUserDao.findAuthenticatedUser(email.toLowerCase(), password);
	}
	
	public NnUser findAuthenticatedMsoUser(String email, String password, long msoId) {
		return nnUserDao.findAuthenticatedMsoUser(email.toLowerCase(), password, msoId);
	}
	
	public void subscibeDefaultChannels(NnUser user) {
		MsoChannelManager channelMngr = new MsoChannelManager();		
		List<MsoChannel> channels = channelMngr.findMsoDefaultChannels(user.getMsoId(), false);		
		SubscriptionManager subManager = new SubscriptionManager();
		for (MsoChannel c : channels) {
			subManager.subscribeChannel(user.getKey().getId(), c.getKey().getId(), c.getSeq(), c.getType(), user.getMsoId());
		}
		log.info("user " +  user.getKey().getId() + "(" + user.getToken() + ") subscribe " + channels.size() + " channels (mso:" + user.getMsoId() + ")");
	}
			
	public NnUser findByEmailAndMso(String email, Mso mso) {
		return nnUserDao.findByEmailAndMsoId(email.toLowerCase(), mso.getKey().getId());
	}

	public List<NnUser> findGuests() {
		return nnUserDao.findGuests();
	}

	public List<NnUser> findAll() {
		return nnUserDao.findAll();
	}
	
	public NnUser findByEmail(String email) {
		return nnUserDao.findByEmail(email.toLowerCase());
	}	
	
	public List<NnUser> findAllByEmail(String email) {
		return nnUserDao.findAllByEmail(email.toLowerCase());
	}	
	
	public NnUser findByToken(String token) {
		return nnUserDao.findByToken(token);
	}
	
	public NnUser findByKey(Key key) {
		return nnUserDao.findByKey(key);
	}

	public NnUser findById(long id) {
		return nnUserDao.findById(id);
	}
	
	public List<NnUser> list(int page, int limit, String sidx, String sord) {
		return nnUserDao.list(page, limit, sidx, sord);
	}
	
	public List<NnUser> list(int page, int limit, String sidx, String sord, String filter) {
		return nnUserDao.list(page, limit, sidx, sord, filter);
	}
	
	public int total() {
		return nnUserDao.total();
	}
	
	public int total(String filter) {
		return nnUserDao.total(filter);
	}
	
}
