package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.appengine.api.datastore.Text;
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

	public List<PdrRaw> findDebuggingInfo(String token, String session) {
		if (session != null)
			return pdrDao.findByUser(token);
		return pdrDao.findByUserAndSession(token, session);
	}
	
	/**
	 * 	Keep the implementation since the requirement can be changed back again.
	 *  @@@ IMPORTANT It needs to be done in task if there's viewLog process  
	 */
	public void processPdr(String pdr, NnUser user, String sessionId) {
		if (pdr == null) {return;}		
		//log.info("pdr:" + pdr);
		PdrRawManager pdrMngr = new PdrRawManager();		
		NnUserWatchedManager watchedMngr = new NnUserWatchedManager();
		String reg = "w\t(\\d++)\t(\\w++)";		
		Pattern pattern = Pattern.compile(reg);
		Matcher m = pattern.matcher(pdr);
		while (m.find()) {			
			long channelId = Long.parseLong(m.group(1));
			String program = m.group(2);
			if (channelId != 0 && !program.equals("0")) {
				NnUserWatched watched = new NnUserWatched(user, channelId, program);
				log.info("user watched channel and program:" + user.getToken() + ";" + channelId + ";" + program);
				watchedMngr.save(watched);
			}
		}
		Text text = new Text(pdr);
		PdrRaw raw = new PdrRaw(user, sessionId, text);			
		pdrMngr.create(raw);
	}
		
}
