package com.nnvmso.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.service.NnUserManager;

@PersistenceCapable(detachable = "true")
public class NnGuest {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private String token;

	@Persistent
	private Date expiredAt;
	
	public static short GUESS_MAXTIMES = 5;
	@Persistent
	private int guessTimes;
	
	@Persistent
	private long captchaId;	
	
	@Persistent
	private short type; //user use NnGuest for captcha use
	public static short TYPE_GUEST = 1;
	public static short TYPE_USER = 2;
	
	@Persistent
	private Date createDate;
		
	public NnGuest(short type) {
		if (type == TYPE_GUEST) {
			String random = NnUserManager.generateToken();
			this.token = random;		
		}
		this.type = type;
	}
	
	public Key getKey() {
		return key;
	}
	public void setKey(Key key) {
		this.key = key;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public long getCaptchaId() {
		return captchaId;
	}
	public void setCaptchaId(long captchaId) {
		this.captchaId = captchaId;
	}
	public Date getExpiredAt() {
		return expiredAt;
	}
	public void setExpiredAt(Date expiredAt) {
		this.expiredAt = expiredAt;
	}
	public int getGuessTimes() {
		return guessTimes;
	}
	public void setGuessTimes(int guessTimes) {
		this.guessTimes = guessTimes;
	}
	public short getType() {
		return type;
	}
	public void setType(short type) {
		this.type = type;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}	
}
