package com.nnvmso.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.google.appengine.repackaged.org.apache.commons.logging.Log;
import com.google.appengine.repackaged.org.apache.commons.logging.LogFactory;

@Controller
@RequestMapping("error")
public class ErrorController extends SimpleMappingExceptionResolver{

	private String viewRoot = "/error/";
	protected final Log logger = LogFactory.getLog(getClass());
	
	@RequestMapping("not-found")
	public String notFound() {
		return viewRoot + "404";
	}
		
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		logger.warn("exception:" + ex.toString());
		String detail = "";
		StackTraceElement[] elements = ex.getStackTrace();
		for (StackTraceElement elm:elements ) {
			detail = detail + elm.toString() + "\n";			
		}		
		logger.warn("exception stacktrace:\n" + detail);		
		return super.resolveException(request, response, handler, ex);
	}
	
}
