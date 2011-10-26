package com.nnvmso.web.admin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.NnUserReport;
import com.nnvmso.model.PdrRaw;
import com.nnvmso.service.NnUserReportManager;
import com.nnvmso.service.PdrRawManager;

@Controller
@RequestMapping("admin/pdr")
public class AdminPdrController {
     	
	@RequestMapping("pdrList")
	public ResponseEntity<String> pdrList(
			@RequestParam(required=false) String user,
			@RequestParam(required=false) String session) {
		PdrRawManager pdrMngr = new PdrRawManager();
		List<PdrRaw> list = pdrMngr.findDebuggingInfo(user, session);
		String output = "";
		for (PdrRaw r : list) {
			output += r.getUserToken() + "\t" + r.getSession() + "\n" + r.getDetail().getValue() + "\n\n";
		}
		return NnNetUtil.textReturn(output);		
	}

	@RequestMapping("reportList")
	public ResponseEntity<String> list(@RequestParam(required=false) String user, 			                             
			                           @RequestParam(required=false) String since) {				
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		List<NnUserReport> list = new ArrayList<NnUserReport>();
		NnUserReportManager reportMngr = new NnUserReportManager();
		
		Date sinceDate = null;
		if (since != null) {
			try {
				sinceDate = sdf.parse(since);
			} catch (ParseException e) {
				return NnNetUtil.textReturn("wrong date format: yyyymmdd");
			}
		}
		if (user == null && since == null) {
			list = reportMngr.findAll();	
		}
		if (sinceDate != null && user == null) {
			System.out.println("since date:" + sinceDate);
			list = reportMngr.findSince(sinceDate);
		}
		if (sinceDate == null && user != null) {
			list = reportMngr.findByUser(user);
		}
		if (sinceDate != null && user != null) {
			list = reportMngr.findByUserSince(user, sinceDate);
		}
		String output = "";
		for (NnUserReport r : list) {
			output += "<p>" + r.getUserToken() + "&nbsp;&nbsp;&nbsp;" + 
			"<a href='pdrList?user=" + r.getUserToken() + "&session=" + r.getSession() + "'>" +   
			r.getSession() + "</a><br/>" + r.getComment() + "</p>";
		}
		return NnNetUtil.htmlReturn(output);
	}
}

