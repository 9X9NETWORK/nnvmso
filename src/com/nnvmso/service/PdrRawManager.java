package com.nnvmso.service;

import java.util.logging.Logger;

import com.nnvmso.dao.PdrRawDao;
import com.nnvmso.model.PdrRaw;

public class PdrRawManager {

	protected static final Logger log = Logger.getLogger(NnUserManager.class.getName());
	
	private PdrRawDao pdrDao= new PdrRawDao();
		
	public void create(PdrRaw pdr) {
		pdrDao.create(pdr);
	}
	
	public PdrRaw save(PdrRaw pdr) {
		return pdrDao.save(pdr);
	}
	
	
}
