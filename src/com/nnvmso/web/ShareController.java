package com.nnvmso.web;

import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.lang.Long;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

import com.nnvmso.lib.*;
import com.nnvmso.model.*;
import com.nnvmso.service.*;

@Controller
@RequestMapping("share")
public class ShareController {

	protected static final Logger log = Logger.getLogger(ShareController.class.getName());
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}
	
	@RequestMapping("{ipgId}")
	public String zooatomics(@PathVariable String ipgId, HttpServletRequest req, HttpServletResponse resp, Model model) {
		log.info("/share/" + ipgId);
		IpgManager ipgMngr = new IpgManager();
		if (!Pattern.matches("^\\d*$", ipgId)) {
			log.info("invalid ipg id");
			return "redirect:/";
		}				
		Ipg ipg = ipgMngr.findById(Long.parseLong(ipgId));
		if (ipg == null) {
			return "redirect:/";
		}
		String now = (new SimpleDateFormat("MM.dd.yyyy")).format(new Date()).toString();
		model.addAttribute("now", now);
		return "player/zooatomics";
	}
}
