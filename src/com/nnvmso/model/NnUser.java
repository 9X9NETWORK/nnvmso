package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.*;

import org.hibernate.validator.constraints.*;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.AuthLib;

/**
 * 9x9 User accounts
 */
@PersistenceCapable(detachable="true")
public class NnUser implements Serializable {	
	private static final long serialVersionUID = -708171304411630395L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
		
	@Persistent
	private String token;
	
	@Persistent
	private long msoId;
	
	public static String GUEST_EMAIL = "guest@9x9.com";
	public static String GUEST_NAME = "Guest";
	@Email
	@Length(min=5, max=80)
	@Persistent
	private String email;
	
	@NotEmpty
	@Persistent
	private String name;	
	
	@Persistent
	private int age;
	
	@Persistent
	private String intro;
			
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
	
	public static short TYPE_ADMIN = 1; 
	public static short TYPE_TBC = 2;
	public static short TYPE_TCO = 3; 
	public static short TYPE_USER = 4;
	public static short TYPE_NN = 5; //default user, must have and only one
	@Persistent
	private short type;
	
	@Persistent
	private String facebookToken;
	
	public NnUser(String email, String password, String name, short type) {
		this.email = email;
		this.salt = AuthLib.generateSalt();
		this.cryptedPassword= AuthLib.encryptPassword(password, this.getSalt());
		this.name = name;
		this.type = type;
	}

	public NnUser(String email, String password, String name, short type, long msoId) {
		this.email = email;
		this.salt = AuthLib.generateSalt();
		this.cryptedPassword= AuthLib.encryptPassword(password, this.getSalt());
		this.name = name;
		this.type = type;
		this.msoId = msoId;
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

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public long getMsoId() {
		return msoId;
	}

	public void setMsoId(long msoId) {
		this.msoId = msoId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getFacebookToken() {
		return facebookToken;
	}

	public void setFacebookToken(String facebookToken) {
		this.facebookToken = facebookToken;
	}
		
}
