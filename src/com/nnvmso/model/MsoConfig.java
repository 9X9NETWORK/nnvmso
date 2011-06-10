package com.nnvmso.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable="true")
public class MsoConfig {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private long msoId;
		
	@Persistent
	private String item;
	public static String CDN = "cdn";
	public static String DEBUG = "debug";
	public static String FBTOKEN = "fbtoken";
	
	@Persistent
	private String value;
	public static String CDN_AMAZON = "amazon";
	public static String CDN_AKAMAI = "akamai";

	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
	
	public MsoConfig() {}
	
	public MsoConfig(long msoId, String item, String value) {
		this.msoId = msoId;
		this.item = item;
		this.value = value;
	}
	
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public long getMsoId() {
		return msoId;
	}

	public void setMsoId(long msoId) {
		this.msoId = msoId;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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
