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
@RequestMapping("set")
public class AdminSetController {
	protected static final Logger logger = Logger.getLogger(AdminSetController.class.getName());

	/**
	 * Set listing
	 * 
	 * @param currentPage current page
	 * @param rowsPerPage rows per page
	 * @param sortIndex sort field
	 * @param sortDirection asc or desc
	 * @param searchField search field
	 * @param searchOper search condition
	 * @param searchString search string 
	 * @param notify set to true for notification page
	 */
	@RequestMapping(value = "list", params = {"page", "rows", "sidx", "sord"})
	public void list(@RequestParam(value = "page")   Integer      currentPage,
	                 @RequestParam(value = "rows")   Integer      rowsPerPage,
	                 @RequestParam(value = "sidx")   String       sortIndex,
	                 @RequestParam(value = "sord")   String       sortDirection,
	                 @RequestParam(required = false) String       searchField,
	                 @RequestParam(required = false) String       searchOper,
	                 @RequestParam(required = false) String       searchString,
	                 @RequestParam(required = false) boolean      notify,
	                 HttpServletRequest req,
	                 HttpServletResponse resp,	                 
	                 OutputStream out) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiGet(urlStr, resp);
	}

	/**
	 * Delete the channel from the set
	 * 
	 * @param set set id
	 * @param id channel id
	 * @return status in text
	 */
	@RequestMapping(value="deleteCh", params = {"id", "set"})
	public @ResponseBody String deleteCh(
			             @RequestParam(required = false) long set,	                 
			             @RequestParam(required = false) long id,
	                     OutputStream out,
		                 HttpServletRequest req,
		                 HttpServletResponse resp) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);		
		return "OK";
	}

	/**
	 * Add the channel to the set
	 * 
	 * @param channel channel id
	 * @param set set id
	 * @param seq channel sequence in the set, it is for featured set, set to 0 for not featured set
	 * @return status in text
	 */
	@RequestMapping(value="addCh")
	public @ResponseBody String addCh(
			             @RequestParam(required = false) long channel,
			             @RequestParam(required = false) long set,	                 
			             @RequestParam(required = false) String seq,
		                 HttpServletRequest req,
		                 HttpServletResponse resp,			             
	                     OutputStream out) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);		
		return "OK";
	}		
			
	/**
	 * Channel edit
	 * 
	 * @param channel channel id
	 * @param set set id
	 * @param seq channel sequence in the set, it is for featured set, set to 0 for not featured set
	 * @return status in text
	 */
	@RequestMapping(value="editCh")
	public @ResponseBody String editCh(
			             @RequestParam(required = false) long channel,
			             @RequestParam(required = false) long set,	                 
			             @RequestParam(required = false) short seq,
		                 HttpServletRequest req,
		                 HttpServletResponse resp,			             
	                     OutputStream out) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);		
		return "OK";		
	}

	/**
	 * Listing channels under the set
	 * 
	 * @param currentPage current page
	 * @param rowsPerPage rows per page
	 * @param sortIndex sort field
	 * @param sortDirection asc or desc
	 * @param searchField search field
	 * @param searchOper search condition
	 * @param searchString search string
	 * @param set set id
	 * @return status in text
	 */
	@RequestMapping(value = "listCh", params = {"page", "rows", "sidx", "sord", "set"})
	public void listCh(
			         @RequestParam(value = "page")   Integer      currentPage,
	                 @RequestParam(value = "rows")   Integer      rowsPerPage,
	                 @RequestParam(value = "sidx")   String       sortIndex,
	                 @RequestParam(value = "sord")   String       sortDirection,
	                 @RequestParam(required = false) String       searchField,
	                 @RequestParam(required = false) String       searchOper,
	                 @RequestParam(required = false) String       searchString,
	                 @RequestParam(required = false) long       set,
	                 HttpServletRequest req,
	                 HttpServletResponse resp,	                 
	                 OutputStream out) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiGet(urlStr, resp);				
	}

	/**
	 * Set delition
	 * 
	 * @param id set id
	 * @return status in text
	 */
	@RequestMapping("delete")
	public @ResponseBody String delete(
			               @RequestParam(required=false) long id,
			               HttpServletRequest req,
			               HttpServletResponse resp ) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);		
		return "OK";
	}

	/**
	 * Set edititon
	 * 
	 * @param id set id
	 * @param name set name
	 * @param intro set description
	 * @param lang set language, en or zh
	 * @param imageUrl image url
	 * @param beautifulUrl outside access url
	 * @param isPublic to show in directory or not
	 * @param featured is featured set or not
	 * @param seq for featured sets. sequence shown in the front page directory 
	 * @return status in text
	 */
	@RequestMapping("edit")
	public @ResponseBody String edit(
			@RequestParam(required=false) long id,
			@RequestParam(required=false) String name,
            @RequestParam(required=false) String intro,
            @RequestParam(required=false) String lang,
            @RequestParam(required=false) String imageUrl,
            @RequestParam(required=false) String beautifulUrl,
            @RequestParam(required=false) String isPublic,
            @RequestParam(required=false) String featured,
            @RequestParam(required=false) String channelIds,
            @RequestParam(required=false) String seq,
            HttpServletRequest req,
            HttpServletResponse resp) {                        
		return "OK";
	}

	/**
	 * Set creation
	 * 
	 * @param name set name
	 * @param intro set description 
	 * @param featured is feauted set or not
	 * @param imageUrl image url
	 * @param beautifulUrl outside access url
	 * @param seq seq for featured sets. sequence shown in the front page directory 
	 * @param lang zh or en
	 * @return status in text
	 */
	@RequestMapping(value = "create", params = {"name", "intro", "featured", "lang", "imageUrl", "beautifulUrl", "seq"})
	public @ResponseBody String create(@RequestParam String name,	                                   
	                                   @RequestParam String intro,
	                                   @RequestParam boolean featured,
	                                   @RequestParam String imageUrl,
	                                   @RequestParam String beautifulUrl,
	                                   @RequestParam String seq,
	                                   @RequestParam String lang,
		              	               HttpServletRequest req,
		            	               HttpServletResponse resp ) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);		
		return "OK";		
	}
		
}
