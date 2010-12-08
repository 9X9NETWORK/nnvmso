package com.nnvmso.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Part of PodcastProgram.
 * Receive Podcast Program information from Transcoding Service 
 */
public class PodcastItem {
	private String title;
	private String description;	
	private String pubDate;
	private String thumbnail;
	private String enclosure;
	private String type;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPubDateString() {
		return pubDate;
	}
	public Date getPubDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm a");
		Date theDate = new Date();
        try {
        	theDate = dateFormat.parse(pubDate);
        	System.out.println("PubDate parsed = " + dateFormat.format(theDate));
        } catch (ParseException e) {
        	System.out.println("ERROR: exception while parse PubDate = " + dateFormat.format(theDate));
        }
		return theDate;
	}
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}
	public String getEnclosure() {
		return enclosure;
	}
	public void setEnclosure(String enclosure) {
		this.enclosure = enclosure;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}				
}
