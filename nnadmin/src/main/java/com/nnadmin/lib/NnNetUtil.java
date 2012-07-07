package com.nnadmin.lib;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class NnNetUtil {
	
	protected final static Logger log = Logger.getLogger(NnNetUtil.class.getName());

	public static void apiGet(String urlStr, HttpServletResponse resp) {
		URL url;
		try {
			url = new URL(urlStr);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    connection.setDoOutput(true);
			connection.setRequestMethod("GET");			
			IOUtils.copy(connection.getInputStream(), resp.getOutputStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}    					
	}

	public static void apiPost(String urlStr, HttpServletRequest req, HttpServletResponse resp) {
		URL url;
		try {
			url = new URL(urlStr);
			@SuppressWarnings("unchecked")
			Map<String, String> map = (Map<String, String>)req.getParameterMap();
			@SuppressWarnings("rawtypes")
			Iterator it = map.entrySet().iterator();
			String content = "";
		    while (it.hasNext()) {
		        @SuppressWarnings("rawtypes")
				Map.Entry pairs = (Map.Entry)it.next();
		        String[] value = (String[]) pairs.getValue();
		        content += pairs.getKey() + "=" + URLEncoder.encode(value[0], "utf8") + "&";
		    }
		    log.info("query:" + content);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    connection.setDoOutput(true);
			connection.setRequestMethod("POST");
	        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		    out.writeBytes(content);
			out.flush();
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = "";
			while((line=in.readLine())!=null) {
				System.out.println(line);
			}
			in.close();			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}    					
	}
	
	public static String getApiUrl(HttpServletRequest req) {
		String url = NnNetUtil.getUrl(req);
		String urlRoot = NnNetUtil.getUrlRoot(req);
		String apiServer = ResourceLib.getApiUrlRootPath();
		String apiUrl = url.replace(urlRoot, apiServer);
		log.info("api url:" + apiUrl);
		return apiUrl;			
	}
	
	public static String getUrl(HttpServletRequest req) {
		String url = req.getRequestURL().toString();		
		String queryStr = req.getQueryString();		
		if (queryStr != null && !queryStr.equals("null"))
			queryStr = "?" + queryStr;
		else 
			queryStr = "";
		url = url + queryStr;
		log.info(url);
		return url;
	}
	
	public static String getUrlRoot(HttpServletRequest req) {
		String url = req.getRequestURL().toString();
	    Pattern p = Pattern.compile("(^http://.*?)/(.*)");	    	    
	    Matcher m = p.matcher(url);
	    String host = "";
	    if (m.find()) {
	    	host = m.group(1);
	    }
		return host;
	}
	
	
}
