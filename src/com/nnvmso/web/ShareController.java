package com.nnvmso.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.IpgManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.MsoProgramManager;
import com.nnvmso.service.NnUserManager;

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
	public String zooatomics(@PathVariable String ipgId, HttpServletResponse resp, Model model) {
		log.info("/share/" + ipgId);
		//invalid ipgid
		IpgManager ipgMngr = new IpgManager();
		if (!Pattern.matches("^\\d*$", ipgId)) {
			log.info("invalid ipg id");
			return "redirect:/";
		}				
		Ipg ipg = ipgMngr.findById(Long.parseLong(ipgId));
		if (ipg == null) {
			log.info("can not find ipg:" + ipgId);
			return "redirect:/";
		}
		//find mso info of the user who shares the ipg
		if (ipg.getUserId() != 0) { //old data does not have userId
			NnUser user = new NnUserManager().findById(ipg.getUserId());		
			if (user != null) {
				log.info("This user," + user.getKey().getId() + ", shares ipg.");
				Mso mso = new MsoManager().findById(user.getMsoId());
				if (mso != null && mso.getName().equals(Mso.NAME_5F)) {
					CookieHelper.setCookie(resp, CookieHelper.MSO, Mso.NAME_5F);
				} else {
					CookieHelper.deleteCookie(resp, CookieHelper.MSO);
				}
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
		log.info("fbImg set on share:" + fbImg);
		return "player/zooatomics";
	}
}
