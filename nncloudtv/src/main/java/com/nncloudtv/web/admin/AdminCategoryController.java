package com.nncloudtv.web.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nncloudtv.lib.JqgridHelper;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Category;
import com.nncloudtv.model.CategoryToNnSet;
import com.nncloudtv.model.NnSet;
import com.nncloudtv.service.CategoryManager;
import com.nncloudtv.service.NnSetManager;

@Controller
@RequestMapping("admin/category")
public class AdminCategoryController {
	
	protected static final Logger logger = Logger.getLogger(AdminCategoryController.class.getName());
	
	private final CategoryManager categoryMngr;	
	
	@Autowired
	public AdminCategoryController(CategoryManager categoryMngr) {
		this.categoryMngr = categoryMngr;
	}	

	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}
	
	@RequestMapping(value="addSet")
	public @ResponseBody String addSet(
			             @RequestParam(required = false) long category,
			             @RequestParam(required = false) long set,
	                     OutputStream out) {
		System.out.println("category:" + category + ";set=" + set);
		NnSetManager csMngr = new NnSetManager();
		NnSet cs = csMngr.findById(set);
		Category cat = categoryMngr.findById(category);
		if (cs == null)
			return "Set does not exist";
		if (cat == null)
			return "Category does not exist";
		categoryMngr.addSet(cat, cs);
		return "OK";		
	}

	@RequestMapping(value="deleteSet")
	public @ResponseBody String deleteSet(
			             @RequestParam(required = false) long category,
			             @RequestParam(required = false) long id,
	                     OutputStream out) {
		if(categoryMngr.deleteSet(category, id))
			return "OK";
		else
			return "there are nothing can delete";
	}
	
	//list every channel under a category	
	@RequestMapping(value = "listSet", params = {"category", "page", "rows", "sidx", "sord"})
	public void listSet(@RequestParam(value = "category") Long         categoryId,
	                    @RequestParam(value = "page")     Integer      currentPage,
	                    @RequestParam(value = "rows")     Integer      rowsPerPage,
	                    @RequestParam(value = "sidx")     String       sortIndex,
	                    @RequestParam(value = "sord")     String       sortDirection,
	                    OutputStream out) {
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
		int totalRecords = categoryMngr.totalCatToSet(filter);
		int totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
		if (currentPage > totalPages)
			currentPage = totalPages;

		//check if data exist		
		List<CategoryToNnSet> listCatToSet =  categoryMngr.listCatToSet(currentPage, rowsPerPage, sortIndex, sortDirection, filter);
		NnSetManager setMngr = new NnSetManager();
		NnSet set = null;
		
		for (CategoryToNnSet catToSet : listCatToSet) {
			System.out.println(catToSet.getSetId());
			set = setMngr.findById(catToSet.getSetId());
			
			Map<String, Object> map = new HashMap<String, Object>();
			List<Object> cell = new ArrayList<Object>();
			if (set != null) {
				cell.add(categoryId);
				cell.add(set.getId());
				cell.add(set.getName());
				cell.add(set.isPublic());
				cell.add(set.getLang());
				map.put("id", set.getId());
				map.put("cell", cell);
				dataRows.add(map);
			}
		}
		
		try {
			mapper.writeValue(out, JqgridHelper.composeJqgridResponse(currentPage, totalPages, totalRecords, dataRows));
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
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
		if (searchField != null && searchOper != null && searchString != null && !searchString.isEmpty()) {
			
			if (searchField.equals("lang")) {
				searchString = NnStringUtil.escapedQuote(searchString);
			}
			
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
			
			//cell.add(category.getMsoId());
			cell.add(category.getId());
			//cell.add(category.getParentId());
			cell.add(category.getName());
			cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(category.getUpdateDate()));
			//cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(category.getCreateDate()));
			cell.add(category.getLang());
			cell.add(category.isPublic());
			cell.add(category.getSeq());
			cell.add(category.getChannelCnt());
			
			map.put("id", category.getId());
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
	public @ResponseBody String create(@RequestParam(required=true) String name,
									   @RequestParam(required=true) String lang,
									   @RequestParam short seq,
									   @RequestParam(required=false) String isPublic) {
		Category category = new Category(name, Boolean.parseBoolean(isPublic));
		category.setLang(lang);
		category.setPublic(Boolean.parseBoolean(isPublic));
		category = categoryMngr.save(category);
		this.adjustSeq(category, seq);
		return "OK";
	}

	@RequestMapping("delete")
	public @ResponseBody String delete(
			@RequestParam(required=true) Long id) {
		Category category = categoryMngr.findById(id);
		categoryMngr.delete(category);
		List<Category> toBeSaved = new ArrayList<Category>();
		List<Category> categories = categoryMngr.findByLang(category.getLang());
		for (int i=0; i<categories.size(); i++) {
			Category c = categories.get(i);
			int seq = i+1;
			if (c.getSeq() != seq) {				
				c.setSeq((short)seq);
				toBeSaved.add(c);
			}
		}
		categoryMngr.saveAll(toBeSaved);
		return "OK";
	}
	
	private void adjustSeq(Category category, short seq) {
		Category exist = categoryMngr.findByLangAndSeq(category.getLang(), seq);
		if (exist == null) {
			category.setSeq(seq);
			categoryMngr.save(category);
			return;
		}
		List<Category> toBeSaved = new ArrayList<Category>();		
		List<Category> categories = categoryMngr.findByLang(category.getLang());
		for (int i=seq; i<categories.size(); i++) {
			Category c = categories.get(i);
			c.setSeq((short)(i+1));
			toBeSaved.add(c);
		}
		category.setSeq(seq);
		toBeSaved.add(category);
		categoryMngr.saveAll(toBeSaved);
	}
	
	@RequestMapping("edit")
	public @ResponseBody String edit(
			@RequestParam(required=true)  Long    id,
			@RequestParam(required=false) String  name,			
	        @RequestParam(required=false) Boolean isPublic,
	        @RequestParam(required=false) short  seq,
	        @RequestParam(required=false) int  channelCnt,
	        @RequestParam(required=false) String  lang) {		
		
		Category category = categoryMngr.findById(id);
		if (category == null) {
			String error = "Category Not Found";
			logger.warning(error);
			return error;
		}		
		if (name != null) {
			logger.info("name = " + name);
			category.setName(name);
		}
		if (isPublic != null) {
			logger.info("isPublic = " + isPublic);
			category.setPublic(isPublic);
		}
		if (lang != null) {
			logger.info("lang = " + lang);
			category.setLang(lang);
		}
		category.setChannelCnt(channelCnt);
		categoryMngr.save(category);

		Category c2 = categoryMngr.findByLangAndSeq(category.getLang(), seq);
		if (category != null && c2 != null) {
			short seq1 = category.getSeq();
			category.setSeq(seq);
			c2.setSeq(seq1);
			categoryMngr.save(category);
			categoryMngr.save(c2);
		}
		if (category != null && c2 == null) {
			category.setSeq(seq);			
			categoryMngr.save(category);
		}
		
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
				writer.write("<option value=\"" + category.getId() + "\">" + category.getName() + "</option>");
			writer.write("</select>");
			writer.close();
		} catch (IOException e) {
			return;
		}
	}
}
