package com.nnvmso.service;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.web.json.transcodingservice.ContentWorker;

import com.nnvmso.web.json.transcodingservice.PostResponse;
@Service
public class ContentWorkerService {
	protected static final Logger log = Logger.getLogger(ContentWorkerService.class.getName());
	private static int TASK_CHANNEL_LOGO_PROCESS = 0;
	private static int TASK_PROGRAM_LOGO_PROCESS = 1;
	private static int TASK_PROGRAM_VIDEO_PROCESS = 2;
	
	
	public void submit(int task, ContentWorker content, HttpServletRequest req) {
		TranscodingService service = new TranscodingService();
		String[] transcodingEnv = service.getTranscodingEnv(req);
		//String transcodingServer = transcodingEnv[0] + "?task=" + task;
		//String transcodingServer = "http://ec2-184-73-152-29.compute-1.amazonaws.com/dev3/hello.php";
		String transcodingServer = "http://ec2-184-73-152-29.compute-1.amazonaws.com/dev3/contentworker.php" + "?task=" + task;
		System.out.println(transcodingServer);
		String callbackUrl = transcodingEnv[1];
		String devel = transcodingEnv[2];		
		content.setCallback(callbackUrl);
		if (!devel.equals("1")) {
			NnNetUtil.urlPostWithJson(transcodingServer, content);			
		}
	}
	
	public void channelLogoProcess(long channelId, String imageUrl, String prefix, HttpServletRequest req) {						
		ContentWorker content = new ContentWorker(
				channelId, imageUrl, null, prefix, false);
		this.submit(TASK_CHANNEL_LOGO_PROCESS, content, req);
	}
	
	 public void programLogoProcess(long programId, String imageUrl, String prefix, HttpServletRequest req) {
		 ContentWorker content = new ContentWorker(programId, imageUrl, null, prefix, false);
		 this.submit(TASK_PROGRAM_LOGO_PROCESS, content, req);
	 }
	
	 public void programVideoProcess(long programId, String videoUrl, String prefix, boolean autoGeneratedLogo, HttpServletRequest req) {
		 ContentWorker content = new ContentWorker(programId, null, videoUrl, prefix, autoGeneratedLogo);
		 this.submit(TASK_PROGRAM_VIDEO_PROCESS, content, req);
	 }
	 
	 public PostResponse channelLogoUpdate(ContentWorker content) {
		 MsoChannelManager channelMngr = new MsoChannelManager();
		 System.out.println("content id:" + content.getId());
		 MsoChannel channel = channelMngr.findById(content.getId());
		 if (channel != null) {
			 System.out.println("channel != null");
			 channel.setImageUrl(content.getImageUrl());
			 channelMngr.save(channel);
			 return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS");
		 } else {
			 return new PostResponse(String.valueOf(NnStatusCode.CHANNEL_INVALID), "CHANNEL INVALID");
		 }
	 }
	 	 
	 public PostResponse programLogoUpdate(ContentWorker content) {
		 MsoProgramManager programMngr = new MsoProgramManager();
		 MsoProgram program = programMngr.findById(content.getId());
		 if (program != null) {
			 program.setImageUrl(content.getImageUrl());
			 programMngr.save(program);
			 return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS");
		 } else {
			 return new PostResponse(String.valueOf(NnStatusCode.PROGRAM_INVALID), "PROGRAM INVALID");			 
		 }
	 }
	 
	 public PostResponse programVideoUpdate(ContentWorker content) {
		 MsoProgramManager programMngr = new MsoProgramManager();
		 MsoProgram program = programMngr.findById(content.getId());
		 if (program != null) {
			 program.setMpeg4FileUrl(content.getVideoUrl());
			 program.setImageLargeUrl(content.getImageUrl());
			 programMngr.save(program);
			 return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS");
		 } else {
			 return new PostResponse(String.valueOf(NnStatusCode.PROGRAM_INVALID), "PROGRAM INVALID");
		 }
	 }
	 
}