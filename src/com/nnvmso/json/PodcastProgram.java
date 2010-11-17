package com.nnvmso.json;

/**
 * Receive Podcast Program information from Transcoding Service 
 */
public class PodcastProgram {
	private String action;
	private String key; //channel key
	private String itemKey; //show key
	private String errorCode;
	private String errorReason;
	private PodcastItem item;
	
	public static String ACTION_UPDATE_ITEM = "updateItem";
	public static String ACTION_UPDATE_ENCLOSURE = "updateEnclosure";	
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getItemKey() {
		return itemKey;
	}
	public void setItemKey(String itemKey) {
		this.itemKey = itemKey;
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
	public PodcastItem getItem() {
		return item;
	}
	public void setItem(PodcastItem item) {
		this.item = item;
	}
}
