package com.nnvmso.web;

import java.util.Date;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nnvmso.lib.PlayerLib;

@Controller
@RequestMapping("version")
public class VersionController {

	@RequestMapping("current")
	public ResponseEntity<String> current() {
		String version = "1";
		String date = (new Date()).toString();
		version = version + " - " + date;
		return PlayerLib.outputReturn(version);		
	}
	
}
