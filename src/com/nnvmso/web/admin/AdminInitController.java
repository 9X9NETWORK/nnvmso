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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.service.EmailService;
import com.nnvmso.service.InitService;

/**
 * for testing only, works only for small set of data
 * 
 * most of the functions are private, turned it on if you need them.
 */	
@Controller
@RequestMapping("admin/init")
public class AdminInitController {
	protected static final Logger log = Logger.getLogger(AdminInitController.class.getName());		
	
	private final InitService initService;		
	private final EmailService emailService;
	
	@Autowired
	public AdminInitController(InitService initService, EmailService emailService) {
		this.initService = initService;
		this.emailService = emailService;
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
		
	@RequestMapping(value="groundStart", method=RequestMethod.GET)
	public String groundStartGet(HttpServletRequest req) {
		return "admin/groundStart";
	}
	
	@RequestMapping(value="groundStart", method=RequestMethod.POST)
	public String groundStartPost(HttpServletRequest req) {
		String host = NnNetUtil.getUrlRoot(req);
		if (host.equals("http://localhost:8888")) {
			initService.setRequest(req);
			initService.initAll(true, true, false);
		}
		return "admin/groundStart";
	}	

	public void sendEmail(String subject) {
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
	
	@SuppressWarnings("unused")
	@RequestMapping("initMso1Channels")
	private ResponseEntity<String> initMso1Channels(@RequestParam boolean devel, @RequestParam boolean trans, HttpServletRequest req) {
		initService.setRequest(req);
		initService.createMso1DefaultChannels(devel, trans);
		this.sendEmail("step3 done: create 9x9 channels");
		return NnNetUtil.textReturn("OK");
	}
	
	@SuppressWarnings("unused")
	@RequestMapping("initMso2Channels")
	private ResponseEntity<String> initMso2Channels(@RequestParam boolean devel, @RequestParam boolean trans, HttpServletRequest req) {
		initService.setRequest(req);
		initService.createMso2DefaultChannels(devel, trans);
		this.sendEmail("step4 done: create 5f channels");
		return NnNetUtil.textReturn("OK");
	}
	
	@SuppressWarnings("unused")
	@RequestMapping("initMso1Ipg")
	private ResponseEntity<String> initMso1Ipg(@RequestParam boolean devel, HttpServletRequest req) {
		initService.setRequest(req);
		initService.createMso1DefaultIpg(devel);
		return NnNetUtil.textReturn("OK");
	}
		
	@SuppressWarnings("unused")
	@RequestMapping("initMso2Ipg")
	private ResponseEntity<String> initMso2Ipg(@RequestParam boolean devel, HttpServletRequest req) {
		initService.setRequest(req);
		initService.createMso2DefaultIpg(devel);
		this.sendEmail("step5 done: 9x9 and 5f default ipgs");
		return NnNetUtil.textReturn("OK");
	}
	
	@RequestMapping("initMso3Channels")
	public ResponseEntity<String> initMso3Channels(@RequestParam boolean devel, @RequestParam boolean trans, HttpServletRequest req) {
		initService.setRequest(req);
		initService.createMso3OwnedChannels(devel, trans);
		this.sendEmail("step6 done: create daai channels");
		return NnNetUtil.textReturn("OK");
	}
	
	@RequestMapping("initMso3ChannelSet")
	public ResponseEntity<String> initMso3ChannelSet(@RequestParam boolean devel, HttpServletRequest req) {
		initService.setRequest(req);
		initService.createMso3ChannelSet(devel);
		this.sendEmail("step7 done: daai channel set");
		return NnNetUtil.textReturn("OK");
	}
	
	@SuppressWarnings("unused")
	@RequestMapping("initProStep2")
	private ResponseEntity<String> initProStep2(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		initService.setRequest(req);
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initMsoAndCategories")
			          .param("debug", String.valueOf(debug)));
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}

	@SuppressWarnings("unused")
	@RequestMapping("initProStep3")
	private ResponseEntity<String> initProStep3(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		initService.setRequest(req);
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initMso1Channels")
			         .param("devel", String.valueOf(devel))
			         .param("trans", String.valueOf(trans)));
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}

