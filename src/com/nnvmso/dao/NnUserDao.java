package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.AuthLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.NnUser;

public class NnUserDao extends GenericDao<NnUser> {

	protected static final Logger log = Logger.getLogger(NnUserDao.class.getName());
	
	public NnUserDao() {
		super(NnUser.class);
	}
	
	public NnUser save(NnUser user) {
		if (user == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(user);
			user = pm.detachCopy(user);
		} finally {
			pm.close();
		}
		return user;
	}
	
	// return MSO only
	@SuppressWarnings("unchecked")
	public NnUser findAuthenticatedMsoUser(String email, String password, long msoId) {
		NnUser user = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(NnUser.class);
			query.setFilter("email == emailParam && msoId == msoIdParam && type == typeParam");
			query.declareParameters("String emailParam, long msoIdParam, short typeParam");				
			List<NnUser> results = (List<NnUser>) query.execute(email, msoId, NnUser.TYPE_3X3);
			if (results.size() > 0) {
				user = results.get(0);		
			} else {
				results = (List<NnUser>) query.execute(email, msoId, NnUser.TYPE_NN);
				if (results.size() > 0)
					user = results.get(0);
			}
			if (user != null) {
				user = pm.detachCopy(user);
				byte[] proposedDigest = AuthLib.passwordDigest(password, user.getSalt());
				if (!Arrays.equals(user.getCryptedPassword(), proposedDigest)) {				
					user = null;
				}
			}
		} finally {
			pm.close();
		}
		return user;		
	}
	
	public NnUser findAuthenticatedUser(String email, String password, long msoId) {
		NnUser user = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(NnUser.class);
			query.setFilter("email == emailParam && msoId == msoIdParam");
			query.declareParameters("String emailParam, long msoIdParam");				
			@SuppressWarnings("unchecked")
			List<NnUser> results = (List<NnUser>) query.execute(email, msoId);
			if (results.size() > 0) {
				user = results.get(0);		
				byte[] proposedDigest = AuthLib.passwordDigest(password, user.getSalt());
				if (!Arrays.equals(user.getCryptedPassword(), proposedDigest)) {				
					user = null;
				}
			}
			user = pm.detachCopy(user);
		} finally {
			pm.close();
		}
		return user;		
	}
		 
	public NnUser findByEmailAndMsoId(String email, long msoId) {
		NnUser user = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(NnUser.class);
			query.setFilter("email == emailParam && msoId == msoIdParam");
			query.declareParameters("String emailParam, " + Key.class.getName() + " msoIdParam");		
			@SuppressWarnings("unchecked")
			List<NnUser> results = (List<NnUser>) query.execute(email, msoId);
			if (results.size() > 0) {
				user = results.get(0);			
			}
			user = pm.detachCopy(user);
		} finally {
			pm.close();
		}
		return user;				
	}

	public NnUser findByToken(String token) {
		NnUser user = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(NnUser.class);
			query.setFilter("token == tokenParam");
			query.declareParameters("String tokenParam");		
			@SuppressWarnings("unchecked")
			List<NnUser> results = (List<NnUser>) query.execute(token);
			if (results.size() > 0) {
				user = results.get(0);			
			}
			user = pm.detachCopy(user);
		} finally {
			pm.close();
		}
		return user;				
	}
	
	public List<NnUser> findByType(short type) {
		List<NnUser> detached = new ArrayList<NnUser>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(NnUser.class);
			query.setFilter("type == " + type);	
			@SuppressWarnings("unchecked")
			List<NnUser> users = (List<NnUser>) query.execute(type);
			detached = (List<NnUser>)pm.detachCopyAll(users);
		} finally {
			pm.close();
		}
		return detached;		
	}
	
	public NnUser findById(long id) {
		NnUser user = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			user = pm.getObjectById(NnUser.class, id);
			user = pm.detachCopy(user);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();			
		}
		return user;		
	}	
	
	public NnUser findByKey(Key key) {
		NnUser user = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			user = pm.getObjectById(NnUser.class, key);
			user = pm.detachCopy(user);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();			
		}
		return user;		
	}
	
}
