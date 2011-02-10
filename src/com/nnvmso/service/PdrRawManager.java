package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.nnvmso.dao.PdrRawDao;
import com.nnvmso.model.PdrRaw;

public class PdrRawManager {

	protected static final Logger log = Logger.getLogger(NnUserManager.class.getName());
	
	private PdrRawDao pdrDao= new PdrRawDao();
			
	public void create(PdrRaw pdr) {
		Date now = new Date();
		pdr.setCreateDate(now);
		pdr.setUpdateDate(now);
		pdrDao.save(pdr);
	}
	
	public PdrRaw save(PdrRaw pdr) {
		pdr.setUpdateDate(new Date());
		return pdrDao.save(pdr);
	}		
	
	/**
	 * 	Keep the implementation since the requirement can be changed back again.
	 *  @@@ IMPORTANT It needs to be done in task if there's viewLog process  
	 */
	public void processPdr(String pdr, long userId, String sessionId) {
		if (pdr == null) {return;}		
		
		PdrRawManager pdrMngr = new PdrRawManager();
		//store raw data !!! change to blob type
		if (pdr.length() > 500) { pdr = pdr.substring(0, 499);}
		PdrRaw raw = new PdrRaw(userId, sessionId, 0, null, pdr);			
		pdrMngr.create(raw);
		/*
		//if the verb is watch, store the data in viewlog
		String[] lines = pdr.split("\n");
		ViewLogManager viewLogMngr = new ViewLogManager();
		try {			
			for (String line : lines) {						
				String[] data = line.split("\t");			
				String verb = data[1];			
				if (verb.equals(PdrRaw.VERB_WATCH)) {
					viewLogMngr.processPdr(line, userId);
				}
			}
		} catch (Exception e) {
			log.info("exception catpures: " + e.getClass());
		}
		*/
	}
	
	public List<PdrRaw> findAll() {
		return pdrDao.findAll();
	}
	
}
