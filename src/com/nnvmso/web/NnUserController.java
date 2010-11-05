package com.nnvmso.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import com.nnvmso.model.NnUser;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.SubscriptionManager;

@Controller
@RequestMapping("user")
public class NnUserController {
		
	private final NnUserManager userMngr;
	private final SubscriptionManager subMngr;
	
	@Autowired
	public NnUserController(NnUserManager userMngr, SubscriptionManager subMngr) {
		this.userMngr = userMngr;
		this.subMngr = subMngr;
	}
	
	@RequestMapping(value="subscribe")	
	public String subscribe(@RequestParam(value="id") String key) {
		NnUser user = userMngr.findByKey(key);
		subMngr.subscribe(user);
		return "hello";
	}
	
	@RequestMapping(value="signup", method=RequestMethod.GET)
	public String signupForm(Model model) {
		NnUser user = new NnUser();
		model.addAttribute("user", user);
		return "user/userSignupForm";
	}	
	
	@RequestMapping(value="signup", method=RequestMethod.POST)
	public String signupSubmit(@ModelAttribute NnUser user, SessionStatus status) {
		userMngr.create(user);
		subMngr.subscribe(user);
		status.setComplete();
		return "redirect:/hello";
	}

}
