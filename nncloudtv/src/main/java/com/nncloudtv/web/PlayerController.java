package com.nncloudtv.web;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.service.PlayerService;

@Controller
@RequestMapping("")
public class PlayerController {

	protected static final Logger log = Logger.getLogger(PlayerController.class.getName());
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}		
		
	@RequestMapping("10ft")
	public String tenft(@RequestParam(value="mso",required=false) String mso, HttpServletRequest req, HttpServletResponse resp, Model model) {
		try {
			PlayerService service = new PlayerService();
			model = service.prepareBrand(model, mso, resp);
		} catch (Throwable t) {
			NnLogUtil.logThrowable(t);			
		}			
		return "player/mini";
	}	
	
	@RequestMapping("tv")
	public String tv(@RequestParam(value="mso",required=false) String mso, HttpServletRequest req, HttpServletResponse resp, Model model) {
		try {
			PlayerService service = new PlayerService();
			model = service.prepareBrand(model, mso, resp);
		} catch (Throwable t) {
			NnLogUtil.logThrowable(t);			
		}
		return "player/mini";
	}	
	
	@RequestMapping("/")
	public String index(
			@RequestParam(value="name",required=false) String name,
			@RequestParam(value="jsp",required=false) String jsp,
			@RequestParam(value="js",required=false) String js,
			@RequestParam(value="mso",required=false) String mso, 
			HttpServletRequest req, HttpServletResponse resp, Model model) {
		try {
			PlayerService service = new PlayerService();		
			model = service.prepareBrand(model, mso, resp);
			model = service.prepareSetInfo(model, name, resp);			
			model.addAttribute("js", "");
			if (js != null && js.length() > 0) {
				model.addAttribute("js", js);
			}
			if (jsp != null && jsp.length() > 0) {
				/*
				if (jsp.equals("crawled")) {
					model = service.prepareCrawled(model);
				}
				*/
				log.info("alternate is enabled: " + jsp);
				return "player/" + jsp;
			}
			//String prefLanguage = req.getHeader("Accept-Language");		
		} catch (Throwable t) {
			NnLogUtil.logThrowable(t);
		}
		return "player/zooatomics";
	}	
	
	/**
	 * original url: /durp
	 * redirect to:  #!landing=durp  
	 */	
	@RequestMapping("{name}")
	public String zooatomics(
			@PathVariable("name") String name,
			//@RequestParam(value="name",required=false) String name,
			@RequestParam(value="jsp",required=false) String jsp,
			@RequestParam(value="js",required=false) String js,
			@RequestParam(value="mso",required=false) String mso, 
			HttpServletRequest req, HttpServletResponse resp, Model model) {
		if (name != null) {
			PlayerService service = new PlayerService();
			String url = service.rewrite(js, jsp); 
			return "redirect:/" + url + "#!landing=" + name;
		}
		//actually won't continue
		try {
			PlayerService service = new PlayerService();		
			model = service.prepareBrand(model, mso, resp);
			model = service.prepareSetInfo(model, name, resp);			
			model = service.preparePlayer(model, js, jsp);			
			if (jsp != null && jsp.length() > 0) {
				return "player/" + jsp;
			}
			//String prefLanguage = req.getHeader("Accept-Language");		
		} catch (Throwable t) {
			NnLogUtil.logThrowable(t);
		}
		return "player/zooatomics";
	}
	
	/**
	 * original url: view?channel=x&episode=y
	 * redirect to:  #!ch=x!ep=y  
	 */
	@RequestMapping("view")
	public String view(@RequestParam(value="mso",required=false) String mso, 
			           HttpServletRequest req, HttpServletResponse resp, Model model, 
			           @RequestParam(value="channel", required=false) String channel,
			           @RequestParam(value="episode", required=false) String episode,
			           @RequestParam(value="js",required=false) String js,
						@RequestParam(value="jsp",required=false) String jsp,
				       @RequestParam(value="ch", required=false) String ch,
				       @RequestParam(value="ep", required=false) String ep) {
		PlayerService service = new PlayerService();
		String url = service.rewrite(js, jsp);
		String cid = channel;
		if (ch != null)
			cid = ch;
		String pid = episode;
		if (ep != null)
			pid = ep;
		String epStr = "";
		if (pid != null)
			epStr = "!ep=" + episode;
		return "redirect:/" + url + "#!ch=" + cid + epStr; 
		/*
		try {
			PlayerService service = new PlayerService();
			model = service.prepareBrand(model, mso, resp);
			if (episode != null) {
				model = service.prepareEpisode(model, episode, resp);
			} else {
				model = service.prepareChannel(model, channel, resp);
			}
			model = service.preparePlayer(model, js, jsp);
			if (jsp != null && jsp.length() > 0) {
				return "player/" + jsp;
			}
			return "player/zooatomics";
		} catch (Throwable t){
			NnLogUtil.logThrowable(t);
			return "player/zooatomics";			
		}
		*/
	}
	
	@RequestMapping("support")
	public String support() {
		return "general/support";
	}	
		
	/*
	 * used for dns redirect watch dog 
	 */
	@RequestMapping("9x9/wd")
	public ResponseEntity<String> watchdog() {		
		return NnNetUtil.textReturn("OK");
	}
	
	/*
	@RequestMapping("flipr")
	public String flipr(HttpServletRequest request,) {		
		Locale locale = request.getLocale();
		if((locale.getCountry()=="TW")||(locale.getCountry()=="CN")) {
			return "redirect:flipr/tw/";
		} else {
			return "redirect:flipr/en/";
		}
	}
	*/
	
}
