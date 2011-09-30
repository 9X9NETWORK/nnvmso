package com.nnvmso.web.admin;

import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Address;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.service.InitService;

/**
 * for testing only, works only for small set of data
 * 
 * most of the functions are private, turned it on if you need them.
 */

//wipe out data: Category, CategoryChannel, CategoryChannelSet, ChannelSet, ChannelSetChannel

//change MsoChannel Schema 
//ChannelStatusMapper (mark all the channels to waiting_approval status)
//initChannelsToTask (mark selected channel to good status)
//mapreduce, updateFtsMapper (update channel fts)

//initSetsToTask
//initCategoriesToTask
//initSetAndChannelsToTask
//initCategoryAndSetsToTask
//initRecommdned
//initSetImagesToTask
//initCategoryCount
//initMso

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
			initService.initAll(false);
		}
		return "admin/groundStart";
	}	

	@RequestMapping("file")
	public ResponseEntity<String> file() {
		return NnNetUtil.textReturn("You will receive an email when it isdone.");
	}
	
	
	//gae environment	
	@RequestMapping("initChannelsToTask")
	public ResponseEntity<String> initChannelsToTask() {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initChannels"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initChannels")
	public ResponseEntity<String> initChannels(HttpServletRequest req) {
		initService.setRequest(req);
		initService.initChannels(true);
		this.sendEmail("init all the channels done");
		return NnNetUtil.textReturn("OK");		
	}
		
	@RequestMapping("initSetsToTask")
	public ResponseEntity<String> initSetsToTask() {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initSets"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initSets")
	public ResponseEntity<String> initSets(HttpServletRequest req) {
		initService.setRequest(req);
		initService.initSets();
		this.sendEmail("init all the sets done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initCategoriesToTask")
	public ResponseEntity<String> initCategoriesToTask() {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initCategories"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initCategories")
	public ResponseEntity<String> initCategories(HttpServletRequest req) {
		initService.setRequest(req);
		initService.initCategories();
		this.sendEmail("init all the categories done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initSetAndChannelsToTask")
	public ResponseEntity<String> initSetAndChannelsToTask() {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initSetAndChannels"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initSetAndChannels")
	public ResponseEntity<String> initSetAndChannels(HttpServletRequest req) {
		initService.setRequest(req);
		initService.initSetAndChannels();
		this.sendEmail("init all the SetsAndChannels done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initCategoryAndSetsToTask")
	public ResponseEntity<String> initCategoryAndSetsToTask() {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initCategoryAndSets"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initCategoryAndSets")
	public ResponseEntity<String> initCategoryAndSets(HttpServletRequest req) {
		initService.setRequest(req);
		initService.initCategoryAndSets();
		this.sendEmail("init all the CategoryAndSets done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initRecommdned")
	public ResponseEntity<String> initRecommended(HttpServletRequest req) {
		initService.setRequest(req);
		initService.initRecommended();
		this.sendEmail("init all the Recommended done");
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
		this.sendEmail("init all the initSetImagesToTask done");
		return NnNetUtil.textReturn("OK");		
	}

	//temp fix
	@RequestMapping("initCategoryCount")
	public ResponseEntity<String> initCategoryCount(HttpServletRequest req) {
		initService.setRequest(req);
		initService.initCategoryCount();
		this.sendEmail("init all the CategoryCount done");
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
	@RequestMapping("deleteChannelsToTask")
	public ResponseEntity<String> deleteChannelsToTask() {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/deleteChannels"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	//temp fix
	@RequestMapping("deleteChannels")
	public ResponseEntity<String> deleteChannels(HttpServletRequest req) {
		initService.setRequest(req);
		initService.deleteUrls();
		this.sendEmail("delete all the channels done");
		return NnNetUtil.textReturn("OK");		
	}
	
	@RequestMapping("testEmail")
	public ResponseEntity<String> testEmail(HttpServletRequest req) {
		log.info("test email here");
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String msgBody = "remove setFrom";
        try {
        	Message msg = new MimeMessage(session);
//        	msg.setFrom(new InternetAddress("gaeadmin@9x9.tv", "yiwen"));
        	Address addr = new InternetAddress("yiwen@teltel.com", "yiwen"); 
        	Address addrs[] = {addr};
        	//msg.addFrom(addrs);
        	msg.setReplyTo(addrs);
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress("nncloudtv@gmail.com", "nncloudtv"));            
        	msg.setHeader("From", "yiwen@teltel.com");
        	msg.setHeader("From_Alias", "yiwen");
        	msg.setHeader("Sender", "gaeadmin@9x9.tv");
        	msg.setHeader("Sender_Alias", "gaeadmin");
        	msg.setHeader("sender", "gaeadmin@9x9.tv");
        	msg.setHeader("sender_Alias", "gaeadmin");
            msg.setSubject("domain test");
            if (msg.getHeader("sender") != null)
            	System.out.println("sender:" + msg.getHeader("sender")[0]);
            if (msg.getHeader("Sender") != null)
            	System.out.println("Sender:" + msg.getHeader("Sender")[0]);            
            if (msg.getHeader("from") != null)
            	System.out.println("from:" + msg.getHeader("from")[0]);
            msg.setText(msgBody);            
            Transport.send(msg);
        } catch (Exception e) {
        	NnLogUtil.logException(e);
		}							
		return NnNetUtil.textReturn("OK");		
	}	
	
	public void sendEmail(String subject) {
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String msgBody = "done";
        try {
        	Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("admin@gmail.com", "nncloudtv"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress("nncloudtv@gmail.com", "nncloudtv"));                             
            msg.setSubject(subject);
            msg.setText(msgBody);
            Transport.send(msg);
        } catch (Exception e) {
        	NnLogUtil.logException(e);
		}					
	}		
	
}
