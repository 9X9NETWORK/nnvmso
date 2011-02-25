package com.nnvmso.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="")
public class WebController {

	@RequestMapping(value="9x9")
	public String nine() {
		return "index";
	}
	
	@RequestMapping(value="5f")
	public String five() {
		return "index";
	}
	
}
