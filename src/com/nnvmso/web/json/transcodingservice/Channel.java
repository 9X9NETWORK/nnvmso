package com.nnvmso.web.json.transcodingservice;

public class Channel {
	private String channelId;
	private String url;
	private String forceEncoding;
	private String lastUpdateTime;

	public Channel(String channelId, String url, String lastUpdateTime, String forceEncoding) {
		this.channelId = channelId;
		this.url = url;
		this.lastUpdateTime = lastUpdateTime;
		this.forceEncoding = forceEncoding;
	}
	
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getLastUpdateTime() {
		return lastUpdateTime;
	}
	public void setLastUpdateTime(String lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public String getForceEncoding() {
		return forceEncoding;
	}

	public void setForceEncoding(String forceEncoding) {
		this.forceEncoding = forceEncoding;
	}
	
}
