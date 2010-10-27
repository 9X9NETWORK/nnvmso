package com.nnvmso.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("error")
public class ErrorController {

	private String viewRoot = "/error/";
	
	@ExceptionHandler(NullPointerException.class)
	public @ResponseBody String nullPointer(NullPointerException e) {
		System.out.println("enter null pointer");
		return viewRoot + "nullPointer";
	}
}
