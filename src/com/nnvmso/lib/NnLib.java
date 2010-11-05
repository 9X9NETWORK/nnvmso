package com.nnvmso.lib;

import javax.servlet.http.HttpServletRequest;

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
	
}
