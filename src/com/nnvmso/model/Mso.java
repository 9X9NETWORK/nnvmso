package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.*;

import org.hibernate.validator.constraints.*;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(detachable="true")
public class Mso implements Serializable {
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

	@Length(min=6, max=20)
	@NotPersistent
	private String password;
	
	@Persistent
	private byte[] cryptedPassword;

	@Persistent
	private byte[] salt;
	
	@Persistent
	private String imageUrl;		

	@Persistent
	private Player player;
	
	@Persistent
	private short maxChannels;
		
	public static final short TYPE_DEFAULT = 0;		
	@Persistent
	private short type;

	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
	
	public Mso() {		
		this.createDate = new Date();
		this.updateDate = new Date();		
	}
		
	public Mso(String email, String name) {
		this.email = email;
		this.name = name;
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

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String thumbnailUrl) {
		this.imageUrl = thumbnailUrl;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
