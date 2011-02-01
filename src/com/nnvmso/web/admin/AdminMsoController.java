package com.nnvmso.web.admin;

import java.util.logging.Logger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import com.nnvmso.lib.*;
import com.nnvmso.model.Mso;
import com.nnvmso.service.*;

@Controller
@RequestMapping("admin/mso")
public class AdminMsoController {
	
	protected static final Logger logger = Logger.getLogger(AdminMsoController.class.getName());

	public final MsoManager msoMngr;
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}

	@Autowired
	public AdminMsoController(MsoManager msoMngr) {
		this.msoMngr = msoMngr;		
	}	
	
	@RequestMapping("create")
	public @ResponseBody String create(@RequestParam(value="name")String name, @RequestParam(value="intro")String intro,
			                           @RequestParam(value="contactEmail")String contactEmail) {
		Mso mso = new Mso(name, intro, contactEmail, Mso.TYPE_MSO);		
		msoMngr.create(mso);		
		return "OK";
	}
	
	@RequestMapping("list")
	public ResponseEntity<String> list() {
		
		List<Mso> msoList = msoMngr.findAll();
		
		String[] title = {"key", "type", "lang", "name", "contactEmail", "intro", "logoUrl", "logoClickUrl", "JingleUrl"};
		String result = "";
		for (Mso mso:msoList) {
			String[] ori = {NnStringUtil.getKeyStr(mso.getKey()),
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
	
	@RequestMapping("modify")
	public @ResponseBody String modify(@RequestParam(required=true)  String key,
	                                   @RequestParam(required=false) String name,
	                                   @RequestParam(required=false) String contactEmail,
	                                   @RequestParam(required=false) String intro,
	                                   @RequestParam(required=false) String preferredLangCode,
	                                   @RequestParam(required=false) String logoUrl,
	                                   @RequestParam(required=false) String logoClickUrl,
	                                   @RequestParam(required=false) String jingleUrl) {
		
		logger.info("name: " + name + " contactEmail: " + contactEmail + " preferredLangCode: " + preferredLangCode + " key: " + key);
		Mso mso = msoMngr.findByKeyStr(key);
		if (mso == null)
			return "Mso Not Found";
		
		if (name != null) {
			mso.setName(name);
			mso.setNameSearch(name.toLowerCase());
		}
		if (contactEmail != null)
			mso.setContactEmail(contactEmail);
		if (intro != null)
			mso.setIntro(intro);
		if (contactEmail != null)
			mso.setContactEmail(contactEmail);
		if (preferredLangCode != null)
			mso.setPreferredLangCode(preferredLangCode);
		if (logoUrl != null)
			mso.setLogoUrl(logoUrl);
		if (logoClickUrl != null)
			mso.setLogoClickUrl(logoClickUrl);
		if (jingleUrl != null)
			mso.setJingleUrl(jingleUrl);
		
		msoMngr.save(mso);
		return "OK";
	}
}
