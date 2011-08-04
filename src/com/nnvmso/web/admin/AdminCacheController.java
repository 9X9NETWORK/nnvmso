package com.nnvmso.web.admin;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Category;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.CategoryManager;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.MsoProgramManager;

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
	public ResponseEntity<String> cacheMso(@RequestParam(value="delete", required=false)boolean delete, 
			                               @RequestParam(value="list", required=false)boolean list,
			                               HttpServletRequest req) {		
		MsoManager msoMngr = new MsoManager();		
		String output = "OK";
		if (delete) { 
			msoMngr.deleteCache();
		} else if (list) { 		
			output = msoMngr.findCache();
		} else {
			msoMngr.findMsoViaHttpReq(req);
		}
		return NnNetUtil.textReturn(output);
	}
	
	/**
	 * currently doing cache all, delete all, and list all, probably not right 
	 */
	@RequestMapping("channel")
	public ResponseEntity<String> cacheChannel(@RequestParam(required=false)Long channel, 
									           @RequestParam(required=false)boolean delete,
									           @RequestParam(required=false)boolean list) {		
		MsoChannelManager channelMngr = new MsoChannelManager();
		String output = "";
		if (list) {
			List<MsoChannel> channels = channelMngr.findCache();			
			for (MsoChannel c : channels) {
				output = output + c.getKey().getId() + "\n";
			}			
		} else if (delete) {
			channelMngr.deleteCache();
		} else {
			channelMngr.cacheAll();
		}
		return NnNetUtil.textReturn(output);
	}
	
	/** 
	 * @param channelId always required
	 * @param delete 1 indicates to delete
	 * @param list 1 indicates listing
	 */
	@RequestMapping("program")
	public ResponseEntity<String> cacheProgram(@RequestParam(required=true)Long channel, 
									           @RequestParam(required=false)boolean delete,
									           @RequestParam(required=false)boolean list) {		
		MsoProgramManager programMngr = new MsoProgramManager();
		String output = "";
		if (list) {
			output = programMngr.findCacheByChannel(channel);
		} else if (delete) {			
			programMngr.deleteCacheByChannel(channel);
		} else {
			programMngr.cacheByChannelId(channel);
		}
		return NnNetUtil.textReturn(output);
	}	
		
	//cache add, one mso at a time
	//cache listing, list all mso's
	/*
	@RequestMapping("category")
	public ResponseEntity<String> cacheCategory(@RequestParam(required=false)Long mso, 
			                                    @RequestParam(required=false)boolean delete,
			                                    @RequestParam(required=false)boolean list,
			                                    HttpServletRequest req) {
		CategoryManager categoryMngr = new CategoryManager();
		String output = "";
		if (delete) {
			categoryMngr.deleteCache(mso);
			output = "Delete";
		} else if (list) {
			output = categoryMngr.findCache();
		} else{		
			List<Category> categories = categoryMngr.findAllByMsoId(mso);
			for (Category c : categories) {
				output = output + c.getName() + "\n";
			}			
		}
		return NnNetUtil.textReturn(output);
	}
	*/	
		
	@RequestMapping("deleteAll")
	public ResponseEntity<String> deleteAll() {		
	    Cache cache = null;
	    try {
	        cache = CacheManager.getInstance().getCacheFactory().createCache(
	            Collections.emptyMap());
	      } catch (CacheException e) {}	      		

		if (cache != null) {
			cache.clear();
		}
		return NnNetUtil.textReturn("OK");
	}
	
}
