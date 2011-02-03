package com.nnvmso.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("admin/index")
public class AdminIndexController {

	@RequestMapping("")
	public String index() {
		return "admin/index";
	}
	
}
