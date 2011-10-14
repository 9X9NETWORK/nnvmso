package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.Mso;

public class ChannelSetDao extends GenericDao<ChannelSet> {
	
	protected static final Logger logger = Logger.getLogger(ChannelSetDao.class.getName());
	
	public ChannelSetDao() {
		super(ChannelSet.class);
	}

	public List<ChannelSet> findAll() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ChannelSet> detached = new ArrayList<ChannelSet>(); 
		try {
			Query q = pm.newQuery(ChannelSet.class);
			q.setOrdering("seq asc");
			@SuppressWarnings("unchecked")
			List<ChannelSet> sets = (List<ChannelSet>) q.execute();
			detached = (List<ChannelSet>)pm.detachCopyAll(sets);
		} finally {
			pm.close();
		}
		return detached;
	}	
	
	public ChannelSet findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		ChannelSet cs = null;
		try {
			cs = pm.getObjectById(ChannelSet.class, id);
			cs = pm.detachCopy(cs);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();			
		}
		return cs;
	}

	public List<ChannelSet> findFeaturedSetsByMso(Mso mso) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ChannelSet> detached = new ArrayList<ChannelSet>(); 
		try {
			Query q = pm.newQuery(ChannelSet.class);
			q.setFilter("featured == featuredParam && msoId == msoIdParam");
			q.declareParameters("boolean featuredParam, long msoIdParam");
			q.setOrdering("createDate asc");
			@SuppressWarnings("unchecked")
			List<ChannelSet> sets = (List<ChannelSet>) q.execute(true, mso.getKey().getId());
			detached = (List<ChannelSet>)pm.detachCopyAll(sets);
		} finally {
			pm.close();
		}
		return detached;
	}	

	public List<ChannelSet> findAllByLang(String lang) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ChannelSet> detached = new ArrayList<ChannelSet>(); 
		try {
			Query q = pm.newQuery(ChannelSet.class);
			q.setFilter("lang == langParam");
			q.declareParameters("String langParam");
			q.setOrdering("seq asc");
			@SuppressWarnings("unchecked")
			List<ChannelSet> sets = (List<ChannelSet>) q.execute(lang);
			detached = (List<ChannelSet>)pm.detachCopyAll(sets);
		} finally {
			pm.close();
		}
		return detached;
	}	
	
	public List<ChannelSet> findFeaturedSets(String lang) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ChannelSet> detached = new ArrayList<ChannelSet>(); 
		try {
			Query q = pm.newQuery(ChannelSet.class);
			q.setFilter("featured == featuredParam && lang == langParam");
			q.declareParameters("boolean featuredParam, String langParam");
			q.setOrdering("seq asc");
			@SuppressWarnings("unchecked")
			List<ChannelSet> sets = (List<ChannelSet>) q.execute(true, lang);
			detached = (List<ChannelSet>)pm.detachCopyAll(sets);
		} finally {
			pm.close();
		}
		return detached;
	}	
	
	public ChannelSet findByBeautifulUrl(String url) {		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ChannelSet detached = null;
		
		try {
			Query query = pm.newQuery(ChannelSet.class);
			query.setFilter("beautifulUrl == beautifulUrlParam");
			query.declareParameters("String beautifulUrlParam");
			@SuppressWarnings("unchecked")
			List<ChannelSet> channelSets = (List<ChannelSet>) query.execute(url);
			if (channelSets.size() > 0) {
				detached = pm.detachCopy(channelSets.get(0));
			}
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return detached;
	}
	
	
	public ChannelSet findByNameSearch(String nameSearch) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ChannelSet detached = null;
		
		try {
			Query query = pm.newQuery(ChannelSet.class);
			query.setFilter("nameSearch == nameSearchParam");
			query.declareParameters("String nameSearchParam");
			@SuppressWarnings("unchecked")
			List<ChannelSet> channelSets = (List<ChannelSet>) query.execute(nameSearch);
			if (channelSets.size() > 0) {
				detached = pm.detachCopy(channelSets.get(0));
			}
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return detached;
	}
}
