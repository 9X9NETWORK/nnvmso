package com.nnvmso.web;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.service.PlayerService;

@Controller
@RequestMapping("")
public class PlayerController {

	protected static final Logger logger = Logger.getLogger(PlayerController.class.getName());
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}		
	
	@RequestMapping("/")
	public String index() {
		return "redirect:9x9";
	}	
	
	/**
	 * to become a 9x9 player, 1)delete cookie, 2)set fb info 
	 */	
	@RequestMapping("{name}")
	public String zooatomics(@RequestParam(value="mso",required=false) String mso, HttpServletRequest req, HttpServletResponse resp, Model model, @PathVariable("name") String name) {
		PlayerService service = new PlayerService();
		model = service.prepareBrand(model, mso, resp);

		String prefLanguage = req.getHeader("Accept-Language");
		System.out.print(prefLanguage); //en-US,en;q=0.8  
		
		return "player/zooatomics";
	}
	
	@RequestMapping("support")
	public String support() {
		return "general/support";
	}	
	
	/*
	@RequestMapping("daai")
	public String daai(@RequestParam(value="mso",required=false) String mso, HttpServletRequest req, HttpServletResponse resp, Model model) {
		PlayerService service = new PlayerService();
		model = service.prepareBrand(model, mso, resp);		
		return "player/antelope";
	}
	*/
	
	/*
	 * used for dns redirect watch dog 
	 */
	@RequestMapping("9x9/wd")
	public ResponseEntity<String> watchdog() {		
		return NnNetUtil.textReturn("OK");
	}
	
	
}
