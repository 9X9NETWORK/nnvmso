package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable="true")
public class PdrRaw implements Serializable {	
	private static final long serialVersionUID = 7348543186240756490L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long userId;
	
	@Persistent
	private String sessionId;

	@Persistent
	private long timeDelta;
	
	@Persistent
	private String verb;
	
	@Persistent
	private String info;

	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
	
	public PdrRaw (long userId, String sessionId, long timeDelta, String verb, String info) {
		this.sessionId = sessionId;
		this.timeDelta = timeDelta;
		this.userId = userId;
		this.verb = verb;
		this.info = info;
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

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
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

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public long getTimeDelta() {
		return timeDelta;
	}

	public void setTimeDelta(long timeDelta) {
		this.timeDelta = timeDelta;
	}
		
}
