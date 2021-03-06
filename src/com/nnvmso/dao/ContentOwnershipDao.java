package com.nnvmso.dao;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.ContentOwnership;

public class ContentOwnershipDao extends GenericDao<ContentOwnership> {
	
	protected static final Logger logger = Logger.getLogger(ContentOwnershipDao.class.getName());
	
	public ContentOwnershipDao() {
		super(ContentOwnership.class);
	}
	
	public List<ContentOwnership> findByMsoIdAndContentType(long msoId, short contentType) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ContentOwnership> detached = new ArrayList<ContentOwnership>();
		
		logger.info("msoId = " + msoId + ", contentType = " + contentType);
		
		try {
			Query query = pm.newQuery(ContentOwnership.class);
			query.setFilter("msoId == msoIdParam && contentType == contentTypeParam");
			query.declareParameters("long msoIdParam, short contentTypeParam");
			@SuppressWarnings("unchecked")
			List<ContentOwnership> list = (List<ContentOwnership>)query.execute(msoId, contentType);
			if (list.size() > 0)
				detached = (List<ContentOwnership>)pm.detachCopyAll(list);
			logger.info("found = " + list.size());
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return detached;
	}

	public List<ContentOwnership> findAllByMsoId(long msoId) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ContentOwnership> detached = new ArrayList<ContentOwnership>();
		
		try {
			Query query = pm.newQuery(ContentOwnership.class);
			query.setFilter("msoId == msoIdParam");
			query.declareParameters("long msoIdParam");
			@SuppressWarnings("unchecked")
			List<ContentOwnership> list = (List<ContentOwnership>)query.execute(msoId);
			if (list.size() > 0)
				detached = (List<ContentOwnership>)pm.detachCopyAll(list);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return detached;
	}
	
	public ContentOwnership findByMsoIdAndChannelId(long msoId, long channelId) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		ContentOwnership ownership = null;
		
		try {
			Query query = pm.newQuery(ContentOwnership.class);
			query.setFilter("msoId == msoIdParam && contentType == contentTypeParam && contentId == contentIdParam");
			query.declareParameters("long msoIdParam, short contentTypeParam, long contentIdParam");
			@SuppressWarnings("unchecked")
			List<ContentOwnership> list = (List<ContentOwnership>)query.execute(msoId, ContentOwnership.TYPE_CHANNEL, channelId);
			if (list.size() > 0)
				ownership = pm.detachCopy(list.get(0));
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return ownership;
	}
	
	public List<ContentOwnership> findAllByChannelId(long channelId) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ContentOwnership> detached = new ArrayList<ContentOwnership>();
		
		try {
			Query query = pm.newQuery(ContentOwnership.class);
			query.setFilter("contentId == contentIdParam && contentType == contentTypeParam");
			query.declareParameters("long contentIdParam, long contentTypeParam");
			@SuppressWarnings("unchecked")
			List<ContentOwnership> list = (List<ContentOwnership>)query.execute(channelId, ContentOwnership.TYPE_CHANNEL);
			if (list.size() > 0)
				detached = (List<ContentOwnership>)pm.detachCopyAll(list);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return detached;
	}
}
