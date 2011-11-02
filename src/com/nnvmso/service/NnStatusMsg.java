package com.nnvmso.service;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
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

	public static String getMsg(int status, Locale locale) {
		try {
			switch (status) {
			  case NnStatusCode.SUCCESS: return messageSource.getMessage("nnstatus.success", new Object[] {NnStatusCode.SUCCESS} , locale); 
			  case NnStatusCode.INFO: return messageSource.getMessage("nnstatus.info", new Object[] {NnStatusCode.INFO} , locale);
			  case NnStatusCode.WARNING: return messageSource.getMessage("nnstatus.warning", new Object[] {NnStatusCode.WARNING} , locale);
			  case NnStatusCode.FATAL: return messageSource.getMessage("nnstatus.fatal", new Object[] {NnStatusCode.FATAL} , locale);
			  case NnStatusCode.ERROR: return messageSource.getMessage("nnstatus.error", new Object[] {NnStatusCode.ERROR} , locale);
			  case NnStatusCode.INPUT_ERROR: return messageSource.getMessage("nnstatus.input_error", new Object[] {NnStatusCode.INPUT_ERROR} , locale);
			  case NnStatusCode.INPUT_MISSING: return messageSource.getMessage("nnstatus.input_missing", new Object[] {NnStatusCode.INPUT_MISSING} , locale);
			  case NnStatusCode.INPUT_BAD: return messageSource.getMessage("nnstatus.input_bad", new Object[] {NnStatusCode.INPUT_BAD} , locale);
			  case NnStatusCode.CAPTCHA_FAILED: return NnStatusCode.CAPTCHA_FAILED + "\t" + "CAPTCHA_FAILED" + "\n";	
			  case NnStatusCode.CAPTCHA_TOOMANY_TRIES: return NnStatusCode.CAPTCHA_TOOMANY_TRIES + "\t" + "CAPTCHA_TOOMANY_TRIES" + "\n";
			  case NnStatusCode.CAPTCHA_EXPIRED: return NnStatusCode.CAPTCHA_EXPIRED + "\t" + "CAPTCHA_EXPIRED" + "\n";
			  case NnStatusCode.CAPTCHA_INVALID: return NnStatusCode.CAPTCHA_ERROR + "\t" + "CAPTCHA_INVALID" + "\n";
			  case NnStatusCode.CAPTCHA_ERROR: return NnStatusCode.CAPTCHA_ERROR + "\t" + "CAPTCHA_ERROR" + "\n";
			  case NnStatusCode.PIWIK_INVALID: return NnStatusCode.PIWIK_INVALID + "\t" + "PIWIK_INVALID" + "\n";
			  case NnStatusCode.PIWIK_ERROR: return NnStatusCode.PIWIK_ERROR + "\t" + "PIWIK_ERROR" + "\n";
			  case NnStatusCode.DEVICE_INVALID: return NnStatusCode.DEVICE_INVALID + "\t" + "DEVICE_INVALID" + "\n";
			  case NnStatusCode.OUTPUT_NO_MSG_DEFINED: return messageSource.getMessage("nnstatus.output_no_msg_defined", new Object[] {NnStatusCode.OUTPUT_NO_MSG_DEFINED} , locale);
			  case NnStatusCode.USER_ERROR: return NnStatusCode.USER_ERROR + "\t" + "user error" + "\n";
			  case NnStatusCode.USER_LOGIN_FAILED: return messageSource.getMessage("nnstatus.user_login_failed", new Object[] {NnStatusCode.USER_LOGIN_FAILED} , locale);
			  case NnStatusCode.USER_EMAIL_TAKEN: return messageSource.getMessage("nnstatus.user_email_taken", new Object[] {NnStatusCode.USER_EMAIL_TAKEN} , locale);
			  case NnStatusCode.USER_INVALID: return messageSource.getMessage("nnstatus.user_invalid", new Object[] {NnStatusCode.USER_INVALID} , locale);
			  case NnStatusCode.USER_TOKEN_TAKEN: return messageSource.getMessage("nnstatus.user_token_taken", new Object[] {NnStatusCode.USER_TOKEN_TAKEN} , locale);
			  case NnStatusCode.USER_PERMISSION_ERROR: return messageSource.getMessage("nnstatus.user_permission_error", new Object[] {NnStatusCode.USER_PERMISSION_ERROR} , locale);
			  case NnStatusCode.ACCOUNT_INVALID: return NnStatusCode.ACCOUNT_INVALID + "\t" + "ACCOUNT_INVALID" + "\n";
			  case NnStatusCode.MSO_ERROR: return messageSource.getMessage("nnstatus.mso_error", new Object[] {NnStatusCode.MSO_ERROR} , locale);
			  case NnStatusCode.MSO_INVALID: return messageSource.getMessage("nnstatus.mso_invalid", new Object[] {NnStatusCode.MSO_INVALID} , locale);
			  case NnStatusCode.CHANNEL_ERROR: return messageSource.getMessage("nnstatus.channel_error", new Object[] {NnStatusCode.CHANNEL_ERROR} , locale);
			  case NnStatusCode.CHANNEL_URL_INVALID: return messageSource.getMessage("nnstatus.channel_url_invalid", new Object[] {NnStatusCode.CHANNEL_URL_INVALID} , locale);
			  case NnStatusCode.CHANNEL_INVALID: return messageSource.getMessage("nnstatus.channel_invalid", new Object[] {NnStatusCode.CHANNEL_INVALID} , locale);
			  case NnStatusCode.CHANNEL_OR_USER_INVALID: return messageSource.getMessage("nnstatus.channel_or_user_invalid", new Object[] {NnStatusCode.CHANNEL_OR_USER_INVALID} , locale);
			  case NnStatusCode.CHANNEL_STATUS_ERROR: return messageSource.getMessage("nnstatus.channel_status_error", new Object[] {NnStatusCode.CHANNEL_STATUS_ERROR} , locale);
			  case NnStatusCode.CHANNEL_MAXSIZE_EXCEEDED: return messageSource.getMessage("nnstatus.channel_maxsize_exceeded", new Object[] {NnStatusCode.CHANNEL_MAXSIZE_EXCEEDED} , locale);
			  case NnStatusCode.SUBSCRIPTION_ERROR: return messageSource.getMessage("nnstatus.subscription_error", new Object[] {NnStatusCode.SUBSCRIPTION_ERROR} , locale);
			  case NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL: return messageSource.getMessage("nnstatus.subscription_duplicate_channel", new Object[] {NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL} , locale);
			  case NnStatusCode.SUBSCRIPTION_SET_OCCUPIED: return messageSource.getMessage("nnstatus.subscription_set_occupied", new Object[] {NnStatusCode.SUBSCRIPTION_SET_OCCUPIED} , locale);
			  case NnStatusCode.SUBSCRIPTION_POS_OCCUPIED: return NnStatusCode.SUBSCRIPTION_POS_OCCUPIED + "\t" + "SUBSCRIPTION_POS_OCCUPIED" + "\n";
			  case NnStatusCode.CATEGORY_ERROR: return messageSource.getMessage("nnstatus.category_error", new Object[] {NnStatusCode.CATEGORY_ERROR} , locale);
			  case NnStatusCode.CATEGORY_INVALID: return messageSource.getMessage("nnstatus.category_invalid", new Object[] {NnStatusCode.CATEGORY_INVALID} , locale);
			  case NnStatusCode.IPG_ERROR: return messageSource.getMessage("nnstatus.ipg_error", new Object[] {NnStatusCode.IPG_ERROR} , locale);
			  case NnStatusCode.IPG_INVALID: return messageSource.getMessage("nnstatus.ipg_invalid", new Object[] {NnStatusCode.IPG_INVALID} , locale);
			  case NnStatusCode.SET_ERROR: return messageSource.getMessage("nnstatus.set_error", new Object[] {NnStatusCode.SET_ERROR} , locale);
			  case NnStatusCode.SET_INVALID: return messageSource.getMessage("nnstatus.set_invalid", new Object[] {NnStatusCode.SET_INVALID} , locale);
			  case NnStatusCode.GAE_ERROR: return messageSource.getMessage("nnstatus.gae_error", new Object[] {NnStatusCode.GAE_ERROR} , locale);
			  case NnStatusCode.GAE_TIMEOUT: return messageSource.getMessage("nnstatus.gae_timeout", new Object[] {NnStatusCode.GAE_TIMEOUT} , locale);
			  case NnStatusCode.DATABASE_ERROR: return messageSource.getMessage("nnstatus.database_error", new Object[] {NnStatusCode.DATABASE_ERROR} , locale);
			  case NnStatusCode.DATABASE_TIMEOUT: return messageSource.getMessage("nnstatus.database_timeout", new Object[] {NnStatusCode.DATABASE_TIMEOUT} , locale);
			  case NnStatusCode.DATABASE_NEED_INDEX: return messageSource.getMessage("nnstatus.database_need_index", new Object[] {NnStatusCode.DATABASE_NEED_INDEX} , locale);	
			  case NnStatusCode.DATABASE_READONLY: return NnStatusCode.DATABASE_READONLY + "\t" + "database_readonly" + "\n";
			  case NnStatusCode.API_DEPRECATED: return NnStatusCode.API_DEPRECATED + "\t" + "API_DEPRECATED" + "\n";	
			  default: return messageSource.getMessage("nnstatus.output_no_msg_defined", new Object[] {status} , locale); 
			}
		} catch (NoSuchMessageException e) {
			return status + "\t" + "no message text defined" + "\n";			
		}
	}
	
}
