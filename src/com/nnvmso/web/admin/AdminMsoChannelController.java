package com.nnvmso.web.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.nnvmso.lib.JqgridHelper;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.CategoryChannelManager;
import com.nnvmso.service.CategoryManager;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.SubscriptionLogManager;

@Controller
@RequestMapping("admin/channel")
public class AdminMsoChannelController {
	protected static final Logger logger = Logger.getLogger(AdminMsoChannelController.class.getName());		
	
	private final MsoChannelManager channelMngr;
	private final UserService       userService;
	
	@Autowired
	public AdminMsoChannelController(MsoChannelManager channelMngr) {
		this.channelMngr = channelMngr;
		this.userService = UserServiceFactory.getUserService();
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
	public ResponseEntity<String> list(@RequestParam(value="status", required=false)String status) {
		String output = "";
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		if (status == null) {
			channels = channelMngr.findAll();
			List<MsoChannel> bad = new ArrayList<MsoChannel>();		
			for (MsoChannel c : channels) {
				if (c.getName() == null || c.getStatus() != MsoChannel.STATUS_SUCCESS || 
					c.isPublic() != true || c.getProgramCount() < 1 ) {
					bad.add(c);
				}
			}
			output = "Total count: " + channels.size() + "\n" + "Bad channel count(not public, name=null, status!=success, programCount < 1): " + bad.size();
			output = output + "\n\n --------------- \n\n";
			output = output + this.printChannelData(channels);				
			output = output + "\n\n --------------- \n\n";
			output = output + this.printChannelData(bad);			
		} else {
			channels = channelMngr.findAllByStatus(Short.valueOf(status));
			output = output + this.printChannelData(channels);
		}
		 
		return NnNetUtil.textReturn(output);
	}
	
	/**
	 * List items in jqGrid format
	 *
	 * A jqGrid response format should look like:
	 *
	 * {
	 *   page: 1
	 *   total: 10
	 *   records: 100
	 *   rows:
	 *   [
	 *     ["13671109", "5f", "http://5f.tv", "MSO", "true", "24"],
	 *     ~
	 *     ~
	 *     ["938362", "9x9", "http://9x9.tv", "NN", "false", "13"]
	 *   ]
	 * }
	 *
	 */
	@RequestMapping(value = "list", params = {"page", "rows", "sidx", "sord"})
	public void list(@RequestParam(value = "page")   Integer      currentPage,
	                 @RequestParam(value = "rows")   Integer      rowsPerPage,
	                 @RequestParam(value = "sidx")   String       sortIndex,
	                 @RequestParam(value = "sord")   String       sortDirection,
	                 @RequestParam(required = false) String       searchField,
	                 @RequestParam(required = false) String       searchOper,
	                 @RequestParam(required = false) String       searchString,
	                                                 OutputStream out) {
		
		SubscriptionLogManager subLogMngr = new SubscriptionLogManager();
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> dataRows = new ArrayList<Map<String, Object>>();
		
		List<MsoChannel> results;
		int totalRecords, totalPages;
		if (searchField != null && searchOper != null && searchString != null
		    && searchOper.equals("eq") && searchField.equals("channel")) {
			
			logger.info("searchString = " + searchString);
			totalRecords = 0;
			totalPages = 1;
			currentPage = 1;
			results = new ArrayList<MsoChannel>();
			if (searchString.matches("^[0-9]+$")) {
				MsoChannel found = channelMngr.findById(Long.parseLong(searchString));
				if (found != null) {
					totalRecords++;
					results.add(found);
				}
			}
		} else if (searchField != null && searchOper != null && searchString != null
		           && searchOper.equals("eq")
		           && (searchField.equals("status") || searchField.equals("contentType") || searchField.equals("isPublic") || searchField.equals("sourceUrl") || searchField.equals("name"))) {
			
			if (searchField.equals("sourceUrl") || searchField.equals("name")) {
				searchString = NnStringUtil.escapedQuote(searchString.toLowerCase());
				searchField += "Search";
			}
			
			String filter = searchField + " == " + searchString;
			logger.info("filter = " + filter);
			totalRecords = channelMngr.total(filter);
			totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
			if (currentPage > totalPages)
				currentPage = totalPages;
			results = channelMngr.list(currentPage, rowsPerPage, "updateDate", "desc", filter);
		} else {
			totalRecords = channelMngr.total();
			totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
			if (currentPage > totalPages)
				currentPage = totalPages;
			results = channelMngr.list(currentPage, rowsPerPage, sortIndex, sortDirection);
		}
		
		for (MsoChannel channel : results) {
			
			Map<String, Object> map = new HashMap<String, Object>();
			List<Object> cell = new ArrayList<Object>();
			
			cell.add(channel.getImageUrl());
			cell.add(channel.getKey().getId());
			cell.add(channel.getName());
			cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(channel.getUpdateDate()));
			cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(channel.getCreateDate()));
			cell.add(channel.getSourceUrl());
			cell.add(channel.getStatus());
			cell.add(channel.getContentType());
			cell.add(channel.isPublic());
			cell.add(channel.getProgramCount());
			cell.add(subLogMngr.findTotalCountByChannelId(channel.getKey().getId()));
			cell.add(channel.getIntro());
			
			map.put("id", channel.getKey().getId());
			map.put("cell", cell);
			dataRows.add(map);
		}
		
