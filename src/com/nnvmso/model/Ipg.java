package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/** 
 * User's IPG. Currently is used for FB.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Ipg implements Serializable {	
	private static final long serialVersionUID = 104915710055479694L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Persistent
	private long userId;
	
	@Persistent
	private long channelId;
	
	@Persistent
	private long programId;
	
	@Persistent
	private String programIdStr;
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
	
	@Persistent(serialized = "true")
	private Hashtable<Integer,Long> channels;
		
	public Hashtable<Integer, Long> getChannels() {
		return channels;
	}

	public void setChannels(Hashtable<Integer, Long> channels) {
		this.channels = channels;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return this.id;
	}
	
	public Date getCreateDate() {
		return this.createDate;
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

	public long getProgramId() {
		return programId;
	}

	public void setProgramId(long programId) {
		this.programId = programId;
	}

	public String getProgramIdStr() {
		return programIdStr;
	}

	public void setProgramIdStr(String programIdStr) {
		this.programIdStr = programIdStr;
	}	
	
}
