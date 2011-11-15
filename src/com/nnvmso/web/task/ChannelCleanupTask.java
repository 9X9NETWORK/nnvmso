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
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoProgramManager;

@Controller
@RequestMapping("task/channel")
public class ChannelCleanupTask {
	
	protected static final Logger log = Logger.getLogger(MergeAccountTask.class.getName());

	//remove youtube episodes.	
	//find non youtube channels
	//remove channels, remove episodes

	@RequestMapping("youtubeList")
	public ResponseEntity<String> youtubeList() throws IOException { 			
		String output = "";
		MsoChannelManager channelMngr = new MsoChannelManager();
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		int cnt = 0;
		channels = channelMngr.findChannelsByType(MsoChannel.CONTENTTYPE_YOUTUBE_CHANNEL);
		cnt = channels.size();
		channels.addAll(channelMngr.findChannelsByType(MsoChannel.CONTENTTYPE_YOUTUBE_PLAYLIST));
		output = "channels youtube channel" + cnt + ";channels total:" + channels.size();
		return NnNetUtil.textReturn(output);
	}
		
	@RequestMapping("podcastList")
	public ResponseEntity<String> podcastList() throws IOException { 			
		String output = "";
		MsoChannelManager channelMngr = new MsoChannelManager();
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		channels = channelMngr.findChannelsByType(MsoChannel.CONTENTTYPE_PODCAST);
		output = "podcast channel size:" + channels.size();
		return NnNetUtil.textReturn(output);
	}
	
	//entry
	@RequestMapping("youtube")
	public ResponseEntity<String> youtube(@RequestParam("start")int start) 
			throws IOException {
		try {						
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/task/channel/runYoutube")
			         .param("start", String.valueOf(start))					
		    );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping(value="runYoutube")
	public ResponseEntity<String> runYoutube(
			@RequestParam("start")int start,
			HttpServletRequest req) {
		String output = "";
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoProgramManager pMngr = new MsoProgramManager();
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		channels = channelMngr.findChannelsByType(MsoChannel.CONTENTTYPE_YOUTUBE_CHANNEL);
		channels.addAll(channelMngr.findChannelsByType(MsoChannel.CONTENTTYPE_YOUTUBE_PLAYLIST));
		log.info("channels total:" + channels.size());
		int end = start + 200;
		if (end > channels.size()) 
			end = channels.size();
		for (int i=start; i<end; i++) {	
			List<MsoProgram> list = new ArrayList<MsoProgram>();
			list = pMngr.findAllByChannelId(channels.get(i).getKey().getId());
			pMngr.deleteAll(list);
		}
		this.sendEmail("runYoutube start " + start, "done");
		return NnNetUtil.textReturn(output);
	}
	
	@RequestMapping("podcast")
	public ResponseEntity<String> podcast(@RequestParam("start")int start) 
			throws IOException {
		try {						
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/task/channel/runPodcast")
			         .param("start", String.valueOf(start))					
		    );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping(value="runPodcast")
	public ResponseEntity<String> runPodcast(
			@RequestParam("start")int start,
			HttpServletRequest req) {
		String output = "";
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoProgramManager pMngr = new MsoProgramManager();
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		channels = channelMngr.findChannelsByType(MsoChannel.CONTENTTYPE_PODCAST);
		log.info("channels total:" + channels.size());
		int end = start + 200;
		if (end > channels.size()) 
			end = channels.size();
		for (int i=start; i<end; i++) {	
			List<MsoProgram> list = new ArrayList<MsoProgram>();
			list = pMngr.findAllByChannelId(channels.get(i).getKey().getId());
			pMngr.deleteAll(list);
			channelMngr.delete(channels.get(i));
		}
		this.sendEmail("runPodcast start " + start, "done");
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
