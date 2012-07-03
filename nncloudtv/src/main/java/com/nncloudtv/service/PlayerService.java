package com.nncloudtv.service;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.Model;

import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.YouTubeLib;
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

	//get all the query string(things after ?) from url except ch/channel, ep/episode
	public String rewrite(HttpServletRequest req) {
		String url = req.getRequestURL().toString();		
		String queryStr = req.getQueryString();		
		if (queryStr != null && !queryStr.equals("null"))
			queryStr = "?" + queryStr;
		else 
			queryStr = "";
		url = url + queryStr;
		Pattern pattern = Pattern.compile("(.*)\\?(.*)");
		Matcher m = pattern.matcher(url);
		if (m.find()) {
			String matched = m.group(2);
			matched = matched.replaceAll("ch=\\d*&?", "");
			log.info("matched 1:" + matched);
			matched = matched.replaceAll("ep=\\d*&?", "");
			log.info("matched 2:" + matched);
			matched = matched.replaceAll("channel=\\d*&?", "");
			log.info("matched 3:" + matched);
			matched = matched.replaceAll("episode=\\d*&?", "");
			log.info("matched 4:" + matched);
			if (matched.length() > 0)
				return "?" + matched;
		}
		return "";
	}
	
	/*
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
	*/
	
	public Model prepareCrawled(Model model, String escaped) {						
		PlayerApiService service = new PlayerApiService();
		log.info("escaped=" + escaped);		
		Pattern pattern = Pattern.compile("(ch=)(\\d*)");
		Matcher m = pattern.matcher(escaped);
		String ch=null, ep=null;
	    if (m.find()) {	    	
	    	ch = m.group(2);
	    }
	    pattern = Pattern.compile("(ep=)(\\d*)");
		m = pattern.matcher(escaped);
	    if (m.find()) {	    	
	    	ep = m.group(2);
	    }
	    String lang = LangTable.LANG_EN;
		if (ch != null) {
			NnChannelManager channelMngr = new NnChannelManager();		
			NnChannel c = channelMngr.findById(Long.parseLong(ch));
			if (c != null) {
				model.addAttribute("crawlChannelTitle", c.getName());
				lang = c.getLang();
				if (ep != null) {					
					NnProgramManager programMngr = new NnProgramManager();
					List<NnProgram> programs = programMngr.findPlayerProgramsByChannel(c.getId());
					if (programs.size() > 0) {
						int i=1;
						for (NnProgram p : programs) {
							if (i > 1 && i < 4) {
								model.addAttribute("crawlEpThumb" + i, p.getImageUrl());
								System.out.println("crawlEpThumb" + i + ":" + p.getImageUrl());
								i++;
							}
							if (p.getId() == Long.parseLong(ep)) {
								if (p.getImageLargeUrl() != null) {
									model.addAttribute("crawlVideoThumb", p.getImageLargeUrl());
								} else {
									model.addAttribute("crawlVideoThumb", p.getImageUrl());
								}
								model.addAttribute("crawlEpisodeTitle", p.getName());
								model.addAttribute("crawlEpThumb" + i, p.getImageUrl());
								i++;
							}
						}
					} else {
						if (c.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL || 
							c.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_PLAYLIST) {
							
						}
					}
				}
			}
		}
		//listRecommended
		String listRecommended = service.listRecommended(lang);		
		String[] sets = listRecommended.split("\n");
		//String setId = "";		
		for (int i=2; i<sets.length; i++) {
			String[] ele = sets[i].split("\t");
			if (i==2) {
				model.addAttribute("crawlSetTitle", ele[1]);
			}
			int seq = i -1;
			model.addAttribute("crawlRecommendTitle" + seq, ele[1]);
			model.addAttribute("crawlRecommendDesc" + seq, ele[2]);
			model.addAttribute("crawlRecommendThumb" + seq, ele[3]);
			model.addAttribute("crawlRecommendCount" + seq, ele[4]);
		}
		return model;
	}
}
