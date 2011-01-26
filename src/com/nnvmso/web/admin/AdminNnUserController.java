package com.nnvmso.web.admin;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.nnvmso.model.*;
import com.nnvmso.service.*;

@Controller
@RequestMapping("admin/nnuser")
public class AdminNnUserController {

	protected static final Logger logger = Logger.getLogger(AdminNnUserController.class.getName());
	
	public final NnUserManager nnUserMngr;
	
	@Autowired
	public AdminNnUserController(NnUserManager nnUserMngr) {
		this.nnUserMngr = nnUserMngr;		
	}	

	@RequestMapping("create")
	public @ResponseBody String create(@RequestParam(value="email")String email, String password, String name) {		
		NnUser nnUser = new NnUser(email, password, name, NnUser.TYPE_USER);		
		nnUserMngr.create(nnUser);		
		return "OK";
	}

	@RequestMapping("login")
	public @ResponseBody String create(@RequestParam(value="email")String email, String password, HttpServletRequest req, HttpServletResponse resp) {
		return "OK";
	}
	
}
