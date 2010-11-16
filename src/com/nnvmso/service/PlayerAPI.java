package com.nnvmso.service;

public class PlayerAPI {
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_INFO = 1;
	public static final int CODE_WARNING = 2;
	public static final int CODE_FATAL = 3;
	
	public static final int CODE_LOGIN_FAILED = 100;
	public static final int CODE_ACCOUNT_EXISTED = 101;
	
	public static final int CODE_MISSING_PARAMS = 200;
	
	public static final String PLAYER_CODE_SUCCESS = "Success";
	public static final String PLAYER_CODE_LOGIN_FAILED = "Login Failed";
	public static final String PLAYER_CODE_ACCOUNT_EXISTED = "Account Existed";
	public static final String PLAYER_CODE_FATAL = "Fatal Error";
	public static final String PLAYER_CODE_MISSING_PARAMS = "Missing Params";
}
