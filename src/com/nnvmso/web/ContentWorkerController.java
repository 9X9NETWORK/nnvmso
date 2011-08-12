package com.nnvmso.web;

import java.util.logging.Logger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.ContentWorkerService;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoProgramManager;
import com.nnvmso.service.NnStatusCode;
import com.nnvmso.web.json.transcodingservice.ContentWorker;
import com.nnvmso.web.json.transcodingservice.MailCastVideo;
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
	
	@RequestMapping("saveNewMailCastVideo")
	public @ResponseBody MailCastVideo saveNewMailCastVideo(@RequestBody MailCastVideo video, HttpServletRequest req) throws NoSuchAlgorithmException {
		
		log.info(video.toString());
		
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		ContentWorkerService workerService = new ContentWorkerService();
		
		MsoChannel channel = channelMngr.findById(video.getChannelId());
		if (channel == null) {
			video.setErrorCode(NnStatusCode.CHANNEL_INVALID);
			video.setErrorReason("CHANNEL_INVALID");
			return video;
		}
		if (channel.getProgramCount() >= MsoChannelManager.MAX_CHANNEL_SIZE) {
			MsoProgram oldest = programMngr.findOldestByChannelId(channel.getKey().getId());
			programMngr.delete(oldest); 			
		}
		MsoProgram program = new MsoProgram(video.getName(), video.getIntro(), "/images/cms/upload_img.jpg", MsoProgram.CONTENTTYPE_DIRECTLINK); 
		program.setOtherFileUrl(video.getFileUrl());
		program.setStorageId(video.getStorageId());
		program.setType(MsoProgram.TYPE_VIDEO);
		program.setContentType(MsoProgram.CONTENTTYPE_DIRECTLINK);
		program.setPublic(true);
		programMngr.create(channel, program);
		
		Long timestamp = System.currentTimeMillis() / 1000L;
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		sha1.update(video.getFileUrl().getBytes());
		String prefix = NnStringUtil.bytesToHex(sha1.digest()) + "_" + timestamp + "_";
		log.info("prefix = " + prefix);
		video.setProgramId(program.getKey().getId());
		
		workerService.programVideoProcess(video.getProgramId(), video.getFileUrl(), prefix, true, req);
		
		video.setErrorCode(NnStatusCode.SUCCESS);
		video.setErrorReason("SUCCESS");
		return video;
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
		MsoProgram program = new MsoProgram(slide.getName(), slide.getIntro(), slide.getImageUrl(), MsoProgram.TYPE_SCRIPT); 
		program.setOtherFileUrl(slide.getFileUrl());
		program.setDuration(slide.getDuration());
		program.setStorageId(slide.getStorageId());
		program.setContentType(MsoProgram.CONTENTTYPE_SCRIPT);
		program.setType(MsoProgram.TYPE_SCRIPT);
		program.setPublic(true);
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
