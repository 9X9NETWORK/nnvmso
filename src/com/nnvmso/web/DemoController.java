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
import com.nnvmso.lib.APILib;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.ChannelManager;
import com.nnvmso.service.ProgramManager;

@Controller
@RequestMapping("demo")
public class DemoController {
	
	private final ProgramManager programMngr; 
	private final ChannelManager channelMngr;
	
	@Autowired
	public DemoController(ProgramManager programMngr, ChannelManager channelMngr) {
		this.programMngr = programMngr;
		this.channelMngr = channelMngr;
	}
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLib.logException(e);
		return "error/exception";				
	}		
	
	@RequestMapping("programList")
	public ResponseEntity<String> programList(@RequestParam(value="channelKey", required = false)String channelKey) {
		List<MsoProgram> programs = new ArrayList<MsoProgram>();		
		if (channelKey == null) {	
			programs = programMngr.findAll(); 
		} else {
			MsoChannel c = channelMngr.findByKey(channelKey);
			if (c != null) {
				programs = programMngr.findByChannel(c);
			} else {
				return APILib.outputReturn("Channel not found");
			}
		}
		String output = "";
		for (MsoProgram p : programs) {
			String cKey = NnLib.getKeyStr(p.getChannelKey());
			String flv = p.getOtherFileUrl();
			String mpeg4 = p.getMpeg4FileUrl();			
			String[] ori = {cKey, flv, mpeg4}; 
			output = output + this.getDelimitedStr(ori);
			output = output + "\n";
		}		
		
		return APILib.outputReturn(output);
	}
		
	private String getDelimitedStr(String[] ori) {
		String delimiter = "||";
		StringBuilder result = new StringBuilder();
		if (ori.length > 0) {
			result.append(ori[0]);
		    for (int i=1; i<ori.length; i++) {
		       result.append(delimiter);
		       result.append(ori[i]);
		    }
		}
		return result.toString();
	}
		
	@RequestMapping("replaceFileUrls")
	public ResponseEntity<String> replaceWithSGFileUrl(
			@RequestParam(value="host", required=false) String host, 
			@RequestParam(value="sg", required=false) String sg,
			@RequestParam(value="box", required=false) String box,
			@RequestParam(value="channelKey", required = false)String channelKey) {
		
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		if (channelKey == null) {	
			programs = programMngr.findAll(); 
		} else {
			MsoChannel c = channelMngr.findByKey(channelKey);
			if (c != null) {
				programs = programMngr.findByChannel(c);
			} else {
				return APILib.outputReturn("Channel not found");
			}
		}
		String regex = "^http://.*?/*?/9x9";
		for (MsoProgram p : programs) {	
			String newHost = "http://" + host + "/";
			if (box != null) {
				newHost = "http://" + host + "/media0/part0/idownloader/9x9";
			}
			String flvFileUrl = p.getOtherFileUrl();
			if (flvFileUrl != null) {
				if (host!=null) {
					flvFileUrl = flvFileUrl.replaceAll(regex, newHost);
				}
				if (sg != null && sg.equals("1")) {
					flvFileUrl = flvFileUrl.replaceFirst("/9x9cache", "/sg9x9cache");
					flvFileUrl = flvFileUrl.replaceFirst("/9x9pod", "/sg9x9pod");
				}
				p.setOtherFileUrl(flvFileUrl);
			}
			String mpeg4FileUrl = p.getMpeg4FileUrl(); 
			if (mpeg4FileUrl != null){
				if (host!=null) {
					mpeg4FileUrl = mpeg4FileUrl.replaceAll(regex, newHost);
				}
				if (sg != null && sg.equals("1")) {
					mpeg4FileUrl = mpeg4FileUrl.replaceFirst("/9x9cache", "/sg9x9cache");
					mpeg4FileUrl = mpeg4FileUrl.replaceFirst("/9x9cache", "/sg9x9pod");
				}
				p.setMpeg4FileUrl(mpeg4FileUrl);
			}
			
			System.out.println(p.getOtherFileUrl() + ";" + p.getMpeg4FileUrl());
			programMngr.save(p);
		}
		String output = "success, " + programs.size() + " program is modified.";		
		return APILib.outputReturn(output);
	}
	
}
