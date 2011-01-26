package com.nnvmso.web.admin;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.*;
import com.nnvmso.service.MsoChannelManager;

@Controller
@RequestMapping("admin/program")
public class AdminMsoProgramController {
	protected static final Logger logger = Logger.getLogger(AdminMsoProgramController.class.getName());		
	
	private final MsoChannelManager channelMngr;
	
	@Autowired
	public AdminMsoProgramController(MsoChannelManager channelMngr) {
		this.channelMngr = channelMngr;
	}

	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}

	//!!! 
	public ResponseEntity<String> programList(@RequestParam(value="channelKey", required = false)String channelKey) {
		return NnNetUtil.textReturn("");
	}
	
		
}
