package com.nnvmso.web.json.transcodingservice;

import java.util.List;

import com.nnvmso.service.NnStatusCode;

public class ProgramInfo {
	private String errorCode;
	private String errorReason;
	private String callback;
	private String channelId;
	private List<Program> programs; 

	public ProgramInfo(String channelId) {
		this.setErrorCode(String.valueOf(NnStatusCode.ERROR));
		this.setErrorReason("Error");
		this.channelId = channelId;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorReason() {
		return errorReason;
	}
	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
	}
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public List<Program> getPrograms() {
		return programs;
	}
	public void setPrograms(List<Program> programs) {
		this.programs = programs;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	  
}
