package com.nnvmso.web.admin;

import java.util.logging.Logger;
import java.util.List;
import java.lang.Boolean;
import java.lang.Long;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import com.nnvmso.lib.*;
import com.nnvmso.model.*;
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
	public ResponseEntity<String> list(@RequestParam(required=false)String ids) {
		
		List<Category> categories = null;
		if (ids == null)
			categories = categoryMngr.findAll();
		else
			categories = categoryMngr.findAllByIds(ids);
		
		String[] title = {"key", "msoId", "isPublic", "channelCount", "name"};
		String result = "";
		for (Category c:categories) {
			String[] ori = {NnStringUtil.getKeyStr(c.getKey()),
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
	
	@RequestMapping("create")
	public @ResponseBody String create(@RequestParam(required=true)String name,
	                                   @RequestParam(required=true)String msoId) {
		
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findById(Long.parseLong(msoId));
		if (mso == null)
			return "Invalid msoId";
		categoryMngr.create(new Category(name, true, Long.parseLong(msoId)));
		return "OK";
	}
	
	@RequestMapping("modify")
	public @ResponseBody String modify(@RequestParam(required=true)  String key,
	                                   @RequestParam(required=false) String name,
	                                   @RequestParam(required=false) String isPublic,
	                                   @RequestParam(required=false) String channelCount) {
		
		logger.info("name: " + name + " isPublic: " + isPublic + " channelCount: " + channelCount + " key: " + key);
		Category category = categoryMngr.findByKeyStr(key);
		if (category == null)
			return "Category Not Found";
		
		if (name != null)
			category.setName(name);
		if (isPublic != null)
			category.setPublic(Boolean.valueOf(isPublic));
		if (channelCount != null)
			category.setChannelCount(Integer.parseInt(channelCount));
		
		categoryMngr.save(category);
		return "OK";
	}
}
