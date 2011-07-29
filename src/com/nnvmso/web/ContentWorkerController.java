package com.nnvmso.web;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.ContentWorkerService;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoProgramManager;
import com.nnvmso.service.NnStatusCode;
import com.nnvmso.web.json.transcodingservice.ContentWorker;
import com.nnvmso.web.json.transcodingservice.PostResponse;
import com.nnvmso.web.json.transcodingservice.Slide;

@Controller
@RequestMapping("content_worker")
public class ContentWorkerController {
	protected static final Logger log = Logger.getLogger(ContentWorkerController.class.getName());
	
	private ContentWorkerService workerService;
	
	@Autowired
	public ContentWorkerController(ContentWorkerService workerService) {
		this.workerService = workerService;
	}
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/blank";
	}
	
	@RequestMapping("saveNewSlide")
	public @ResponseBody Slide saveNewSlide(@RequestBody Slide slide,
            							    HttpServletRequest req) {
		log.info(slide.toString());
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoChannelManager channelMngr = new MsoChannelManager();		
		MsoChannel channel = channelMngr.findById(Long.valueOf(slide.getChannelId()));		
		if (channel == null) {
			slide.setErrorCode(NnStatusCode.CHANNEL_INVALID);
			slide.setErrorReason("CHANNEL_INVALID");
			return slide;
		}		
		if (channel.getProgramCount() >= MsoChannelManager.MAX_CHANNEL_SIZE) {
			MsoProgram oldest = programMngr.findOldestByChannelId(channel.getKey().getId());
			programMngr.delete(oldest); 			
		}
		MsoProgram program = new MsoProgram(slide.getName(), slide.getIntro(), slide.getImageUrl(), MsoProgram.TYPE_SLIDE); 
		program.setOtherFileUrl(slide.getFileUrl());
		program.setDuration(slide.getDuration());
		programMngr.create(channel, program);
		slide.setProgramId(program.getKey().getId());
		slide.setErrorCode(NnStatusCode.SUCCESS);
		slide.setErrorReason("SUCCESS");
		return slide;
	}
	
	@RequestMapping("channel_logo_update")
	public @ResponseBody PostResponse channelLogoUpdate(@RequestBody ContentWorker content, HttpServletRequest req) {
		log.info(content.toString());
		PostResponse resp = workerService.channelLogoUpdate(content);
		return resp;
	}
	
	@RequestMapping("program_logo_update")
	public @ResponseBody PostResponse programLogoUpdate(@RequestBody ContentWorker content, HttpServletRequest req) {
		log.info(content.toString());		
		PostResponse resp = workerService.programLogoUpdate(content);
		return resp;
	}

	@RequestMapping("program_video_update")
	public @ResponseBody PostResponse programVideoUpdate(@RequestBody ContentWorker content, HttpServletRequest req) {	
		log.info(content.toString());
		PostResponse resp = workerService.programVideoUpdate(content);
		return resp;
	}

}
