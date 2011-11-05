package com.nnvmso.service;

import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.Model;

import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.lib.YouTubeLib;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;

public class PlayerService {
	
	protected static final Logger logger = Logger.getLogger(PlayerService.class.getName());
	
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
		
		ChannelSetManager setMngr = new ChannelSetManager();
		ChannelSet channelSet = setMngr.findByBeautifulUrl(name);
		if (channelSet != null) {
			logger.info("found set name = " + name);
			model.addAttribute("fbName", NnStringUtil.htmlSafeChars(channelSet.getName()));
			model.addAttribute("fbDescription", NnStringUtil.htmlSafeChars(channelSet.getIntro()));
			model.addAttribute("fbImg", NnStringUtil.htmlSafeChars(channelSet.getImageUrl()));
		}
		
		return model;
	}
	
	public Model prepareEpisode(Model model, String pid,
			HttpServletResponse resp) {
		
		if (pid.matches("[0-9]+")) {
			MsoProgramManager programMngr = new MsoProgramManager();
			MsoProgram program = programMngr.findById(Long.valueOf(pid));
			if (program != null) {
				if (program.getContentType() == MsoProgram.CONTENTTYPE_YOUTUBE) {
					String regex = "/watch\\?v=(\\w+)";
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(program.getOtherFileUrl());
					if (matcher.find()) {
						String videoId = matcher.group(1);
						logger.info("youtube found = " + videoId);
						Map<String, String> entry = YouTubeLib.getYouTubeVideoEntry(videoId);
						model.addAttribute("fbName", NnStringUtil.htmlSafeChars(entry.get("title")));
						model.addAttribute("fbDescription", NnStringUtil.htmlSafeChars(entry.get("description")));
						model.addAttribute("fbImg", NnStringUtil.htmlSafeChars(entry.get("thumbnail")));
					} else {
						model.addAttribute("fbName", NnStringUtil.htmlSafeChars(program.getName()));
						model.addAttribute("fbDescription", NnStringUtil.htmlSafeChars(program.getIntro()));
						if (program.getImageLargeUrl() != null)
							model.addAttribute("fbImg", NnStringUtil.htmlSafeChars(program.getImageLargeUrl()));
						else
							model.addAttribute("fbImg", NnStringUtil.htmlSafeChars(program.getImageUrl()));
					}
				} else {
					logger.info("episode found = " + pid);
					model.addAttribute("fbName", NnStringUtil.htmlSafeChars(program.getName()));
					model.addAttribute("fbDescription", NnStringUtil.htmlSafeChars(program.getIntro()));
					if (program.getImageLargeUrl() != null)
						model.addAttribute("fbImg", NnStringUtil.htmlSafeChars(program.getImageLargeUrl()));
					else
						model.addAttribute("fbImg", NnStringUtil.htmlSafeChars(program.getImageUrl()));
				}
			}
		} else {
			Map<String, String> entry = YouTubeLib.getYouTubeVideoEntry(pid);
			model.addAttribute("fbName", NnStringUtil.htmlSafeChars(entry.get("title")));
			model.addAttribute("fbDescription", NnStringUtil.htmlSafeChars(entry.get("description")));
			model.addAttribute("fbImg", NnStringUtil.htmlSafeChars(entry.get("thumbnail")));
		}
		return model;
	}

	public Model prepareChannel(Model model, String cid,
			HttpServletResponse resp) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findById(Long.valueOf(cid));
		if (channel != null) {
			logger.info("found channel = " + cid);
			model.addAttribute("fbName", NnStringUtil.htmlSafeChars(channel.getName()) + " | 9x9.tv");
			model.addAttribute("fbDescription", NnStringUtil.htmlSafeChars(channel.getIntro()));
			model.addAttribute("fbImg", NnStringUtil.htmlSafeChars(channel.getImageUrl()));
		}
		return model;
	}

}
