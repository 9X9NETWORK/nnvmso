package com.nnvmso.web.admin;

import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.service.InitService;

/**
 * for testing only, works only for small set of data
 */	
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

	@RequestMapping("deleteAll")
	public ResponseEntity<String> initialize(HttpServletRequest req) {
		initService.setRequest(req);
		initService.deleteAll();
		return NnNetUtil.textReturn("Done.\nYou might also want to use \"initMsoAndCategories\"?");
	}
 
	@RequestMapping("initMsoAndCategories")
	public ResponseEntity<String> initCategories(HttpServletRequest req, @RequestParam(value="debug")boolean debug) {
		initService.setRequest(req);
		initService.initMsoAndCategories(debug);
		return NnNetUtil.textReturn("OK");		
	}
		
	//devel mode, whether to use test data or production mode   
	//debug mode, whether to turn on player's debugging information
	@RequestMapping("initDevel")
	public ResponseEntity<String> initAll(@RequestParam(value="debug")boolean debug, HttpServletRequest req) { 
		initService.setRequest(req);
		initService.initAll(true, debug, false);
		return NnNetUtil.textReturn("OK");		
	}
	
	//intended to be executed as a task
	@RequestMapping("initPro")
	public ResponseEntity<String> initPro(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		log.info("init task kicked in");	

		initService.setRequest(req);
		initService.initAll(devel, debug, trans);
		log.info("init task done. trying to send out email now");		
		
		Properties props = new Properties();
		Session session = Session.getInstance(props);
		
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("nncloudtv@gmail.com", "nncloudtv"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress("nncloudtv@gmail.com", "nncloudtv"));                             
            msg.setSubject("init task is finished");
            msg.setText("init task is finished.");
            Transport.send(msg);
        } catch (Exception e) {
        	NnLogUtil.logException(e);
		}			
		
		return NnNetUtil.textReturn("OK");		
	}		
	
	@RequestMapping("initProTask")
	public ResponseEntity<String> initProTask(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug) {
		System.out.println(String.valueOf(devel));
		QueueFactory.getDefaultQueue().add(
		      TaskOptions.Builder.withUrl("/admin/init/initPro")
		         .param("devel", String.valueOf(devel))
		         .param("trans", String.valueOf(trans))
		         .param("debug", String.valueOf(debug)));
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}		
	
	@RequestMapping("changeMso")
	public ResponseEntity<String> changeMso(@RequestParam(value="mso")String mso, HttpServletResponse resp) {
		CookieHelper.setCookie(resp, CookieHelper.MSO, mso);
		return NnNetUtil.textReturn("OK");
	}
}
