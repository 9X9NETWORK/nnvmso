package com.nnvmso.web.admin;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.lib.NnStringUtil;
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

	@RequestMapping("subscription")
	public ResponseEntity<String> subscription(@RequestParam(required=false) String token, @RequestParam(required=false) Long id) {
		SubscriptionManager subMngr = new SubscriptionManager();
		NnUser user = null;
		if (token != null) {
			user = nnUserMngr.findByToken(token);
		} else {
			user = nnUserMngr.findById(id);
		}
		if (user == null) { return NnNetUtil.textReturn("user does not exist"); }		
		String output = "email\tkey\tid\ttoken\n-----------------\n";
		output = output + user.getEmail() + "\t" + NnStringUtil.getKeyStr(user.getKey()) + "\t" + user.getKey().getId() + "\t" + user.getToken();
		output = output + "\n\n";
		output = output + "key\tid\tname\turl\ttype\tprogramCount\tstatus\n-----------------\n";
		List<MsoChannel> channels = subMngr.findSubscribedChannels(user.getKey().getId(), user.getMsoId());		
		for (MsoChannel c : channels) {			
			output = output + NnStringUtil.getKeyStr(c.getKey()) + "\t" + c.getKey().getId() + "\t" + c.getName() + "\t" + c.getSourceUrl() + 
							  "\t" + c.getType() + "\t" + c.getProgramCount() + "\t" + c.getStatus();
			output = output + "\n";
		}
		return NnNetUtil.textReturn(output);
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
