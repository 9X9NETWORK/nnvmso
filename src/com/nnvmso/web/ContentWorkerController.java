package com.nnvmso.web;

import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.service.ContentWorkerService;
import com.nnvmso.service.NnStatusCode;
import com.nnvmso.service.NnStatusMsg;
import com.nnvmso.web.json.transcodingservice.ContentWorker;
import com.nnvmso.web.json.transcodingservice.PostResponse;

@Controller
@RequestMapping("content_worker")
public class ContentWorkerController {
	protected static final Logger log = Logger.getLogger(TranscodingServiceController.class.getName());
	
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

	//test
	@RequestMapping("channel_logo_process")
	public @ResponseBody PostResponse channelLogoProcess(@RequestParam String channel, HttpServletRequest req) {
		PostResponse resp = new PostResponse(String.valueOf(NnStatusCode.SUCCESS), NnStatusMsg.successStr(Locale.ENGLISH));
		workerService.channelLogoProcess(Long.valueOf(channel), "my_imageUrl", "my_prefix", req);
		return resp;
	}

	//test	
	@RequestMapping("program_logo_process")
	public @ResponseBody PostResponse programLogoProcess(@RequestParam String program, HttpServletRequest req) {
		PostResponse resp = new PostResponse(String.valueOf(NnStatusCode.SUCCESS), NnStatusMsg.successStr(Locale.ENGLISH));
		workerService.programLogoProcess(Long.valueOf(program), "my_imageUrl", "my_prefix", req);
		return resp;
	}
	
	//test
	@RequestMapping("program_video_process")
	public @ResponseBody PostResponse programVideoProcess(@RequestParam String program, HttpServletRequest req) {
		PostResponse resp = new PostResponse(String.valueOf(NnStatusCode.SUCCESS), NnStatusMsg.successStr(Locale.ENGLISH));		
		workerService.programVideoProcess(Long.valueOf(program), "my_videoUrl", "my_prefix", true, req);
		return resp;
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
