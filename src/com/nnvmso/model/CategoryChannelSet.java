package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable = "true")
public class CategoryChannelSet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6253012826484625104L;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long categoryId;
	
	@Persistent
	private long channelSetId;
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;

	public CategoryChannelSet(long channelSetId, long categoryId) {
		this.channelSetId = channelSetId;
		this.categoryId = categoryId;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}

	public long getCategoryId() {
		return categoryId;
	}

	public void setChannelSetId(long channelSetId) {
		this.channelSetId = channelSetId;
	}

	public long getChannelSetId() {
		return channelSetId;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}
	
}
