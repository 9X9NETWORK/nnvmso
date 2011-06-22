package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable = "true")
public class ChannelSetAutosharing implements Serializable {
	
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
	private long channelSetId;
	
	@Persistent
	private short type;
	
	@Persistent
	private Date createDate;
	
	public ChannelSetAutosharing(long msoId, long channelSetId, short type) {
		this.msoId = msoId;
		this.channelSetId = channelSetId;
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

	public void setChannelSetId(long channelId) {
		this.channelSetId = channelId;
	}

	public long getChannelSetId() {
		return channelSetId;
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
}
