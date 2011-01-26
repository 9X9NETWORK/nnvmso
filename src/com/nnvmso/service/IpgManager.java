package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.IpgDao;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;

public class IpgManager {

	protected static final Logger log = Logger.getLogger(IpgDao.class.getName());
	
	private IpgDao ipgDao = new IpgDao();	
	
	public void create(Ipg ipg) {
		ipgDao.create(ipg);
	}	
	
	public Ipg save(Ipg ipg) {
		return ipgDao.save(ipg);
	}	
	
	public Ipg findById(Long id) {
		return ipgDao.findById(id);
	}	
	
	public List<Ipg> findByUser(NnUser user) {
		return ipgDao.findByUser(user);
	}
	
	public List<MsoProgram> findIpgPrograms(Ipg ipg) {
		Hashtable<Short,Key> hashTable = ipg.getChannels();
		MsoProgramManager programMngr = new MsoProgramManager();
		Key[] channelKeys = new Key[hashTable.size()];
		int i = 0;
		for (Enumeration<Short> e = hashTable.keys(); e.hasMoreElements(); i++) {
			Short grid = (Short)e.nextElement();
			channelKeys[i] = (Key)hashTable.get(grid);
		}		
		return programMngr.findAllByKeys(channelKeys);		
	}
	
	
	public List<MsoChannel> findIpgChannels(Ipg ipg) {
		Hashtable<Short,Key> hash = ipg.getChannels();
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		MsoChannelManager msoChannelMngr = new MsoChannelManager();
		MsoChannel c;
		for (Enumeration<Short> e = hash.keys(); e.hasMoreElements();) {
			Short grid;
			grid = (Short)e.nextElement();
			c = msoChannelMngr.findByKey(hash.get(grid));
			if (c != null) {
				c.setSeq(grid.shortValue());
				channels.add(c);
			}
		}
		return channels;
	}
	
}
