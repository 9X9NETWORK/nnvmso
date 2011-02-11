package com.nnvmso.web.admin;

import java.util.ArrayList;
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
import com.nnvmso.model.*;
import com.nnvmso.service.CategoryManager;
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
	
	//!!! all sorts of risk to listAll
	@RequestMapping("list")
	public ResponseEntity<String> list() {
		List<MsoChannel> channels = channelMngr.findAll();
		List<MsoChannel> bad = new ArrayList<MsoChannel>();		
		for (MsoChannel c : channels) {
			if (c.getName() == null || c.getStatus() != MsoChannel.STATUS_SUCCESS || 
				c.isPublic() != true || c.getProgramCount() < 1 ) {
				bad.add(c);
			}
		}
		String output = "Total count: " + channels.size() + "\n" + "Bad channel count(not public, name=null, status!=success, programCount < 1): " + bad.size();
		output = output + "\n\n --------------- \n\n";
		output = output + this.printChannelData(channels);				
		output = output + "\n\n --------------- \n\n";
		output = output + this.printChannelData(bad);
		return NnNetUtil.textReturn(output);
	}
	
	private String printChannelData(List<MsoChannel> channels) {
		String[] title = {"key", "id", "name", "sourceUrl", "isPublic", "status", "programCount"};		
		String result = "";		
		for (MsoChannel c : channels) {
			String[] ori = {NnStringUtil.getKeyStr(c.getKey()),
				    	    String.valueOf(c.getKey().getId()),
				    	    c.getName(),
				    	    c.getSourceUrl(), 
				    	    String.valueOf(c.isPublic()),
				    	    String.valueOf(c.getStatus()), 
				    	    String.valueOf(c.getProgramCount())}; 						
			result = result + NnStringUtil.getDelimitedStr(ori);		
			result = result + "\n";			
		}
		String output = NnStringUtil.getDelimitedStr(title) + "\n" + result;		
		return output;
	}
	
	//add only, a channel's categories are accumulated
	@RequestMapping("addCategories")
	public @ResponseBody String addCategories(@RequestParam(required=true)long channel, String categories) {
		CategoryManager categoryMngr = new CategoryManager();
		List<Category> categoryList = categoryMngr.findCategoriesByIdStr(categories);
		List<Category> list = categoryMngr.changeCategory(channel, categoryList);
		String output = "";
		for (Category c : list) {
			output = output + c.getKey().getId() + "\t" + c.getName() + "<br/>";
		}
		return output;
	}
	
	@RequestMapping("deleteCategories")
	public @ResponseBody String deleteCategories(@RequestParam(required=true)long channel, String categories) {
		String output = "success";
		CategoryManager categoryMngr = new CategoryManager();
		boolean success = categoryMngr.deleteCategory(channel, categories);
		if (!success) {output = "failed";}
		return output;
	}
		
	@RequestMapping("listCategories")
	public @ResponseBody String listCategories(@RequestParam(required=true)long channel) {
		CategoryManager categoryMngr = new CategoryManager();
		List<Category> categories = categoryMngr.findCategoriesByChannelId(channel);
		System.out.print("list category size:" + categories.size());
		String output = "";
		for (Category c : categories) {
			output = output + c.getKey().getId() + "\t" + c.getName() + "<br/>";
		}
		return output;
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
		
		channelMngr.save(channel);
		return "OK";
	}
	
	@RequestMapping("findUnUniqueSourceUrl")
	public ResponseEntity<String> findUnUniqueSourceUrl() {
		List<MsoChannel> channels = channelMngr.findUnUniqueSourceUrl();
		String result = this.printChannelData(channels);;
		return NnNetUtil.textReturn(result);
	}
}
