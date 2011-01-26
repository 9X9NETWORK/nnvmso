package com.nnvmso.web.admin;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.model.Category;
import com.nnvmso.service.*;

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
	 
	@RequestMapping("list")
	public @ResponseBody String list(@RequestParam(required=false)String ids) {
		return "OK";
	}
}
