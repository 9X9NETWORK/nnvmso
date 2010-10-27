package com.nnvmso.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("statistics")
public class StatisticsController {
	@RequestMapping("index")
	public ModelAndView handleRequest() {
		return new ModelAndView("statistics");
	}
}
