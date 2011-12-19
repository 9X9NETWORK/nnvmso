package com.nnvmso.web.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.jdo.JDOUserException;

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
import com.nnvmso.lib.PiwikLib;
import com.nnvmso.lib.YouTubeLib;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.ContentOwnership;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.CategoryChannelManager;
import com.nnvmso.service.CategoryManager;
import com.nnvmso.service.ContentOwnershipManager;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.SubscriptionLogManager;
import com.nnvmso.service.TranscodingService;

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
	
	@RequestMapping("tmpList")
	public ResponseEntity<String> tmpList(
				HttpServletRequest req) {
		String[] urls = {
				"http://www.youtube.com/user/goodtv/user/FBE16B28C166951F",
				"http://www.youtube.com/user/DianaAmazing",
				"http://www.youtube.com/user/tbwtv",
				"http://www.youtube.com/user/TVHS109",
				"http://www.youtube.com/user/ntdchinese",
				"http://www.youtube.com/user/ChinaTimes",
				"http://www.youtube.com/user/TheChineseNews",				
		};
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		for (String url : urls) {
			String checkedUrl = YouTubeLib.formatCheck(url);
			MsoChannel c = channelMngr.findBySourceUrlSearch(checkedUrl);
			channels.add(c);
		}
		String output = "";
		for (MsoChannel c : channels) {
			output += c.getKey().getId() + "\t" + c.getSourceUrl() + "\n";
		}
		return NnNetUtil.textReturn(output);
	}	

	@RequestMapping("createPiwik")
	public @ResponseBody String createPiwik(
				HttpServletRequest req,
				@RequestParam(value="id",required=false) long id) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel c = channelMngr.findById(id);
		if (c != null) {
			if (c.getPiwik() == null) {
				String piwikId = PiwikLib.createPiwikSite(0, c.getKey().getId(), req);
				logger.info("piwikId:" + piwikId);
				c.setPiwik(piwikId);
				channelMngr.save(c);					
			}			
		}
		return "OK";
	}
	
	@RequestMapping("createBatch")
	public @ResponseBody String createBatch(
				HttpServletRequest req,
				@RequestParam(value="devel",required=false) boolean devel) {
		String[] urls = {
				"http://www.youtube.com/playlist?list=PL189FD87828064376",
		};
		String[] names= {
				"八方論談廣播節目",
		};
		for (int i=0; i<urls.length; i++) {
			channelMngr.create(urls[i], names[i], devel, req);
		}
		return "OK";
	}		
	
	@RequestMapping("create")
	public @ResponseBody String create(
				HttpServletRequest req,
			    @RequestParam(value="sourceUrl", required=false)String url,
				@RequestParam(value="name", required=false) String name,
				@RequestParam(value="devel",required=false) boolean devel) {
		channelMngr.create(url, name, false, req);
		return "OK";
	}	

	/*
	@RequestMapping("list")
	public ResponseEntity<String> list(
			@RequestParam(value="status", required=false)String status,
			@RequestParam(value="since", required=false)String since) {		
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date sinceDate = null;
		String output = "";
		
		if (since != null) {
			try {
				sinceDate = sdf.parse(since);
			} catch (ParseException e) {
				return NnNetUtil.textReturn("wrong date format: yyyymmdd");
			}			
			channels.addAll(channelMngr.findSince(sinceDate));			
		}
		
		List<MsoChannel> bad = new ArrayList<MsoChannel>();		
		if (status == null) {
			channels = channelMngr.findAll();
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
		}
		
		output = output + this.printChannelData(channels);		
		return NnNetUtil.textReturn(output);
	}
	*/
	
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
	public @ResponseBody String list	(
			         @RequestParam(value = "page")   Integer      currentPage,
	                 @RequestParam(value = "rows")   Integer      rowsPerPage,
	                 @RequestParam(value = "sidx")   String       sortIndex,
	                 @RequestParam(value = "sord")   String       sortDirection,
	                 @RequestParam(required = false) String       searchField,
	                 @RequestParam(required = false) String       searchOper,
	                 @RequestParam(required = false) String       searchString,
	                 @RequestParam(required = false) String       set,
	                 @RequestParam(required = false) boolean      notify,
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
		           && (searchField.equals("status") || 
		        	   searchField.equals("contentType") || 
		        	   searchField.equals("isPublic") || 
		        	   searchField.equals("featured") || 
		        	   searchField.equals("sourceUrl") || 
		        	   searchField.equals("langCode"))) {			
			if (searchField.equals("sourceUrl")) {
				searchString = NnStringUtil.escapedQuote(searchString.toLowerCase());
				searchField += "Search";
			}  else if (searchField.equals("langCode")) {
				searchString = NnStringUtil.escapedQuote(searchString);
			}			
			String filter = searchField + " == " + searchString;
			logger.info("filter = " + filter);

			totalRecords = channelMngr.total(filter);
			totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
			if (currentPage > totalPages)
				currentPage = totalPages;
			results = channelMngr.list(currentPage, rowsPerPage, "updateDate", "desc", filter);
		}else if (searchField != null && searchOper != null && searchString != null
		           && searchOper.equals("eq")
		           && searchField.equals("name")){
			//use fuzzy search for "name"
			
			results = new ArrayList<MsoChannel>();
			List<MsoChannel> totalResults = new ArrayList<MsoChannel>();
			try{
				totalResults = MsoChannelManager.searchChannelEntries(searchString);
				totalRecords = totalResults.size();
				totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
				
				if(totalPages==0)
				{
					currentPage = 1;
					totalPages = 1;
				}
				else if(currentPage > totalPages)
					currentPage = totalPages;
				
				if(totalRecords>0)
				{
					for(int i=(currentPage-1)*rowsPerPage;i<currentPage*rowsPerPage;i++)
					{
						if(i<totalRecords)
						{
							results.add(totalResults.get(i));
						}
					}
				}
			}
			catch(JDOUserException e)
			{
				//handle illegal input from user, return an empty page, see more at "MsoChannelManager.searchChannelEntries".
				logger.warning("illegal input from user");
				logger.warning(e.getMessage());

				totalRecords = 0;
				totalPages = 1;
				currentPage = 1;
			}
		}else {		
			totalRecords = channelMngr.total();
			totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
			if (currentPage > totalPages)
				currentPage = totalPages;
			results = channelMngr.list(currentPage, rowsPerPage, sortIndex, sortDirection);
		}
		
		for (MsoChannel channel : results) {			
			Map<String, Object> map = new HashMap<String, Object>();
			List<Object> cell = new ArrayList<Object>();
			boolean qualified = true;
			if (notify) {
				qualified = false;
				Calendar cal = Calendar.getInstance();		
				cal.add(Calendar.DAY_OF_MONTH, - 100);
				Date d = cal.getTime();
				if (channel.getCreateDate().after(d)) {
					if (channel.getStatus() == MsoChannel.STATUS_SUCCESS) {
						qualified = true;
					}
				}
			}
			if (qualified) {
				//cell.add(channel.getImageUrl());
				cell.add(channel.getKey().getId());
				cell.add(channel.getName());
				//cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(channel.getUpdateDate()));
				//cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(channel.getCreateDate()));
				cell.add(channel.getSourceUrl());
				cell.add(channel.getStatus());
				cell.add(channel.getContentType());
				cell.add(channel.getLangCode());
				cell.add(channel.isPublic());
				cell.add(channel.getPiwik());
				cell.add(channel.getProgramCount());
				cell.add(subLogMngr.findTotalCountByChannelId(channel.getKey().getId()));
				cell.add(channel.getSourceUrl());
				//cell.add(channel.getIntro());
				
				map.put("id", channel.getKey().getId());
				map.put("cell", cell);
				dataRows.add(map);
			}
		}
		
		try {
			mapper.writeValue(out, JqgridHelper.composeJqgridResponse(currentPage, totalPages, totalRecords, dataRows));
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
		return "OK";
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
		// TODO: deal with Category.channelCount
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
	                                   @RequestParam(required=false) String  langCode,
	                                   @RequestParam(required=false) Boolean isPublic,
	                                   @RequestParam(required=false) Boolean featured,
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
		if (langCode != null) {
			logger.info("langCode = " + langCode);
			channel.setLangCode(langCode);
		}
		if (isPublic != null) {
			logger.info("isPublic = " + isPublic);
			channel.setPublic(isPublic);
		}
		if (featured != null) {
			logger.info("featured = " + featured);
			channel.setFeatured(featured);
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
