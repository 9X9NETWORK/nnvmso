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
		List<ContentOwnership> detachedOwnershipList = new ArrayList<ContentOwnership>();
		
		try {
			Query query = pm.newQuery();
			query.setFilter("msoId == msoIdParam");
			query.setFilter("contentType == contentTypeParam");
			query.declareParameters("long msoIdParam");
			query.declareParameters("short comtentTypeParam");
			@SuppressWarnings("unchecked")
			List<ContentOwnership> list = (List<ContentOwnership>)query.execute(msoId, contentType);
			detachedOwnershipList = (List<ContentOwnership>)pm.detachCopyAll(list);
		} catch (JDOObjectNotFoundException e) {
		}
		
		return detachedOwnershipList;
	}
}
