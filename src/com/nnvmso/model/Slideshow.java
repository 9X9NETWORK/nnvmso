package com.nnvmso.model;

import java.io.Serializable;

public class Slideshow implements Serializable {
	private String[] slides;
	private String[] audios;
	private String slideinfo;
	
	public Slideshow() {}

	public String[] getSlides() {
		return slides;
	}


	public void setSlides(String[] slides) {
		this.slides = slides;
	}


	public String[] getAudios() {
		return audios;
	}


	public void setAudios(String[] audios) {
		this.audios = audios;
	}


	public String getSlideinfo() {
		return slideinfo;
	}

	public void setSlideinfo(String slideinfo) {
		this.slideinfo = slideinfo;
	}
	
}
