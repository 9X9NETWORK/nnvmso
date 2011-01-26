package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/** 
 * Mso's default IPG
 */
@PersistenceCapable(detachable="true")
public class MsoIpg  implements Serializable {
	private static final long serialVersionUID = -1577337108738062176L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
		
	@Persistent
	private Key msoKey; 

	@Persistent 
	private Key channelKey;
	
	public static short TYPE_GENERAL = 1;
	public static short TYPE_READONLY = 2;
	@Persistent	
	private short type;
	
	@Persistent
	private int seq;
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
	
	public MsoIpg(Key msoKey, Key channelKey, int seq, short type) {
		this.msoKey = msoKey;
		this.channelKey = channelKey;
		this.seq = seq;
		this.type = type;
	}
	
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getMsoKey() {
		return msoKey;
	}

	public void setMsoKey(Key msoKey) {
		this.msoKey = msoKey;
	}

	public Key getChannelKey() {
		return channelKey;
	}

	public void setChannelKey(Key channelKey) {
		this.channelKey = channelKey;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
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
