package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import com.nnvmso.lib.APILib;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;

public class PlayerAPI {
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_INFO = 1;
	public static final int CODE_WARNING = 2;
	public static final int CODE_FATAL = 3;
	public static final int CODE_ERROR = 4;	
	
	public static final int CODE_LOGIN_FAILED = 100;
	
	public static final int CODE_MISSING_PARAMS = 200;
	
	public static final String PLAYER_CODE_SUCCESS = "Success";
	public static final String PLAYER_CODE_LOGIN_FAILED = "Login Failed";
	public static final String PLAYER_CODE_FATAL = "Fatal Error";
	public static final String PLAYER_CODE_ERROR = "Error";
	public static final String PLAYER_CODE_MISSING_PARAMS = "Missing Params";

	public static final String PLAYER_NOT_FOUND = "Not found";
	public static final String PLAYER_CHANNEL_OR_USER_UNEXISTED = "Channel/User does not exist";
	public static final String PLAYER_RSS_NOT_VALID = "RSS feed is not valid";
	public static final String PLAYER_USER_TOKEN_INVALID = "Invalid user token";
	public static final String PLAYER_EMAIL_TAKEN = "This email is already registered";

	public void addToProgramInfoCache(List<MsoProgram> programs) {
		Cache cache = null;		
	    try {
	        CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
	        cache = (Cache) cacheFactory.createCache(Collections.emptyMap());
	    } catch (CacheException e) {
	    }        

		List<MsoProgram> channelPrograms = new ArrayList<MsoProgram>();
		long tempId = 0;

		for (int i=0; i<programs.size(); i++) {
			MsoProgram p = programs.get(i);
			if ((tempId != 0 && p.getChannelKey().getId() != tempId) || (i == programs.size()-1)) {
				if (i == programs.size()-1) {
					channelPrograms.add(programs.get(i));
				}
				String info = this.composeProgramInfoStr(channelPrograms);	    		
				System.out.println("add to cache (" + channelPrograms.get(0).getChannelKey().getId() + ")\n" + info);
				cache.put(channelPrograms.get(0).getChannelKey().getId(), info);
				channelPrograms.clear();				
			}
			
			if (p.getKey().getId() != tempId) {
	    		tempId = programs.get(i).getChannelKey().getId();
	    		channelPrograms.add(programs.get(i));	    						
			}						
		}
	}	
	
	public String composeChannelLineupStr(MsoChannel c) {
		String intro = c.getIntro();
		if (intro != null) {
			int introLenth = (intro.length() > 256 ? 256 : intro.length()); 
			intro = intro.substring(0, introLenth);				
			intro.replaceAll("\\s", " ");
		} else {
			intro = "";
		}
		String type = "";
		if (c.getType() == MsoChannel.TYPE_SYSTEM) {
			type = "SYSTEM";
		} else {
			type = "PODCAST";
		}
		String[] ori = {Short.toString(c.getGrid()), 
					    String.valueOf(c.getKey().getId()),
					    c.getName(),
					    intro,
					    c.getImageUrl(),
					    String.valueOf(c.getProgramCount()),
					    type,
					    String.valueOf(c.getStatus())
					    };
		String output = APILib.getTabDelimitedStr(ori);
		return output;
		
	}
	
	public String composeProgramInfoStr(List<MsoProgram> programs) {
		String output = "";		
		for (MsoProgram p : programs) {
			String url1 = p.getMpeg4FileUrl();
			String url2 = p.getWebMFileUrl();
			String url3 = p.getOtherFileUrl();
			String url4 = p.getAudioFileUrl();
			if (p.getType().equals(MsoProgram.TYPE_SLIDESHOW)) {
				url1 = "/player/nnscript?program=" + p.getId();
			}			
			String intro = p.getIntro();			
			if (intro != null) {
				int introLenth = (intro.length() > 256 ? 256 : intro.length()); 
				intro = intro.substring(0, introLenth);
				intro = intro.replaceAll("\\s", " ");				
			} else {
				intro = "";
			}
			
			String[] ori = {String.valueOf(p.getChannelId()), 
					        String.valueOf(p.getKey().getId()), 
					        p.getName(), 
					        intro,
					        p.getType(), 
					        p.getDuration(),
					        p.getImageUrl(),
					        p.getImageLargeUrl(),
					        url1, 
					        url2, 
					        url3, 
					        url4, 
					        String.valueOf(p.getUpdateDate().getTime())};
			output = output + APILib.getTabDelimitedStr(ori);
			output = output.replaceAll("null", "");
			output = output + "\n";
		}
		return output;		
	}
	
}
