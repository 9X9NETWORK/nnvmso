package com.nnvmso.web.admin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.MsoProgramManager;

@Controller
@RequestMapping("admin/program")
public class AdminMsoProgramController {
	protected static final Logger logger = Logger.getLogger(AdminMsoProgramController.class.getName());		
	
	private final MsoProgramManager programMngr;
	
	@Autowired
	public AdminMsoProgramController(MsoProgramManager programMngr) {
		this.programMngr = programMngr;
	}

	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}	
	
	@RequestMapping(value="create")
	public @ResponseBody String create(@RequestParam String channel) {		
		//MsoProgram p = programMngr.findOldestByChannelId(Long.valueOf(id));
		//System.out.println("p is " + p.getKey().getId());
		return "OK";
	}
	
	
	@RequestMapping(value="delete")
	public @ResponseBody String delete(@RequestParam(value="id") String id) {
		MsoProgram p = programMngr.findById(Long.valueOf(id));
		programMngr.delete(p);
		return "OK";
	}
	
	@RequestMapping("mp4")
	public ResponseEntity<String> mp4(@RequestParam(value="channel")long channelId) {
		//find all programs, including the not public ones
		List<MsoProgram> programs = programMngr.findAllByChannelId(channelId);		
		String result = "";
		for (MsoProgram p:programs) {
			result = result + p.getMpeg4FileUrl() + "\n";
		}		
		return NnNetUtil.textReturn(result);
	}
	
	@RequestMapping("list")
	public ResponseEntity<String> list(@RequestParam(value="channel")long channelId) {
		//find all programs, including the not public ones
		List<MsoProgram> programs = programMngr.findAllByChannelId(channelId);
		String[] title = {"id", "channelId", "isPublic", "status", "updateDate", "name"};		
		String result = "";
		for (MsoProgram p:programs) {
			String[] ori = {String.valueOf(p.getKey().getId()),
			                String.valueOf(p.getChannelId()),
			                Boolean.toString(p.isPublic()),
			                String.valueOf(p.getStatus()),
			                NnStringUtil.getDateString(p.getUpdateDate()),
			                p.getName()};
			result = result + NnStringUtil.getDelimitedStr(ori);
			result = result + "\n";
		}
		String output = NnStringUtil.getDelimitedStr(title) + "\n" + result;
		return NnNetUtil.textReturn(output);
	}
	
	@RequestMapping("modify")
	public @ResponseBody String modify(@RequestParam(required=true)String id,
							           @RequestParam(required=false)String status, 
	                                   @RequestParam(required=false)String updateDate) {
		
		logger.info("updateDate: " + updateDate + " id: " + id);
		MsoProgram program = programMngr.findById(Long.parseLong(id));
		if (program == null)
			return "Program Not Found";
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			if (updateDate != null) {
				Date date = sdf.parse(updateDate);
				program.setUpdateDate(date);
			}
			if (status != null) {
				program.setStatus(Short.valueOf(status));
			}
		} catch (ParseException e) {
			return "Parsing Error";
		}
		
		programMngr.save(program);
		return "OK";
	}
	
	
}
