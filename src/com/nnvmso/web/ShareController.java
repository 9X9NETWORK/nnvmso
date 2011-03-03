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
		//invalid ipgid
		IpgManager ipgMngr = new IpgManager();
		if (!Pattern.matches("^\\d*$", ipgId)) {
			log.info("invalid ipg id");
			return "redirect:/";
		}				
		Ipg ipg = ipgMngr.findById(Long.parseLong(ipgId));
		if (ipg == null) {
			return "redirect:/";
		}
		//find mso info of the user who shares the ipg
		NnUser user = new NnUserManager().findById(ipg.getUserId());		
		if (user != null) {
			Mso mso = new MsoManager().findById(user.getMsoId());
			if (mso != null && mso.getName().equals(Mso.NAME_5F)) {
				CookieHelper.setCookie(resp, CookieHelper.MSO, Mso.NAME_5F);
			} else {
				CookieHelper.deleteCookie(resp, CookieHelper.MSO);
			}
		}
		//give fb icon
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoProgram p = programMngr.findById(ipg.getProgramId()); 
		String now = (new SimpleDateFormat("MM.dd.yyyy")).format(new Date()).toString();
		model.addAttribute("now", now);		
		String fbImg = "http://9x9ui.s3.amazonaws.com/9x9playerV39/images/9x9-facebook-icon.png";
		if (p != null && p.getImageUrl() != null && p.getImageUrl().length() > 0) {
			fbImg = p.getImageUrl();
		}
		model.addAttribute("fbImg", fbImg);
		
		return "player/zooatomics";
	}
}
