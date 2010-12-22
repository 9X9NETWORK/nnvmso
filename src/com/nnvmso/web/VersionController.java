package com.nnvmso.web;

import java.util.Date;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.APILib;

@Controller
@RequestMapping("version")
public class VersionController {

	@RequestMapping("current")
	public @ResponseBody String current() {
		String version = "12";
		String date = (new Date()).toString();
		version = version + " - " + date;
		return version;
		//return APILib.outputReturn(version);		
	}	
}
