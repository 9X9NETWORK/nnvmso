package com.nnvmso.service;

import java.util.logging.Logger;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.jsr107cache.Cache;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.nnvmso.lib.CacheFactory;

@Service
// for stateless, we save session in cache now, or other media later
public class SessionService {
	
	protected static final Logger logger = Logger.getLogger(SessionService.class.getName());
	
	private HttpSession session;
	
	public SessionService() {
		session = null;
	}
	
	public SessionService(HttpServletRequest request) {
		session = request.getSession();
		Cache cache = CacheFactory.get();
		if (cache != null) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) cache.get(this.session.getId());
			if (map != null) {
				Iterator<String> iterator = map.keySet().iterator();
				while (iterator.hasNext()) {
					String name = iterator.next();
					logger.info("cached session = " + name);
					session.setAttribute(name, map.get(name));
				}
			}
		}
	}
	
	public void saveSession(HttpSession session) {
		this.session = session;
		saveSession();
	}
	
	public void saveSession() {
		Cache cache = CacheFactory.get();
		if (cache != null) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			for (@SuppressWarnings("unchecked") Enumeration<String> enu = (Enumeration<String>)session.getAttributeNames(); enu.hasMoreElements();) {
				String name = (String)enu.nextElement();
				logger.info("put to cache = " + name);
				map.put(name, session.getAttribute(name));
			}
			cache.put(session.getId(), map);
		}
	}
	
	public void removeSession() {
		Cache cache = CacheFactory.get();
		if (cache != null) {
			cache.remove(session.getId());
		}
		session.invalidate();
	}
	
	public HttpSession getSession() {
		return session;
	}
}
