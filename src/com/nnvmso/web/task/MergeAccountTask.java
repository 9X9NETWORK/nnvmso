package com.nnvmso.web.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
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
	public ResponseEntity<String> removeGuest(@RequestParam("start")int start) 
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
		int end = start + 400;
		if (end > users.size()) 
			end = users.size();
		for (int i=start; i<start+400; i++) {	
			List<Subscription> list = new ArrayList<Subscription>();
			list = subMngr.findAllByUser(users.get(i).getKey().getId());
			for (Subscription s : list) {
				s.setType((short)10);
			}
			subMngr.saveAll(list);
		}
		return NnNetUtil.textReturn(output);
	}

	//entry
	@RequestMapping("removeSub")
	public ResponseEntity<String> removeSub() throws IOException {
		try {						
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/task/account/runRemoveSub")
		    );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping(value="runRemoveSub")
	public ResponseEntity<String> runRemoveSub(HttpServletRequest req) {
		String output = "";
		NnUserManager userMngr = new NnUserManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		SubscriptionManager subMngr = new SubscriptionManager();
		List<NnUser> users = userMngr.findAll();
		for (NnUser user : users) {
			List<Subscription> list = subMngr.findAllByUser(user.getKey().getId());
			List<Subscription> deleted = new ArrayList<Subscription>();
			for (Subscription s : list) {
				MsoChannel c = channelMngr.findById(s.getChannelId());
				if (c.getContentType() == MsoChannel.CONTENTTYPE_SYSTEM || c.getContentType() == MsoChannel.CONTENTTYPE_PODCAST) {
					deleted.add(s);
				}
			}			
			subMngr.deleteAll(deleted);
		}
		return NnNetUtil.textReturn(output);
	}
	

}
