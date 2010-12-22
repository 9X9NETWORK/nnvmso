package com.nnvmso.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.appengine.api.datastore.Key;
 
@PersistenceCapable(detachable="true")
public class IPG implements Serializable {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private Key userKey;
	
	@Persistent
	private List<Key> channelKeys = new ArrayList<Key>();
	
	@Persistent
	private List<Short> grids = new ArrayList<Short>();
	
	@Persistent
	private boolean shared; 
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
		
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getUserKey() {
		return userKey;
	}

	public void setUserKey(Key userKey) {
		this.userKey = userKey;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public List<Key> getChannelKeys() {
		return channelKeys;
	}

	public void setChannelKeys(List<Key> channelKeys) {
		this.channelKeys = channelKeys;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public List<Short> getGrids() {
		return grids;
	}

	public void setGrid(List<Short> grids) {
		this.grids = grids;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}	
	
}