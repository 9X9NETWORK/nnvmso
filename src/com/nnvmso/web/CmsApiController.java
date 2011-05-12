package com.nnvmso.web;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.ContentOwnershipManager;

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
	
	@RequestMapping("listOwnedChannels")
	public @ResponseBody List<MsoChannel> listOwnedChannels(@RequestParam Long msoId) {
		
		logger.info("msoId = " + msoId);
		
		return new ContentOwnershipManager().findOwnedChannelsByMsoId(msoId);
	}
}
