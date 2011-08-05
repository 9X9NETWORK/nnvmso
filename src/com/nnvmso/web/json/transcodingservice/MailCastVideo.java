package com.nnvmso.web.json.transcodingservice;

public class MailCastVideo {
	
	private long channelId;
	
	private String storageId;
	
	private String name;
	
	private String intro;
	
	private String fileUrl;
	
	private long programId;
	
	private int errorCode;
	
	private String errorReason;
	
	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}
	
	public long getChannelId() {
		return channelId;
	}
	
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}
	
	public String getStorageId() {
		return storageId;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setIntro(String intro) {
		this.intro = intro;
	}
	
	public String getIntro() {
		return intro;
	}
	
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	
	public String getFileUrl() {
		return fileUrl;
	}
	
	public void setProgramId(long programId) {
		this.programId = programId;
	}

	public long getProgramId() {
		return programId;
	}
	
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
	}
	
	public String getErrorReason() {
		return errorReason;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("channelId: " + channelId + ";");
		buffer.append("storageId: " + storageId + ";");
		buffer.append("name: " + name + ";");
		buffer.append("intro: " + intro + ";");
		buffer.append("fileUrl:" + fileUrl + ";");
		return buffer.toString();		
	}
}
