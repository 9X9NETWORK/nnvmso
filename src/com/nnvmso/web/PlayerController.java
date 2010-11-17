package com.nnvmso.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;

import com.nnvmso.form.UserSignupForm;
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
