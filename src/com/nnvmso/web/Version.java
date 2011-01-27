package com.nnvmso.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nnvmso.lib.NnNetUtil;;

@Controller
@RequestMapping("version")
public class Version {
	
	@RequestMapping("current")
	public ResponseEntity<String> current() {
		String appVersion = "12";
		String server = "alpha";
		String svn = "";
		String info = "app version: " + appVersion + "\n"; 
		info = info + "app server: " + server + "\n";
		info = info + "svn: " + svn;
		return NnNetUtil.textReturn(info);
	}	
	
}
