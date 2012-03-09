package com.nnvmso.web.task;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.Mapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tools.mapreduce.ConfigurationXmlUtil;
import com.google.appengine.tools.mapreduce.DatastoreInputFormat;
import com.google.appengine.tools.mapreduce.MapReduceState;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.Subscription;
import com.nnvmso.model.SubscriptionLog;
import com.nnvmso.service.ChannelSetManager;
import com.nnvmso.service.EmailService;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.SubscriptionLogManager;
import com.nnvmso.service.SubscriptionManager;
import com.nnvmso.task.mapper.DMSubscriptionCounterMapper;
import com.nnvmso.task.mapper.DMUserCounterMapper;

///task/datamining/setSubCountToTask
///task/datamining/userCountTask
///task/datamining/subCountTask
///task/datamining/channelSubCountToTask
///task/datamining/newChannelCountToTask

@Controller
@RequestMapping("task/datamining")
public class DataminingTask {
	protected static final Logger log = Logger.getLogger(DataminingTask.class.getName());		

	@RequestMapping("setSubCountToTask")
	public ResponseEntity<String> setsSubCountToTask() {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/task/datamining/setSubCount")
		);			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
			
	@RequestMapping("setSubCount")
	public ResponseEntity<String> setSubCount(HttpServletRequest req) throws IOException {
		ChannelSetManager csMngr = new ChannelSetManager();
		MsoManager msoMngr = new MsoManager();
		SubscriptionManager subMngr = new SubscriptionManager();
		List<Mso> msos = msoMngr.findByType(Mso.TYPE_ENTERPRISE);
		String content = "";		
		for (Mso m : msos) {
			if (!m.getName().contains("test")) {
				List<ChannelSet> setlist = csMngr.findByMso(m.getKey().getId());
				for (ChannelSet set : setlist) {
					List<MsoChannel> chlist = csMngr.findChannelsBySet(set);
					long cnt = 0;
					for (MsoChannel ch : chlist) {
						List<Subscription> list = subMngr.findByChannel(ch.getKey().getId());
						cnt += list.size();
					}
					log.info("channel size for this mso:" + chlist.size() + ";" + m.getName());
					content += (m.getName() + " has " + chlist.size() + " channels.\n");
					content += ("subscribers:" + cnt + "\n\n");
				}				
			}			 
		}		 		
		EmailService emailService = new EmailService();		
    	String host = NnNetUtil.getUrlRoot(req);
		String subject = "[statistics] partner performance";
		String msgBody = "host:" + host + "\n\n" + content;
		log.info(content);
		String toEmail = this.toEmail(host);
		emailService.sendEmail(subject, msgBody, toEmail, "nncloudtv");
		
		return NnNetUtil.textReturn("OK");
	}
	
	private String toEmail(String host) {
		String to = "nncloudtv@gmail.com";
		if (host.contains("9x9tvprod") || host.contains("prod.9x9.tv") || 
			host.contains("9x9.tv") || host.contains("www.9x9.tv")) {
			to = "dan.lee@9x9.tv";
		}
		return to;
			
	}
	
