package com.nnvmso.web.admin;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("admin/index")
public class AdminIndexController {

	protected static final Logger logger = Logger.getLogger(AdminIndexController.class.getName());		
	
	@RequestMapping("")
	public String index() {
		return "admin/index";
	}
	
	@RequestMapping("ui")
	public String oss(Model model, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		PrintWriter writer = response.getWriter();
		String thisURL = request.getRequestURI();
		UserService userService = UserServiceFactory.getUserService();
		
		response.setContentType("text/html");
		
		if (userService.isUserLoggedIn()) {
			if (userService.isUserAdmin()) {
				User user = userService.getCurrentUser();
				model.addAttribute("admin", user.getNickname());
				model.addAttribute("logoutURL", userService.createLogoutURL(thisURL));
				logger.info(user.getEmail() + " landing");
				return "admin/ui";
			} else {
				writer.println("This Google Account is not an Administrator, please <a href=\"" + userService.createLoginURL(thisURL) + "\">re-login</a> again.");
				return null;
			}
		} else {
			writer.println("You are not logged in, please use Google Account to <a href=\"" + userService.createLoginURL(thisURL) + "\">login</a> as a Administrator.");
			return null;
		}
	}

}
