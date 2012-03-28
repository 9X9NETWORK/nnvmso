package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.AuthLib;
import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.service.MsoManager;

public class NnUserDao extends GenericDao<NnUser> {

	protected static final Logger log = Logger.getLogger(NnUserDao.class.getName());
	
	public NnUserDao() {
		super(NnUser.class);
	}

	public NnUser findById(long id) {
		PersistenceManager pm = NnUserDao.getPersistenceManager((short) 1, null);
		NnUser detached = null;
		try {
			NnUser user = (NnUser)pm.getObjectById(NnUser.class, id);
			if (user == null)
				pm = NnUserDao.getPersistenceManager((short)2, null);
			detached = (NnUser)pm.detachCopy(user);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return detached;		
	}	
	
	//use either shard or token to determine partition, default shard 1 if nothing
	public static PersistenceManager getPersistenceManager(short shard, String token) {
		if (shard != 0) {
			if (shard != 1)
				return PMF.getNnUser2().getPersistenceManager();
			else 
				return PMF.getNnUser1().getPersistenceManager();
		}
		if (token != null) {
			if (token.contains("1-")) {
				return PMF.getNnUser1().getPersistenceManager();
			} else {
				return PMF.getNnUser2().getPersistenceManager();
			}
		}		
		return PMF.getNnUser1().getPersistenceManager();
	}
				
	public NnUser save(NnUser user) {
		if (user == null) {return null;}
		PersistenceManager pm = NnUserDao.getPersistenceManager(user.getShard(), user.getToken());
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
		PersistenceManager pm = NnUserDao.getPersistenceManager(NnUser.SHARD_DEFAULT, null);
		MsoManager msoMngr = new MsoManager();
		long nnId = msoMngr.findNNMso().getId();
		try {
			Query query = pm.newQuery(NnUser.class);
			query.setFilter("email == emailParam && msoId == msoIdParam && type == typeParam");
			query.declareParameters("String emailParam, long msoIdParam, short typeParam");
			List<NnUser> results = (List<NnUser>) query.execute(email, msoId, NnUser.TYPE_3X3);
			if (results.size() > 0) {
				user = results.get(0);		
			} else {
				// make 9x9 and 5F login as possible
				results = (List<NnUser>) query.execute(email, nnId, NnUser.TYPE_NN);  // super account !!!
				if (results.size() > 0) {
					user = results.get(0);
				} else {
					results = (List<NnUser>) query.execute(email, msoId, NnUser.TYPE_TBC);
					if (results.size() > 0) {
						user = results.get(0);
					} else {
						results = (List<NnUser>) query.execute(email, msoId, NnUser.TYPE_ENTERPRISE);
						if (results.size() > 0) {
							user = results.get(0);
						}
					}
				}
			}
			if (user != null) {
				byte[] proposedDigest = AuthLib.passwordDigest(password, user.getSalt());
				if (!Arrays.equals(user.getCryptedPassword(), proposedDigest)) {				
					user = null;
				} else {
					user = pm.detachCopy(user);
				}
			}
		} finally {
			pm.close();
		}
		return user;		
	}
	
	public NnUser findAuthenticatedUser(String email, String password, short shard) {
		NnUser user = null;
		PersistenceManager pm = NnUserDao.getPersistenceManager(shard, null);
		try {
			Query query = pm.newQuery(NnUser.class);
			query.setFilter("email == emailParam");
			query.declareParameters("String emailParam");				
			@SuppressWarnings("unchecked")
			List<NnUser> results = (List<NnUser>) query.execute(email);
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
	
	public List<NnUser> findByType(short type) {
		List<NnUser> detached = new ArrayList<NnUser>();
		PersistenceManager pm = NnUserDao.getPersistenceManager(NnUser.SHARD_DEFAULT, null);
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
	
	public NnUser findByToken(String token) {
		NnUser user = null;
		PersistenceManager pm = NnUserDao.getPersistenceManager((short)0, token);
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

	public NnUser findNNUser() {
		List<NnUser> detached = new ArrayList<NnUser>();
		PersistenceManager pm = NnUserDao.getPersistenceManager((short)1, null);
		try {
			Query query = pm.newQuery(NnUser.class);
			query.setFilter("type == " + NnUser.TYPE_NN);	
			@SuppressWarnings("unchecked")
			List<NnUser> users = (List<NnUser>) query.execute(NnUser.TYPE_NN);
			detached = (List<NnUser>)pm.detachCopyAll(users);
		} finally {
			pm.close();
		}
		if (detached != null)
			return detached.get(0);
		return null;		
	}
	
	public NnUser findByEmail(String email, short shard) {
		NnUser user = null;
		PersistenceManager pm = NnUserDao.getPersistenceManager(shard, null);
		try {
			Query query = pm.newQuery(NnUser.class);
			query.setFilter("email == emailParam");
			query.declareParameters("String emailParam");		
			@SuppressWarnings("unchecked")
			List<NnUser> results = (List<NnUser>) query.execute(email);
			if (results.size() > 0) {
				user = results.get(0);			
			}
			user = pm.detachCopy(user);
		} finally {
			pm.close();
		}
		return user;				
	}	
	
	public List<NnUser> findByTypeAndMso(Short type, Long msoId) {
		List<NnUser> detached = new ArrayList<NnUser>();
		PersistenceManager pm = NnUserDao.getPersistenceManager(NnUser.SHARD_DEFAULT, null);
		try {
			Query query = pm.newQuery(NnUser.class);
			query.setFilter("type == typeParam && msoId == msoIdParam");
			query.declareParameters("short typeParam, long msoIdParam");
			@SuppressWarnings("unchecked")
			List<NnUser> results = (List<NnUser>) query.execute(type, msoId);
			if (results.size() > 0) {
				detached = (List<NnUser>) pm.detachCopyAll(results);
			}
		} finally {
			pm.close();
		}
		return detached;
	}
	
}
