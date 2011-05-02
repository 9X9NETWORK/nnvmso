package com.nnvmso.dao;

import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.ChannelAutosharing;

public class ChannelAutosharingDao extends GenericDao<ChannelAutosharing> {
	
	public ChannelAutosharingDao() {
		super(ChannelAutosharing.class);
	}
	
	public boolean isChannelAutosharedTo(long channelId, short type) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		boolean result = false;
		
		try {
			Query query = pm.newQuery(ChannelAutosharing.class);
			query.setFilter("channelId == channelIdParam");
			query.setFilter("type == typeParam");
			query.declareParameters("long channelIdParam");
			query.declareParameters("short typeParam");
			@SuppressWarnings("unchecked")
			List<ChannelAutosharing> list = (List<ChannelAutosharing>) query.execute(channelId, type);
			if (list.size() > 0)
				result = true;
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		
		return result;
	}
	
}
