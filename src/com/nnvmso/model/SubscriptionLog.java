package com.nnvmso.model;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@SuppressWarnings("serial")
@PersistenceCapable(detachable="true")
public class SubscriptionLog implements Serializable {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long msoId;
	
	@Persistent
	private long channelId;
	
	@Persistent
	private long setId;
	
	@Persistent
	private int count;

	public SubscriptionLog() {}
	public SubscriptionLog(long msoId, long channelId) {
		this.msoId = msoId;
		this.channelId = channelId;
		this.count = 1;
	}
	
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public long getMsoId() {
		return msoId;
	}

	public void setMsoId(long msoId) {
		this.msoId = msoId;
	}

	public long getChannelId() {
		return channelId;
	}

	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getSetId() {
		return setId;
	}

	public void setSetId(long setId) {
		this.setId = setId;
	}
	
}
