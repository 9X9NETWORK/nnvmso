package com.nnvmso.web.task;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
import com.nnvmso.model.Subscription;
import com.nnvmso.service.ChannelSetManager;
import com.nnvmso.service.EmailService;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.SubscriptionManager;
import com.nnvmso.task.mapper.DMSubscriptionCounterMapper;
import com.nnvmso.task.mapper.DMUserCounterMapper;

///task/datamining/setSubCountToTask
///task/datamining/userCountTask
///task/datamining/subCountTask


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
					List<MsoChannel> chlist = csMngr.findChannelsById(set.getKey().getId());
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

	    	/*
	    	Date now = new Date();
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd000000");
	    	String since = sdf.format(now);
	    	log.info("Count user since:" + since);	    	
	    	conf.set("since", since);
	    	*/
	    	
			Calendar now = Calendar.getInstance();
		    now.add(Calendar.DATE, -1);
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd000000");
	    	String since = sdf.format(now.getTime());
	    	now.add(Calendar.DATE, 1);
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
	    now.add(Calendar.DATE, -1);
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd000000");
    	String since = sdf.format(now.getTime());
    	now.add(Calendar.DATE, 1);
    	String before = sdf.format(now.getTime());
    	
    	return NnNetUtil.textReturn(since + ";" + before);
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
				content += "number of users subscribe to " + grid[i] + "  channels: " + cnt + "\n";
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
	

	
}
