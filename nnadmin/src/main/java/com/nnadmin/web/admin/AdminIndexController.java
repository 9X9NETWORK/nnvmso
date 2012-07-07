package com.nnadmin.web.admin;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nnadmin.lib.ResourceLib;

@Controller
@RequestMapping("")
public class AdminIndexController {
	
	protected static final Logger log = Logger.getLogger(AdminIndexController.class.getName());		

	@RequestMapping("index")
	public String index() {
		return "admin/index";
	}

	/*
	@RequestMapping(value="login", method = RequestMethod.GET)
	public String login(ModelMap model) {		
		return "admin/login";
 
	}
 
	@RequestMapping(value="loginfailed", method = RequestMethod.GET)
	public String loginerror(ModelMap model) {
		return "admin/login";
 
	}
 
	@RequestMapping(value="logout", method = RequestMethod.GET)
	public String logout(ModelMap model) {
		return "admin/login";
 
	}
	*/	
	
	@RequestMapping("/index/ui")
	public String oss(Model model, 
				      HttpServletRequest request, 
			          HttpServletResponse response,
			          Principal principal)
			throws IOException {		
		//String name = principal.getName();
		System.out.println("root path:" + ResourceLib.getExternalRootPath());
		//model.addAttribute("username", name);
		model.addAttribute("root", ResourceLib.getExternalRootPath());		
		response.setContentType("text/html");
		return "admin/ui";
	}
	
}