	@SuppressWarnings("unused")
	@RequestMapping("initProStep4")
	private ResponseEntity<String> initProStep4(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		initService.setRequest(req);
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initMso2Channels")
			         .param("devel", String.valueOf(devel))
			         .param("trans", String.valueOf(trans)));		
		return NnNetUtil.textReturn("You will receive an email when it is done.\nDo no proceed to step5 until all the channels are ready.");
	}

	@SuppressWarnings("unused")
	@RequestMapping("initProStep5")
	private ResponseEntity<String> initProStep5(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		initService.setRequest(req);
		QueueFactory.getDefaultQueue().add(
	      TaskOptions.Builder.withUrl("/admin/init/initMso1Ipg")
	         .param("devel", String.valueOf(devel)));

		QueueFactory.getDefaultQueue().add(
	      TaskOptions.Builder.withUrl("/admin/init/initMso2Ipg")
	         .param("devel", String.valueOf(devel)));		
		
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initProStep6")
	public ResponseEntity<String> initProStep6(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		initService.setRequest(req);
		initService.initializeMso3AndCategories(debug);
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initMso3Channels")
			         .param("devel", String.valueOf(devel))
			         .param("trans", String.valueOf(trans)));
		
		return NnNetUtil.textReturn("You will receive an email when it is done.\nDo no proceed to step7 until all the channels are ready.");
	}
	
	@RequestMapping("initProStep7")
	public ResponseEntity<String> initProStep7(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		initService.setRequest(req);
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initMso3ChannelSet")
			         .param("devel", String.valueOf(devel)));
		
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	/**
	 * Example: admin/init/initProTask?devel=0&trans=1&debug=1"	
	 * @param debug whether to turn on player's debugging information
	 * @param devel whether to use test data (versus data from PM)
	 * @param trans whether to submit data to transcoding service. turn off for internal data integretiy testing.
	 */	
	@SuppressWarnings("unused")
	@RequestMapping("initProStep1")
	private ResponseEntity<String> initProTask(@RequestParam boolean devel, @RequestParam boolean trans, @RequestParam boolean debug, HttpServletRequest req) {
		initService.setRequest(req);
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/deleteAll"));
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}		
	
	@SuppressWarnings("unused")
	@RequestMapping("changeMso")
	private ResponseEntity<String> changeMso(@RequestParam(value="mso")String mso, HttpServletResponse resp) {
		CookieHelper.setCookie(resp, CookieHelper.MSO, mso);
		return NnNetUtil.textReturn("OK");
	}
	
	@SuppressWarnings("unused")
	@RequestMapping("emailTest")
	private ResponseEntity<String> emailTest() {
		emailService.sendEmailToAdmin("email test done", "done");
		return NnNetUtil.textReturn("OK");
	}
		
	@SuppressWarnings("unused")
	@RequestMapping("email")
	private ResponseEntity<String> emailTask() {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/emailTest"));
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}

	@SuppressWarnings("unused")
	@RequestMapping("deleteAll")
	private ResponseEntity<String> initialize(HttpServletRequest req) {
		initService.setRequest(req);
		initService.deleteAll();
		this.sendEmail("step1 done: delete all");
		return NnNetUtil.textReturn("OK");
	}
 
	@SuppressWarnings("unused")
	@RequestMapping("initMsoAndCategories")
	private ResponseEntity<String> initMsoAndCategories(HttpServletRequest req, @RequestParam(value="debug")boolean debug) {
		initService.setRequest(req);
		initService.initMsoAndCategories(debug);
		this.sendEmail("step2 done: initiate mso and categories");
		return NnNetUtil.textReturn("OK");		
	}
	
}
