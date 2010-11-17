package com.nnvmso.json;

/**
 * Pass RSS feed to Transcoding Service
 */
public class PodcastFeed {

	private String key; //channel key
	private String rss; //rss feed
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getRss() {
		return rss;
	}
	public void setRss(String rss) {
		this.rss = rss;
	}
	
}
