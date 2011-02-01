package com.nnvmso.web.admin;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.service.InitService;

/**
 * for testing only, works only for small set of data
 */	
@Controller
@RequestMapping("admin/init")
public class AdminInitController {
	protected static final Logger logger = Logger.getLogger(AdminInitController.class.getName());		
	
	private final InitService initService;		
	
	@Autowired
	public AdminInitController(InitService initService) {
		this.initService = initService;
	}		
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}

	@RequestMapping("deleteAll")
	public ResponseEntity<String> initialize(HttpServletRequest req) {
		initService.setRequest(req);
		initService.deleteAll();
		return NnNetUtil.textReturn("Done.\nYou might also want to use \"initMsoAndCategories\"?");
	}
 
	@RequestMapping("initMsoAndCategories")
	public ResponseEntity<String> initCategories(HttpServletRequest req, @RequestParam(value="debug")boolean debug) {
		initService.setRequest(req);
		initService.initMsoAndCategories(debug);
		return NnNetUtil.textReturn("OK");		
	}
		
	//devel mode, whether not to submit data to transcoding service
	//debug mode, whether to turn on player's debugging information
	@RequestMapping("initAll")
	public ResponseEntity<String> initAll(@RequestParam(value="devel")boolean devel, @RequestParam(value="debug")boolean debug, HttpServletRequest req) { 
		initService.setRequest(req);
		initService.initAll(devel, debug);
		return NnNetUtil.textReturn("OK");		
	}
	
	@RequestMapping("changeMso")
	public ResponseEntity<String> changeMso(@RequestParam(value="mso")String mso, HttpServletResponse resp) {
		CookieHelper.setCookie(resp, CookieHelper.MSO, mso);
		return NnNetUtil.textReturn("OK");
	}
}
