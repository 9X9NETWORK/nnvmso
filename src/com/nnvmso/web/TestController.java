package com.nnvmso.web;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.DebugLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.AwsMessage;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.PodcastChannel;
import com.nnvmso.model.PodcastProgram;
import com.nnvmso.model.ProgramScript;
import com.nnvmso.model.Slideshow;
import com.nnvmso.service.ChannelManager;
import com.nnvmso.service.ProgramManager;

@Controller
@RequestMapping("test")
public class TestController {
	@ExceptionHandler(NullPointerException.class)
	public @ResponseBody String nullPointer(NullPointerException e) {
		System.out.println("enter null pointer");
		return "/error/nullPointer";
	}

	@RequestMapping("set")
	public String settype() {
		ProgramManager programMngr = new ProgramManager();
		programMngr.findAllAndSetWhatever();
		return "hello/hello";		
	}
	
	@RequestMapping("ip")
	public String ip(HttpServletRequest req) {
		System.out.println("hostname=" + req.getLocalAddr() + ";" + req.getLocalPort() + ";" + req.getRequestURI());
		return "hello";
	}
	
	@RequestMapping("nnscript")
	public ResponseEntity<String> nnScript(@RequestParam(value="program") long programId)
	{
		ProgramManager service = new ProgramManager();
		String script = service.findGroupById(programId).getNnScript().getScript().getValue();
		
		System.out.println(DebugLib.OUT + script);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(script, headers, HttpStatus.OK);		
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
		String key = "aghubmUzdm1zb3IRCxIKTXNvUHJvZ3JhbRi2BAw";
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
		msg.setThumbnail("thumbnail");
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
	
	@RequestMapping("podcast")
	public String podcast() {
		PodcastChannel podcast = new PodcastChannel();
		podcast.setTitle("title");
		podcast.setImage("http://image/abc");
		podcast.setDescription("description");
		PodcastProgram[] items = new PodcastProgram[2];
		PodcastProgram p1 = new PodcastProgram();
		p1.setTitle("item1");
		p1.setDescription("description1");
		p1.setEnclosure("http://file1");
		p1.setImage("http://image1");
		PodcastProgram p2 = new PodcastProgram();
		p2.setTitle("item2");
		p2.setDescription("description2");
		p2.setEnclosure("http://file2");
		p2.setImage("http://image2");
		items[0] = p1;
		items[1] = p2;
		podcast.setItems(items);
		
		String urlStr = "http://localhost:8888/podcast/create";
        URL url;
		try {
			url = new URL(urlStr);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.writeValue(writer, podcast);
	        System.out.println(DebugLib.OUT + "json:" + mapper.writeValueAsString(podcast));	        
	        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {	        	
	        	System.out.println("response not ok!" + connection.getResponseCode());
	        }
	        writer.close();	        
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		
		return "hello/hello";
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
