package com.nnvmso.web;

import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("")
public class ErrorController {

	protected static final Logger logger = Logger.getLogger(ErrorController.class.getName());		
	private String viewRoot = "/error/";
	
	@RequestMapping("not-found")
	public String notFound() {
		return viewRoot + "404";
	}
		
	@RequestMapping("error")
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		String detail = "";
		StackTraceElement[] elements = ex.getStackTrace();
		for (StackTraceElement elm:elements ) {
			detail = detail + elm.toString() + "\n";			
		}		
		logger.severe("exception:" + ex.toString());
		logger.severe("exception stacktrace:\n" + detail);
		String now = (new Date()).toString();
		return new ModelAndView(viewRoot + "error", "now", now);
	}
}
