package com.nnvmso.lib;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.nnvmso.web.TestController;

public class NnLib {
	
	protected final static Logger logger = Logger.getLogger(TestController.class.getName());
	
	public static String getKeyStr(Key key) {
		return KeyFactory.keyToString(key);
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
	
	public static void logException(Exception e) {
		String detail = "";
		StackTraceElement[] elements = e.getStackTrace();
		for (StackTraceElement elm:elements ) {
			detail = detail + elm.toString() + "\n";			
		}
		logger.severe("exception:" + e.toString());
		logger.severe("exception stacktrace:\n" + detail);
		String now = (new Date()).toString();		
	}
	
	public static void urlPostWithJson(String urlStr, Object obj) {
        URL url;
		try {
			url = new URL(urlStr);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.writeValue(writer, obj);
	        System.out.println(DebugLib.OUT + "url fetch-json:" + mapper.writeValueAsString(obj));	        
	        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {	        	
	        	System.out.println("response not ok!" + connection.getResponseCode());
	        }
	        writer.close();	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
