package com.nnvmso.lib;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class NnLib {
	public static String getKeyStr(Key key) {
		return KeyFactory.keyToString(key);
	}

	public static String getUrlRoot(HttpServletRequest req) {	
		String host = req.getLocalAddr();
		String port = Integer.toString(req.getLocalPort());
		if (port.equals("80")) {
			port = "";
		} else {
			port = ":" + port;
		}
		return "http://" + host + port; 		
	}

	public static void urlFetch(String urlStr, Object obj) {
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