		try {
			mapper.writeValue(out, JqgridHelper.composeJqgridResponse(currentPage, totalPages, totalRecords, dataRows));
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
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
	
	@RequestMapping("addCategory")
	public @ResponseBody String addCategory(@RequestParam(value = "channel")  Long channelId,
	                                        @RequestParam(value = "category") Long categoryId) {
		
		logger.info("admin = " + userService.getCurrentUser().getEmail());
		
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		CategoryManager categoryMngr = new CategoryManager();
		logger.info("categoryId = " + categoryId);
		Category category = categoryMngr.findById(categoryId);
		if (category == null) {
			String error = "Invalid Category";
			logger.warning(error);
			return error;
		}
		logger.info("channelId = " + channelId);
		MsoChannel channel = channelMngr.findById(channelId);
		if (channel == null) {
			String error = "Invalid Channel";
			logger.warning(error);
			return error;
		}
		CategoryChannel cc = ccMngr.findByCategoryIdAndChannelId(categoryId, channelId);
		if (cc != null) {
			String error = "Channel Is Already in Category";
			logger.warning(error);
			return error;
		}
		
		ccMngr.create(new CategoryChannel(categoryId, channelId));
		// add channel count
		category.setChannelCount(category.getChannelCount() + 1);
		categoryMngr.save(category);
		return "OK";
	}
	
	@RequestMapping("deleteCategories")
	public @ResponseBody String deleteCategories(@RequestParam(required=true)long channel, String categories) {
		if (categories == null) {return "fail";}
		String output = "success";
		//find channel
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel c = channelMngr.findById(channel);
		if (c != null) {
			//find all the categories
			CategoryChannelManager ccMngr = new CategoryChannelManager();
			CategoryManager categoryMngr = new CategoryManager();
			List<Long> categoryIdList = new ArrayList<Long>();	
			String[] arr = categories.split(",");
			for (int i=0; i<arr.length; i++) { categoryIdList.add(Long.parseLong(arr[i])); }
			//delete them in CategoryChannel table
			List<Category> existing = categoryMngr.findAllByIds(categoryIdList);
			ccMngr.deleteChannelCategory(c, existing);
		}
		return output;
	}
	
	@RequestMapping("deleteCategory")
	public @ResponseBody String deleteCategory(@RequestParam(value = "id") Long ccId) {
		
		logger.info("admin = " + userService.getCurrentUser().getEmail());
		
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		
		logger.info("ccId = " + ccId);
		CategoryChannel cc = ccMngr.findById(ccId);
		if (cc == null) {
			String error = "CategoryChannel Does Not Exist";
			logger.warning(error);
			return error;
		}
		ccMngr.delete(cc);
		return "OK";
	}
		
	@RequestMapping("listCategories")
	public @ResponseBody String listCategories(@RequestParam(required=true)long channel) {
		CategoryManager categoryMngr = new CategoryManager();
		List<Category> categories = categoryMngr.findCategoriesByChannelId(channel);
		String output = "";
		for (Category c : categories) {
			output = output + c.getKey().getId() + "\t" + c.getName() + "<br/>";
		}
		return output;
	}
		
	@RequestMapping(value = "listCategories", params = {"channel", "page", "rows", "sidx", "sord"})
	public void listCategories(@RequestParam(value = "channel") Long         channelId,
	                           @RequestParam(value = "page")    Integer      currentPage,
	                           @RequestParam(value = "rows")    Integer      rowsPerPage,
	                           @RequestParam(value = "sidx")    String       sortIndex,
	                           @RequestParam(value = "sord")    String       sortDirection,
	                                                            OutputStream out) {
		
		CategoryManager categoryMngr = new CategoryManager();
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> dataRows = new ArrayList<Map<String, Object>>();
		
		// no channel was specified
		if (channelId == 0) {
			try {
				mapper.writeValue(out, JqgridHelper.composeJqgridResponse(1, 1, 0, new ArrayList<Map<String, Object>>()));
			} catch (IOException e) {
				logger.warning(e.getMessage());
			}
			return;
		}
		
		String filter = "channelId == " + channelId;
		int totalRecords = ccMngr.total(filter);
		int totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
		if (currentPage > totalPages)
			currentPage = totalPages;
		
		List<CategoryChannel> results = ccMngr.list(currentPage, rowsPerPage, sortIndex, sortDirection, filter);
		
		for (CategoryChannel cc : results) {
			
			Map<String, Object> map = new HashMap<String, Object>();
			List<Object> cell = new ArrayList<Object>();
			
			Category category = categoryMngr.findById(cc.getCategoryId());
			
			cell.add(category.getMsoId());
			cell.add(cc.getChannelId());
			cell.add(cc.getCategoryId());
			cell.add(category.getName());
			cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cc.getUpdateDate()));
			cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cc.getCreateDate()));
			cell.add(category.isPublic());
			cell.add(category.getChannelCount());
			
			map.put("id", cc.getKey().getId());
			map.put("cell", cell);
			dataRows.add(map);
		}
		
		try {
			mapper.writeValue(out, JqgridHelper.composeJqgridResponse(currentPage, totalPages, totalRecords, dataRows));
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
	}
	
	@RequestMapping("modify")
	public @ResponseBody String modify(@RequestParam(required=true)  Long    id,
	                                   @RequestParam(required=false) String  name,
	                                   @RequestParam(required=false) String  intro,
	                                   @RequestParam(required=false) String  imageUrl,
	                                   @RequestParam(required=false) Short   status,
	                                   @RequestParam(required=false) Boolean isPublic,
	                                   @RequestParam(required=false) Integer programCount) {
		
		logger.info("admin = " + userService.getCurrentUser().getEmail());
		
		logger.info("channelId = " + id);
		MsoChannel channel = channelMngr.findById(id);
		if (channel == null)
			return "Channel Not Found";
		
		if (name != null) {
			logger.info("name = " + name);
			channel.setName(name);
		}
		if (imageUrl != null) {
			logger.info("imageUrl = " + imageUrl);
			channel.setImageUrl(imageUrl);
		}
		if (intro != null) {
			logger.info("intro = " + intro);
			if (intro.length() > 255)
				return "Introduction Is Too Long";
			channel.setIntro(intro);
		}
		//!!! change counter implementation
		if (status != null) {
			logger.info("status = " + status);
			channel.setStatus(status);
		}
		if (isPublic != null) {
			logger.info("isPublic = " + isPublic);
			channel.setPublic(isPublic);
		}
		if (programCount != null) {
			logger.info("programCount = " + programCount);
			channel.setProgramCount(programCount);
		}		
		channelMngr.save(channel);
		if (status != null) {
			CategoryManager categoryMngr = new CategoryManager();
			CategoryChannelManager ccMngr = new CategoryChannelManager();
			List<CategoryChannel> ccList = ccMngr.findAllByChannelId(channel.getKey().getId());
			List<Long> categoryIds = new ArrayList<Long>();
			for (CategoryChannel cc : ccList) {
				categoryIds.add(cc.getCategoryId());
			}
			List<Category> categories = categoryMngr.findAllByIds(categoryIds);
			for (Category c : categories) {
				List<MsoChannel> channels = channelMngr.findPublicChannelsByCategoryId(c.getKey().getId());
				c.setChannelCount(channels.size());
				categoryMngr.save(c);				
			}			
		}
		return "OK";
	}
	
	@RequestMapping("findUnUniqueSourceUrl")
	public ResponseEntity<String> findUnUniqueSourceUrl() {
		List<MsoChannel> channels = channelMngr.findUnUniqueSourceUrl();
		String result = this.printChannelData(channels);;
		return NnNetUtil.textReturn(result);
	}
}
