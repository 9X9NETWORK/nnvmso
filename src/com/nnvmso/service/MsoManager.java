package com.nnvmso.service;

import java.util.Arrays;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.springframework.stereotype.Service;

import com.nnvmso.lib.PMF;
import com.nnvmso.lib.AuthLib;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;

@Service
public class MsoManager {
	public void create(Mso mso) {
		mso.setSalt(AuthLib.generateSalt());
		mso.setCryptedPassword(AuthLib.encryptPassword(mso.getPassword(), mso.getSalt()));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(mso);
		pm.close();
	}
	
	public List<Mso> findAll() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		String query = "select from " + Mso.class.getName() + " order by createDate";
		List<Mso> msos = (List<Mso>) pm.newQuery(query).execute();
		msos.size(); //touch
		pm.close();
		return msos;
	}
	
	public Mso findByKey(String key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Mso mso = pm.getObjectById(Mso.class, key);
		pm.close();
		return mso;
	}

	public Mso findByEmail(String email) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(Mso.class);
		query.setFilter("email == '" + email + "'");	
		List<Mso> results = (List<Mso>) query.execute();		
		Mso mso = results.get(0);
		return mso;
	}
	
	public Mso msoAuthenticated(String email, String password) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(Mso.class);
		query.setFilter("email == '" + email + "'");		
		List<Mso> results = (List<Mso>) query.execute();
		Mso mso = null;
		if (results.size() > 0) {
			mso = results.get(0);		
			byte[] proposedDigest = AuthLib.passwordDigest(password, mso.getSalt());
			if (!Arrays.equals(mso.getCryptedPassword(), proposedDigest)) {				
				mso = null;
			}
			pm.close();
		}
		return mso;
	}	
}
