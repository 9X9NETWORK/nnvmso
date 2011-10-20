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
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoProgramManager;

/**
 * temporary fix, to be removed
 *
 */
@Controller
@RequestMapping("task/calibration")
public class CalibrationTask {
	protected static final Logger log = Logger.getLogger(CalibrationTask.class.getName());
	
	//entry, correct channel's programCount: channelCount -> runChannels -> runPrograms
	@RequestMapping("channelCount")
	public ResponseEntity<String> channelCount() throws IOException {
		try {						
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/task/calibration/runChannels")
		    );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return NnNetUtil.textReturn("OK");
	}

	//run through each channel, 
	@RequestMapping(value="runChannels")
	public ResponseEntity<String> runChannels(HttpServletRequest req) {
		String output = "";
		MsoChannelManager channelMngr = new MsoChannelManager();
		List<MsoChannel> channels = channelMngr.findNonPodcasts();
		for (MsoChannel c : channels) {			
			try {						
				QueueFactory.getDefaultQueue().add(
						TaskOptions.Builder.withUrl("/task/calibration/runPrograms")
						     .param("channel", String.valueOf(c.getKey().getId()))
			    );
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		return NnNetUtil.textReturn(output);
	}

	@RequestMapping(value="runPrograms")
	public ResponseEntity<String> runPrograms(HttpServletRequest req) {
		int channelId = Integer.parseInt(req.getParameter("channel"));
		String output = "";
		MsoProgramManager programMngr = new MsoProgramManager();
		List<MsoProgram> list = programMngr.findAllByChannelId(channelId);
		int count = list.size();
		log.info("program count: " + count + "(channelId:" + channelId + ")");
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findById(channelId);
		channel.setProgramCount(count);
		channelMngr.save(channel);
		return NnNetUtil.textReturn(output);
	}

	//entry, remove programs more than 50, programsRemoveEntry->programRemoveChannels->programsRemove
	/*
	@RequestMapping("programsRemoveEntry")
	public ResponseEntity<String> programsRemoveEntry() throws IOException {
		try {						
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/task/calibration/programRemoveChannels")
		    );
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return NnNetUtil.textReturn("OK");
	}
	*/

	//run through each channel,
	/*
	@RequestMapping(value="programRemoveChannels")
	public ResponseEntity<String> programRemoveChannels(HttpServletRequest req) {
		String output = "";
		MsoChannelManager channelMngr = new MsoChannelManager();
		List<MsoChannel> channels = channelMngr.findProgramsMoreThanMax();
		log.info("# of channels > 50 episodes:" + channels.size());
		for (MsoChannel c : channels) {			
			try {						
				QueueFactory.getDefaultQueue().add(
						TaskOptions.Builder.withUrl("/task/calibration/programsRemove")
						     .param("channel", String.valueOf(c.getKey().getId()))
			    );
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		return NnNetUtil.textReturn(output);
	}
	*/

	/*
	@RequestMapping(value="programsRemove")
	public ResponseEntity<String> programsRemove(HttpServletRequest req) {
		int channelId = Integer.parseInt(req.getParameter("channel"));
		String output = "";
		MsoProgramManager programMngr = new MsoProgramManager();
		int count = programMngr.findAndDeleteProgramsOlderThanMax(channelId);
		log.info("program count: " + count + "(channelId:" + channelId + ")");
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findById(channelId);
		channel.setProgramCount(count);
		channelMngr.save(channel);
		return NnNetUtil.textReturn(output);
	}
	*/
	
}