	@RequestMapping("userCountTask")
	public ResponseEntity<String> userCountTask() throws IOException {
	    Configuration conf = new Configuration(false);
	    try {
	    	conf.setClass("mapreduce.map.class", DMUserCounterMapper.class, Mapper.class);
	        conf.setClass("mapreduce.inputformat.class", DatastoreInputFormat.class, InputFormat.class);
	    	conf.set(DatastoreInputFormat.ENTITY_KIND_KEY, "NnUser");
			Calendar now = Calendar.getInstance();
		    now.add(Calendar.DATE, -2);
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd000000");
	    	String since = sdf.format(now.getTime());
	    	now.add(Calendar.DATE, -1);
	    	String before = sdf.format(now.getTime());
	    	
	    	log.info("Count user since:" + since + "; before:" + before);	    	
	    	conf.set("since", since);	    	
	    	conf.set("before", before);	    	
	    	conf.set("mapreduce.appengine.donecallback.url", "/task/datamining/userCountTaskCompleted");
	    	String configXml = ConfigurationXmlUtil.convertConfigurationToXml(conf);
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/mapreduce/start")
					      .param("configuration", configXml)
			);
	    } catch (Exception e) {	
	    	log.info(e.getMessage());
	    }
		return NnNetUtil.textReturn("OK");
	}
	
	@RequestMapping(value="test")
	public ResponseEntity<String> test(HttpServletRequest req) {
		Calendar now = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    	now.add(Calendar.DATE, -1);
    	String before = sdf.format(now.getTime());
	    now.add(Calendar.DATE, -1);
    	String since = sdf.format(now.getTime());
    	
    	return NnNetUtil.textReturn(since + ";" + before);
	}

	@RequestMapping(value="test1")
	public ResponseEntity<String> test1(HttpServletRequest req) {
		String astr = "1";
		int a = Integer.parseInt(astr);
		astr = String.format("%08d", a + 1);
    	return NnNetUtil.textReturn("a=" + astr);
	}
	
	@RequestMapping(value="userCountTaskCompleted")
	public ResponseEntity<String> userCountTaskCompleted(HttpServletRequest req) {
		String output = "OK";
		String jobIdName = req.getParameter("job_id");
		JobID jobId = JobID.forName(jobIdName);
		// A future iteration of this will likely contain a default
		// option if we don't care which DatastoreService instance we use.
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();				
		try {
			// We get the state back from the job_id parameter. The state is
			// serialized and stored in the datastore, so we pass an instance
			// of the datastore service.
			MapReduceState mrState = MapReduceState.getMapReduceStateFromJobID(
							datastore, jobId);
			Counters counters = mrState.getCounters();
			CounterGroup counterGroup = counters.getGroup("AccountCount");			
			long totalUserCount = counterGroup.findCounter("totalUserCount").getValue();			
			long newUserCount = counterGroup.findCounter("newUserCount").getValue();
			long activeUserCount = counterGroup.findCounter("activeUserCount").getValue();
			
			EmailService emailService = new EmailService();
			
			Calendar now = Calendar.getInstance();
		    now.add(Calendar.DATE, -1);
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	    	String since = sdf.format(now.getTime());
	    	
	    	/*
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	    	Date now = new Date();
	    	String since = sdf.format(now);
	    	*/
	    	String host = NnNetUtil.getUrlRoot(req);
			String subject = "[statistics] user count (" + since + ")";
			String msgBody = "host:" + host + "\n\n";			
			msgBody = msgBody + "new user count:" + newUserCount + "\n";
			msgBody = msgBody + "active user count:" + activeUserCount + "\n";			
			msgBody = msgBody + "total user count:" + totalUserCount + "\n";
			log.info(msgBody);
			String toEmail = this.toEmail(host);
			emailService.sendEmail(subject, msgBody, toEmail, "nncloudtv");
		} catch (EntityNotFoundException e) {
			output = "No datastore state";
		}
		
		return NnNetUtil.textReturn(output);
	}

	@RequestMapping("subCountTask")
	public ResponseEntity<String> subCountTask() throws IOException {
	    Configuration conf = new Configuration(false);
	    try {
	    	conf.setClass("mapreduce.map.class", DMSubscriptionCounterMapper.class, Mapper.class);
	        conf.setClass("mapreduce.inputformat.class", DatastoreInputFormat.class, InputFormat.class);
	    	conf.set(DatastoreInputFormat.ENTITY_KIND_KEY, "Subscription");	    	
	    	
			Calendar now = Calendar.getInstance();
		    now.add(Calendar.DATE, -1);
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	    	String before = sdf.format(now.getTime());
	    	log.info("before:" + before);	    	
	    	conf.set("before", before);	    		    		    		    	
	    	conf.set("mapreduce.appengine.donecallback.url", "/task/datamining/subCountTaskCompleted");
	    	String configXml = ConfigurationXmlUtil.convertConfigurationToXml(conf);
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/mapreduce/start")
					      .param("configuration", configXml)
			);
	    } catch (Exception e) {	
	    	log.info(e.getMessage());
	    }
		return NnNetUtil.textReturn("OK");
	}
	
	@RequestMapping(value="subCountTaskCompleted")
	public ResponseEntity<String> subCountTaskCompleted(HttpServletRequest req) {
		String output = "OK";
		String jobIdName = req.getParameter("job_id");
		JobID jobId = JobID.forName(jobIdName);
		// A future iteration of this will likely contain a default
		// option if we don't care which DatastoreService instance we use.
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();				
		try {
			// We get the state back from the job_id parameter. The state is
			// serialized and stored in the datastore, so we pass an instance
			// of the datastore service.
			MapReduceState mrState = MapReduceState.getMapReduceStateFromJobID(
							datastore, jobId);
			Counters counters = mrState.getCounters();
			CounterGroup counterGroup = counters.getGroup("SubCount");
			String content = "";			
			long[] grid = new long[81];
			for (int i=0; i<grid.length; i++) {
				grid[i] = i+1;
			}
			
			for (int i=0; i<grid.length; i++) {
				long cnt = counterGroup.findCounter(String.valueOf(grid[i])).getValue();
				//content += "number of users subscribe to " + grid[i] + "  channels: " + cnt + "\n";
				content += cnt + "\n";
			}
						
			EmailService emailService = new EmailService();			
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	    	Date now = new Date();
	    	String since = sdf.format(now);
	    	String host = NnNetUtil.getUrlRoot(req);
			String subject = "[statistics] subscription count (" + since + ")";
			log.info(content);
			String toEmail = this.toEmail(host);
			String msgBody = "host:" + host + "\n\n" + content;
			emailService.sendEmail(subject, msgBody, toEmail, "nncloudtv");
		} catch (EntityNotFoundException e) {
			output = "No datastore state";
		}
		
		return NnNetUtil.textReturn(output);
	}
	

	@RequestMapping("channelSubCountToTask")
	public ResponseEntity<String> channelSubCountToTask() {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/task/datamining/channelSubCount")
		);			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
			
	@RequestMapping("channelSubCount")
	public ResponseEntity<String> channelSubCount(HttpServletRequest req) throws IOException {
		SubscriptionLogManager logMngr = new SubscriptionLogManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		List<SubscriptionLog> list = logMngr.findAll();
		String content = "";
		for (int i=0; i<200; i++) {
			if (list.size() > i) {
				MsoChannel c = channelMngr.findById(list.get(i).getChannelId());
				if (c !=null) {
					content += 
						"channel " + c.getKey().getId() + ", " + c.getName() + " : " + list.get(i).getCount() + "\n";
				}
			}
		}
		
		EmailService emailService = new EmailService();		
    	String host = NnNetUtil.getUrlRoot(req);
		String subject = "[statistics] channel performance";
		String msgBody = "host:" + host + "\n\n" + content;
		log.info(content);
		String toEmail = this.toEmail(host);
		emailService.sendEmail(subject, msgBody, toEmail, "nncloudtv");
		
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping("newChannelCountToTask")
	public ResponseEntity<String> channelCountToTask() {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/task/datamining/newChannelCount")
		);			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	//mapreduce it somehow?	
	@RequestMapping("newChannelCount")
	public ResponseEntity<String> newChannelCount(HttpServletRequest req) throws IOException {
		List<MsoChannel> list = new ArrayList<MsoChannel>();
		Calendar now = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");    	
    	//now.add(Calendar.DATE, -1);
    	String before = sdf.format(now.getTime());
	    now.add(Calendar.DATE, -1);
    	String since = sdf.format(now.getTime());
		try {
			Date sinceDate = sdf.parse(since);
			Date beforeDate = sdf.parse(before);
			log.info("sinceDate:" + sinceDate + ";beforeDate:" + beforeDate);
	    	MsoChannelManager channelMngr = new MsoChannelManager();
	    	NnUserManager userMngr = new NnUserManager();
	    	list = channelMngr.findBetweenDates(sinceDate, beforeDate);
			EmailService emailService = new EmailService();						
	    	String host = NnNetUtil.getUrlRoot(req);
	    	String range = "sinceDate:" + sinceDate + ";beforeDate:" + beforeDate;
			String content = "New Channel Count:" + list.size() + "\n\n";
			String subject = "[statistics] new channel added";
			String msgBody = "host:" + host + "\n" + "range:" + range + "\n\n" + content;
			for (MsoChannel c : list) {
				msgBody += c.getKey().getId() + "  name:" +  c.getName() + ";url=" + c.getSourceUrl();
				NnUser user = userMngr.findById(c.getUserId());
				if (user != null) {
					msgBody += ";user=" + user.getEmail();
				}
				msgBody += "\n";
			}
			log.info("msgBody" + msgBody);
			String toEmail = this.toEmail(host);
			emailService.sendEmail(subject, msgBody, toEmail, "nncloudtv");
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return NnNetUtil.textReturn("OK");
	}
	
}
