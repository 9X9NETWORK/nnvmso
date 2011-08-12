package com.nnvmso.web.json.transcodingservice;

public class FBPost {
	private String message;
	private String picture;
	private String link;
	private String name;
	private String caption;
	private String description;
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public String getPicture() {
		return picture;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getLink() {
		return link;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getCaption() {
		return caption;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
}
