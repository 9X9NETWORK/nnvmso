package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.ChannelAutosharingDao;
import com.nnvmso.dao.ChannelSetAutosharingDao;
import com.nnvmso.model.ChannelAutosharing;
import com.nnvmso.model.ChannelSetAutosharing;

@Service
public class AutosharingService {
	
	protected static final Logger logger = Logger.getLogger(AutosharingService.class.getName());
	
	private ChannelAutosharingDao channelAutosharingDao = new ChannelAutosharingDao();
	private ChannelSetAutosharingDao channelSetAutosharingDao = new ChannelSetAutosharingDao();
	
	///////// channel autosharing /////////
	
	public List<ChannelAutosharing> findAllByChannelId(long channelId) {
		return channelAutosharingDao.findAllByChannelId(channelId);
	}
	
	public List<ChannelAutosharing> findAllByChannelIdAndMsoId(long channelId, long msoId) {
		return channelAutosharingDao.findAllByChannelIdAndMsoId(channelId, msoId);
	}
	
	public boolean isChannelAutosharedByMso(long msoId, long channelId, short type) {
		return channelAutosharingDao.isChannelAutosharedByMso(msoId, channelId, type);
	}
	
	public ChannelAutosharing findChannelAutosharing(long msoId, long channelId, short type) {
		return channelAutosharingDao.findChannelAutosharing(msoId, channelId, type);
	}
	
	public void save(ChannelAutosharing autosharing) {
		channelAutosharingDao.save(autosharing);
	}
	
	public void create(ChannelAutosharing autosharing) {
		autosharing.setCreateDate(new Date());
		channelAutosharingDao.save(autosharing);
	}
	
	public void delete(ChannelAutosharing autosharing) {
		channelAutosharingDao.delete(autosharing);
	}
	
	///////// channel set autosharing /////////
	
	public List<ChannelSetAutosharing> findAllByChannelSetId(long channelSetId) {
		return channelSetAutosharingDao.findAllByChannelSetId(channelSetId);
	}
	
	public List<ChannelSetAutosharing> findAllByChannelSetIdAndMsoId(long channelSetId, long msoId) {
		return channelSetAutosharingDao.findAllByChannelSetIdAndMsoId(channelSetId, msoId);
	}
	
	public ChannelSetAutosharing findChannelSetAutosharing(long msoId, long channelSetId, short type) {
		return channelSetAutosharingDao.findChannelSetAutosharing(msoId, channelSetId, type);
	}
	
	public void create(ChannelSetAutosharing autosharing) {
		autosharing.setCreateDate(new Date());
		channelSetAutosharingDao.save(autosharing);
	}
	
	public void delete(ChannelSetAutosharing autosharing) {
		channelSetAutosharingDao.delete(autosharing);
	}
	
	public List<ChannelAutosharing> findAllByChannelIdAndType(long channelId, short type) {
		return channelAutosharingDao.findAllByChannelIdAndType(channelId, type);
		
	}

	public List<ChannelAutosharing> findAllChannelsByMsoId(long msoId) {
		return channelAutosharingDao.findAllChannelsByMsoId(msoId);
	}
}
