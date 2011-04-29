package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.*;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable = "true")
public class ContentOwnership implements Serializable {
	
	protected static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long msoId;
	
	/**
	 * channelSetId / channelId / programId according to contentType
	 */
	@Persistent
	private long contentId;
	
	@Persistent
	private short contentType;
	public static final short TYPE_CHANNELSET = 1;
	public static final short TYPE_CHANNEL = 2;
	public static final short TYPE_PROGRAM = 3;
	
	@Persistent
	private short createMode;
	public static final short MODE_UPLOAD = 1;
	public static final short MODE_CURATE = 2;
	
	@Persistent
	private Date createDate;
	
	public Date getCreateDate() {
		return createDate;
	}
	
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public short getCreateMode() {
		return createMode;
	}
	
	public void setCreateMode(short createMode) {
		this.createMode = createMode;
	}
	
	public short getContentType() {
		return contentType;
	}
	
	public void setContentType(short contentType) {
		this.contentType = contentType;
	}
	
	public long getContentId() {
		return contentId;
	}
	
	public void setContentId(long contentId) {
		this.contentId = contentId;
	}
	
	public long getMsoId() {
		return msoId;
	}
	
	public void setMsoId(long msoId) {
		this.msoId = msoId;
	}
	
	public Key getKey() {
		return key;
	}
	
	public void setKey(Key key) {
		this.key = key;
	}
	
}
