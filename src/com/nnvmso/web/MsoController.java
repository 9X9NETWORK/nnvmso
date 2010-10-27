package com.nnvmso.web;

import javax.servlet.http.*;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import com.nnvmso.model.Mso;
import com.nnvmso.service.AuthService;
import com.nnvmso.service.MsoService;

@Controller
@RequestMapping("mso")
public class MsoController {
	
	private final MsoService msoService;
	private final AuthService authService;
	
	@Autowired
	public MsoController(MsoService msoService, AuthService authService) {
		this.msoService = msoService;
		this.authService = authService;
	}			

	@RequestMapping(value="")
	public String index() {
		return "mso/index";
	}
	
	@RequestMapping(value="login" ,method = RequestMethod.GET)
	public String loginForm(Model model) {
		Mso mso = new Mso();
		model.addAttribute(mso);
		return ("mso/msoLoginForm");
	}	
	
	@RequestMapping(value="login",method = RequestMethod.POST)	
	public String login(@ModelAttribute Mso mso, HttpSession session, SessionStatus status)
	{
		mso = msoService.msoAuthenticated(mso.getEmail(), mso.getPassword());
		if (mso != null) {
			authService.setAuthSession(session, "mso", mso);
			status.setComplete();
		}
		return "redirect:/channel/list";
	}		
	
	@RequestMapping("setting")
	public ModelAndView setting(HttpServletRequest req, HttpServletResponse resp)
	{
		return new ModelAndView("setting");
	}
	
}
