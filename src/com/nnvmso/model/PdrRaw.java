package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

@PersistenceCapable(detachable="true")
public class PdrRaw implements Serializable {	
	private static final long serialVersionUID = 7348543186240756490L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long userId;

	@Persistent
	private long deviceId;
	
	@Persistent
	private String session;

	@Persistent
	private String userToken;

	@Persistent
	private String deviceToken;
	
	@Persistent
	private Text detail;
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
	
	public PdrRaw (NnUser user, NnDevice device, String session, Text detail) {
		this.session = session;
		if (user != null) {
			this.userId = user.getKey().getId();
			this.userToken = user.getToken();			
		}
		if (device != null) {
			this.deviceId = device.getKey().getId();
			this.deviceToken = device.getToken();
		}
		this.detail = detail;
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

	public Text getDetail() {
		return detail;
	}

	public void setDetail(Text detail) {
		this.detail = detail;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
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
