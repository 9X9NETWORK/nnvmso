package com.nnvmso.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.ChannelSetManager;
import com.nnvmso.service.MsoChannelManager;

public class PiwikLib {

	protected static final Logger log = Logger.getLogger(PiwikLib.class.getName());

	public static String createPiwikSite(long setId, long channelId, HttpServletRequest req) {
		if (setId == 0 && channelId == 0)
			return null;
		ChannelSetManager setMngr = new ChannelSetManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		ChannelSet cs = null;
		MsoChannel c = null;
		if (setId != 0) {
			cs = setMngr.findById(setId);
			if (cs == null) {
				log.info("querying for empty channel set");
				return null;
			}
			if (cs.getPiwik() != null && cs.getPiwik().length() > 0) {
				log.info("not creating anything new:" + cs.getKey().getId());
				return cs.getPiwik();
			}
		}
		if (channelId != 0) {
			c = channelMngr.findById(channelId);
			if (c == null) {
				log.info("querying for empty channel");
				return null;
			}
			if (c.getPiwik() != null && c.getPiwik().length() > 0) {
				log.info("not creating anything new:" + c.getKey().getId());
				return c.getPiwik();
			}
		}
			
		String urlRoot = NnNetUtil.getUrlRoot(req);
		String site = "";
		if (urlRoot.contains("demo")) {
			site = "demo.";
		} else if (urlRoot.contains("localhost") ||
				   urlRoot.contains("office") ||
				   urlRoot.contains("beta")){
			return null;
		} else if (urlRoot.contains("alpha")) {
			site = "alpha.";
		} else if (urlRoot.contains("puppy")) {
			site = "dev.";
		} else if (urlRoot.contains("qa")) {
			site = "qa.";
		}		
		String postHost = "http://" + site + "piwik.9x9.tv";
		if (urlRoot.contains("cms")) {
			postHost = "http://piwik.teltel.com";
		}
		String contentUrl = urlRoot + "?";
		String siteName = "";
		if (channelId != 0) {
			contentUrl += "ch=" + channelId;
			siteName = String.valueOf(channelId);
		} else if (setId != 0) {
			contentUrl += "set=" + setId;
			siteName = String.valueOf(setId);
		}
				
		String urlStr = postHost + "/index.php?";
		urlStr += "jsoncallback=jsonp1316430986921";
		urlStr += "&method=SitesManager.addSite";
		urlStr += "&module=API";
		urlStr += "&format=JSON";
		urlStr += "&token_auth=23ed70e585b18033d7150f917232d1f4"; 
		urlStr += "&urls=" + contentUrl;
		urlStr += "&siteName=" + siteName;
		
		//urlStr = "http://piwik.teltel.com/index.php?jsoncallback=jsonp1316424512664&method=SitesManager.getSitesIdFromSiteUrl&url=http%3A%2F%2Fcms.9x9.tv%2F9x9&module=API&format=JSON&token_auth=23ed70e585b18033d7150f917232d1f4";
		System.out.println("url:" + urlStr);			
		//HTTP GET
		URL url;
        String idsite = "";
		try {
			url = new URL(urlStr);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);	      
	        BufferedReader rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));;
	        String line = null;
	        //line = "jsonp1316430986921({\"value\":18})";
	        while ((line = rd.readLine()) != null) {
	        	if (line.contains("value")) {
	        		log.info("line:" + line);	       
	        		idsite = line.substring(line.indexOf("value\":")+7, line.indexOf("})"));
	        	}
	        }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String id = idsite;
		if (c != null) {
			c.setPiwik(id);
			channelMngr.save(c);
		} else if (cs != null) {		
			cs.setPiwik(id);
			setMngr.save(cs);
		}
		
		return idsite;
		////http://piwik.dev.9x9.tv/index.php?jsoncallback=jsonp1316424512664&method=SitesManager.getSitesIdFromSiteUrl&url=http%3A%2F%2Fcms.9x9.tv%2F9x9&module=API&format=JSON&token_auth=23ed70e585b18033d7150f917232d1f4		
	}
	
	//urlRoot example, "http://qa.9x9.tv"
	public static String createPiwikSite(long setId, long channelId, String urlRoot) {
		log.info("setID:" + setId + ";urlRoot:" + urlRoot);
		if (setId == 0 && channelId == 0)
			return null;
		ChannelSetManager setMngr = new ChannelSetManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		ChannelSet cs = null;
		MsoChannel c = null;
		if (setId != 0) {
			cs = setMngr.findById(setId);
			if (cs == null) {
				log.info("querying for empty channel set");
				return null;
			}
			if (cs.getPiwik() != null && cs.getPiwik().length() > 0) {
				log.info("not creating anything new:" + cs.getKey().getId());
				return cs.getPiwik();
			}
		}
		if (channelId != 0) {
			c = channelMngr.findById(channelId);
			if (c == null) {
				log.info("querying for empty channel");
				return null;
			}
			if (c.getPiwik() != null && c.getPiwik().length() > 0) {
				log.info("not creating anything new:" + c.getKey().getId());
				return c.getPiwik();
			}
		}
			
		String site = "";
		if (urlRoot.contains("demo")) {
			site = ".demo";
		} else if (urlRoot.contains("alpha") ||  
				   urlRoot.contains("localhost") || 
				   urlRoot.contains("puppy") || 
				   urlRoot.contains("office") ||
				   urlRoot.contains("beta")){
			site = ".dev";
		}
		String postHost = "http://piwik" + site + ".9x9.tv";
		if (urlRoot.contains("cms")) {
			postHost = "http://piwik.teltel.com";
		}
		String contentUrl = urlRoot + "?";
		String siteName = "";
		if (channelId != 0) {
			contentUrl += "ch=" + channelId;
			siteName = String.valueOf(channelId);
		} else if (setId != 0) {
			contentUrl += "set=" + setId;
			siteName = String.valueOf(setId);
		}
				
		String urlStr = postHost + "/index.php?";
		urlStr += "jsoncallback=jsonp1316430986921";
		urlStr += "&method=SitesManager.addSite";
		urlStr += "&module=API";
		urlStr += "&format=JSON";
		urlStr += "&token_auth=23ed70e585b18033d7150f917232d1f4"; 
		urlStr += "&urls=" + contentUrl;
		urlStr += "&siteName=" + siteName;
		
		//urlStr = "http://piwik.teltel.com/index.php?jsoncallback=jsonp1316424512664&method=SitesManager.getSitesIdFromSiteUrl&url=http%3A%2F%2Fcms.9x9.tv%2F9x9&module=API&format=JSON&token_auth=23ed70e585b18033d7150f917232d1f4";
		log.info("url:" + urlStr);			
		//HTTP GET
		URL url;
        String idsite = "";
		try {
			url = new URL(urlStr);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);	      
	        BufferedReader rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));;
	        String line = null;
	        //line = "jsonp1316430986921({\"value\":18})";
	        while ((line = rd.readLine()) != null) {
	        	if (line.contains("value")) {
	        		log.info("line:" + line);	       
	        		idsite = line.substring(line.indexOf("value\":")+7, line.indexOf("})"));
	        	}
	        }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String id = idsite;
		if (c != null) {
			c.setPiwik(id);
			channelMngr.save(c);
		} else if (cs != null) {		
			cs.setPiwik(id);
			setMngr.save(cs);
		}
		
		return idsite;
		////http://piwik.dev.9x9.tv/index.php?jsoncallback=jsonp1316424512664&method=SitesManager.getSitesIdFromSiteUrl&url=http%3A%2F%2Fcms.9x9.tv%2F9x9&module=API&format=JSON&token_auth=23ed70e585b18033d7150f917232d1f4		
	}	
	
}
