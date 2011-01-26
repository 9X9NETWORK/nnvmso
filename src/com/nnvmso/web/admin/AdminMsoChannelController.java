package com.nnvmso.web.admin;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.*;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;

@Controller
@RequestMapping("admin/channel")
public class AdminMsoChannelController {
	protected static final Logger logger = Logger.getLogger(AdminMsoChannelController.class.getName());		
	
	private final MsoChannelManager channelMngr;
	
	@Autowired
	public AdminMsoChannelController(MsoChannelManager channelMngr) {
		this.channelMngr = channelMngr;
	}

	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}

	@RequestMapping("create")
	public @ResponseBody String create(
				@RequestParam(value="url")String url, 
				@RequestParam(value="categories")String categoryIds, 
				@RequestParam(value="userEmail")String email ) {				
		return "";
	}
	
	@RequestMapping("list")
	public ResponseEntity<String> list() {
		List<MsoChannel> channels = channelMngr.findPublicChannels();
		String[] title = {"key", "name", "sourceUrl", "status", "programCount"};		
		String result = "";
		for (MsoChannel c:channels) {
			String[] ori = {NnStringUtil.getKeyStr(c.getKey()),
						    c.getName(),
							c.getSourceUrl(), 
						    String.valueOf(c.getStatus()), 
						    String.valueOf(c.getProgramCount())}; 						
			result = result + NnStringUtil.getDelimitedStr(ori);		
			result = result + "\n";
		}
		String output = NnStringUtil.getDelimitedStr(title) + result + "\n";
		return NnNetUtil.textReturn(output);	
	}
}
