package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.*;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable = "true")
public class ChannelSetChannel implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long channelId;
	
	@Persistent
	private long channelSetId;
	
	@Persistent
	private int seq;
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
	
	public ChannelSetChannel(long channelSetId, long channelId, int seq) {
		this.channelSetId = channelSetId;
		this.channelId = channelId;
		this.seq = seq;
	}

	public Date getUpdateDate() {
		return updateDate;
	}
	
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
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
	
	public long getChannelSetId() {
		return channelSetId;
	}
	
	public void setChannelSetId(long channelSetId) {
		this.channelSetId = channelSetId;
	}
	
	public long getChannelId() {
		return channelId;
	}
	
	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}
	
	public Key getKey() {
		return key;
	}
	
	public void setKey(Key key) {
		this.key = key;
	}
}
