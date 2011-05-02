package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable = "true")
public class SnsAuth implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long msoId;
	
	@Persistent
	private short type;
	public static final short TYPE_FACEBOOK = 1;
	public static final short TYPE_TWITTER = 2;
	public static final short TYPE_PLURK = 3;
	public static final short TYPE_SINA = 4;
	
	@Persistent
	private String token;
	
	@Persistent
	private String secrete;
	
	@Persistent
	private Date createDate;

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}

	public void setMsoId(long msoId) {
		this.msoId = msoId;
	}

	public long getMsoId() {
		return msoId;
	}

	public void setType(short type) {
		this.type = type;
	}

	public short getType() {
		return type;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setSecrete(String secrete) {
		this.secrete = secrete;
	}

	public String getSecrete() {
		return secrete;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getCreateDate() {
		return createDate;
	}
}
