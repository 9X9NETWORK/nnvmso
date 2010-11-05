package com.nnvmso.web;

import javax.servlet.http.*;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import com.nnvmso.model.Mso;
import com.nnvmso.service.AuthService;
import com.nnvmso.service.MsoManager;
import com.nnvmso.validator.LoginValidator;

@Controller
@RequestMapping("mso")
@SessionAttributes("mso")
public class MsoController {

	private final MsoManager msoMngr;
	private final AuthService authService;
	private final LoginValidator loginValidator;

	@Autowired
	public MsoController(MsoManager msoMngr, AuthService authService,
			LoginValidator loginValidator) {
		this.msoMngr = msoMngr;
		this.authService = authService;
		this.loginValidator = loginValidator;
	}

	@RequestMapping(value = "")
	public String index() {
		return "mso/index";
	}

	@RequestMapping(value = "login", method = RequestMethod.GET)
	public String loginForm(Model model) {
		Mso mso = new Mso();
		model.addAttribute(mso);
		return ("mso/msoLoginForm");
	}

	@RequestMapping(value = "login", method = RequestMethod.POST)
	public String login(@ModelAttribute Mso mso, BindingResult result,
			Model model, HttpSession session, SessionStatus status) {
		loginValidator.validate(mso, result);
		if (result.hasErrors()) {
			model.addAttribute("mso", mso);
			return "mso/msoLoginForm";
		}
		mso = msoMngr.msoAuthenticated(mso.getEmail(), mso.getPassword());
		if (mso == null) {
			result.addError(new ObjectError("required.email", "Invalid credential."));
			return "mso/msoLoginForm";
		} else {
			authService.setAuthSession(session, "mso", mso);
			status.setComplete();
		}
		return "redirect:/channel/list";
	}

	@RequestMapping("setting")
	public ModelAndView setting(HttpServletRequest req, HttpServletResponse resp) {
		return new ModelAndView("setting");
	}

}
