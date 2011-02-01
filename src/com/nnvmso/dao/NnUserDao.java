package com.nnvmso.dao;

import java.util.Arrays;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.mortbay.log.Log;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.AuthLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.NnUser;

public class NnUserDao {		
	public NnUser save(NnUser user) {
		if (user == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(user);
		user = pm.detachCopy(user);
		pm.close();		
		return user;
	}

	public NnUser findAuthenticatedUser(String email, String password, long msoId) {
		Log.info("email=" + email + ";password=" + password + ";msoId:" + msoId);
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(NnUser.class);
		query.setFilter("email == emailParam && msoId == msoIdParam");
		query.declareParameters("String emailParam, long msoIdParam");				
		@SuppressWarnings("unchecked")
		List<NnUser> results = (List<NnUser>) query.execute(email, msoId);
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
		 
	public NnUser findByEmailAndMsoId(String email, long msoId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(NnUser.class);
		query.setFilter("email == emailParam && msoId == msoIdParam");
		query.declareParameters("String emailParam, " + Key.class.getName() + " msoIdParam");		
		@SuppressWarnings("unchecked")
		List<NnUser> results = (List<NnUser>) query.execute(email, msoId);
		NnUser user = null;
		if (results.size() > 0) {
			user = results.get(0);			
		}
		user = pm.detachCopy(user);
		pm.close();
		return user;				
	}

	public NnUser findByToken(String token) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(NnUser.class);
		query.setFilter("token == tokenParam");
		query.declareParameters("String tokenParam");		
		@SuppressWarnings("unchecked")
		List<NnUser> results = (List<NnUser>) query.execute(token);
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
