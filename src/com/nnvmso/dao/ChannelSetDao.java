package com.nnvmso.dao;

import java.util.logging.Logger;

import com.nnvmso.model.ChannelSet;

public class ChannelSetDao extends GenericDao<ChannelSet> {
	
	protected static final Logger logger = Logger.getLogger(ChannelSetDao.class.getName());
	
	public ChannelSetDao() {
		super(ChannelSet.class);
	}
}
