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
		this.sendEmail("step1 done: delete all");
		return NnNetUtil.textReturn("OK");
	}
 
	@RequestMapping("initMsoAndCategories")
	public ResponseEntity<String> initMsoAndCategories(HttpServletRequest req, @RequestParam(value="debug")boolean debug) {
		initService.setRequest(req);
		initService.initMsoAndCategories(debug);
		this.sendEmail("step2 done: initiate mso and categories");
		return NnNetUtil.textReturn("OK");		
	}
		
	/**
	 * @param debug whether to turn on player's debugging information
	 */
	@RequestMapping("initDevel")
	public ResponseEntity<String> initAllDevel(@RequestParam(value="debug")boolean debug, HttpServletRequest req) { 
		initService.setRequest(req);
		initService.initAll(true, debug, false);
		return NnNetUtil.textReturn("OK");		
	}
	
	private void sendEmail(String subject) {
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String msgBody = "done";
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
	
	@RequestMapping("initMso1Channels")
	public ResponseEntity<String> initMso1Channels(@RequestParam boolean devel, @RequestParam boolean trans, HttpServletRequest req) {
		initService.setRequest(req);
		initService.createMso1DefaultChannels(devel, trans);
		this.sendEmail("step3 done: create 9x9 channels");
		return NnNetUtil.textReturn("OK");
	}
	
	@RequestMapping("initMso2Channels")
	public ResponseEntity<String> initMso2Channels(@RequestParam boolean devel, @RequestParam boolean trans, HttpServletRequest req) {
		initService.setRequest(req);
		initService.createMso2DefaultChannels(devel, trans);
		this.sendEmail("step4 done: create 5f channels");
		return NnNetUtil.textReturn("OK");
	}
	
	@RequestMapping("initMso1Ipg")
	public ResponseEntity<String> initMso1Ipg(@RequestParam boolean devel, HttpServletRequest req) {
		initService.setRequest(req);
		initService.createMso1DefaultIpg(devel);
		return NnNetUtil.textReturn("OK");
	}
		
	@RequestMapping("initMso2Ipg")
	public ResponseEntity<String> initMso2Ipg(@RequestParam boolean devel, HttpServletRequest req) {
		initService.setRequest(req);
		initService.createMso2DefaultIpg(devel);
		this.sendEmail("step5 done: 9x9 and 5f default ipgs");
		return NnNetUtil.textReturn("OK");
	}
		
	@RequestMapping("initProStep2")
	public ResponseEntity<String> initProStep2(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		initService.setRequest(req);
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initMsoAndCategories")
			          .param("debug", String.valueOf(debug)));
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping("initProStep3")
	public ResponseEntity<String> initProStep3(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		initService.setRequest(req);
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initMso1Channels")
			         .param("devel", String.valueOf(devel))
			         .param("trans", String.valueOf(trans)));
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping("initProStep4")
	public ResponseEntity<String> initProStep4(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		initService.setRequest(req);
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initMso2Channels")
			         .param("devel", String.valueOf(devel))
			         .param("trans", String.valueOf(trans)));
		
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping("initProStep5")
	public ResponseEntity<String> initProStep5(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		initService.setRequest(req);
		QueueFactory.getDefaultQueue().add(
	      TaskOptions.Builder.withUrl("/admin/init/initMso1Ipg")
	         .param("devel", String.valueOf(devel)));

		QueueFactory.getDefaultQueue().add(
	      TaskOptions.Builder.withUrl("/admin/init/initMso2Ipg")
	         .param("devel", String.valueOf(devel)));		
		
		return NnNetUtil.textReturn("OK");
	}
	
	/**
	 * @param debug whether to turn on player's debugging information
	 * @param devel whether to use test data (versus data from PM)
	 * @param trans whether to submit data to transcoding service. turn off for internal data integretiy testing.
	 */	
	@RequestMapping("initProStep1")
	public ResponseEntity<String> initProTask(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		initService.setRequest(req);
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/deleteAll"));
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}		
	
	@RequestMapping("changeMso")
	public ResponseEntity<String> changeMso(@RequestParam(value="mso")String mso, HttpServletResponse resp) {
		CookieHelper.setCookie(resp, CookieHelper.MSO, mso);
		return NnNetUtil.textReturn("OK");
	}
}
