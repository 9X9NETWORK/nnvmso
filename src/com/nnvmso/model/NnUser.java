package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.*;

import org.hibernate.validator.constraints.*;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable="true")
public class NnUser implements Serializable {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Email
	@Length(min=5, max=80)
	@Persistent
	private String email;
	
	@NotEmpty
	@Persistent
	private String name;	
	
	@Persistent
	private String intro;
	
	@NotEmpty
	@Persistent
	private Key msoKey;
		
	@Persistent
	private String imageUrl;

	@Length(min=6, max=20)
	@NotPersistent
	private String password;
	
	@Persistent
	private byte[] cryptedPassword;

	@Persistent
	private byte[] salt;
		
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
	
	public static short TYPE_MOCK_USER = 0;
	public static short TYPE_TEST_USER = 1;
	public static short TYPE_GENERAL_USER = 2;
	
	@Persistent
	private short type;
	
	public NnUser() {
		this.createDate = new Date();
		this.updateDate = new Date();		
	}

	public NnUser(String email) {		
		this.email = email;
		this.type = NnUser.TYPE_GENERAL_USER;  
		this.createDate = new Date();
		this.updateDate = new Date();
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Key getMsoKey() {
		return msoKey;
	}

	public void setMsoKey(Key msoKey) {
		this.msoKey = msoKey;
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

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public byte[] getCryptedPassword() {
		return cryptedPassword;
	}

	public void setCryptedPassword(byte[] cryptedPassword) {
		this.cryptedPassword = cryptedPassword;
	}

	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

}
