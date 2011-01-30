package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.model.*;
import com.nnvmso.dao.*;

@Service
public class SubscriptionManager {		

	protected static final Logger log = Logger.getLogger(SubscriptionManager.class.getName());
	
	SubscriptionDao subDao = new SubscriptionDao(); 

	public List<Subscription> findAllByUser(long userId) {
		return subDao.findAllByUserId(userId);
	}
	
	public boolean subscribeChannel(long userId, long channelId, int seq, short type) {
		Subscription existed = subDao.findByUserIdAndChannelId(userId, channelId);
		if (existed != null) {
			log.info("user trying to subscribe a channel that has been subscribed.");
			return false;
		}
		Subscription s = new Subscription(userId, channelId, seq, type);
		Date now = new Date();
		s.setCreateDate(now);
		s.setUpdateDate(now);
		subDao.save(s);
		log.info("user subscribe a new channel " + channelId + ";grid=" + seq);
		return true;
	}
	
	public void unsubscribeChannel(long userId, long channelId) {
		Subscription s = subDao.findByUserIdAndChannelId(userId, channelId);
		if (s != null) { subDao.delete(s); }
	}
	
	//!!! findByIds
	public List<MsoChannel> findSubscribedChannels(long userId) {
		List<Subscription> subs = subDao.findAllByUserId(userId);
		System.out.println("subscription: " + subs.size());
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		MsoChannelDao channelDao = new MsoChannelDao();		
		for (Subscription s : subs) {
			MsoChannel c = channelDao.findById(s.getChannelId()); //!!!
			c.setSeq(s.getSeq());
			c.setType(s.getType());
			channels.add(c);
		}
		return channels;			 
	}

	public boolean changeSeq(long userId, int seq1, int seq2) {
		List<Subscription> subs = subDao.findAllByUserIdAndSeq(userId, seq1, seq2);
		if (subs.size() != 2) {
			log.info("Chagne seq does not find matching data. User key=" + userId + "; seq1=" + seq1 + "; seq2=" + seq2);
			return false;
		}
		System.out.println("subs1=" + subs.get(0).getKey().getId() + ";" + subs.get(0).getSeq() + ";subs2=" + subs.get(1).getKey().getId() + ";" + subs.get(1).getSeq());
		if (subs.get(0).getSeq() == seq1) {
			subs.get(0).setSeq(seq2);
			subs.get(1).setSeq(seq1);
		} else {
			subs.get(0).setSeq(seq1);
			subs.get(1).setSeq(seq2);
		}
		subDao.saveAll(subs);
		return true;
	}
}
