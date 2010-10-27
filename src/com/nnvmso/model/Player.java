package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

@PersistenceCapable(detachable="true")
public class Player implements Serializable {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent(mappedBy = "player")
	private Mso mso;

	@Persistent
	private Text code;
		
	@Persistent
	private String logoUrl;
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;

	public Player() {
		this.createDate = new Date();
		this.updateDate = new Date();		
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Mso getMso() {
		return mso;
	}

	public void setMso(Mso mso) {
		this.mso = mso;
	}

	public Text getCode() {
		return code;
	}

	public void setCode(Text code) {
		this.code = code;
	}

	public String getCodeValue() {
		return code == null ? null : code.getValue();
	}
	
	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
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
