package com.nnvmso.web;

import java.util.List;

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

import com.nnvmso.lib.DebugLib;
import com.nnvmso.lib.NnLib;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.Player;
import com.nnvmso.service.ChannelService;
import com.nnvmso.service.MsoService;
import com.nnvmso.service.NnUserService;
import com.nnvmso.service.PlayerService;

@Controller
@RequestMapping("admin")
@SessionAttributes("player")
public class AdminController {

	public final MsoService msoService;
	public final PlayerService playerService;
	public final NnUserService userService;
	
	@Autowired
	public AdminController(MsoService msoService, PlayerService playerService, NnUserService userService) {
		this.msoService = msoService;
		this.playerService = playerService;
		this.userService = userService;
	}	
	
	@RequestMapping(value="index")
	public String index(Model model) {
		Mso mso = msoService.findByEmail("a@a.com");
		Mso awsMso = msoService.findByEmail("aws@9x9.com");
		ChannelService cService = new ChannelService();
		MsoChannel c = cService.findByMso(awsMso.getKey()).get(0);
		NnUser user = userService.findByEmail("u@u.com");
		String hostname = "http://nnvmso.appspot.com";
		if (DebugLib.NNDEVEL) {
			hostname = "http://localhost:8888";
		}
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
	public String msoSingupSubmit(@ModelAttribute Mso mso) {
		msoService.create(mso);
		return "redirect:/admin/msoListing";		
	}

	@RequestMapping("msoListing")
	public String msoListing(Model model) {
		List<Mso> msos = msoService.findAll();
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
		playerService.create(msoKey, player);
		status.setComplete();
		return "redirect:/admin/playerEdit?id=" + msoKey;
	}	
	
	@RequestMapping(value="playerEdit", method=RequestMethod.GET)
	public String editForm(@RequestParam(value="id") String msoKey, Model model) {		
		Player player = playerService.findByMsoKey(msoKey);
		player.getMso().setKeyStr(msoKey);		
		model.addAttribute("player", player);
		return ("admin/playerForm");
	}
	
	@RequestMapping(value="playerEdit", method=RequestMethod.POST)
	public String onEditSubmit(@RequestParam(value="id") String msoKey, @ModelAttribute("player") Player player, BindingResult result, SessionStatus status) {
		playerService.save(msoKey, player);
		status.setComplete();
		return "hello";
	}
	
	@RequestMapping("playerContainer")
    public @ResponseBody String container(@RequestParam(value="id") String msoKey) {
		Player player = playerService.findByMsoKey(msoKey);
		return player.getCode().getValue();
    }
	
}
