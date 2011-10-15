package com.nnvmso.web.admin;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.InitService;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.TranscodingService;
import com.nnvmso.web.json.transcodingservice.PostUrl;
import com.nnvmso.web.json.transcodingservice.RtnProgram;

/**
 * for testing only, works only for small set of data
 * 
 * most of the functions are private, turned it on if you need them.
 */


/* first run */
//wipe out data: Category, CategoryChannel, CategoryChannelSet, ChannelSet, ChannelSetChannel
//change MsoChannel Schema 
//initChannelsToTask (mark selected channel to good status)
//mapreduce, updateFtsMapper (update channel fts)
//initSetsToTask?isEnglish=true&isDevel=false
//initChannelsToTask
//initCategoriesToTask
//initSetAndChannelsToTask
//initCategoryAndSetsToTask
//initRecommdned
//initSetImagesToTask
//initCategoryCount
//initMso

/* 2nd run */
//wipe out data: Category, CategoryChannel, CategoryChannelSet, ChannelSet, ChannelSetChannel
//ChannelStatusMapper (mark all the channels to waiting_approval status)

//initSetsToTask?isEnglish=true&isDevel=false
//initChannelsToTask?isEnglish=true&isDevel=false
//initCategoriesToTask?isEnglish=true&isDevel=false
//initSetAndChannelsToTask?isEnglish=true&isDevel=false
//initCategoryAndSetsToTask?isEnglish=true&isDevel=false
//initRecommdned?isEnglish=true
//initSetImagesToTask?isEnglish=true

//initSetsToTask?isEnglish=false&isDevel=false
//initChannelsToTask?isEnglish=false&isDevel=false
//initCategoriesToTask?isEnglish=false&isDevel=false
//initSetAndChannelsToTask?isEnglish=false&isDevel=false
//initCategoryAndSetsToTask?isEnglish=false&isDevel=false
//initRecommdned?isEnglish=false
//initSetImagesToTask?isEnglish=false

//initCategoryCountToTask

@Controller
@RequestMapping("admin/init")
public class AdminInitController {
	protected static final Logger log = Logger.getLogger(AdminInitController.class.getName());		
	
	private final InitService initService;		
	
