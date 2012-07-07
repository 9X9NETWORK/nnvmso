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
@RequestMapping("mso")
public class AdminMsoController {

	protected static final Logger log = Logger.getLogger(AdminMsoController.class.getName());
		
	/**
	 * Mso creation
	 * 
	 * @param name mso name
	 * @param contactEmail contact email
	 * @param password password
	 * @param logoUrl logo image url
	 * @param type mso type
	 * @return status in text
	 */
	@RequestMapping(value = "create", params = {"name", "contactEmail", "password", "logoUrl", "type"})
	public @ResponseBody String create(@RequestParam String name,
	                                   @RequestParam String contactEmail,
	                                   @RequestParam String password,
	                                   @RequestParam String logoUrl,
	                                   @RequestParam Short  type,
								       HttpServletRequest req,
								       HttpServletResponse resp) {	                 
		log.info("name = " + name);
		log.info("contactEmail = " + contactEmail);
		log.info("password = " + password);
		log.info("logoUrl = " + logoUrl);
		log.info("type = " + type);
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);
		return "OK";
	}

	/**
	 * Mso listing
	 * 
	 * @param currentPage current page
	 * @param rowsPerPage rows per page
	 * @param sortIndex sort field
	 * @param sortDirection asc or desc
	 * @param searchField search field name
	 * @param searchOper search condition
	 * @param searchString search string
	 */
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
		
	/**
	 * Mso editing
	 * 
	 * @param id mso id
	 * @param name mso name
	 * @param title mso title
	 * @param contactEmail contact email
	 * @param intro mso description
	 * @param lang mso default language, en or zh
	 * @param logoUrl mso logo image url
	 * @param jingleUrl mso jingle url
	 * @return
	 */
	@RequestMapping("modify")
	public @ResponseBody String modify(@RequestParam(required=true)  Long   id,
	                                   @RequestParam(required=false) String name,
	                                   @RequestParam(required=false) String title,
	                                   @RequestParam(required=false) String contactEmail,
	                                   @RequestParam(required=false) String intro,
	                                   @RequestParam(required=false) String lang,
	                                   @RequestParam(required=false) String logoUrl,
	                                   @RequestParam(required=false) String jingleUrl,
								       HttpServletRequest req,
								       HttpServletResponse resp) {	                 
		log.info("msoId = " + id);
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);
		return "OK";
	}

}
