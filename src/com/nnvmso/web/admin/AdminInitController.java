package com.nnvmso.web.admin;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

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
 * for testing, works only for small set of data
 */	
@Controller
@RequestMapping("admin/init")
public class AdminInitController {
	protected static final Logger logger = Logger.getLogger(AdminInitController.class.getName());		
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}

	@RequestMapping("deleteAll")
	public ResponseEntity<String> initialize() {
		InitService initService = new InitService();
		initService.deleteAll();
		return NnNetUtil.textReturn("Done.\nYou might also want to use \"initMsoAndCategories\"?");
	}

	@RequestMapping("initMsoAndCategories")
	public ResponseEntity<String> initCategories() {
		InitService initService = new InitService();
		initService.initMsoAndCategories();
		return NnNetUtil.textReturn("OK");		
	}
	
	@RequestMapping("initDefaultChannels")
	public ResponseEntity<String> initTestChannels() {
		InitService initService = new InitService();
		initService.initTestData();
		return NnNetUtil.textReturn("OK");		
	}
	
	@RequestMapping("initAll")
	public ResponseEntity<String> initAll() {
		InitService initService = new InitService();
		initService.initAll();
		return NnNetUtil.textReturn("OK");		
	}
	
	@RequestMapping("changeMso")
	public ResponseEntity<String> changeMso(@RequestParam(value="mso")String mso, HttpServletResponse resp) {
		CookieHelper.setCookie(resp, CookieHelper.MSO, mso);
		return NnNetUtil.textReturn("OK");
	}
}
