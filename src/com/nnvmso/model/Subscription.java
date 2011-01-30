package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.appengine.api.datastore.Key;

/**
 * User's channel subscriptions. 
 */
@SuppressWarnings("serial")
@PersistenceCapable(detachable="true")
public class Subscription implements Serializable {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private long userId;
	
	@Persistent
	private long channelId;

	@Persistent
	private Date createDate;
	
	@Persistent
	private int seq;
	
	@Persistent
	private short type; //use with msoIpg
	
	@Persistent
	private Date updateDate;
	
	public Subscription(long userId, long channelId, int seq, short type) {
		this.userId = userId;
		this.channelId= channelId;
		this.seq = seq;
		this.type = type;
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

	public long getChannelId() {
		return channelId;
	}

	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}	
}