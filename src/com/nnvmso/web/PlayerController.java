package com.nnvmso.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import com.nnvmso.lib.*;
import com.nnvmso.model.*;
import com.nnvmso.service.*;

@Controller
@RequestMapping("player")
public class PlayerController {
		
	@RequestMapping("zooatomics")
	public String zooatomics(HttpServletRequest req, HttpServletResponse resp) {		
		if (CookieHelper.getCookie(req, "platform") == null) {
			CookieHelper.setCookie(resp, "platform", "gae");
		}
		return "player/zooatomics";
	}
		
	@RequestMapping("embed")
	public String embeded(Model model) {
		MsoManager service = new MsoManager();
		Mso mso = service.findByEmail("default_mso@9x9.com");
		model.addAttribute("msoKey", NnLib.getKeyStr(mso.getKey()));
		return ("player/embed");
	}
	
}
