package com.nnvmso.lib;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nnvmso.service.PlayerApiService;

public class YouTubeLib {
	
	protected static final Logger log = Logger.getLogger(PlayerApiService.class.getName());
	
	/** 
	 * 1. remove those invalid keywords we already know.
	 * 2. merge the following formats to one, http://www.youtube.com/user/<userid>
	 *    http://www.youtube.com/<usrid>
	 *    http://www.youtube.com/user/<usrid>
	 *    http://www.youtube.com/profile?user=<usrid>
	 * 3. youtube api call 
	 * Examples: they should all become http://www.youtube.com/user/davidbrucehughes    
	 *    http://www.youtube.com/profile?user=davidbrucehughes#g/u
	 *    http://www.youtube.com/davidbrucehughes#g/a
	 *    http://www.youtube.com/user/davidbrucehughes#g/p
	 */		
	public static String formatCheck(String url) {
		if (url == null) {return null;}
		String[] invalid = {"index", "videos", "watch",
		                    "entertainment", "music", "news", "movies",
		                    "comedy", "gaming", "sports", "education",
		                    "shows",  "trailers",   
		                    "store", "channels", "contests_main"};		
		HashSet<String> dic = new HashSet<String>();
		for (int i=0; i<invalid.length; i++) {
			dic.add(invalid[i]);
		}
		String result = null;
		String reg = "^(http|https)://?(\\w+\\.)?youtube.com/(user/|profile\\?user=)?([A-Za-z0-9]+)";
		Pattern pattern = Pattern.compile(reg);
		Matcher m = pattern.matcher(url);
		while (m.find()) {
			if (dic.contains(m.group(4))) {return null;}
			result = "http://www.youtube.com/user/" + m.group(4);
			result = result.toLowerCase();
			log.info("original url:" + url + ";result=" + result);			
		}
		//if (!youTubeCheck(result)) {return null;} //till the function is fixed
		return result;
	}
	
	/**
	 * YouTube API request format, http://gdata.youtube.com/feeds/api/users/androidcentral
	 * This function currently checks only if the query status is not 200.
	 * 
	 * @@@ IMPORTANT: This code will be blocked by YouTube, need to add user's IP, indicating you are on behalf of the user.
	 * 
	 * @param urlStr support only format of http://www.youtube.com/user/android  
	 */
	public static boolean youTubeCheck(String urlStr) {		
		String[] splits = urlStr.split("/");
		String apiReq = "http://gdata.youtube.com/feeds/api/users/" + splits[splits.length-1];

		URL url;
		try {
			//HTTP GET
			url = new URL(apiReq);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
	        int statusCode = connection.getResponseCode();
	        if (statusCode != HttpURLConnection.HTTP_OK) {
	        	log.info("yutube GET response not ok with url:" + urlStr + "; status code = " + connection.getResponseCode());
	        	return false;
	        }
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
}
