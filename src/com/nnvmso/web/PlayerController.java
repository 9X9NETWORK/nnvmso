package com.nnvmso.web;

import java.util.Date;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.nnvmso.lib.*;

@Controller
@RequestMapping("")
public class PlayerController {

	protected static final Logger logger = Logger.getLogger(PlayerController.class.getName());
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}		
	
	@RequestMapping("/")
	public String index() {
		return "redirect:9x9";
	}
	
	@RequestMapping("9x9")
	public String zooatomics(@RequestParam(value="mso",required=false) String mso, HttpServletRequest req, HttpServletResponse resp, Model model) {
		CookieHelper.deleteCookie(resp, CookieHelper.MSO);
		String now = (new SimpleDateFormat("MM.dd.yyyy")).format(new Date()).toString();
		model.addAttribute("now", now);
		return "player/zooatomics";
	}

	/*
	 * used for dns redirect watch dog 
	 */
	@RequestMapping("9x9/wd")
	public ResponseEntity<String> watchdog() {		
		return NnNetUtil.textReturn("OK");
	}
	
	
}
