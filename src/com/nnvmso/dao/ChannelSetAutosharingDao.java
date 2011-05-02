package com.nnvmso.dao;

import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.ChannelSetAutosharing;

public class ChannelSetAutosharingDao extends GenericDao<ChannelSetAutosharing> {
	
	public ChannelSetAutosharingDao() {
		super(ChannelSetAutosharing.class);
	}
	
	public boolean isChannelSetAutosharedTo(long channelSetId, short type) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		boolean result = false;
		
		try {
			Query query = pm.newQuery(ChannelSetAutosharing.class);
			query.setFilter("channelSetId == channelSetIdParam");
			query.setFilter("type == typeParam");
			query.declareParameters("long channelSetIdParam");
			query.declareParameters("short typeParam");
			@SuppressWarnings("unchecked")
			List<ChannelSetAutosharing> list = (List<ChannelSetAutosharing>) query.execute(channelSetId, type);
			if (list.size() > 0)
				result = true;
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return result;
	}
	
}
