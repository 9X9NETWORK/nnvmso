package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/*
 * Mso's configurations
 */
@PersistenceCapable(table="mso_config", detachable="true")
public class MsoConfig {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private long id;

	@Persistent
	@Column(name="mso_id")
	private long msoId;
		
	@Persistent
	@Column(jdbcType="VARCHAR", length=255)
	private String item;
	public static String CDN = "cdn";
	public static String DEBUG = "debug";
	public static String FBTOKEN = "fbtoken"; //regardless of the brand	
	
	@Persistent
	@Column(jdbcType="VARCHAR", length=255)
	private String value;
	public static String CDN_AMAZON = "amazon";
	public static String CDN_AKAMAI = "akamai";

	@Persistent
	@Column(name="create_date")
	private Date createDate;
	
	@Persistent
	@Column(name="update_date")
	private Date updateDate;
	
	public MsoConfig() {}
	
	public MsoConfig(long msoId, String item, String value) {
		this.msoId = msoId;
		this.item = item;
		this.value = value;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
