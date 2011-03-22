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
			log.info("user trying to subscribe a channel that has been subscribed." + channelId);
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
		return true;
	}
	
	public Subscription findByUserIdAndChannelId(long userId, long channelId) {
		Subscription s = subDao.findByUserIdAndChannelId(userId, channelId);
		return s;
	}
	
	public void unsubscribeChannel(Subscription s) {
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
			if ( c!= null) {
				c.setSeq(s.getSeq());
				c.setType(s.getType());
				if (msoId != 0) {
					SubscriptionLog sublog = sublogMngr.findByMsoIdAndChannelId(msoId, c.getKey().getId());			
					if (sublog != null) {c.setSubscriptionCount(sublog.getCount());}
				}			
				channels.add(c);
			}
		}
		return channels;			 
	}

	//move from seq1 to seq2
	public boolean moveSeq(long userId, int seq1, int seq2) {						
		Subscription sub = subDao.findByUserIdAndSeq(userId, seq1);
		if (sub == null) {return false;}
		sub.setSeq(seq2);
		subDao.save(sub);
		return true;
	}
	
	public List<Subscription> list(int page, int limit, String sidx, String sord) {
		return subDao.list(page, limit, sidx, sord);
	}
	
	public List<Subscription> list(int page, int limit, String sidx, String sord, String filter) {
		return subDao.list(page, limit, sidx, sord, filter);
	}
	
	public int total() {
		return subDao.total();
	}
	
	public int total(String filter) {
		return subDao.total(filter);
	}
	
}
