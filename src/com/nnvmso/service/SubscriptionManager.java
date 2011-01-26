package com.nnvmso.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.nnvmso.model.*;
import com.nnvmso.dao.*;

@Service
public class SubscriptionManager {		

	protected static final Logger log = Logger.getLogger(SubscriptionManager.class.getName());
	
	SubscriptionDao subDao = new SubscriptionDao(); 
	
	//!!! is it necessary to make it boolean, or check if user is null etc
	public boolean channelSubscribe(NnUser user, MsoChannel channel, int seq, short type) {
		if (user == null || channel == null) {return false;}
		Subscription existed = subDao.findByUserKeyAndChannelKey(user.getKey(), channel.getKey());
		if (existed != null) {
			log.info("user trying to subscribe a channel that has been subscribed.");
			return false;
		}
		Subscription s = new Subscription(user.getKey(), channel.getKey(), seq, type);
		subDao.create(s);
		log.info("user subscribe a new channel " + channel.getKey().getId() + ";grid=" + seq);
		return true;
	}
	
	public void channelUnsubscribe(String userKey, long channelId) {
		MsoChannel channel = new MsoChannelDao().findById(channelId);
		try {
			Subscription s = subDao.findByUserKeyAndChannelKey(KeyFactory.stringToKey(userKey), channel.getKey());
			subDao.delete(s);
		} catch (IllegalArgumentException e) { log.info("invalid key string"); }
	}

	public List<MsoChannel> findSubscribedChannels(String userKey) {
		try {
			List<Subscription> subs = subDao.findAllByUserKey(KeyFactory.stringToKey(userKey));
			System.out.println("subscription: " + subs.size());
			List<MsoChannel> channels = new ArrayList<MsoChannel>();
			MsoChannelDao channelDao = new MsoChannelDao();		
			for (Subscription s : subs) {
				MsoChannel c = channelDao.findByKey(s.getChannelKey()); //!!!
				c.setSeq(s.getSeq());
				c.setType(s.getType());
				channels.add(c);
			}
			return channels;
		} catch (IllegalArgumentException e) {
			 log.info("invalid key string");
		}
		return null;			 
	}

	public boolean changeSeq(Key userKey, int seq1, int seq2) {
		List<Subscription> subs = subDao.findAllByUserKeyAndSeq(userKey, seq1, seq2);
		if (subs.size() != 2) {
			log.info("Chagne seq does not find matching data. User key=" + userKey + "; seq1=" + seq1 + "; seq2=" + seq2);
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
