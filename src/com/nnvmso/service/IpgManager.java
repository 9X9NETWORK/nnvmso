package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import com.nnvmso.dao.IpgDao;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.Subscription;

public class IpgManager {

	protected static final Logger log = Logger.getLogger(IpgDao.class.getName());
	
	private IpgDao ipgDao = new IpgDao();	
	
	public void create(Ipg ipg, long userId) {
		Date now = new Date();
		ipg.setUpdateDate(now);
		ipg.setCreateDate(now);
		ipg.setUserId(userId);

		Hashtable<Integer,Long> channels = new Hashtable<Integer, Long>();
		SubscriptionManager sMngr = new SubscriptionManager();		
		List<Subscription> subscriptions = sMngr.findAllByUser(userId);		
		int size = subscriptions.size();
		for (int i = 0; i < size; i++) {
			Subscription s = subscriptions.get(i);
			System.out.println("grid:" + s.getSeq());
			System.out.println("subscribed channel id:" + s.getChannelId());
			channels.put(new Integer(s.getSeq()), s.getChannelId());
		}
		ipg.setChannels(channels);
		log.info("new IPG size: " + channels.size());		
		ipgDao.save(ipg);
	}	
	
	public Ipg save(Ipg ipg) {
		return ipgDao.save(ipg);
	}	
	
	public Ipg findById(Long id) {
		return ipgDao.findById(id);
	}	
	
	public List<Ipg> findByUserId(long userId) {
		return ipgDao.findByUserId(userId);
	}
	
	public List<MsoProgram> findIpgPrograms(Ipg ipg) {
		Hashtable<Integer,Long> hashTable = ipg.getChannels();
		MsoProgramManager programMngr = new MsoProgramManager();
		List<Long> channelIds = new ArrayList<Long>();		
		int i = 0;		
		for (Enumeration<Integer> e = hashTable.keys(); e.hasMoreElements(); i++) {
			Integer grid = (Integer)e.nextElement();
			channelIds.add((long)hashTable.get(grid));
		}		
		return programMngr.findAllByChannelIdsAndIsPublic(channelIds, true);
	}
		
	public List<MsoChannel> findIpgChannels(Ipg ipg) {
		Hashtable<Integer,Long> hash = ipg.getChannels();
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		MsoChannelManager msoChannelMngr = new MsoChannelManager();
		MsoChannel c;
		for (Enumeration<Integer> e = hash.keys(); e.hasMoreElements();) {
			Integer grid = (Integer)e.nextElement();
			c = msoChannelMngr.findById(hash.get(grid));
			if (c != null) {
				c.setSeq(grid.intValue());
				channels.add(c);
			}
		}
		return channels;
	}
	
}