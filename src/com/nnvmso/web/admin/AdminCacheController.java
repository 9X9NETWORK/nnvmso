package com.nnvmso.web.admin;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Category;
import com.nnvmso.model.Mso;
import com.nnvmso.service.CategoryManager;
import com.nnvmso.service.MsoManager;

@Controller
@RequestMapping("admin/cache")
public class AdminCacheController {

	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}
	
	//cache add, one mso at a time, depends on the url
	//cache delete, delete all
	@RequestMapping("mso")
	public ResponseEntity<String> cacheMso(@RequestParam(value="delete", required=false)boolean delete, HttpServletRequest req) {
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findMsoViaHttpReq(req);
		if (delete) { msoMngr.deleteCache();}
		return NnNetUtil.textReturn(mso.getName());
	}
	
	//cache add, one mso at a time
	@RequestMapping("category")
	public ResponseEntity<String> cacheCategory(@RequestParam("mso") long msoId, @RequestParam(value="delete", required=false)boolean delete, HttpServletRequest req) {
		CategoryManager categoryMngr = new CategoryManager();
		String output = "";
		if (delete) {
			categoryMngr.deleteCache(msoId);
			output = "Delete";
		} else {
			List<Category> categories = categoryMngr.findAllByMsoId(msoId);
			for (Category c : categories) {
				output = output + c.getName() + "\n";
			}
		}
		return NnNetUtil.textReturn(output);
	}	
		
}
