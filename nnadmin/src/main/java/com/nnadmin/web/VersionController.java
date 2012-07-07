package com.nnadmin.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("version")
public class VersionController {
		
	@RequestMapping("current")
	@ResponseBody
	public String current() {
        String appVersion = "3.1.12.3";
        String svn = "2766";
        String packagedTime = "2012-06-01 00:04:01.456000";
		String info = "app version: " + appVersion + "\n"; 
		info = info + "svn: " + svn + "\n";
		info = info + "packaged time: " + packagedTime + "\n";
		return info;
		//return NnNetUtil.textReturn(info);
	}		
}
