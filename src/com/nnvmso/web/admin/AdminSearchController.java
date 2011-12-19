package com.nnvmso.web.admin;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.DbDumper;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.NnUserManager;

@Controller
@RequestMapping("admin/search")
public class AdminSearchController {

	protected static final Logger log = Logger.getLogger(AdminSearchController.class.getName());		
	
	@RequestMapping(value="test")
	public ResponseEntity<String> search(@RequestParam(value="text", required=false) String search) {
		/*
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		List<MsoChannel> searchResults = SearchJanitor.searchChannelEntries(search, pm);
		String text = "";
		for (MsoChannel c : searchResults) {
			text += "c name:" + c.getName() + ";" + c.getIntro() + "\n";
		}
		*/
		return NnNetUtil.textReturn("hello");
	}
	
}
