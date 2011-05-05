package com.nnvmso.web;

import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nnvmso.lib.NnLogUtil;

@Controller
@RequestMapping("CMSAPI")
public class CmsApiController {
	protected static final Logger logger = Logger.getLogger(CmsApiController.class.getName());
	
	//private final CmsApiService cmsApiService = new CmsApiService();
	//private static MessageSource messageSource = new ClassPathXmlApplicationContext("locale.xml");
	//private Locale locale = Locale.TRADITIONAL_CHINESE; // NOTE hard-coded
	
	//private void prepService(HttpServletRequest req) {
	//}
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/blank";
	}
	
}
