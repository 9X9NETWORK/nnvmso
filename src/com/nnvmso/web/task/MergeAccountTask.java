package com.nnvmso.web.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.Subscription;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.SubscriptionManager;

@Controller
@RequestMapping("task/account")
public class MergeAccountTask {
	
	protected static final Logger log = Logger.getLogger(MergeAccountTask.class.getName());		
	
	//entry
	@RequestMapping("markSub")
	public ResponseEntity<String> markSub(@RequestParam("start")int start) 
			throws IOException {
		try {						
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/task/account/runMarkSub")
			         .param("start", String.valueOf(start))					
		    );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping(value="runMarkSub")
	public ResponseEntity<String> runMarkSub(
			@RequestParam("start")int start,
			HttpServletRequest req) {
		String output = "";
		NnUserManager userMngr = new NnUserManager();
		SubscriptionManager subMngr = new SubscriptionManager();
		List<NnUser> users = userMngr.findAll();
		log.info("user total:" + users.size());
		int end = start + 200;
		if (end > users.size()) 
			end = users.size();
		try {
			for (int i=start; i<end; i++) {	
				List<Subscription> list = new ArrayList<Subscription>();
				list = subMngr.findAllByUser(users.get(i).getKey().getId());
				for (Subscription s : list) {
					s.setType((short)10);
				}
				subMngr.saveAll(list);
			}
		} catch (Exception e) {
			return NnNetUtil.textReturn("exception happens");
		}
		this.sendEmail("runMarkSub", "done");
		return NnNetUtil.textReturn(output);
	}

	//entry
	@RequestMapping("removeSub")
	public ResponseEntity<String> removeSub(@RequestParam("start")int start) throws IOException {
		try {						
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/task/account/runRemoveSub")
					  .param("start", String.valueOf(start))
		    );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return NnNetUtil.textReturn("OK");
	}

	//entry
	@RequestMapping("userList")
	public ResponseEntity<String> userList(
			@RequestParam("page")int page,
			@RequestParam("limit")int limit
			) { 			
		NnUserManager userMngr = new NnUserManager();				
		List<NnUser> users = userMngr.list(page, limit, null, null);		
		String output = "user total:" + users.size();		
		return NnNetUtil.textReturn(output);
	}
	
	//remove podcast subscription
	@RequestMapping(value="runRemoveSub")
	public ResponseEntity<String> runRemoveSub(
			@RequestParam("start")int start,
			HttpServletRequest req) {
		String output = "";
		NnUserManager userMngr = new NnUserManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		SubscriptionManager subMngr = new SubscriptionManager();
		List<NnUser> users = userMngr.findUsers();
		log.info("user total:" + users.size());
		int end = start + 200;
		if (end > users.size()) 
			end = users.size();
		for (int i=start; i<end; i++) {	
			List<Subscription> list = subMngr.findAllByUser(users.get(i).getKey().getId());
			List<Subscription> deleted = new ArrayList<Subscription>();
			for (Subscription s : list) {
				MsoChannel c = channelMngr.findById(s.getChannelId());
				if (c == null)
					deleted.add(s); 
				if (c != null && (c.getContentType() == MsoChannel.CONTENTTYPE_SYSTEM || c.getContentType() == MsoChannel.CONTENTTYPE_PODCAST)) {
					deleted.add(s);
				}
			}			
			subMngr.deleteAll(deleted);
		}
		this.sendEmail("runRemoveSub" + start, "done");
		return NnNetUtil.textReturn(output);
	}

	//entry
	@RequestMapping("removeSubBad")
	public ResponseEntity<String> removeSubBad(@RequestParam("start")int start) throws IOException {
		try {						
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/task/account/runRemoveSubBad")
					  .param("start", String.valueOf(start))
		    );
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return NnNetUtil.textReturn("OK");
	}
	
	//remove podcast subscription
	@RequestMapping(value="runRemoveSubBad")
	public ResponseEntity<String> runRemoveSubBad(
			@RequestParam("start")int start,
			HttpServletRequest req) {
		String output = "";
		NnUserManager userMngr = new NnUserManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		SubscriptionManager subMngr = new SubscriptionManager();
		List<NnUser> users = userMngr.findUsers();
		log.info("user total:" + users.size());
		int end = start + 200;
		if (end > users.size()) 
			end = users.size();
		for (int i=start; i<end; i++) {	
			List<Subscription> list = subMngr.findAllByUser(users.get(i).getKey().getId());
			List<Subscription> deleted = new ArrayList<Subscription>();
			for (Subscription s : list) {
				MsoChannel c = channelMngr.findById(s.getChannelId());
				if (c == null)
					deleted.add(s); 
				if (c != null && (c.getStatus() == MsoChannel.STATUS_INVALID_FORMAT || c.getStatus() == MsoChannel.STATUS_NO_VALID_EPISODE)) {					
					deleted.add(s);
				}
			}			
			subMngr.deleteAll(deleted);
		}
		this.sendEmail("runRemoveSubBad" + start, "done");
		return NnNetUtil.textReturn(output);
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
