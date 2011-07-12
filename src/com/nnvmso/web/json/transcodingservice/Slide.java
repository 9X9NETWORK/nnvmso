package com.nnvmso.web.json.transcodingservice;

public class Slide {
	private long channelId;
	private String name;
	private String intro;
	private String imageUrl;
	private String imageLargeUrl;
	private String duration;
	private String fileUrl;
	private long programId;
	private int errorCode;
	private String errorReason;	
		
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIntro() {
		return intro;
	}
	public void setIntro(String intro) {
		this.intro = intro;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getImageLargeUrl() {
		return imageLargeUrl;
	}
	public void setImageLargeUrl(String imageLargeUrl) {
		this.imageLargeUrl = imageLargeUrl;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getFileUrl() {
		return fileUrl;
	}
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	public long getChannelId() {
		return channelId;
	}
	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}
	public long getProgramId() {
		return programId;
	}
	public void setProgramId(long programId) {
		this.programId = programId;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorReason() {
		return errorReason;
	}
	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
	}
	public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("channel id: " + channelId + ";");
        buffer.append("name: " + name + ";");
        buffer.append("intro: " + intro + ";");
        buffer.append("imageUrl: " + imageUrl + ";");
        buffer.append("imageLargeUrl: " + imageLargeUrl + ";");
        buffer.append("duration:" + duration + ";");
        buffer.append("fileUrl:" + fileUrl + ";");
        buffer.append("programId:" + programId + ";");
        return buffer.toString();		
	}	
}