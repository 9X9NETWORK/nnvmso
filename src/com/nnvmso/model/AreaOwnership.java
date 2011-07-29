package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable = "true")
public class AreaOwnership implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long userId;
	
	@Persistent
	private long setId;

	@Persistent
	private short type;
	public static final short TYPE_USER = 1;
	public static final short TYPE_RO = 2;
	
	@Persistent
	private String setName;

	@Persistent
	private String setImageUrl;
	
	@Persistent
	private short areaNo;
	
	@Persistent
	private int status;
	public static final int STATUS_FREE = 0;
	public static final int STATUS_OCCUPIED = 1;
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getUserId() {
		return userId;
	}

	public long getSetId() {
		return setId;
	}

	public void setSetId(long setId) {
		this.setId = setId;
	}

	public String getSetName() {
		return setName;
	}

	public void setSetName(String setName) {
		this.setName = setName;
	}

	public void setAreaNo(short areaNo) {
		this.areaNo = areaNo;
	}

	public short getAreaNo() {
		return areaNo;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
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

	public String getSetImageUrl() {
		return setImageUrl;
	}

	public void setSetImageUrl(String setImageUrl) {
		this.setImageUrl = setImageUrl;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}	
}
