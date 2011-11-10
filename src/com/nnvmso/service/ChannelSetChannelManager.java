package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.ChannelSetChannelDao;
import com.nnvmso.model.ChannelSetChannel;
import com.nnvmso.model.MsoChannel;

@Service
public class ChannelSetChannelManager {
	protected static final Logger logger = Logger.getLogger(ChannelSetChannelManager.class.getName());
	
	ChannelSetChannelDao cscDao = new ChannelSetChannelDao();

	public void saveAll(List<ChannelSetChannel> list) {
		cscDao.saveAll(list);
	}
	
	public void create(ChannelSetChannel csc) {
		Date now = new Date();
		csc.setCreateDate(now);
		csc.setUpdateDate(now);
		cscDao.save(csc);
	}
	
	public void delete(ChannelSetChannel csc) {
		cscDao.delete(csc);
	}

	public void deleteAll(List<ChannelSetChannel> list) {
		cscDao.deleteAll(list);
	}
	
	public ChannelSetChannel save(ChannelSetChannel csc) {
		csc.setUpdateDate(new Date());
		cscDao.save(csc);
		return csc;
	}
	
	public List<ChannelSetChannel> findByChannelSetId(long channelSetId) {
		return cscDao.findByChannelSetId(channelSetId);
	}

	public ChannelSetChannel findBySetAndChannel(long channelSetId, long channelId) {
		return cscDao.findBySetAndChannel(channelSetId, channelId);
	}
	
	
	//move from seq1 to seq2
	public boolean moveSeq(long channelSetId, int seq1, int seq2) {
		ChannelSetChannel csc1 = cscDao.findByChannelSetIdAndSeq(channelSetId, seq1);
		ChannelSetChannel csc2 = cscDao.findByChannelSetIdAndSeq(channelSetId, seq2);
		if (csc1 == null) {return false;}
		csc1.setSeq(seq2);
		this.save(csc1);
		if (csc2 != null) {
			csc2.setSeq(seq1);
			this.save(csc2);
		}
		return true;
	}
	
	public void addChannel(long channelSetId, MsoChannel channel) {
		ChannelSetChannel csc = cscDao.findByChannelSetIdAndSeq(channelSetId, channel.getSeq());
		if (csc == null) {
			csc = new ChannelSetChannel(channelSetId, channel.getKey().getId(), channel.getSeq());
			this.create(csc);
		} else {
			csc.setChannelId(channel.getKey().getId());
			this.save(csc);
		}
	}
	
	public void removeChannel(long channelSetId, int seq) {
		ChannelSetChannel csc = cscDao.findByChannelSetIdAndSeq(channelSetId, seq);
		if (csc != null)
			this.delete(csc);
	}
	
	public List<ChannelSetChannel> findAllByChannelId(long channelId) {
		return cscDao.findAllByChannelId(channelId);
	}
	
	public void appendChannel(long channelSetId, MsoChannel channel) {
		ChannelSetChannel csc = cscDao.findBySetAndChannel(channelSetId, channel.getKey().getId());
		if (csc != null)
			return;
		for (int i = 1; i < 1000; i++) {
			if (cscDao.findByChannelSetIdAndSeq(channelSetId, i) == null) {
				csc = new ChannelSetChannel(channelSetId, channel.getKey().getId(), i);
				this.create(csc);
				break;
			}
		}
	}
}
