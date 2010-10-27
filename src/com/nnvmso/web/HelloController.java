package com.nnvmso.web;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelloController {
	
	@RequestMapping("hello")
	public ModelAndView handleRequest(HttpServletRequest resq, HttpServletResponse resp) {
		String now = (new Date()).toString();	
		return new ModelAndView("hello", "now", now);
	}
}
