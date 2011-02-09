package com.nnvmso.dao;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.MsoIpg;

public class MsoIpgDao {
		
	public MsoIpg save(MsoIpg msoIpg) {
		if (msoIpg == null) {return null;}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(msoIpg);
		msoIpg = pm.detachCopy(msoIpg);
		pm.close();		
		return msoIpg;
	}
	
	public void delete(MsoIpg msoIpg) {
		if (msoIpg != null) {
			PersistenceManager pm = PMF.get().getPersistenceManager();		
			pm.deletePersistent(msoIpg);
			pm.close();
		}
	}

	public List<MsoIpg> findAllByMsoId(long msoId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(MsoIpg.class);
		q.setFilter("msoId == msoIdParam");
		q.declareParameters("long msoIdParam");
		q.setOrdering("seq asc");
		@SuppressWarnings("unchecked")
		List<MsoIpg> ipg = (List<MsoIpg>)q.execute(msoId);
		ipg = (List<MsoIpg>)pm.detachCopyAll(ipg);
		pm.close();
		return ipg;
	}
	
	public MsoIpg findByMsoIdAndChannelId(long msoId, long channelId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoIpg.class);
		q.setFilter("msoId == msoIdParam && channelId== channelIdParam");
		q.setOrdering("seq asc");
		q.declareParameters("long msoIdParam, long channelIdParam");
		@SuppressWarnings("unchecked")
		List<MsoIpg> results = (List<MsoIpg>)q.execute(msoId, channelId);
		MsoIpg msoIpg = null;
		if (results.size() > 0) {
			msoIpg = results.get(0);
			msoIpg = pm.detachCopy(msoIpg);
		}
		pm.close();
		return msoIpg;
	}
	
	public MsoIpg findByMsoIdAndSeq(long msoId, int seq) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoIpg.class);
		q.setFilter("msoId == msoIdParam && seq == seqParam");
		q.declareParameters("long msoIdParam, int seqParam");
		@SuppressWarnings("unchecked")
		List<MsoIpg> results = (List<MsoIpg>)q.execute(msoId, seq);
		MsoIpg msoIpg = null;
		if (results.size() > 0) {
			msoIpg = results.get(0);
			msoIpg = pm.detachCopy(msoIpg);
		}
		pm.close();
		return msoIpg;
	}
}
