package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.MsoIpgDao;
import com.nnvmso.model.MsoIpg;

@Service
public class MsoIpgManager {
	protected static final Logger log = Logger.getLogger(MsoIpgManager.class.getName());
	
	private MsoIpgDao msoIpgDao = new MsoIpgDao();
 
	/**
	 * @@@ IMPORTANT: 
	 * Check channel status is your responsibility (for now, for quick initialization work).
	 * Please use MsoChannelManager, isCounterQualified(MsoChannel channel) 
	 */
	public void create(MsoIpg msoIpg) {
		Date now = new Date();
		msoIpg.setCreateDate(now);
		msoIpg.setUpdateDate(now);
		this.save(msoIpg);
	}
	
	public void save(MsoIpg msoIpg) {
		msoIpg.setUpdateDate(new Date());
		msoIpgDao.save(msoIpg);
	}
	
	public void deleteMsoIpg(long msoId, long channelId) {
		MsoIpg msoIpg = msoIpgDao.findByMsoIdAndChannelId(msoId, channelId);
		if (msoIpg != null) { msoIpgDao.delete(msoIpg); }
	}
	
	public List<MsoIpg> findAllByMsoId(long msoId) {
		return msoIpgDao.findAllByMsoId(msoId);
	}
	
	public MsoIpg findByMsoIdAndChannelId(long msoId, long channelId) {
		return msoIpgDao.findByMsoIdAndChannelId(msoId, channelId);
	}
	
	public MsoIpg findByMsoIdAndSeq(long msoId, int seq) {
		return msoIpgDao.findByMsoIdAndSeq(msoId, seq);
	}
}
