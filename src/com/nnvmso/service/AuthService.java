package com.nnvmso.service;

import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.model.Mso;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.MsoManager;

@Service
public class AuthService {
	
	protected static final Logger logger = Logger.getLogger(AuthService.class.getName());
	
	public Mso msoAuthenticate(String email, String password, long msoId) {
		
		NnUserManager userMngr = new NnUserManager();
		MsoManager    msoMngr  = new MsoManager();
		
		NnUser user = userMngr.findAuthenticatedMsoUser(email, password, msoId);
		if (user == null)
			return null;
		return msoMngr.findById(user.getMsoId());
	}
	
}
