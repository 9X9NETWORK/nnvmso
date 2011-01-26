package com.nnvmso.service;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Transition implementation before we can switch to real locale instead of mso.
 * 
 * Shortcut to retrieve messages that are used often.  
 * 
 */
@Service
public class NnStatusMsg {
	private static MessageSource messageSource = new ClassPathXmlApplicationContext("locale.xml");
		
	public static String inputError(Locale locale) {
		return messageSource.getMessage("nnstatus.input_error", new Object[] {NnStatusCode.INPUT_ERROR} , locale);
	}

	public static String userInvalid(Locale locale) {
		return messageSource.getMessage("nnstatus.user_invalid", new Object[] {NnStatusCode.USER_INVALID} , locale);
	}

	public static String msoInvalid(Locale locale) {
		return messageSource.getMessage("nnstatus.mso_invalid", new Object[] {NnStatusCode.MSO_INVALID} , locale);
	}
	
	public static String successStr(Locale locale) {
		return messageSource.getMessage("nnstatus.success", new Object[] {NnStatusCode.SUCCESS} , locale);
	}
	
	public static String inputMissing(Locale locale) {
		return messageSource.getMessage("nnstatus.input_missing", new Object[] {NnStatusCode.INPUT_MISSING}, locale);
	}
	
	public static String errorStr(Locale locale) {
		return messageSource.getMessage("nnstatus.error", new Object[] {NnStatusCode.ERROR} , locale);
	}

}
