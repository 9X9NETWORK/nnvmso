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

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.MsoChannelManager;

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
		String[] title = {"id", "name", "sourceUrl", "status", "programCount"};		
		String result = "";
		for (MsoChannel c:channels) {
			String[] ori = {String.valueOf(c.getKey().getId()),
						    c.getName(),
							c.getSourceUrl(), 
						    String.valueOf(c.getStatus()), 
						    String.valueOf(c.getProgramCount())}; 						
			result = result + NnStringUtil.getDelimitedStr(ori);		
			result = result + "\n";
		}
		String output = NnStringUtil.getDelimitedStr(title) + "\n" + result;
		return NnNetUtil.textReturn(output);	
	}
	
	@RequestMapping("modify")
	public @ResponseBody String modify(@RequestParam(required=true)  String id,
	                                   @RequestParam(required=false) String name,
	                                   @RequestParam(required=false) String status,
	                                   @RequestParam(required=false) String programCount) {
		
		logger.info("name: " + name + " status: " + status + " programCount: " + programCount + " key: " + id);
		MsoChannel channel = channelMngr.findById(Long.parseLong(id));
		if (channel == null)
			return "Channel Not Found";
		
		if (name != null)
			channel.setName(name);
		if (status != null)
			channel.setStatus(Short.parseShort(status));
		if (programCount != null)
			channel.setProgramCount(Integer.parseInt(programCount));
		
		channelMngr.save(null, channel);
		return "OK";
	}
}
