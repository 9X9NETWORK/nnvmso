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
	
	public boolean subscribeChannel(long userId, long channelId, int seq, short type, long msoId) {
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
		SubscriptionLogManager sublogMngr = new SubscriptionLogManager();
		SubscriptionLog sublog = sublogMngr.findByMsoIdAndChannelId(msoId, channelId);
		if (sublog == null) {
			sublog = new SubscriptionLog(msoId, channelId);
		} else {
			sublog.setCount(sublog.getCount()+1);
		}
		sublogMngr.save(sublog);		
		log.info("user subscribe a new channel " + channelId + ";grid=" + seq);
		return true;
	}
	
	public void unsubscribeChannel(long userId, long channelId) {
		Subscription s = subDao.findByUserIdAndChannelId(userId, channelId);
		if (s != null) { subDao.delete(s); }
	}
	
	/**
	 * @param msoId used if interested in finding subscription count, pass 0 if not.  	      	    
	 */
	public List<MsoChannel> findSubscribedChannels(long userId, long msoId) {
		List<Subscription> subs = subDao.findAllByUserId(userId);
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		MsoChannelManager channelMngr = new MsoChannelManager();
		SubscriptionLogManager sublogMngr = new SubscriptionLogManager();
		for (Subscription s : subs) {
			MsoChannel c = channelMngr.findById(s.getChannelId()); //!!!
			c.setSeq(s.getSeq());
			c.setType(s.getType());
			if (msoId != 0) {
				SubscriptionLog sublog = sublogMngr.findByMsoIdAndChannelId(msoId, c.getKey().getId());			
				if (sublog != null) {c.setSubscriptionCount(sublog.getCount());}
			}			
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
