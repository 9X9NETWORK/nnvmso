package com.nnvmso.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nnvmso.lib.FacebookLib;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.web.json.transcodingservice.FBPost;

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
	
	@RequestMapping("facebookTest") // delete after
	private ResponseEntity<String> facebookTest() throws IOException {
		
		FacebookLib facebookLib = new FacebookLib();
		FBPost fbPost = new FBPost();
		fbPost.setMessage("測試測試測試....");
		fbPost.setPicture("http://farm3.static.flickr.com/2035/2131941347_d3438dc316.jpg");
		fbPost.setLink("http://9x9.tv");
		fbPost.setName("標題標題標題");
		fbPost.setCaption("連結 http://9x9.tv 有沒有");
		fbPost.setDescription("描述本文");
		facebookLib.postToFacebook("701881375", fbPost);
		return NnNetUtil.textReturn("OK");
	}
}
