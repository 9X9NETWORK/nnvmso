package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/** 
 * Category is created by MSO. Each channel belongs to at least a category. 
 */
@PersistenceCapable(detachable="true")
public class Category implements Serializable {

	private static final long serialVersionUID = -3641562781732198821L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long parentId;
	
	@Persistent
	private long msoId;
	
	@Persistent
	private String name;
	
	@Persistent
	private boolean isPublic;

	@Persistent
	private boolean isInIpg;
	
	@Persistent	
	private short type;
	public static final short TYPE_NORESTRICTION = 0;
	public static final short TYPE_RESTRICTED = 1;	
	public static final short TYPE_PERSONAL = 2;
	public static final short TYPE_YOUTUBE = 3;
	
	@Persistent
	private short seq;
	
	@Persistent
	private int channelCount;
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
	
	public Category(String name, boolean isPublic, long msoId) {
		this.name = name;
		this.isPublic = isPublic;
		this.msoId= msoId;
		this.isInIpg = true;
		this.type = Category.TYPE_RESTRICTED;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isPublic() {
		return isPublic;
	}
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
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
	
	public int getChannelCount() {
		return channelCount;
	}

	public void setChannelCount(int channelCount) {
		this.channelCount = channelCount;
	}

	public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("name: " + name + ";");
        buffer.append("key: " + key + ";");
        buffer.append("msoId: " + msoId + ";");
        buffer.append("channelCount: " + channelCount + ";");
        return buffer.toString();
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public long getParentId() {
		return parentId;
	}

	public boolean isInIpg() {
		return isInIpg;
	}

	public void setInIpg(boolean isInIpg) {
		this.isInIpg = isInIpg;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
	}
	
}
