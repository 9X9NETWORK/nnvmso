package com.nnvmso.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.NnLib;
import com.nnvmso.lib.PlayerLib;
import com.nnvmso.model.*;
import com.nnvmso.service.*;

@Controller
@RequestMapping("box")
public class BoxAPIController {
	
	private final ProgramManager programMngr; 
	private final ChannelManager channelMngr;
	
	@Autowired
	public BoxAPIController(ProgramManager programMngr, ChannelManager channelMngr) {
		this.programMngr = programMngr;
		this.channelMngr = channelMngr;
	}

	/**
	 * To be ignored. 
	 */
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLib.logException(e);
		return "error/exception";				
	}		
	
	/**
	 * <p>Replace all the webMFileUrl and mpeg4FileUrl with the passing ip address.
	 *    If channel param is not seen, the api will do global replacements.</p>
	 * <p> For example, http://s3.aws.com/9x9cache/1.web will be placed with http://<host>/9x9cache/1.web</p>
	 * 
	 * <p>Example:
	 *    <blockquote>
	 *      http://localhost:8888/box/replaceFileUrl?ip=192.168.1.1 <br/>
	 *    	http://localhost:8888/box/replaceFileUrl?ip=192.168.1.1&channel=295
	 *    </blockquote>
	 * </p> 
	 * 
	 * @param host host name
	 * @param channel channel id
	 * @return 
	 */
	@RequestMapping("replaceFileUrl")
	public ResponseEntity<String> replaceFileUrl(@RequestParam(value="host") String host, @RequestParam(value="channel", required = false)String channel) {
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		if (channel == null) {	
			programs = programMngr.findAll(); 
		} else {
			MsoChannel c = channelMngr.findById(Long.parseLong(channel));
			if (c != null) {
				programs = programMngr.findByChannel(c);
			} else {
				return PlayerLib.outputReturn("Channel not found");
			}
		}
		String regex = "^http://.*?/";
		for (MsoProgram p : programs) {
			String toBeReplaced = "http://" + host + "/";
			String webMFileUrl = p.getWebMFileUrl();
			if (webMFileUrl != null) {								
				p.setWebMFileUrl(webMFileUrl.replaceAll(regex, toBeReplaced));
			}
			String mpeg4FileUrl = p.getMpeg4FileUrl(); 
			if (mpeg4FileUrl != null){
				p.setMpeg4FileUrl(mpeg4FileUrl.replaceAll(regex, toBeReplaced));
			}
			System.out.println(p.getWebMFileUrl() + ";" + p.getMpeg4FileUrl());
			programMngr.save(p);
		}
		String output = "success, " + programs.size() + " program is modified.";		
		return PlayerLib.outputReturn(output);
	}

}
