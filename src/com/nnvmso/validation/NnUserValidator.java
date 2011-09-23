package com.nnvmso.validation;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.nnvmso.model.LangTable;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.NnStatusCode;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.validation.BasicValidator;
import com.nnvmso.validation.NnUserValidator;

public class NnUserValidator {
	protected static final Logger log = Logger.getLogger(NnUserValidator.class.getName());	
	
	public static int validate(String email, String password, String name, HttpServletRequest req) {
		if (!BasicValidator.validateRequired(new String[] {email, password, name} ))
			return NnStatusCode.INPUT_MISSING;		
		if (!BasicValidator.validateEmail(email))
			return NnStatusCode.INPUT_BAD;
		int status = NnUserValidator.validatePassword(password);
		if (status != NnStatusCode.SUCCESS)
			return status;
		//verify user
		NnUser user = new NnUserManager().findByEmail(email);
		if (user != null) {
			log.info("user email taken:" + user.getEmail() + ";user token=" + user.getToken());
			return NnStatusCode.USER_EMAIL_TAKEN;
		}		
		return NnStatusCode.SUCCESS;
	}
	
	public static int validateProfile(NnUser user) {
		System.out.println("gender:" + user.getGender());
		if (user.getGender() > 2 || user.getGender() < 0) {
			log.info("gender error:" + user.getGender() + ";" + user.getEmail());
			return NnStatusCode.INPUT_BAD;
		}
		String dob = user.getDob();
		if (dob != null) {
			if (!Pattern.matches("^\\d*$", dob)) {
				log.info("dob error:" + dob + ";" + user.getEmail());
				return NnStatusCode.INPUT_BAD;
			}
		}
		String lang = user.getLang();
		if (lang != null && lang.length() > 0) {
			if (lang.equals(LangTable.LANG_EN) && lang.equals(LangTable.LANG_ZH)) {
				log.info("lang error:" + lang + ";" + user.getEmail());
				return NnStatusCode.INPUT_BAD;
			}
		}
		log.info("profile success");
		return NnStatusCode.SUCCESS;
	}
	
	public static int validatePassword(String password) {
		if (!BasicValidator.validateRequired(new String[] {password} ))
			return NnStatusCode.INPUT_MISSING;
		int limit = 6;
		if (password.length() < limit) {
			return NnStatusCode.INPUT_MISSING;
		}
		return NnStatusCode.SUCCESS;
	}

}
