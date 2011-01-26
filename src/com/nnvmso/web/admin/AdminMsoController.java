package com.nnvmso.web.admin;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.NnLogUtil;
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
	
}
