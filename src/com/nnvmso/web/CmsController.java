package com.nnvmso.web;

import java.security.SignatureException;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.AmazonLib;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.model.Mso;
import com.nnvmso.service.AuthService;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.SessionService;

@Controller
public class CmsController {
	
	protected static final Logger logger = Logger.getLogger(CmsController.class.getName());
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/blank";
	}
	
	@RequestMapping(value = "{msoName}/admin", method = RequestMethod.GET)
	public String admin(HttpServletRequest request, @PathVariable("msoName") String msoName, Model model) {
		
		SessionService sessionService = new SessionService(request);
		HttpSession session = sessionService.getSession();
		
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findByName(msoName);
		if (mso == null)
			return "error/404";
		model.addAttribute("msoLogo", mso.getLogoUrl());
		
		Mso sessionMso = (Mso)session.getAttribute("mso");
		if (sessionMso != null && sessionMso.getKey().getId() == mso.getKey().getId()) {
			
			return "redirect:cms/channelSetManagement";
		} else {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					logger.info(cookie.getName());
					if (cookie.getName().length() > 0 && cookie.getName().compareTo("cms_login_" + msoName) == 0) {
						String[] split = cookie.getValue().split("\\|");
						if (split.length >= 2) {
							model.addAttribute("email", split[0]);
							model.addAttribute("password", split[1]);
						}
					}
				}
			}
			sessionService.removeSession();
			return "cms/login";
		}
	}
	
	@RequestMapping(value = "{msoName}/admin", method = RequestMethod.POST)
	public String login(HttpServletRequest request,
	                    HttpServletResponse response,
	                    Model model,
	                    @RequestParam String email,
	                    @RequestParam String password,
	                    @RequestParam(required = false) Boolean rememberMe,
	                    @PathVariable String msoName) {
		
		logger.info(msoName);
		logger.info("email = " + email);
		logger.info("password = " + password);
		logger.info("rememberMe = " + rememberMe);
		
		SessionService sessionService = new SessionService(request);
		AuthService authService = new AuthService();
		MsoManager msoMngr = new MsoManager();
		
		Mso mso = msoMngr.findByName(msoName);
		if (mso == null)
			return "error/404";
		String msoLogo = mso.getLogoUrl();
		mso = authService.msoAuthenticate(email, password, mso.getKey().getId());
		if (mso == null) {
			logger.info("login failed");
			model.addAttribute("email", email);
			model.addAttribute("password", password);
			model.addAttribute("error", "Invalid email / password");
			model.addAttribute("msoLogo", msoLogo);
			sessionService.removeSession();
			return "cms/login";
		}
		
		HttpSession session = sessionService.getSession();
		session.setAttribute("mso", mso);
		sessionService.saveSession(session);
		
		// set cookie
		if (rememberMe != null && rememberMe) {
			logger.info("set cookie");
			response.addCookie(new Cookie("cms_login_" + msoName, email + "|" + password));
		} else {
			response.addCookie(new Cookie("cms_login_" + msoName, ""));
		}
		
		return "redirect:/" + msoName + "/channelManagement";
	}
	
	@RequestMapping(value = "{msoName}/logout")
	public String logout(Model model, HttpServletRequest request, @PathVariable String msoName) {
		SessionService sessionService = new SessionService(request);
		sessionService.removeSession();
		return "redirect:/" + msoName + "/admin";
	}
	
	@RequestMapping(value = "{msoName}/{cmsTab}")
	public String channelManagement(HttpServletRequest request, @PathVariable String msoName, @PathVariable String cmsTab, Model model) throws SignatureException {
		
		SessionService sessionService = new SessionService(request);
		HttpSession session = sessionService.getSession();
		
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findByName(msoName);
		if (mso == null)
			return "redirect:error/404";
		
		Mso sessionMso = (Mso)session.getAttribute("mso");
		if (sessionMso != null && sessionMso.getKey().getId() == mso.getKey().getId()) {
			model.addAttribute("msoLogo", mso.getLogoUrl());
			model.addAttribute("mso", mso);
			model.addAttribute("msoId", mso.getKey().getId());
			model.addAttribute("logoutUrl", "/" + msoName + "/logout");
			if (cmsTab.equals("channelManagement")) {
				return "cms/channelManagement";
			} else if (cmsTab.equals("channelSetManagement")) {
				String policy = AmazonLib.buildS3Policy("9x9tmp", "public-read", "image/");
				model.addAttribute("s3Policy", policy);
				model.addAttribute("s3Signature", AmazonLib.calculateRFC2104HMAC(policy));
				model.addAttribute("s3Id", AmazonLib.AWS_ID);
				return "cms/channelSetManagement";
			} else {
				return "error/404";
			}
		} else {
			sessionService.removeSession();
			return "redirect:/" + msoName + "/admin";
		}
	}
}