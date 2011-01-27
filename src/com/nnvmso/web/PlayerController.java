package com.nnvmso.web;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.*;

@Controller
@RequestMapping("player")
public class PlayerController {

	protected static final Logger logger = Logger.getLogger(PlayerController.class.getName());
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}		
	
	@RequestMapping("zooatomics")
	public String zooatomics(@RequestParam(value="mso",required=false) String mso, HttpServletRequest req, HttpServletResponse resp) {		
		if (CookieHelper.getCookie(req, CookieHelper.PLATFORM) == null) {
			CookieHelper.setCookie(resp, CookieHelper.PLATFORM, CookieHelper.PLATFORM_GAE);
		}		
		CookieHelper.setCookie(resp, CookieHelper.MSO, mso);
		return "player/zooatomics";
	}

	
}
