package com.nnvmso.web.task;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Category;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.Subscription;
import com.nnvmso.service.CategoryManager;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.SubscriptionManager;

@Controller
@RequestMapping("task/mergeAccount")
public class MergeAccountTask {
	
	protected static final Logger log = Logger.getLogger(MergeAccountTask.class.getName());
	
	
	//entry, entry, correct category's channelCount, categoryChannelCount -> runCategories -> runCategoryChannels
	//find non guest, go through each, if extra, remove the 9x9
	@RequestMapping("entry")
	public ResponseEntity<String> categoryChannelCount() throws IOException {
		try {						
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/task/mergeAccount/run")
		    );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return NnNetUtil.textReturn("OK");
	}

	//run through each category, to envoke categorychannel count
	@RequestMapping(value="run")
	public ResponseEntity<String> run(HttpServletRequest req) {
		String output = "";
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findByName("9x9");
		NnUserManager userMngr = new NnUserManager();
		SubscriptionManager subMngr = new SubscriptionManager();
		List<NnUser> users = userMngr.findNoneGuests();
		for (NnUser user : users) {
			List<NnUser> list = userMngr.findAllByEmail(user.getEmail());
			if (list.size() > 1) {
				for (NnUser u : list) {
					if (u.getMsoId() == mso.getKey().getId()) {
						List<Subscription> subs = subMngr.findAllByUser(u.getKey().getId());
						subMngr.deleteAll(subs);
						log.info("deleting:" + u.getEmail() + ";msoId:" + u.getKey().getId());
						userMngr.delete(u);
					}
				}
			}
		}
		return NnNetUtil.textReturn(output);
	}
	

}
