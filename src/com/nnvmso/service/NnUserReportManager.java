package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.nnvmso.dao.NnUserReportDao;
import com.nnvmso.model.NnDevice;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.NnUserReport;

public class NnUserReportManager {
	protected static final Logger log = Logger.getLogger(NnUserReportManager.class.getName());
	
	private NnUserReportDao reportDao = new NnUserReportDao();
	
	public void create(NnUser user, NnDevice device, String session, String comment) {
		NnUserReport report = new NnUserReport(user, device, session, comment);
		report.setCreateDate(new Date());
		reportDao.save(report);
	}
	
	public List<NnUserReport> findAll() {
		return reportDao.findAll(); 
	}
	
	public List<NnUserReport> findSince(Date since) {
		return reportDao.findSince(since);
	}
	
	public List<NnUserReport> findByUser(String token) {
		return reportDao.findByUser(token);
	}

	public List<NnUserReport> findByUserSince(String token, Date since) {
		return reportDao.findByUserSince(token, since);
	}
	
}

