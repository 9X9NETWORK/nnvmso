package com.nnvmso.web;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.model.Mso;
import com.nnvmso.service.MsoManager;

@Controller
public class CmsController {
	
	protected static final Logger logger = Logger.getLogger(CmsController.class.getName());
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/blank";
	}
	
	@RequestMapping("{msoName}/admin")
	public String admin(HttpServletRequest request,
			@PathVariable("msoName") String msoName,
			Model model) {
		
		logger.info(msoName);
		
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findByName(msoName);
		if (mso == null)
			return "error/404";
		
		model.addAttribute("mso", mso);
		
		return "cms/channelSetManagement";
	}
}
