package com.nnvmso.service;

public class PlayerAPI {
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_INFO = 1;
	public static final int CODE_WARNING = 2;
	public static final int CODE_FATAL = 3;
	public static final int CODE_ERROR = 4;	
	
	public static final int CODE_LOGIN_FAILED = 100;
	
	public static final int CODE_MISSING_PARAMS = 200;
	
	public static final String PLAYER_CODE_SUCCESS = "Success";
	public static final String PLAYER_CODE_LOGIN_FAILED = "Login Failed";
	public static final String PLAYER_CODE_FATAL = "Fatal Error";
	public static final String PLAYER_CODE_ERROR = "Error";
	public static final String PLAYER_CODE_MISSING_PARAMS = "Missing Params";
	
	public static final String PLAYER_CHANNEL_OR_USER_UNEXISTED = "Channel/User does not exist";
	public static final String PLAYER_RSS_NOT_VALID = "RSS feed is not valid";
	public static final String PLAYER_USER_TOKEN_INVALID = "Invalid user token";
	public static final String PLAYER_EMAIL_TAKEN = "This email is already registered";
	
}
