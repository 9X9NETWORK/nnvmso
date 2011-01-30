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

	public List<MsoIpg> findAllByMsoId(long msoId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(MsoIpg.class);
		q.setFilter("msoId == msoIdParam");
		q.declareParameters("long msoIdParam");
		@SuppressWarnings("unchecked")
		List<MsoIpg> ipg = (List<MsoIpg>)q.execute(msoId);
		ipg = (List<MsoIpg>)pm.detachCopyAll(ipg);
		pm.close();
		return ipg;
	}
	
}
