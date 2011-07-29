package com.nnvmso.web;

import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.nnvmso.lib.NnLogUtil;

@Controller
@RequestMapping("")
public class ErrorController {

	protected static final Logger logger = Logger.getLogger(ErrorController.class.getName());		
	private String viewRoot = "/error/";
	
	@RequestMapping("not-found")
	public String notFound() {
		System.out.println("enter");
		return "error/404";
	}

	@RequestMapping("internal-error")
	public String internalEror() {
		return "error/blank";
	}
	
	//final frontier
	@RequestMapping("error")
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		NnLogUtil.logException(ex);
		String now = (new Date()).toString();
		return new ModelAndView(viewRoot + "error", "now", now);
	}
}
