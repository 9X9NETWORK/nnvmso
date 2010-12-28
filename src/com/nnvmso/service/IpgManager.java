package com.nnvmso.service;

import java.lang.Long;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;
import java.text.DateFormat;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.JDOObjectNotFoundException;

import org.springframework.stereotype.Service;

import com.nnvmso.model.NnUser;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.Subscription;
import com.nnvmso.lib.PMF;
import com.nnvmso.service.ChannelManager;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Key;

@Service
public class IpgManager {
	
	public Ipg findById(Long id) {
		System.out.println("IpgManager.findById(" + id + ")");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Ipg ipg = null, detached = null;
		try {
			DateFormat df = DateFormat.getDateInstance();
			ipg = (Ipg)pm.getObjectById(Ipg.class, id);
			System.out.println("ipg channel count: " + ipg.getChannels().size()); // touch
			System.out.println("ipg owner: " + ipg.getUser().getName());
			System.out.println("ipg date: " + df.format(ipg.getCreateDate()));
			detached = (Ipg)pm.detachCopy(ipg);
		} catch (JDOObjectNotFoundException e) {
		}
		pm.close();
		return detached;
	}
	
	// NOTE: this function may not be functional yet
	public List<Ipg> findByUser(NnUser user) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Ipg.class);
		//query.setFilter("user == userParam");
		//query.declareParameters("NnUser userParam");
		List<Ipg> results = (List<Ipg>)query.execute();
		System.out.println("ipg count = " + results.size());
		pm.close();
		return results;
	}
	
	public List<MsoProgram> findIpgPrograms(Ipg ipg) {
		Hashtable<Short,Key> hashTable = ipg.getChannels();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Key[] channelKeys = new Key[hashTable.size()];
		int i = 0;
		for (Enumeration e = hashTable.keys(); e.hasMoreElements(); i++) {
			Short grid = (Short)e.nextElement();
			channelKeys[i] = (Key)hashTable.get(grid);
		}
		Query q = pm.newQuery(MsoProgram.class, ":p.contains(channelKey)");
		List<MsoProgram> programs = (List<MsoProgram>) q.execute(Arrays.asList(channelKeys));
		List<MsoProgram> results = new ArrayList<MsoProgram>();
		results.addAll(programs);
		Iterator<MsoProgram> iter = results.iterator();
		while(iter.hasNext()) {
			MsoProgram p = iter.next();
			if (!p.isPublic()) {
				iter.remove();
			}
		}
		pm.close();
		return results;
	}
	
	public List<MsoChannel> findIpgChannels(Ipg ipg) {
		Hashtable<Short,Key> hash = ipg.getChannels();
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		ChannelManager cMngr = new ChannelManager();
		MsoChannel c;
		for (Enumeration e = hash.keys(); e.hasMoreElements();) {
			Short grid;
			try {
				grid = (Short)e.nextElement();
				c = cMngr.findByKey(KeyFactory.keyToString((Key)hash.get(grid)));
			} catch (JDOObjectNotFoundException ex) {
				continue;
			}
			c.setGrid(grid.shortValue());
			channels.add(c);
		}
		return channels;
	}
	
	public void save(Ipg ipg) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(ipg);
		pm.close();
	}
}
