package com.nnvmso.web;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Mso;
import com.nnvmso.service.FBService;

/** 
 * temporary controller, just used for routing, move to xml later,  
 */
@Controller
@RequestMapping("5f")
public class FifthFloorController {

	protected static final Logger logger = Logger.getLogger(FifthFloorController.class.getName());
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}		
	
	/**
	 * to become a 5f player, 1)set cookie, 2)set fb info 
	 */
	@RequestMapping("")
	public String zooatomics(HttpServletResponse resp, Model model) {		
		CookieHelper.setCookie(resp, CookieHelper.MSO, Mso.NAME_5F);
		FBService fbService = new FBService();
		model = fbService.setBrandMetadata(model, Mso.NAME_5F);
		model.addAttribute("brandInfo", "5f");
		return "player/zooatomics";
	}

	//for 5f domain share redirect
	@RequestMapping("share/{ipgId}")
	public String share(@PathVariable String ipgId) {
		return "redirect:/share/" + ipgId;		
	}
	
	/*
	 * used for dns redirect watch dog 
	 */
	@RequestMapping("wd")
	public ResponseEntity<String> watchdog() {		
		return NnNetUtil.textReturn("OK");
	}
	
	
}
