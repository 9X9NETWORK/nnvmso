package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable = "true")
public class ChannelAutosharing implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2644869375639221703L;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long msoId;
	
	@Persistent
	private long channelId;
	
	@Persistent
	private short type;
	
	@Persistent
	private String parameter;
	
	@Persistent
	private Date createDate;
	
	public ChannelAutosharing(long msoId, long channelId, short type) {
		this.msoId = msoId;
		this.channelId = channelId;
		this.type = type;
	}
	
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

	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}

	public long getChannelId() {
		return channelId;
	}

	public void setType(short type) {
		this.type = type;
	}

	public short getType() {
		return type;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getParameter() {
		return parameter;
	}
}
