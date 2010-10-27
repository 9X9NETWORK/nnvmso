package com.nnvmso.web;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.DebugLib;
import com.nnvmso.model.AwsMessage;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.Slideshow;

@Controller
@RequestMapping("test")
public class TestController {
	@ExceptionHandler(NullPointerException.class)
	public @ResponseBody String nullPointer(NullPointerException e) {
		System.out.println("enter null pointer");
		return "/error/nullPointer";
	}
	
	
	@RequestMapping("nullPointer")
	public String throwNullPointer() {
		System.out.println("throws!");
		throw new NullPointerException();
	}
	
	@RequestMapping("slideshow")
	public String slideshow() {
		String createDate = new Date().toString();
		String bucket = "com-aws";
		String key = "aglub19hcHBfaWRyDgsSB0NvbnRlbnQYqQEM";
		String token = "";
		String mpeg4FileUrl = "http://mpeg4FileUrl/";
		String webMFileUrl = "http://web/";
		String errorCode = "3"; 
		Slideshow slideshow = new Slideshow();
		String[] files = {"1.jpg", "2.jpg", "3.jpg"};
		String[] audios = {"1.wav", "2.wav"};
		slideshow.setSlides(files);
		slideshow.setAudios(audios);
		slideshow.setSlideinfo("a.cvs");		
		AwsMessage msg = new AwsMessage(bucket, key, (new Date()).toString(), token);
		msg.setType(MsoProgram.TYPE_SLIDESHOW);
		msg.setFileUrl(mpeg4FileUrl);
		msg.setSlideshow(slideshow);		
		String urlStr = "http://localhost:8888/aws/contentUpdate";
        URL url;
		try {
			url = new URL(urlStr);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.writeValue(writer, msg);
	        System.out.println(DebugLib.OUT + "json:" + mapper.writeValueAsString(msg));	        
	        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {	        	
	        	System.out.println("response not ok!" + connection.getResponseCode());
	        }
	        writer.close();	        
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return "hello";
	}
	
	//trigger /aws/contentUpdate, simulate 9x9.tv api	
	@RequestMapping("contentUpdate")
	public String contentUpdate() {
		String createDate = new Date().toString();
		String bucket = "com-aws";		
		String key = "aglub19hcHBfaWRyDgsSB0NvbnRlbnQYogEM";
		String token = "";
		String mpeg4FileUrl = "http://mpeg4FileUrl";
		String webMFileUrl = "http://web";
		String errorCode = "3"; 
		AwsMessage msg = new AwsMessage(bucket, key, (new Date()).toString(), token);
		msg.setFileUrl(webMFileUrl);

		String urlStr = "http://localhost:8888/aws/contentUpdate";
        URL url;
		try {
			url = new URL(urlStr);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.writeValue(writer, msg);
	        System.out.println(DebugLib.OUT + "json:" + mapper.writeValueAsString(msg));	        
	        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {	        	
	        	System.out.println("response not ok!" + connection.getResponseCode());
	        }
	        writer.close();	        
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return "hello";
	} 
}
