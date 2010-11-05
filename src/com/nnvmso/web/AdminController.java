package com.nnvmso.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.nnvmso.lib.NnLib;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.Player;
import com.nnvmso.service.ChannelManager;
import com.nnvmso.service.InitService;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.PlayerManager;

@Controller
@RequestMapping("admin")
@SessionAttributes("{player}")
public class AdminController {

	public final MsoManager msoMngr;
	public final PlayerManager playerMngr;
	public final NnUserManager userMngr;
	
	@Autowired
	public AdminController(MsoManager msoMngr, PlayerManager playerMngr, NnUserManager userMngr) {
		this.msoMngr = msoMngr;
		this.playerMngr = playerMngr;
		this.userMngr = userMngr;
	}	
	
	@RequestMapping(value="init")
	public String init() {
		InitService service = new InitService();
		service.init();
		return "hello/hello";
	}
	
	@RequestMapping(value="index")
	public String index(Model model, HttpServletRequest req, HttpServletResponse resp) {
		Mso mso = msoMngr.findByEmail("a@a.com");
		Mso awsMso = msoMngr.findByEmail("aws@9x9.com");
		ChannelManager channelMngr = new ChannelManager();
		MsoChannel c = channelMngr.findByMso(awsMso.getKey()).get(0);
		NnUser user = userMngr.findByEmail("u@u.com");
		String hostname = NnLib.getUrlRoot(req);
		model.addAttribute("hostname", hostname); 
		model.addAttribute("userKey", NnLib.getKeyStr(user.getKey()));
		model.addAttribute("msoKey", NnLib.getKeyStr(mso.getKey()));
		model.addAttribute("awsChannel", c.getId());
		return "admin/index";
	}
	
	@RequestMapping(value="msoSignup", method=RequestMethod.GET)
	public String msoSignupForm(Model model) {	    			
		Mso mso = new Mso();
		model.addAttribute(mso);
		return ("admin/msoSignupForm");		
	}
	
	@RequestMapping(value="msoSignup", method=RequestMethod.POST)
	public String msoSingupSubmit(@Valid Mso mso, BindingResult result) {
		if (result.hasErrors()) {
			return "admin/msoSignupForm";
		}
		msoMngr.create(mso);
		return "redirect:/admin/msoListing";		
	}

	@RequestMapping("msoListing")
	public String msoListing(Model model) {
		List<Mso> msos = msoMngr.findAll();
		System.out.println(msos.size());
		model.addAttribute("msos", msos);
		return ("admin/msoList");
	}

	@RequestMapping(value="playerCreate", method=RequestMethod.GET)
	public String playerCreateForm(Model model) {	    			
		Player player = new Player();
		model.addAttribute(player);		
		return ("admin/playerForm");		
	}
	
	@RequestMapping(value="playerCreate", method = RequestMethod.POST)
	public String playerCreatSubmit(@ModelAttribute Player player, SessionStatus status) {
		String msoKey = player.getMso().getKeyStr();
		playerMngr.create(msoKey, player);
		status.setComplete();
		return "redirect:/admin/playerEdit?id=" + msoKey;
	}	
	
	@RequestMapping(value="playerEdit", method=RequestMethod.GET)
	public String editForm(@RequestParam(value="id") String msoKey, Model model) {
		Player player = playerMngr.findByMsoKey(msoKey);
		player.getMso().setKeyStr(msoKey);		
		model.addAttribute("player", player); 
		model.addAttribute("code", new String(""));
		return ("admin/playerForm");
	}
	
	@RequestMapping(value="playerEdit", method=RequestMethod.POST)
	public String onEditSubmit(@RequestParam(value="id") String msoKey, @ModelAttribute("player") Player player, BindingResult result, SessionStatus status) {
		playerMngr.save(msoKey, player);
		status.setComplete();
		return "hello/hello";
	}
	
	@RequestMapping("playerContainer")
    public @ResponseBody String container(@RequestParam(value="id") String msoKey) {
		Player player = playerMngr.findByMsoKey(msoKey);
		return player.getCode().getValue();
    }
	
}
