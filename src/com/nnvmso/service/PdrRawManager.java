package com.nnvmso.service;

import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nnvmso.dao.PdrRawDao;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.NnUserWatched;
import com.nnvmso.model.PdrRaw;

public class PdrRawManager {

	protected static final Logger log = Logger.getLogger(PdrRawManager.class.getName());
	
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
	public void processPdr(String pdr, NnUser user, String sessionId) {
		if (pdr == null) {return;}		
		
		PdrRawManager pdrMngr = new PdrRawManager();		
		NnUserWatchedManager watchedMngr = new NnUserWatchedManager();
		String reg = "w \t (\\d++) \t (\\w++)";		
		Pattern pattern = Pattern.compile(reg);
		Matcher m = pattern.matcher(pdr);
		while (m.find()) {			
			long channelId = Long.parseLong(m.group(1));
			String program = m.group(2);
			NnUserWatched watched = new NnUserWatched(user, channelId, program);
			log.info("user watched channel and program:" + user.getToken() + ";" + channelId + ";" + program);
			watchedMngr.save(watched);
		}		
						
		//log.info("pdr length: " + pdr.length());
		int times = Math.round(pdr.length() / 500) + 1;
		int start = 0;
		int end = 499;
		if (pdr.length() < end) { end = pdr.length();}
		for (int i=0; i<times; i++) {
			String detail = pdr.substring(start, end);
			PdrRaw raw = new PdrRaw(user.getKey().getId(), sessionId, 0, null, detail);			
			pdrMngr.create(raw);
			start = start + 500;
			end = end  + 499;
			if (pdr.length() < end) { end = pdr.length();}
		}		
		
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
		
}
