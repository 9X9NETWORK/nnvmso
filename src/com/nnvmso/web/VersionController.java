package com.nnvmso.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.web.json.facebook.FBPost;

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
	
	@SuppressWarnings("unused")
	@RequestMapping("facebookTest") // delete after
	private ResponseEntity<String> facebookTest() throws IOException {
		
		//ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectMapper mapper = new ObjectMapper();
		
		//FacebookLib facebookLib = new FacebookLib();
		FBPost fbPost = new FBPost();
		fbPost.setMessage("還是在測試....");
		fbPost.setPicture("http://big5.eastday.com:82/gate/big5/mil.eastday.com/m/20080503/images/01265406.jpg");
		fbPost.setLink("http://9x9.tv");
		fbPost.setName("外媒:中國神秘武器擊落美軍3架F-22和1架B-2");
		fbPost.setCaption("連結 9x9.tv 倒底有沒有");
		fbPost.setDescription("2月16日，美軍一架B-2隱形轟炸機，在太平洋關島的空軍基地起飛後不久墜毀，機上兩名飛行員及時安全彈出駕駛艙，這是B-2隱形轟炸機服役以來，首次發生墜毀事故。");
		fbPost.setFacebookId("701881375");
		//facebookLib.postToFacebook(fbPost);
		//mapper.writeValueAsString(fbPost);
		QueueFactory.getDefaultQueue().add(TaskOptions.Builder
		                                       .withUrl("/CMSAPI/postToFacebook")
		                                       .payload(mapper.writeValueAsBytes(fbPost), "application/json"));
		return NnNetUtil.textReturn(mapper.writeValueAsString(fbPost));
	}
}
