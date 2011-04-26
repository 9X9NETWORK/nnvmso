package com.nnvmso.web;

import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.YouTubeLib;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.FBService;
import com.nnvmso.service.IpgManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.MsoProgramManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.PlayerService;

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
		
		PlayerService playerService = new PlayerService();		
		String msoName = null;
		//find mso info of the user who shares the ipg
		if (ipg.getUserId() != 0) { //old data does not have userId
			NnUser user = new NnUserManager().findById(ipg.getUserId());		
			if (user != null) {
				log.info("This user," + user.getKey().getId() + ", shares ipg.");
				Mso mso = new MsoManager().findById(user.getMsoId());
				if (mso != null) msoName = mso.getNameSearch();
			}
		}
		model = playerService.prepareBrand(model, msoName, resp);

		//fb: change to episode mode
		FBService fbService = new FBService();
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoProgram p = programMngr.findById(ipg.getProgramId()); 		
		if (p != null) {
			model = fbService.setEpisodeMetadata(model, p.getName(), p.getIntro(), p.getImageUrl());
		} else {
			// client approach without programId but programIdStr instead
			String programIdStr = ipg.getProgramIdStr();
			if (programIdStr != null && programIdStr.length() > 0) {
				Map<String, String> videoEntry = YouTubeLib.getYouTubeVideoEntry(programIdStr);
				model = fbService.setEpisodeMetadata(model, videoEntry.get("title"), videoEntry.get("description"), videoEntry.get("thumbnail"));
			}
		}
		return "player/zooatomics";
	}
}
