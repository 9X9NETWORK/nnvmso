package com.nnvmso.web.admin;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.Math;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.codehaus.jackson.map.ObjectMapper;

import com.nnvmso.lib.JqgridHelper;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.SubscriptionLog;
import com.nnvmso.service.CategoryChannelManager;
import com.nnvmso.service.CategoryManager;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.SubscriptionLogManager;

@Controller
@RequestMapping("admin/category")
public class AdminCategoryController {
	
	protected static final Logger logger = Logger.getLogger(AdminCategoryController.class.getName());
	
	private final CategoryManager categoryMngr;	
	private final UserService     userService;
	
	@Autowired
	public AdminCategoryController(CategoryManager categoryMngr) {
		this.categoryMngr = categoryMngr;
		this.userService  = UserServiceFactory.getUserService();
	}	

	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}
	
	//list every channel under a category
	@RequestMapping(value="channelList")
	public ResponseEntity<String> channelList(@RequestParam("category")long id) {		
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		MsoChannelManager cMngr = new MsoChannelManager();
		List<CategoryChannel> ccs = ccMngr.findAllByCategoryId(id);
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		for (CategoryChannel cc : ccs) {
			channels.add(cMngr.findById(cc.getChannelId()));
		}
		String output = "";
		Category theC = new CategoryManager().findById(id);
		if (theC == null) {
			output = "category does not exist";
		} else {
			output = "category count = " + new CategoryManager().findById(id).getChannelCount() + "\n";
			output = output + "id \t name \t program count \t status \t isPublic\n";
			for (MsoChannel c : channels) {
				output = output + c.getKey().getId() + "\t" + c.getName() + "\t" + + c.getProgramCount() + "\t" + c.getStatus() + "\t" + c.isPublic() + "\n";
			}
		}
		return NnNetUtil.textReturn(output);
	}
	
	@RequestMapping(value = "channelList", params = {"category", "page", "rows", "sidx", "sord"})
	public void channelList(@RequestParam(value = "category") Long         categoryId,
	                        @RequestParam(value = "page")     Integer      currentPage,
	                        @RequestParam(value = "rows")     Integer      rowsPerPage,
	                        @RequestParam(value = "sidx")     String       sortIndex,
	                        @RequestParam(value = "sord")     String       sortDirection,
	                                                          OutputStream out) {
		
		SubscriptionLogManager subLogMngr  = new SubscriptionLogManager();
		CategoryChannelManager ccMngr      = new CategoryChannelManager();
		MsoChannelManager      channelMngr = new MsoChannelManager();
		
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> dataRows = new ArrayList<Map<String, Object>>();
		
		Category category = categoryMngr.findById(categoryId);
		if (category == null) {
			try {
				mapper.writeValue(out, JqgridHelper.composeJqgridResponse(1, 1, 0, new ArrayList<Map<String, Object>>()));
			} catch (IOException e) {
				logger.warning(e.getMessage());
			}
			return;
		}
		
		String filter = "categoryId == " + categoryId;
		int totalRecords = ccMngr.total(filter);
		int totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
		if (currentPage > totalPages)
			currentPage = totalPages;
		
		List<CategoryChannel> results = ccMngr.list(currentPage, rowsPerPage, sortIndex, sortDirection, filter);
		
		for (CategoryChannel cc : results) {
			
			Map<String, Object> map = new HashMap<String, Object>();
			List<Object> cell = new ArrayList<Object>();
			
			MsoChannel channel = channelMngr.findById(cc.getChannelId());
			SubscriptionLog subLog = subLogMngr.findByMsoIdAndChannelId(category.getMsoId(), cc.getChannelId());
			int subscribers = 0;
			if (subLog != null)
				subscribers = subLog.getCount();
			
			cell.add(cc.getCategoryId());
			cell.add(cc.getChannelId());
			cell.add(channel.getName());
			cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cc.getUpdateDate()));
			cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cc.getCreateDate()));
			cell.add(channel.isPublic());
			cell.add(channel.getContentType());
			cell.add(subscribers);
			
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
	
	@RequestMapping("list")
	public ResponseEntity<String> list(@RequestParam(required=false)String mso) {
		
		List<Category> categories = null;
		if (mso == null) {
			categories = categoryMngr.findAll();
		} else {	
			categories = categoryMngr.findAllByMsoId(Long.parseLong(mso));
		}
		
		String[] title = {"id", "msoId", "isPublic", "channelCount", "name"};
		String result = "";
		for (Category c:categories) {
			String[] ori = {String.valueOf(c.getKey().getId()),
			                Long.toString(c.getMsoId()),
			                Boolean.toString(c.isPublic()),
			                String.valueOf(c.getChannelCount()),
			                c.getName()};
			result = result + NnStringUtil.getDelimitedStr(ori);
			result = result + "\n";
		}
		String output = NnStringUtil.getDelimitedStr(title) + "\n" + result;
		return NnNetUtil.textReturn(output);
	}
	
	@RequestMapping(value = "list", params = {"page", "rows", "sidx", "sord"})
	public void list(@RequestParam(value = "page")   Integer      currentPage,
	                 @RequestParam(value = "rows")   Integer      rowsPerPage,
	                 @RequestParam(value = "sidx")   String       sortIndex,
	                 @RequestParam(value = "sord")   String       sortDirection,
	                 @RequestParam(required = false) String       searchField,
	                 @RequestParam(required = false) String       searchOper,
	                 @RequestParam(required = false) String       searchString,
	                                                 OutputStream out) {
		
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> dataRows = new ArrayList<Map<String, Object>>();
		
		String filter = "";
		if (searchField != null && searchOper != null && searchString != null) {
			
			Map<String, String> opMap = JqgridHelper.getOpMap();
			if (opMap.containsKey(searchOper)) {
				filter = searchField + " " + opMap.get(searchOper) + " " + searchString;
				logger.info("filter: " + filter);
			}
		}
		
		int totalRecords = categoryMngr.total(filter);
		int totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
		if (currentPage > totalPages)
			currentPage = totalPages;
		
		List<Category> results = categoryMngr.list(currentPage, rowsPerPage, sortIndex, sortDirection, filter);
		
		for (Category category : results) {
			
			Map<String, Object> map = new HashMap<String, Object>();
			List<Object> cell = new ArrayList<Object>();
			
			cell.add(category.getMsoId());
			cell.add(category.getKey().getId());
			cell.add(category.getName());
			cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(category.getUpdateDate()));
			cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(category.getCreateDate()));
			cell.add(category.isPublic());
			cell.add(category.getChannelCount());
			
			map.put("id", category.getKey().getId());
			map.put("cell", cell);
			dataRows.add(map);
		}
		
		try {
			mapper.writeValue(out, JqgridHelper.composeJqgridResponse(currentPage, totalPages, totalRecords, dataRows));
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
	}
	
	@RequestMapping("create")
	public @ResponseBody String create(@RequestParam(required=true)  String  name,
	                                   @RequestParam(required=false) Boolean isPublic,
	                                   @RequestParam(required=true)  Long    msoId) {
		
		logger.info("admin = " + userService.getCurrentUser().getEmail());
		
		logger.info("msoId = " + msoId);
		logger.info("name = " + name);
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findById(msoId);
		if (mso == null) {
			String error = "Invalid msoId";
			logger.warning(error);
			return error;
		}
		if (isPublic == null)
			isPublic = true;
		categoryMngr.create(new Category(name, isPublic, msoId));
		return "OK";
	}
	
	@RequestMapping("modify")
	public @ResponseBody String modify(@RequestParam(required=true)  Long    id,
	                                   @RequestParam(required=false) String  name,
	                                   @RequestParam(required=false) Boolean isPublic,
	                                   @RequestParam(required=false) Long    msoId,
	                                   @RequestParam(required=false) Integer channelCount) {
		
		logger.info("admin = " + userService.getCurrentUser().getEmail());
		
		logger.info("categoryId = " + id);
		Category category = categoryMngr.findById(id);
		if (category == null) {
			String error = "Category Not Found";
			logger.warning(error);
			return error;
		}
		
		if (msoId != null) {
			MsoManager msoMngr = new MsoManager();
			logger.info("msoId = " + msoId);
			if (msoMngr.findById(msoId) == null) {
				String error = "Invalid msoId";
				logger.warning(error);
				return error;
			}
			category.setMsoId(msoId);
		}
		if (name != null) {
			logger.info("name = " + name);
			category.setName(name);
		}
		if (isPublic != null) {
			logger.info("isPublic = " + isPublic);
			category.setPublic(isPublic);
		}
		if (channelCount != null) {
			logger.info("channelCount = " + channelCount);
			category.setChannelCount(channelCount);
		}
		
		categoryMngr.save(category);
		return "OK";
	}
	
	@RequestMapping("categoriesHtmlSelectOptions")
	public void categoriesHtmlSelectOptions(HttpServletResponse response, OutputStream out) {
		
		response.setContentType("text/html;charset=utf-8");
		OutputStreamWriter writer;
		try {
			writer = new OutputStreamWriter(out, "UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			return;
		}
		List<Category> categories = categoryMngr.findAll();
		try {
			writer.write("<select>");
			for (Category category : categories)
				writer.write("<option value=\"" + category.getKey().getId() + "\">" + category.getName() + "</option>");
			writer.write("</select>");
			writer.close();
		} catch (IOException e) {
			return;
		}
	}
}
