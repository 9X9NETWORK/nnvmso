package com.nnvmso.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.IPGDao;
import com.nnvmso.lib.APILib;
import com.nnvmso.model.IPG;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.Subscription;

@Service
public class IPGManager {

//	@Autowired
//	private IPGDao ipgDao;
	
	public IPG saveCurrentSnapshot(String userKey) {
		NnUserManager userMngr = new NnUserManager();
		SubscriptionManager subMngr = new SubscriptionManager();
		NnUser found = userMngr.findByKey(userKey);	
		List<Key> keys = new ArrayList<Key>();
		List<Short> grids = new ArrayList<Short>();
		IPG ipg = new IPG();
		if (found != null) {
			List<Subscription> subscriptions = subMngr.findAll(found);			
			for (Subscription s : subscriptions) {
				keys.add(s.getChannelKey());
				grids.add(s.getSeq());
			}
			ipg.setChannelKeys(keys);
			ipg.setGrid(grids);
		} else {
			System.out.println("null???");
		}
		IPGDao ipgDao = new IPGDao();
		ipgDao.create(ipg);
		return ipg;
	}
	
	public IPG findById(String id) {
		IPGDao ipgDao = new IPGDao();
		IPG ipg = ipgDao.findById(Long.valueOf(id));
		return ipg;
	}
}
