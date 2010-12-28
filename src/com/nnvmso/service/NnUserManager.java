package com.nnvmso.service;

import java.util.Arrays;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.nnvmso.lib.*;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.Ipg;

@Service
public class NnUserManager {
	
	public void setUserCookie(HttpServletResponse resp, String userKey) {
		CookieHelper.setCookie(resp, "user", userKey);
	}
	
	// ============================================================
	// find
	// ============================================================
	public NnUser nnUserAuthenticated(String email, String password) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(NnUser.class);
		query.setFilter("email == '" + email + "'");	
		@SuppressWarnings("unchecked")
		List<NnUser> results = (List<NnUser>) query.execute();
		NnUser user = null;
		if (results.size() > 0) {
			user = results.get(0);		
			byte[] proposedDigest = AuthLib.passwordDigest(password, user.getSalt());
			if (!Arrays.equals(user.getCryptedPassword(), proposedDigest)) {				
				user = null;
			}
			pm.close();
		}
		return user;
	}
	
	public List<NnUser> findAll() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		String query = "select from " + NnUser.class.getName() + " order by createDate";
		@SuppressWarnings("unchecked")
		List<NnUser> users = (List<NnUser>) pm.newQuery(query).execute();
		users.size(); //touch
		pm.close();
		return users;		
	}
	
	public NnUser findByEmail(String email) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(NnUser.class);
		query.setFilter("email == '" + email + "'");	
		@SuppressWarnings("unchecked")
		List<NnUser> results = (List<NnUser>) query.execute();
		NnUser user = null;
		if (results.size() > 0) {
			user = results.get(0);			
		}
		pm.close();
		return user;
	}
	
	public NnUser findByKey(String key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		NnUser user = null;
		try {
			user = pm.getObjectById(NnUser.class, key);
		} catch (JDOObjectNotFoundException e) {
		}
		pm.close();
		return user;
	}

	// ============================================================
	// c.u.d
	// ============================================================
	public NnUser createViaPlayer(NnUser user) {
		//find user's mso
		MsoManager msoMngr = new MsoManager();
		msoMngr.findByEmail("default_mso@9x9.com");
		Mso mso = new Mso();		
		this.save(user, mso);
		//subscribe default channel
		SubscriptionManager sMngr = new SubscriptionManager();
		ChannelManager cMngr = new ChannelManager();
		MsoChannel system = cMngr.findSystemChannels().get(0);
		sMngr.channelSubscribe(user, system, (short)1);
		//return
		return user;		
	}
	
	// ============================================================
	// c.u.d <--- What does this mean ?
	// ============================================================
	// Use IPG parameter instead of system IPG
	public NnUser createViaPlayer(NnUser user, Ipg ipg) {
		//find user's mso
		MsoManager msoMngr = new MsoManager();
		msoMngr.findByEmail("default_mso@9x9.com");
		Mso mso = new Mso();
		this.save(user, mso);
		//subscribe IPG channel
		IpgManager ipgMngr = new IpgManager();
		SubscriptionManager sMngr = new SubscriptionManager();
		List<MsoChannel> ipgChannels = ipgMngr.findIpgChannels(ipg);
		for (MsoChannel c : ipgChannels)
			sMngr.channelSubscribe(user, c, c.getGrid());
		//return
		return user;
	}
	
	public void save(NnUser user, Mso mso) {
		user.setSalt(AuthLib.generateSalt());
		user.setCryptedPassword(AuthLib.encryptPassword(user.getPassword(), user.getSalt()));
		user.setMsoKey(mso.getKey());
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(user);
		pm.close();
	}
	
	public void updateUser(NnUser user) {
		MsoManager msoMngr = new MsoManager();
		msoMngr.findByEmail("default_mso@9x9.com");
		Mso mso = new Mso();		
		this.save(user, mso);
	}

}
