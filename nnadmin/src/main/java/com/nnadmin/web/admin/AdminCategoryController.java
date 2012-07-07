package com.nnadmin.web.admin;

import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnadmin.lib.NnNetUtil;

@Controller
@RequestMapping("category")
public class AdminCategoryController {
	
	protected static final Logger logger = Logger.getLogger(AdminCategoryController.class.getName());

    @RequestMapping("world")
    @ResponseBody
    public String world(HttpServletRequest req) throws Exception {
    	String addr = req.getLocalAddr();
    	String port = String.valueOf(req.getLocalPort());
    	String name = req.getLocalName();
        String message = addr + "; " + port + "; " + name;
        return message;
    }    
	
	/**
	 * Category create
	 * 
	 * @param name category nama
	 * @param lang category language, en or zh
	 * @param seq category sequence in the directory
	 * @param isPublic to show in directory or not
	 * @return status in text
	 */
	@RequestMapping("create") 	
	public @ResponseBody String create(@RequestParam(required=true) String name,
									   @RequestParam(required=true) String lang,
									   @RequestParam short seq,
									   @RequestParam(required=false) String isPublic,
									   HttpServletRequest req,
									   HttpServletResponse resp) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);
		return "OK";
	}
	
	@RequestMapping(value = "list", params = {"page", "rows", "sidx", "sord"})
	public void list(@RequestParam(value = "page")   Integer      currentPage,
	                 @RequestParam(value = "rows")   Integer      rowsPerPage,
	                 @RequestParam(value = "sidx")   String       sortIndex,
	                 @RequestParam(value = "sord")   String       sortDirection,
	                 @RequestParam(required = false) String       searchField,
	                 @RequestParam(required = false) String       searchOper,
	                 @RequestParam(required = false) String       searchString,
	                 HttpServletRequest req,
	                 HttpServletResponse resp,
	                 OutputStream out) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiGet(urlStr, resp);
	}

	@RequestMapping("delete")
	public @ResponseBody String delete(
				@RequestParam(required=true) Long id,
	            HttpServletRequest req,
	            HttpServletResponse resp) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);
		return "OK";
	}
	
	/**
	 * Category edit
	 * 
	 * @param id category id
	 * @param name catetory name
	 * @param isPublic to be shown in directory or not
	 * @param seq sequence in the directory
	 * @param channelCnt channel count
	 * @param lang language, zh or en
	 * @return status in text
	 */
	@RequestMapping("edit")
	public @ResponseBody String edit(
			@RequestParam(required=true)  Long    id,
			@RequestParam(required=false) String  name,			
	        @RequestParam(required=false) Boolean isPublic,
	        @RequestParam(required=false) short  seq,
	        @RequestParam(required=false) int  channelCnt,
	        @RequestParam(required=false) String  lang,
            HttpServletRequest req,
            HttpServletResponse resp) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);		
		return "OK";
	}

	/**
	 * Add the set to the category
	 * 
	 * @param category category id
	 * @param set set id
	 * @return status in text
	 */
	@RequestMapping(value="addSet")
	public @ResponseBody String addSet(
			             @RequestParam(required = false) long category,
			             @RequestParam(required = false) long set,
			             HttpServletRequest req,
			             HttpServletResponse resp,			             
	                     OutputStream out) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);
		return "OK";		
	}

	/**
	 * List sets of the category
	 * 
	 * @param categoryId category id
	 * @param currentPage current page
	 * @param rowsPerPage rows per page
	 * @param sortIndex sorting field
	 * @param sortDirection asc or desc
	 */
	//list every channel under a category	
	@RequestMapping(value = "listSet", params = {"category", "page", "rows", "sidx", "sord"})
	public void listSet(@RequestParam(value = "category") Long         categoryId,
	                    @RequestParam(value = "page")     Integer      currentPage,
	                    @RequestParam(value = "rows")     Integer      rowsPerPage,
	                    @RequestParam(value = "sidx")     String       sortIndex,
	                    @RequestParam(value = "sord")     String       sortDirection,
			            HttpServletRequest req,
			            HttpServletResponse resp,			             
	                    OutputStream out) {
		System.out.println("category:" + categoryId + ";page:" + currentPage + ";rows:" + 
				rowsPerPage + ";sidex:" + sortIndex + ";sortDirection:sortDirection");		
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiGet(urlStr, resp);
	}
}