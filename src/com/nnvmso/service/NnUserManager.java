package com.nnvmso.service;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.springframework.stereotype.Service;

import com.nnvmso.lib.*;
import com.nnvmso.model.Mso;
import com.nnvmso.model.NnUser;

@Service
public class NnUserManager {
	public NnUser findByEmail(String email) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(NnUser.class);
		query.setFilter("email == '" + email + "'");	
		List<NnUser> results = (List<NnUser>) query.execute();		
		NnUser user = results.get(0);
		return user;
	}
	
	public NnUser findByKey(String key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		NnUser user = pm.getObjectById(NnUser.class, key);
		return user;
	}

	// ============================================================
	// c.u.d
	// ============================================================		
	public void create(NnUser user, Mso mso) {
		user.setSalt(AuthLib.generateSalt());
		user.setCryptedPassword(AuthLib.encryptPassword(user.getPassword(), user.getSalt()));
		user.setMsoKey(mso.getKey());
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(user);
		pm.close();
	}
	
	
}
