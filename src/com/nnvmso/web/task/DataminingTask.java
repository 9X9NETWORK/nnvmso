package com.nnvmso.web.task;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.EmailService;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.task.mapper.DMUserCounterMapper;

@Controller
@RequestMapping("task/datamining")
public class DataminingTask {
	protected static final Logger log = Logger.getLogger(DataminingTask.class.getName());		
		
	@RequestMapping("userCountTask")
	public ResponseEntity<String> userCountTask() throws IOException {
	    Configuration conf = new Configuration(false);
	    try {
	    	conf.setClass("mapreduce.map.class", DMUserCounterMapper.class, Mapper.class);
	        conf.setClass("mapreduce.inputformat.class", DatastoreInputFormat.class, InputFormat.class);
	    	conf.set(DatastoreInputFormat.ENTITY_KIND_KEY, "NnUser");
	    	
	    	Date now = new Date();
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd000000");
	    	String since = sdf.format(now);
	    	log.info("Count user since:" + since);	    	
	    	conf.set("since", since);
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
	
	//mapreduce it somehow?	
	@RequestMapping("channelCountTask")
	public ResponseEntity<String> channelCountTask(HttpServletRequest req) throws IOException {
		List<MsoChannel> list = new ArrayList<MsoChannel>();
    	Date now = new Date();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");    	
    	String since = sdf.format(now);
    	since = since + "000000";
		try {
			Date sinceDate = sdf.parse(since);
	    	MsoChannelManager channelMngr = new MsoChannelManager();
	    	list = channelMngr.findfindAllAfterTheDate(sinceDate);
			EmailService emailService = new EmailService();						
	    	String host = NnNetUtil.getUrlRoot(req);
			String subject = since.substring(0, 8) + " new channels(" + host + ")";			
			String msgBody = "New Channel Count:" + list.size() + "\n";
			for (MsoChannel c : list) {
				msgBody = msgBody + "name:" +  c.getName() + ";url=" + c.getSourceUrl();
			}
			System.out.println(msgBody);
			emailService.sendEmail(subject, msgBody, "nncloudtv@gmail.com", "nncloudtv");	    	
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return NnNetUtil.textReturn("OK");
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
			long totalAccountCount = counterGroup.findCounter("totalAccountCount").getValue();
			long totalGuestCount =  counterGroup.findCounter("totalGuestCount").getValue();
			long totalUserCount = counterGroup.findCounter("totalUserCount").getValue();
			long newGuestCount = counterGroup.findCounter("newGuestCount").getValue();
			long newUserCount = counterGroup.findCounter("newUserCount").getValue();
			
			EmailService emailService = new EmailService();
			
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	    	Date now = new Date();
	    	String since = sdf.format(now);
	    	String host = NnNetUtil.getUrlRoot(req);
			String subject = since + " user account statistics(" + host + ")";
			String msgBody = "";			
			msgBody = msgBody + "Total Count:" + totalAccountCount + "\n";
			msgBody = msgBody + "Total Guest Count:" + totalGuestCount + "\n";
			msgBody = msgBody + "Total User Count:" + totalUserCount + "\n";
			msgBody = msgBody + "---------------------------------------" + "\n";
			msgBody = msgBody + "New Guest Count:" + newGuestCount + "\n";
			msgBody = msgBody + "New User Count:" + newUserCount + "\n";
			System.out.println(msgBody);
			emailService.sendEmail(subject, msgBody, "nncloudtv@gmail.com", "nncloudtv");
		} catch (EntityNotFoundException e) {
			output = "No datastore state";
		}
		
		return NnNetUtil.textReturn(output);
	}
	
	
}
