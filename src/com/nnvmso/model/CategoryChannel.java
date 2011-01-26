package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * Category and MsoChannel establish connections through CategoryChannel  
 */
@PersistenceCapable(detachable="true")
public class CategoryChannel implements Serializable {

	private static final long serialVersionUID = -3245839522361848219L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private Key categoryKey;
	
	@Persistent
	private Key channelKey;

	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;

	public CategoryChannel(Key categoryKey, Key channelKey) {
		this.categoryKey = categoryKey;
		this.channelKey = channelKey;
	}
	
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getCategoryKey() {
		return categoryKey;
	}

	public void setCategoryKey(Key categoryKey) {
		this.categoryKey = categoryKey;
	}

	public Key getChannelKey() {
		return channelKey;
	}

	public void setChannelKey(Key channelKey) {
		this.channelKey = channelKey;
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
}
