package com.nncloudtv.service;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.Model;

import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.NnSet;

public class PlayerService {
	
	protected static final Logger log = Logger.getLogger(PlayerService.class.getName());
	
	public Model prepareBrand(Model model, String msoName, HttpServletResponse resp) {
		if (msoName != null) {
			msoName = msoName.toLowerCase();
		} else {
			msoName = Mso.NAME_9X9;
		}
		
		FBService fbService = new FBService();
		model = fbService.setBrandMetadata(model, msoName);
		if (msoName.equals(Mso.NAME_5F)) {
			model.addAttribute("brandInfo", "5f");
			CookieHelper.setCookie(resp, CookieHelper.MSO, Mso.NAME_5F);
		} else {
			model.addAttribute("brandInfo", "9x9");
			CookieHelper.deleteCookie(resp, CookieHelper.MSO); //delete brand cookie
		}
		return model;		
	}

	public Model prepareSetInfo(Model model, String name,
			HttpServletResponse resp) {		
		NnSetManager setMngr = new NnSetManager();
		NnSet set = setMngr.findBybeautifulUrl(name);
		if (set != null) {
			log.info("found set name = " + name);
			model.addAttribute("fbName", NnStringUtil.htmlSafeChars(set.getName()));
			model.addAttribute("fbDescription", NnStringUtil.htmlSafeChars(set.getIntro()));
			model.addAttribute("fbImg", NnStringUtil.htmlSafeChars(set.getImageUrl()));
		}
		
		return model;
	}
	
	public Model prepareEpisode(Model model, String pid,
			HttpServletResponse resp) {
		
		if (pid.matches("[0-9]+")) {
			NnProgramManager programMngr = new NnProgramManager();
			NnProgram program = programMngr.findById(Long.valueOf(pid));
			if (program != null) {
				log.info("episode found = " + pid);
				model.addAttribute("fbName", NnStringUtil.htmlSafeChars(program.getName()));
				model.addAttribute("fbDescription", NnStringUtil.htmlSafeChars(program.getIntro()));
				model.addAttribute("fbImg", NnStringUtil.htmlSafeChars(program.getImageUrl()));
			}
		} else {
			/*
			Map<String, String> entry = YouTubeLib.getYouTubeVideoEntry(pid);
			model.addAttribute("fbName", NnStringUtil.htmlSafeChars(entry.get("title")));
			model.addAttribute("fbDescription", NnStringUtil.htmlSafeChars(entry.get("description")));
			model.addAttribute("fbImg", NnStringUtil.htmlSafeChars(entry.get("thumbnail")));
			*/
		}
		return model;
	}

	public Model prepareChannel(Model model, String cid,
			HttpServletResponse resp) {
		NnChannelManager channelMngr = new NnChannelManager();
		NnChannel channel = channelMngr.findById(Long.valueOf(cid));
		if (channel != null) {
			log.info("found channel = " + cid);
			model.addAttribute("fbName", NnStringUtil.htmlSafeChars(channel.getName()));
			model.addAttribute("fbDescription", NnStringUtil.htmlSafeChars(channel.getIntro()));
			model.addAttribute("fbImg", NnStringUtil.htmlSafeChars(channel.getImageUrl()));
		}
		return model;
	}

	public Model preparePlayer(Model model, String js, String jsp) {
		model.addAttribute("js", "");
		if (js != null && js.length() > 0) {
			model.addAttribute("js", js);
		}
		if (jsp != null && jsp.length() > 0) {
			log.info("alternate is enabled: " + jsp);
		}
		return model;
	}

	public String rewrite(String js, String jsp) {
		String url = "";
		if (jsp != null)
			url += "?jsp=" + jsp;
		if (js != null) {
			if (jsp != null)
				url += "&js=" + js;
			else
				url += "?js=" + js;
		}
		return url;
	}
	
	public Model prepareCrawled(Model model) {
		PlayerApiService service = new PlayerApiService();
		//listRecommended
		String listRecommended = service.listRecommended(LangTable.LANG_EN);		
		String[] sets = listRecommended.split("\n");
		String setId = "";
		for (int i=2; i<sets.length; i++) {
			String[] ele = sets[i].split("\t");
			if (i==2) {
				model.addAttribute("crawlSetTitle", ele[1]);
				setId = ele[0];
			}
			model.addAttribute("crawlRecommendTitle" + i, ele[1]);
			model.addAttribute("crawlRecommendDesc" + i, ele[2]);
			model.addAttribute("crawlRecommendThumb" + i, ele[3]);
			model.addAttribute("crawlRecommendCount" + i, ele[4]);
		}
		String setInfo = service.setInfo(setId, null);
		String[] setInfoSections = setInfo.split("--\n");
		String[] channels = setInfoSections[2].split("\n");
		log.info("<<<< set id >>>>" + setId);
		log.info("<<<< channels >>>>" + channels[0]);
		for (int i=0; i<1; i++) {
			String[] ele = channels[i].split("\t");
			model.addAttribute("crawlChannelTitle" + i, ele[2]);			
			model.addAttribute("crawlVideoThumb" + i, ele[4]);
		}
		return model;
	}
}
