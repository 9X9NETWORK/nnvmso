package com.nnvmso.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.Player;
import com.nnvmso.service.ChannelManager;
import com.nnvmso.service.InitService;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.PlayerAPI;
import com.nnvmso.service.PlayerManager;
import com.nnvmso.service.ProgramManager;

@Controller
@RequestMapping("admin")
@SessionAttributes("{player}")
public class AdminController {

	protected static final Logger logger = Logger.getLogger(AdminController.class.getName());
	
	public final MsoManager msoMngr;
	public final PlayerManager playerMngr;
	public final NnUserManager userMngr;
	public final ChannelManager channelMngr;
	public final ProgramManager programMngr;
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLib.logException(e);
		return "error/exception";				
	}	
	
	@Autowired
	public AdminController(MsoManager msoMngr, PlayerManager playerMngr, NnUserManager userMngr, ChannelManager channelMngr, ProgramManager programMngr) {
		this.msoMngr = msoMngr;		
		this.playerMngr = playerMngr;
		this.userMngr = userMngr;
		this.channelMngr = channelMngr;
		this.programMngr = programMngr;
	}	

	@RequestMapping(value="cacheChannelLineup")
	public @ResponseBody String cacheChannel() {
		
		Cache cache = null;		
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());
        } catch (CacheException e) {
            // ...
        }        
		List<MsoChannel> channels = new ArrayList<MsoChannel>();		
		channels = channelMngr.findAllPublic();		 
		for (MsoChannel c : channels) {			
			if (cache.get(c.getKey()) == null) {
				cache.put(c.getKey(), c);				
			} else {
				MsoChannel cached = (MsoChannel)cache.get(c.getKey());
				System.out.println("in the cache:" + cached.getName());
			}
		}				
		return "cache channel";
	}
	
	@RequestMapping(value="cacheProgram")
	public String cacheProgram() {
		Cache cache = null;		
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());
        } catch (CacheException e) {
            // ...
        }        
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		List<MsoProgram> programs = new ArrayList<MsoProgram>();		
		channels = channelMngr.findAllPublic();		
        PlayerAPI tool = new PlayerAPI(); 
		for (MsoChannel c : channels) {
			programs = programMngr.findByChannelIdAndIsPublic(c.getId(), true);
			String info = tool.composeProgramInfoStr(programs);
			cache.put(c.getKey().getId(), info);
		}
		return "hello/hello";
	}
	
	@RequestMapping(value="init")
	public String init() {
		InitService service = new InitService();
		service.init();
		return "hello/hello";
	}

	@RequestMapping(value="createPodcasts")
	public String createPodcasts(HttpServletRequest req) {
		InitService service = new InitService();
		service.createPodcastChannels(req);
		return "hello/hello";
	}
	
	@RequestMapping(value="createChannels")
	public String createChannels() {
		InitService service = new InitService();
		service.createChannels();
		return "hello/hello";
	}
	
	@RequestMapping(value="index")
	public String index(Model model, HttpServletRequest req, HttpServletResponse resp) {
		Mso mso = msoMngr.findByEmail("default_mso@9x9.com");
		Mso awsMso = msoMngr.findByEmail("aws@9x9.com");
		ChannelManager channelMngr = new ChannelManager();
		MsoChannel c = channelMngr.findByMso(awsMso.getKey()).get(0);
		NnUser user = userMngr.findByEmail("default_user@9x9.com");
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
	public String msoSingupSubmit(@Valid Mso mso, BindingResult result, HttpServletRequest req) {
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
	
	@RequestMapping("playerContainer")
    public @ResponseBody String container(@RequestParam(value="id") String msoKey) {
		Player player = playerMngr.findByMsoKey(msoKey);
		return player.getCode().getValue();
    }
	
}
