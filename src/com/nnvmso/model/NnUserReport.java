package com.nnvmso.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable="true")
public class NnUserReport {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long userId;
	
	@Persistent
	private String userToken;

	@Persistent
	private long deviceId;
	
	@Persistent
	private String deviceToken;
	
	@Persistent
	private String comment;
	
	@Persistent
	private String session;
	
	@Persistent
	private Date createDate;

	public NnUserReport() {}
	public NnUserReport(NnUser user, NnDevice device, String session, String comment) {
		if (user != null) {
			this.userId = user.getKey().getId();
			this.userToken = user.getToken();
		}
		if (device != null) {
			this.deviceId = device.getKey().getId();
			this.deviceToken = device.getToken();
		}
		this.session = session;
		this.comment = comment;
	}
	
	public Key getKey() {
		return key;
	}
	public void setKey(Key key) {
		this.key = key;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getSession() {
		return session;
	}
	public void setSession(String session) {
		this.session = session;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getUserToken() {
		return userToken;
	}
	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}
	public long getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(long deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceToken() {
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}	
}
