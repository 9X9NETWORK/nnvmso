package com.nnvmso.web;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import com.nnvmso.form.UserSignupForm;
import com.nnvmso.model.Mso;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.SubscriptionManager;
import com.nnvmso.validator.NnUserSignupValidator;

@Controller
@RequestMapping("user")
public class NnUserController {
		
	private final NnUserManager userMngr;
	private final SubscriptionManager subMngr;
	private final NnUserSignupValidator signupValidator;
	
	@Autowired
	public NnUserController(NnUserManager userMngr, SubscriptionManager subMngr, NnUserSignupValidator signupValidator) {
		this.signupValidator = signupValidator;
		this.userMngr = userMngr;
		this.subMngr = subMngr;
	}
	
	@RequestMapping(value="subscribe")
	public String subscribe(@RequestParam(value="id") String key) {
		NnUser user = userMngr.findByKey(key);
		subMngr.msoSubscribe(user);
		return "hello";
	}
	
	@RequestMapping(value="signup", method=RequestMethod.GET)
	public String signupForm(Model model) {
		UserSignupForm form = new UserSignupForm();
		model.addAttribute("form", form);		
		return "user/userSignupForm";
	}	
	
	@RequestMapping(value="signup", method=RequestMethod.POST)
	public String signupSubmit(@ModelAttribute("form") @Valid UserSignupForm form, BindingResult result, Model model, SessionStatus status) {
		Mso mso = form.getMso();
		NnUser user = form.getUser();
		signupValidator.validate(form, result);
		if (result.hasErrors()) {
			model.addAttribute("form", form);
			return "user/userSignupForm";
		}
		MsoManager msoMngr = new MsoManager();
		Mso mymso = msoMngr.findByEmail(mso.getEmail());
		userMngr.save(user, mymso);
		subMngr.msoSubscribe(user);
		
		status.setComplete();
		return "redirect:/hello";
	}

}
