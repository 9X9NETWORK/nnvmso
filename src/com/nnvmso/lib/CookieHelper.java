package com.nnvmso.lib;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieHelper {

	//add expiration
	static public void setCookie(HttpServletResponse resp, String cookieName, String cookieValue) {		
		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setPath("/");
		resp.addCookie(cookie);		
	}
	
	static public String getCookie(HttpServletRequest req, String cookieName) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals(cookieName)) {
					return cookies[i].getValue();
				}           
			}
		}
		return null;
	}
}
