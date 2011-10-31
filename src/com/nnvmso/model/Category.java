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
	public static String UNCATEGORIZED = "UNCATEGORIZED";
	private String name;
	
	@Persistent
	private boolean isPublic;
	
	@Persistent
	private int channelCount;
	
	@Persistent
	private int subCategoryCnt;
	
	@Persistent
	private String lang;

	@Persistent
	private short seq;	
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;	
	
	public Category(String name, boolean isPublic, long msoId) {
		this.name = name;
		this.isPublic = isPublic;
		this.msoId= msoId;
	}

	public Category(String name) {
		this.name = name;
		this.setPublic(true);
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

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
	}

	public int getSubCategoryCnt() {
		return subCategoryCnt;
	}

	public void setSubCategoryCnt(int subCategoryCnt) {
		this.subCategoryCnt = subCategoryCnt;
	}
	
}
