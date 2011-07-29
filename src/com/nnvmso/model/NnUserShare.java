package com.nnvmso.model;

import java.util.Date;
import java.util.Hashtable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class NnUserShare {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Persistent
	private long userId;

	@Persistent
	private long setId;
	
	@Persistent
	private long channelId;
	
	@Persistent
	private long programId;
	
	@Persistent
	private String programIdStr;
	
	@Persistent(serialized = "true")
	private Hashtable<Integer,Long> channels;
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getSetId() {
		return setId;
	}

	public void setSetId(long setId) {
		this.setId = setId;
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

	public Hashtable<Integer, Long> getChannels() {
		return channels;
	}

	public void setChannels(Hashtable<Integer, Long> channels) {
		this.channels = channels;
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
