package com.nnvmso.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.commons.lang.RandomStringUtils;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable="true")
public class Captcha {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private long batch;
	
	@Persistent
	private String name;
	
	@Persistent
	private String fileName;
	
	@Persistent
	private String random;
	
	@Persistent
	private Date createDate;
	
	public static short ACTION_SIGNUP = 1;
	public static short ACTION_EMAIL = 2;
	
	public Captcha(long batch, String name, String fileName) {
		this.batch = batch;
		this.name = name;
		this.fileName = fileName;
		String random = RandomStringUtils.randomAlphabetic(10);
		this.random = random;
		Date now = new Date();
		this.createDate = now;
	}
	
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public long getBatch() {
		return batch;
	}

	public void setBatch(long batch) {
		this.batch = batch;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getRandom() {
		return random;
	}

	public void setRandom(String random) {
		this.random = random;
	}
		
}
