package com.nnvmso.dao;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.AuthLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.NnUser;

public class NnUserDao {
	
	public void create(NnUser user) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Date now = new Date();
		user.setEmail(user.getEmail().toLowerCase());
		user.setCreateDate(now);
		user.setUpdateDate(now);
		pm.makePersistent(user);
		pm.close();		
	}
	
	public NnUser save(NnUser user) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		user.setUpdateDate(new Date());
		user.setSalt(AuthLib.generateSalt());
		user.setCryptedPassword(AuthLib.encryptPassword(user.getPassword(), user.getSalt()));		
		pm.makePersistent(user);
		user = pm.detachCopy(user);
		pm.close();		
		return user;
	}

	public NnUser findAuthenticatedUser(String email, String password, Key msoKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(NnUser.class);
		query.setFilter("email == emailParam && msoKey == msoKeyParam");
		query.declareParameters("String emailParam, " + Key.class.getName() + " msoKeyParam");				
		@SuppressWarnings("unchecked")
		List<NnUser> results = (List<NnUser>) query.execute(email, msoKey);
		NnUser user = null;
		if (results.size() > 0) {
			user = results.get(0);		
			byte[] proposedDigest = AuthLib.passwordDigest(password, user.getSalt());
			if (!Arrays.equals(user.getCryptedPassword(), proposedDigest)) {				
				user = null;
			}
		}
		user = pm.detachCopy(user);
		pm.close();
		return user;		
	}
		 
	public NnUser findByEmailAndMsoKey(String email, Key msoKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(NnUser.class);
		query.setFilter("email == emailParam && msoKey == msoKeyParam");
		query.declareParameters("String emailParam, " + Key.class.getName() + " msoKeyParam");		
		@SuppressWarnings("unchecked")
		List<NnUser> results = (List<NnUser>) query.execute(email, msoKey);
		NnUser user = null;
		if (results.size() > 0) {
			user = results.get(0);			
		}
		user = pm.detachCopy(user);
		pm.close();
		return user;				
	}
	
	public List<NnUser> findByType(short type) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(NnUser.class);
		query.setFilter("type == " + type);	
		@SuppressWarnings("unchecked")
		List<NnUser> users = (List<NnUser>) query.execute(type);
		users = (List<NnUser>)pm.detachCopyAll(users);
		pm.close();
		return users;		
	}
	
	public NnUser findByKey(Key key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		NnUser user = null;
		try {
			user = pm.getObjectById(NnUser.class, key);
			user = pm.detachCopy(user);
		} catch (JDOObjectNotFoundException e) {
		}		
		user = pm.detachCopy(user);
		pm.close();
		return user;		
	}
	
}
