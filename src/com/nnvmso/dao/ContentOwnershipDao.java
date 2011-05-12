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
}