	@Autowired
	public AdminInitController(InitService initService) {
		this.initService = initService;
	}		
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		if (e.getClass().equals(MissingServletRequestParameterException.class) ||
			e.getClass().equals(IllegalStateException.class)) {
		} else {
			NnLogUtil.logException(e);			
		}
		return "error/exception";				
	}
	
	//local machine
	@RequestMapping(value="groundStart", method=RequestMethod.GET)
	public String groundStartGet(HttpServletRequest req) {
		return "admin/groundStart";
	}
	
	@RequestMapping(value="groundStart", method=RequestMethod.POST)
	public String groundStartPost(HttpServletRequest req) {
		String host = NnNetUtil.getUrlRoot(req);
		if (host.equals("http://localhost:8888")) {
			initService.initAll(false, true);
		}
		return "admin/groundStart";
	}	

	@RequestMapping("file")
	public ResponseEntity<String> file() {
		return NnNetUtil.textReturn("You will receive an email when it isdone.");
	}
	
	
	//gae environment	
	@RequestMapping("initChannelsToTask")
	public ResponseEntity<String> initChannelsToTask(
			@RequestParam(value="isDevel",required=false) boolean isDevel) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initChannels")
			         .param("isDevel", String.valueOf(isDevel))			      			      
		);			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initChannels")
	public ResponseEntity<String> initChannels(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,			
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initChannels(isEnglish, isDevel);
		this.sendEmail("init all the channels done", "done");
		return NnNetUtil.textReturn("OK");		
	}
		
	@RequestMapping("initSetsToTask")
	public ResponseEntity<String> initSetsToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initSets")
			        .param("isEnglish", String.valueOf(isEnglish))   
			        .param("isDevel", String.valueOf(isDevel))			        
			      );			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initSets")
	public ResponseEntity<String> initSets(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initSets(isEnglish, isDevel);
		this.sendEmail("init all the sets done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initCategoriesToTask")
	public ResponseEntity<String> initCategoriesToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel			
			) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initCategories")
			        .param("isEnglish", String.valueOf(isEnglish))   
			        .param("isDevel", String.valueOf(isDevel))			        			      
			      );			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initCategories")
	public ResponseEntity<String> initCategories(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish, 
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initCategories(isEnglish);
		this.sendEmail("init all the categories done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initSetAndChannelsToTask")
	public ResponseEntity<String> initSetAndChannelsToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {									
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initSetAndChannels")
			        .param("isEnglish", String.valueOf(isEnglish))   
			        .param("isDevel", String.valueOf(isDevel))			        			      			      
			      );			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initSetAndChannels")
	public ResponseEntity<String> initSetAndChannels(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,				
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initSetAndChannels(isEnglish);
		this.sendEmail("init all the SetsAndChannels done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initCategoryAndSetsToTask")
	public ResponseEntity<String> initCategoryAndSetsToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {										
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initCategoryAndSets")
			        .param("isEnglish", String.valueOf(isEnglish))   
			        .param("isDevel", String.valueOf(isDevel)));			        			      			      			      
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initCategoryAndSets")
	public ResponseEntity<String> initCategoryAndSets(
		@RequestParam(value="isEnglish",required=false) boolean isEnglish,
		@RequestParam(value="isDevel",required=false) boolean isDevel,				
		HttpServletRequest req) {
		initService.setRequest(req);
		initService.initCategoryAndSets(isEnglish);
		this.sendEmail("init all the CategoryAndSets done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initRecommdned")
	public ResponseEntity<String> initRecommended(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initRecommended(isEnglish);
		this.sendEmail("init all the Recommended done", "done");
		return NnNetUtil.textReturn("OK");		
	}
	
	@RequestMapping("initSetImagesToTask")
	public ResponseEntity<String> initSetImagesToTask(HttpServletRequest req) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initSetImages"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}	

	@RequestMapping("initSetImages")
	public ResponseEntity<String> initSetImages(HttpServletRequest req) {
		initService.setRequest(req);
		initService.initSetImages();
		this.sendEmail("init all the initSetImagesToTask done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initCategoryCountToTask")
	public ResponseEntity<String> initCategoryCountToTask(HttpServletRequest req) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initCategoryCount"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}	
	
	//temp fix
	@RequestMapping("initCategoryCount")
	public ResponseEntity<String> initCategoryCount(HttpServletRequest req) {
		initService.setRequest(req);
		initService.initCategoryCount();
		this.sendEmail("init all the CategoryCount done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	//temp fix
	@RequestMapping("addMsoConfig")
	public ResponseEntity<String> initMso(HttpServletRequest req) {
		initService.setRequest(req);
		initService.addMsoConfig();
		return NnNetUtil.textReturn("OK");		
	}

	//temp fix
	@RequestMapping("badChannelReportToTask")
	public ResponseEntity<String> badChannelReportToTask() {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/badChannelReport"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("mapleTest")
	public ResponseEntity<String> mapleTest(HttpServletRequest req) {
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findByEmail("mso@9x9.tv");		
		MsoChannelManager channelMngr = new MsoChannelManager();
			
		String url="http://www.maplestage.net/show/國民女王/";
		MsoChannel c = channelMngr.findBySourceUrlSearch(url);
		if (c == null) {					
			c = new MsoChannel(url, user.getKey().getId());
			c.setStatus(MsoChannel.STATUS_PROCESSING);
			c.setContentType(channelMngr.getContentTypeByUrl(url));
			channelMngr.create(c);
			TranscodingService tranService = new TranscodingService();
			tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);
			channelMngr.save(c);
		}
		return NnNetUtil.textReturn("OK");		
	}				
	
	//temp fix
	@RequestMapping("postTest")
	public ResponseEntity<String> postTest(HttpServletRequest req) {
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findByEmail("mso@9x9.tv");		
		MsoChannelManager channelMngr = new MsoChannelManager();
			
		String url="http://www.maplestage.net/show/台灣演义/";
		PostUrl postUrl = new PostUrl();
		postUrl.setRss(url);	
		//String transcodingServer = "http://puppy.9x9.tv/admin/init/mapleReceive";
		String transcodingServer = "http://puppy.9x9.tv/playerAPI/mapleReceive";
		NnNetUtil.urlPostWithJson(transcodingServer, postUrl);
//		MsoChannel c = channelMngr.findBySourceUrlSearch(url);
//		if (c == null) {					
//			log.info("create maple:" + url);
//		}

		return NnNetUtil.textReturn("OK");		
	}		

	//temp fix
	@RequestMapping("missingUser")
	public ResponseEntity<String> missingUser() {
		NnUserManager userMngr = new NnUserManager();
		NnUser user = new NnUser("mso@9x9.tv", "9x9mso", "9x9 mso", NnUser.TYPE_NN);
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findNNMso();
		user.setMsoId(mso.getKey().getId());
		userMngr.create(user, null);
		return NnNetUtil.textReturn("OK");		
	}		
	
	//temp fix
	@RequestMapping("badChannelReport")
	public ResponseEntity<String> badChannelReport() {
		String report = initService.reportBadChannels();
		System.out.println(report);
		this.sendEmail("badChannelReport", report);
		return NnNetUtil.textReturn("OK");		
	}		
	
	public void sendEmail(String subject, String msgBody) {
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
        	Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("nncloudtv@gmail.com", "nncloudtv"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress("nncloudtv@gmail.com", "nncloudtv"));                             
            msg.setSubject(subject);
            msg.setText(msgBody);
            Transport.send(msg);
        } catch (Exception e) {
        	NnLogUtil.logException(e);
		}					
	}
	
}
