package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.*;

import com.google.appengine.api.datastore.Key;

/**
 * Mso, AKA TBC(?)
 */
@PersistenceCapable(detachable="true")
public class Mso implements Serializable {

	private static final long serialVersionUID = 352047930355952392L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
		
	@Persistent 
	private String name;

	@Persistent
	private String nameSearch; //used for search
	
	@Persistent 
	private String intro;
		
	@Persistent
	private String logoUrl;
	
	@Persistent
	private String jingleUrl;
	
	@Persistent
	private String contactEmail;
	
	public static short TYPE_NN = 1; //default mso, must have and must have ONLY one
	public static short TYPE_MSO= 2;
	@Persistent
	private short type;
		
	@Persistent
	private String preferredLangCode;
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;

	public Mso(String name, String intro, String contactEmail, short type) {
		this.name = name;
		this.intro = intro;
		this.contactEmail = contactEmail;
		this.type = type;
	}
	
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getNameSearch() {
		return nameSearch;
	}

	public void setNameSearch(String nameSearch) {
		this.nameSearch = nameSearch;
	}

	public String getJingleUrl() {
		return jingleUrl;
	}

	public void setJingleUrl(String jingleUrl) {
		this.jingleUrl = jingleUrl;
	}

	public String getPreferredLangCode() {
		return preferredLangCode;
	}

	public void setPreferredLangCode(String preferredLangCode) {
		this.preferredLangCode = preferredLangCode;
	}	
}
