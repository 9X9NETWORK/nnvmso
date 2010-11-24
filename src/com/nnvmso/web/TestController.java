package com.nnvmso.web;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.CookieGenerator;

import com.nnvmso.json.AwsMessage;
import com.nnvmso.json.PodcastChannel;
import com.nnvmso.json.PodcastFeed;
import com.nnvmso.json.PodcastItem;
import com.nnvmso.json.PodcastProgram;
import com.nnvmso.json.Slideshow;
import com.nnvmso.lib.*;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.PodcastService;
import com.nnvmso.service.ProgramManager;

@Controller
@RequestMapping("test")
public class TestController {

	@RequestMapping("deleteCookie")
	public String deleteCookie(HttpServletResponse resp) {
		CookieHelper.deleteCookie(resp, "user");
		return "hello/hello";
	}
	
	@RequestMapping("validateRss")
	public String validateRss() {
		PodcastService service = new PodcastService();
		String[] podcastInfo = service.getPodcastInfo("http://podcast.msnbc.com/audio/podcast/MSNBC-MTP-NETCAST-M4V.xml");
		System.out.println(podcastInfo[0] + ";" + podcastInfo[1] + ";" + podcastInfo[2]);
		//service.validateRSS("http://channel9.msdn.com/feeds/rss");
		//service.validateRSS("http://www.mevio.com/feeds/geekbrief.xml"); //contenttype=text/xml;charset=utf-8
		//service.validateRSS("http://m.podshow.com/media/365/episodes/237452/geekbrief-237452-06-25-2010.mp4");
		return "hello/hello";
	}
	
	//1. create a podcast from my own portal
	//2. simulate a channel udpate
	//3. simulate a program update
	//4. simulate an enclosure update
	@RequestMapping("channelUpdate")
	public String channelUpdate(@RequestParam(value="key") String channelKey) {
		PodcastChannel c = new PodcastChannel();
		c.setKey(channelKey);		
		c.setTitle("channel1");
		
		String url = "http://localhost:8888/podcastAPI/channelUpdate";
		NnLib.urlPostWithJson(url, c);
		return "";
	}
	
	@RequestMapping("itemCreate")
	public void programUpdate(@RequestParam(value="key") String channelKey) {
		PodcastProgram program = new PodcastProgram();
		program.setKey(channelKey);
		program.setAction(PodcastProgram.ACTION_UPDATE_ITEM);
		PodcastItem item = new PodcastItem();
		item.setTitle("item1");
		item.setDescription("i am item1");
		item.setEnclosure("mpeg4");
		program.setItem(item);
		String url = "http://localhost:8888/podcastAPI/itemUpdate";
		NnLib.urlPostWithJson(url, program);
	}

	@RequestMapping("itemModify")
	public void programModify(@RequestParam(value="key") String itemKey) {
		PodcastProgram program = new PodcastProgram();		
		program.setAction(PodcastProgram.ACTION_UPDATE_ENCLOSURE);		
		PodcastItem item = new PodcastItem();
		program.setKey("");
		program.setItemKey(itemKey);
		item.setEnclosure("webm");
		program.setItem(item);
		String url = "http://localhost:8888/podcastAPI/itemUpdate";
		NnLib.urlPostWithJson(url, program);
		System.out.println("back to original modify");		
	}	
	
	@RequestMapping("playerUserLogin")
	public String playerUserLogin() {
		return "test/playerUserLogin";
	}
	
	@RequestMapping("playerUserSignup")
	public String playerUserSignup() {		
		return "test/playerUserSignup";
	}
	
	@RequestMapping("playerPodcast") 
	public String playerPodcast() {
		return "test/playerPodcast";
	}
	
	@RequestMapping("mycookie") 
	public String mycookie(HttpServletResponse resp, HttpServletRequest req) {
		CookieHelper.setCookie(resp, "test", "bla");
		System.out.println("result=" + CookieHelper.getCookie(req, "bla"));		
		return "hello/hello";
	}
	
	@RequestMapping("cookie")
	public String cookie(HttpServletResponse resp) {
		CookieGenerator cookie = new CookieGenerator();
		System.out.println(cookie.getCookieName());
		
		cookie.setCookieDomain("mycookiedomain");
		cookie.setCookieName("user");
		cookie.addCookie(resp, "hello");
		System.out.println(cookie.getCookieName());
		return "hello/hello";
	}
	
	@RequestMapping("rssPost")	
	public String podcastpost(@RequestBody PodcastFeed feed) {
		System.out.println(feed.getKey());
		System.out.println(feed.getRss());
		return "hello";
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
		
	@ExceptionHandler(NullPointerException.class)
	public String nullPointer(NullPointerException e) {
		System.out.println("enter null pointer");
		return "hello/hello";
	}
	
	@RequestMapping("nullPointer")
	public String throwNullPointer() {
		System.out.println("null pointer throws!");
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
	
	/*
	@RequestMapping("podcast")
	public String podcast() {
		PodcastChannel podcast = new PodcastChannel();
		podcast.setTitle("title");
		podcast.setImage("http://image/abc");
		podcast.setDescription("description");
		PodcastItem[] items = new PodcastItem[2];
		PodcastItem p1 = new PodcastItem();
		p1.setTitle("item1");
		p1.setDescription("description1");
		p1.setEnclosure("http://file1");
		p1.setImage("http://image1");
		PodcastItem p2 = new PodcastItem();
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
	*/
	
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
