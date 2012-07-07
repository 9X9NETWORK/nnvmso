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
@RequestMapping("channel")
public class AdminChannelController {
	protected static final Logger logger = Logger.getLogger(AdminChannelController.class.getName());		
	
	/**
	 * Channel creation
	 * 
	 * @param url source url
	 * @param name channel name
	 * @param isPublic to be shown in the directory or not
	 * @param devel is in devel mode or not, i.e. whether to submit to transcoding service 
	 * @return status in text
	 */
	@RequestMapping("create")
	public @ResponseBody String create(				
			                           @RequestParam(value="sourceUrl", required=false)String url,
				                       @RequestParam(value="name", required=false) String name,
				                       @RequestParam(required=false) Boolean isPublic,
				                       @RequestParam(value="devel",required=false) boolean devel,
				                       HttpServletRequest req,
					                   HttpServletResponse resp) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);		
		return "OK";
	}	
	
	/**
	 * Channel listing. List items in jqGrid format.
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
	 * @param currentPage current page
	 * @param rowsPerPage rows per page
	 * @param sortIndex sort field
	 * @param sortDirection asc or desc
	 * @param searchField search field
	 * @param searchOper search condition
	 * @param searchString search string 
	 * @param notify set to true for notification page
	 * @return status in text
	 */
	@RequestMapping(value = "list", params = {"page", "rows", "sidx", "sord"})
	public void list (
			         @RequestParam(value = "page")   Integer      currentPage,
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
	 * Channel modification
	 * 
	 * @param id channel id
	 * @param name channel name
	 * @param intro channel description
	 * @param imageUrl channel image url
	 * @param status channel status
	 * @param isPublic to show in the directory or not
	 * @param programCnt program count
	 * @return status in text
	 */
	@RequestMapping("modify")
	public @ResponseBody String modify(@RequestParam(required=true)  Long    id,
	                                   @RequestParam(required=false) String  name,
	                                   @RequestParam(required=false) String  intro,
	                                   @RequestParam(required=false) String  imageUrl,
	                                   @RequestParam(required=false) Short   status,
	                                   @RequestParam(required=false) Boolean isPublic,
	                                   @RequestParam(required=false) Integer programCnt,
	                                   HttpServletRequest req,
	              	                   HttpServletResponse resp) {
		String urlStr = NnNetUtil.getApiUrl(req);
		NnNetUtil.apiPost(urlStr, req, resp);
		return "OK";
	}	
	
	
}
