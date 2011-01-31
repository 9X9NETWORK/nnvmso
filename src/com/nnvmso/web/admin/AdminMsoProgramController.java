package com.nnvmso.web.admin;

import java.util.Date;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.*;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoProgramManager;
import com.nnvmso.model.MsoProgram;

@Controller
@RequestMapping("admin/program")
public class AdminMsoProgramController {
	protected static final Logger logger = Logger.getLogger(AdminMsoProgramController.class.getName());		
	
	private final MsoChannelManager channelMngr;
	private final MsoProgramManager programMngr;
	
	@Autowired
	public AdminMsoProgramController(MsoChannelManager channelMngr, MsoProgramManager programMngr) {
		this.channelMngr = channelMngr;
		this.programMngr = programMngr;
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
	
	@RequestMapping("modify")
	public @ResponseBody String modify(@RequestParam(required=true)  String key,
	                                   @RequestParam(required=false) String updateDate) {
		
		logger.info("updateDate: " + updateDate + " key: " + key);
		MsoProgram program = programMngr.findByKeyStr(key);
		if (program == null)
			return "Program Not Found";
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			Date date = sdf.parse(updateDate);
			program.setUpdateDate(date);
		} catch (ParseException e) {
			return "Parsing Error";
		}
		
		programMngr.save(program);
		return "OK";
	}
}
