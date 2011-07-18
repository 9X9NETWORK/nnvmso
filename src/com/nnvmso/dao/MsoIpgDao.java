package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.MsoIpg;

public class MsoIpgDao extends GenericDao<MsoIpg> {
	
	protected static final Logger logger = Logger.getLogger(MsoIpgDao.class.getName());
	
	public MsoIpgDao() {
		super(MsoIpg.class);
	}
	
	public MsoIpg save(MsoIpg msoIpg) {
		if (msoIpg == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			logger.info("save msoIpg (msoId = " + msoIpg.getMsoId() + ", channelId = " + msoIpg.getChannelId() + ")");
			pm.makePersistent(msoIpg);
			msoIpg = pm.detachCopy(msoIpg);
		} finally {
			pm.close();
		}
		return msoIpg;
	}
	
	public void delete(MsoIpg msoIpg) {
		if (msoIpg != null) {
			logger.info("delete msoIpg (msoId = " + msoIpg.getMsoId() + ", channelId = " + msoIpg.getChannelId() + ")");
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				pm.deletePersistent(msoIpg);
			} finally {
				pm.close();
			}
		}
	}

	//add isPublic, didn't make it consistent naming convention
	public List<MsoIpg> findAllByMsoId(long msoId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<MsoIpg> detached = new ArrayList<MsoIpg>(); 
		try {
			Query q = pm.newQuery(MsoIpg.class);
			q.setFilter("msoId == msoIdParam && isPublic == isPublicParam");
			q.declareParameters("long msoIdParam, boolean isPublicParam");
			q.setOrdering("seq asc");
			@SuppressWarnings("unchecked")
			List<MsoIpg> ipg = (List<MsoIpg>)q.execute(msoId, true);
			detached = (List<MsoIpg>)pm.detachCopyAll(ipg);
		} finally {
			pm.close();
		}
		return detached;
	}
	
	public MsoIpg findByMsoIdAndChannelId(long msoId, long channelId) {
		MsoIpg msoIpg = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(MsoIpg.class);
			q.setFilter("msoId == msoIdParam && channelId== channelIdParam");
			q.setOrdering("seq asc");
			q.setRange(0, 1);
			q.declareParameters("long msoIdParam, long channelIdParam");
			@SuppressWarnings("unchecked")
			List<MsoIpg> results = (List<MsoIpg>)q.execute(msoId, channelId);
			if (results.size() > 0) {
				msoIpg = results.get(0);
				msoIpg = pm.detachCopy(msoIpg);
			}
		} finally {
			pm.close();
		}
		return msoIpg;
	}
	
	public MsoIpg findByMsoIdAndSeq(long msoId, int seq) {
		MsoIpg msoIpg = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(MsoIpg.class);
			q.setFilter("msoId == msoIdParam && seq == seqParam");
			q.declareParameters("long msoIdParam, int seqParam");
			@SuppressWarnings("unchecked")
			List<MsoIpg> results = (List<MsoIpg>)q.execute(msoId, seq);
			if (results.size() > 0) {
				msoIpg = results.get(0);
				msoIpg = pm.detachCopy(msoIpg);
			}
		} finally {
			pm.close();
		}
		return msoIpg;
	}
}
