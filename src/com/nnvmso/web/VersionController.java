package com.nnvmso.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.MsoChannelManager;

@Controller
@RequestMapping("version")
public class VersionController {
	
	@RequestMapping("encoding")
	public ResponseEntity<String> encoding() {
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		String output = "";
		MsoChannelManager channelMngr = new MsoChannelManager();
		channels = channelMngr.findAll();		
		for (MsoChannel c : channels) {
			if (c.getIntro().contains("a") || c.getIntro().contains("o"))
				output = output + c.getName() + "\n";
		}	
		return NnNetUtil.textReturn(output);
	}			
	
	@RequestMapping("current")
	public ResponseEntity<String> current() {
		String appVersion = "12";
		String server = "alpha";
		String svn = "$Revision$";
		String info = "app version: " + appVersion + "\n"; 
		info = info + "app server: " + server + "\n";
		info = info + "svn: " + svn;
		return NnNetUtil.textReturn(info);
	}	
	
}
