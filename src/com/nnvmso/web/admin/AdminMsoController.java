package com.nnvmso.web.admin;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.Math;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import org.codehaus.jackson.map.ObjectMapper;

import com.nnvmso.lib.*;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ContentOwnership;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.*;

@Controller
@RequestMapping("admin/mso")
public class AdminMsoController {
	
	protected static final Logger logger = Logger.getLogger(AdminMsoController.class.getName());

	public final MsoManager  msoMngr;
	public final UserService userService;
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}

	@Autowired
	public AdminMsoController(MsoManager msoMngr) {
		this.msoMngr     = msoMngr;		
		this.userService = UserServiceFactory.getUserService();
	}	
	
	@RequestMapping("create")
	public @ResponseBody String create(@RequestParam(value="name")String name, @RequestParam(value="intro")String intro,
			                           @RequestParam(value="contactEmail")String contactEmail) {
		Mso mso = new Mso(name, intro, contactEmail, Mso.TYPE_MSO);		
		msoMngr.create(mso);		
		return "OK";
	}
	
	@RequestMapping(value = "create", params = {"name", "contactEmail", "password", "logoUrl", "type"})
	public @ResponseBody String create(@RequestParam String name,
	                                   @RequestParam String contactEmail,
	                                   @RequestParam String password,
	                                   @RequestParam String logoUrl,
	                                   @RequestParam Short  type) {
		logger.info("name = " + name);
		logger.info("contactEmail = " + contactEmail);
		logger.info("password = " + password);
		logger.info("logoUrl = " + logoUrl);
		logger.info("type = " + type);
		
		NnUserManager userMngr = new NnUserManager();
		MsoManager msoMngr = new MsoManager();
		ChannelSetManager setMngr = new ChannelSetManager();
		
		Mso found = msoMngr.findByName(name);
		if (found != null) {
			return "Name In Used";
		}
		short userType;
		if (type == Mso.TYPE_3X3) {
			userType = NnUser.TYPE_3X3;
		} else if (type == Mso.TYPE_ENTERPRISE) {
			userType = NnUser.TYPE_ENTERPRISE;
		} else {
			return "Only Type 3x3 Is Applicable";
		}
		
		Mso mso = new Mso(name, name, contactEmail, type);
		mso.setTitle(name);
		mso.setPreferredLangCode(Mso.LANG_EN);
		mso.setLogoUrl(logoUrl);
		msoMngr.create(mso);
		
		NnUser user = new NnUser(contactEmail, password, name, userType);
		user.setMsoId(mso.getKey().getId());
		userMngr.create(user, null);
		
		ChannelSet channelSet = new ChannelSet(mso.getKey().getId(), name, name, true);
		channelSet.setDefaultUrl(String.valueOf(mso.getKey().getId()));
		channelSet.setBeautifulUrl(name);
		channelSet.setPublic(false); // to prevent set to appear to directory
		setMngr.create(channelSet, new ArrayList<MsoChannel>());
		
		//channelSet ownership
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		ownershipMngr.create(new ContentOwnership(), mso, channelSet);
		
		return "OK";
	}
	
	@RequestMapping("list")
	public ResponseEntity<String> list() {		
		List<Mso> msoList = msoMngr.findAll();
		
		String[] title = {"id", "type", "lang", "name", "contactEmail", "intro", "logoUrl", "logoClickUrl", "JingleUrl"};
		String result = "";
		for (Mso mso:msoList) {
			String[] ori = {String.valueOf(mso.getKey().getId()),
			                Short.toString(mso.getType()),
			                mso.getPreferredLangCode(),
			                mso.getName(),
			                mso.getContactEmail(),
			                mso.getIntro(),
			                mso.getLogoUrl(),
			                mso.getLogoClickUrl(),
			                mso.getJingleUrl()};
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
		
		NnUserManager userMngr = new NnUserManager();
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> dataRows = new ArrayList<Map<String, Object>>();
		
		String filter = "";
		if (searchField != null && searchOper != null && searchString != null && !searchString.isEmpty()) {
			
			Map<String, String> opMap = JqgridHelper.getOpMap();
			if (opMap.containsKey(searchOper)) {
				filter = searchField + " " + opMap.get(searchOper) + " " + searchString;
				logger.info("filter: " + filter);
				sortIndex = "updateDate";
				sortDirection = "desc";
			}
		}
		
		int totalRecords = msoMngr.total(filter);
		int totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
		if (currentPage > totalPages)
			currentPage = totalPages;
		
		List<Mso> results = msoMngr.list(currentPage, rowsPerPage, sortIndex, sortDirection, filter);
		
		for (Mso mso : results) {
			
			Map<String, Object> map = new HashMap<String, Object>();
			List<Object> cell = new ArrayList<Object>();
			
			cell.add(mso.getLogoUrl());
			cell.add(mso.getKey().getId());
			cell.add(mso.getName());
			cell.add(mso.getTitle());
			cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mso.getUpdateDate()));
			cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mso.getCreateDate()));
			cell.add(mso.getLogoClickUrl());
			cell.add(mso.getJingleUrl());
			cell.add(mso.getType());
			cell.add(mso.getPreferredLangCode());
			cell.add(mso.getContactEmail());
			cell.add("********");
			cell.add(mso.getIntro());
			cell.add(userMngr.total("msoId == " + mso.getKey().getId() + " && email != '" + NnUser.GUEST_EMAIL + "'"));
			
			map.put("id", mso.getKey().getId());
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
	public @ResponseBody String modify(@RequestParam(required=true)  Long   id,
	                                   @RequestParam(required=false) String name,
	                                   @RequestParam(required=false) String title,
	                                   @RequestParam(required=false) String contactEmail,
	                                   @RequestParam(required=false) String intro,
	                                   @RequestParam(required=false) String preferredLangCode,
	                                   @RequestParam(required=false) String logoUrl,
	                                   @RequestParam(required=false) String logoClickUrl,
	                                   @RequestParam(required=false) String jingleUrl) {
		
		logger.info("admin = " + userService.getCurrentUser().getEmail());
		
		logger.info("msoId = " + id);
		Mso mso = msoMngr.findById(id);
		if (mso == null) {
			String error = "MSO Not Found";
			logger.warning(error);
			return error;
		}
		
		if (name != null) {
			logger.info("name = " + name);
			mso.setName(name);
			mso.setNameSearch(name.toLowerCase());
		}
		if (name != null) {
			logger.info("title = " + title);
			mso.setTitle(title);
		}
		if (contactEmail != null) {
			logger.info("contactEmail = " + contactEmail);
			mso.setContactEmail(contactEmail);
		}
		if (intro != null) {
			logger.info("intro = " + intro);
			mso.setIntro(intro);
		}
		if (preferredLangCode != null) {
			logger.info("preferredLangCode = " + preferredLangCode);
			mso.setPreferredLangCode(preferredLangCode);
		}
		if (logoUrl != null) {
			logger.info("logoUrl = " + logoUrl);
			mso.setLogoUrl(logoUrl);
		}
		if (logoClickUrl != null) {
			logger.info("logoClickUrl = " + logoClickUrl);
			mso.setLogoClickUrl(logoClickUrl);
		}
		if (jingleUrl != null) {
			logger.info("jingleUrl = " + jingleUrl);
			mso.setJingleUrl(jingleUrl);
		}
		
		msoMngr.save(mso);
		return "OK";
	}
	
	@RequestMapping("msoHtmlSelectOptions")
	public void msoHtmlSelectOptions(HttpServletResponse response, OutputStream out) {
		
		response.setContentType("text/html;charset=utf-8");
		OutputStreamWriter writer;
		try {
			writer = new OutputStreamWriter(out, "UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			return;
		}
		List<Mso> msoList = msoMngr.findAll();
		try {
			writer.write("<select>");
			writer.write("<option selected=\"selected\" value=\"0\">None</option>");
			for (Mso mso : msoList) {
				writer.write("<option value=\"" + mso.getKey().getId() + "\">" + mso.getName() + "</option>");
			}
			writer.write("</select>");
			writer.close();
		} catch (IOException e) {
			return;
		}
	}
}
