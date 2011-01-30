package com.nnvmso.service;

import java.util.Date;
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
	}
	
	public PdrRaw save(PdrRaw pdr) {
		pdr.setUpdateDate(new Date());
		return pdrDao.save(pdr);
	}		
	
	public void processPdr(String pdr, long userId, String sessionId) {
		log.info("original pdr: \n" + pdr);
		String[] lines = pdr.split("\n");
		PdrRawManager pdrMngr = new PdrRawManager();
		ViewLogManager viewLogMngr = new ViewLogManager();
		try {
			for (String line : lines) {						
				String[] data = line.split("\t");			
				long delta = Long.parseLong(data[0]);
				String verb = data[1];			
				int beginningIndex = data[0].length() + data[1].length() + 2;
				String info = line.substring(beginningIndex);
				PdrRaw raw = new PdrRaw(userId, sessionId, delta, verb, info);			
				pdrMngr.save(raw);			
				if (verb.equals("watched")) {
					viewLogMngr.processPdr(line, userId);
				}
			}
		} catch (Exception e) {
			log.info("exception catpures: " + e.getClass());
		}
	}
	
	
}
