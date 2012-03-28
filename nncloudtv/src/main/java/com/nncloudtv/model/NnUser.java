package com.nncloudtv.model;

import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.*;

import com.nncloudtv.lib.AuthLib;

/**
 * 9x9 User accounts
 */
@PersistenceCapable(table="nnuser", detachable="true")
public class NnUser implements Serializable {	
	private static final long serialVersionUID = -708171304411630395L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private long id;
		
	@Persistent
	@Column(jdbcType="VARCHAR", length=255)
	private String token;  //each user has a unique token
	
	@Persistent
	private short shard; //which shard a user belongs to
	public static short SHARD_DEFAULT = 1;
	public static short SHARD_CHINESE = 2;
	
	
	@Persistent
	private long msoId; //which mso a user belongs to
	
	@Persistent
	@Column(jdbcType="VARCHAR", length=255)
	private String email;
	
	@Persistent
	@Column(jdbcType="VARCHAR", length=255)
	private String name;	
	
	@Persistent
	private String dob; //for now it's year
	
	@Persistent
	@Column(jdbcType="VARCHAR", length=255)
	private String intro;
			
	@Persistent
	@Column(jdbcType="VARCHAR", length=255)
	private String imageUrl;

	@NotPersistent
	private String password;
	
	@Persistent
	private byte[] cryptedPassword;

	@Persistent
	private byte[] salt;
	
	@Persistent
	@Column(jdbcType="VARCHAR", length=255)
	private String sphere; //content region

	@Persistent
	@Column(jdbcType="VARCHAR", length=5)
	private String lang; //ui language

	@Persistent
	@Column(jdbcType="VARCHAR", length=255)
	private String ip; //ui language
	
	@Persistent
	private short gender;
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
	
	@Persistent
	private short type; //user/s type
	public static short TYPE_ADMIN = 1; 
	public static short TYPE_TBC = 2;
	public static short TYPE_TCO = 3; 
	public static short TYPE_USER = 4;
	public static short TYPE_NN = 5; //default user, must have and only one
	public static short TYPE_3X3 = 6;
	public static short TYPE_ENTERPRISE = 7;
		
	public static String GUEST_EMAIL = "guest@9x9.com";
	public static String GUEST_NAME = "Guest";
	
	@Persistent
	@Column(jdbcType="VARCHAR", length=255)
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
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public short getShard() {
		return shard;
	}

	public void setShard(short shard) {
		this.shard = shard;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getSphere() {
		return sphere;
	}

	public void setSphere(String sphere) {
		this.sphere = sphere;
	}

	public short getGender() {
		return gender;
	}

	public void setGender(short gender) {
		this.gender = gender;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
