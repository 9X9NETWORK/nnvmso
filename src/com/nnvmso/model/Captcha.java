package com.nnvmso.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

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
	private double random;
	
	@Persistent
	private Date createDate;
	
	public static short ACTION_SIGNUP = 1;
	public static short ACTION_EMAIL = 2;
		
	@Persistent
	private boolean toBeExpired;
	
	@Persistent
	private Date lockedDate; //even it's to be expired, but it's still used by someone
	
	public Captcha(long batch, String name, String fileName) {
		this.batch = batch;
		this.name = name;
		this.fileName = fileName;
		this.random = Math.random();
		Date now = new Date();
		this.createDate = now;
		this.toBeExpired = false;
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

	public double getRandom() {
		return random;
	}

	public void setRandom(double random) {
		this.random = random;
	}

	public boolean isToBeExpired() {
		return toBeExpired;
	}

	public void setToBeExpired(boolean toBeExpired) {
		this.toBeExpired = toBeExpired;
	}

	public Date getLockedDate() {
		return lockedDate;
	}

	public void setLockedDate(Date lockedDate) {
		this.lockedDate = lockedDate;
	}
		
}
