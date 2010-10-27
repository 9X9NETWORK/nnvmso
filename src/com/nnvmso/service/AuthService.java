package com.nnvmso.service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.Mso;

@Service
public class AuthService {
	private final static int ITERATION_NUMBER = 10;	

	public Object getAuthSession(HttpSession session, String sessionName) {
		Mso mso = (Mso)session.getAttribute("mso");
		//!!! for devel
		if (mso == null) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Query q = pm.newQuery(Mso.class);
			q.setFilter("email == emailParam");
			q.declareParameters("String emailParam");
			List<Mso> results = (List<Mso>)q.execute("a@a.com");
			session.setAttribute(sessionName, results.get(0));
		}
		return session.getAttribute(sessionName);
	}
	
	public void setAuthSession(HttpSession session, String sessionName, Object obj) {
		session.setAttribute(sessionName, obj); 
	}	
	
	 

}
