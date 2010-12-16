package com.nnvmso.web;

import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.nnvmso.lib.NnLib;

@Controller
@RequestMapping("statistics")
public class StatisticsController {
	protected static final Logger logger = Logger.getLogger(StatisticsController.class.getName());
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLib.logException(e);
		return "error/exception";				
	}		
	
	@RequestMapping("index")
	public ModelAndView handleRequest() {
		return new ModelAndView("statistics/statistics");
	}
}
