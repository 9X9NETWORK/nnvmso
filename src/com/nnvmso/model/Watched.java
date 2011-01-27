package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable="true")
public class Watched implements Serializable {

	private static final long serialVersionUID = -4611831552437881824L;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private Key userKey;
	
	@Persistent
	private Key channelKey;
	
	@Persistent(serialized = "true")
	private HashSet<Long> programs = new HashSet<Long>();

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

	public Key getChannelKey() {
		return channelKey;
	}

	public void setChannelKey(Key channelKey) {
		this.channelKey = channelKey;
	}

	public HashSet<Long> getPrograms() {
		return programs;
	}

	public void setPrograms(HashSet<Long> programs) {
		this.programs = programs;
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
