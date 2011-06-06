package com.nncloudtv.dao;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.NnSet;

public class NnSetDao extends GenericDao<NnSet> {
	
	protected static final Logger logger = Logger.getLogger(NnSetDao.class.getName());
	
	public NnSetDao() {
		super(NnSet.class);
	}

	public NnSet findById(long id) {
		PersistenceManager pm = PMF.getContent().getPersistenceManager();		
		NnSet cs = null;
		try {
			cs = pm.getObjectById(NnSet.class, id);
			cs = pm.detachCopy(cs);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();			
		}
		return cs;
	}
	
	public NnSet findByBeautifulUrl(String url) {		
		PersistenceManager pm = PMF.getContent().getPersistenceManager();
		NnSet detached = null;
		
		try {
			Query query = pm.newQuery(NnSet.class);
			query.setFilter("beautifulUrl == beautifulUrlParam");
			query.declareParameters("String beautifulUrlParam");
			@SuppressWarnings("unchecked")
			List<NnSet> channelNnSets = (List<NnSet>) query.execute(url);
			if (channelNnSets.size() > 0) {
				detached = pm.detachCopy(channelNnSets.get(0));
			}
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return detached;
	}
	
	//!!!lower case
	public NnSet findByName(String name) {		
		PersistenceManager pm = PMF.getContent().getPersistenceManager();
		NnSet detached = null;		
		try {
			Query query = pm.newQuery(NnSet.class);
			query.setFilter("name == nameParam");
			query.declareParameters("String nameParam");			
			@SuppressWarnings("unchecked")
			List<NnSet> channelNnSets = (List<NnSet>) query.execute(name);
			if (channelNnSets.size() > 0) {
				detached = pm.detachCopy(channelNnSets.get(0));
			}
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return detached;
	}
}