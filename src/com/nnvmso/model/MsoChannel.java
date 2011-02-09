package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.*;
import com.google.appengine.api.datastore.Key;

/**
 * 9x9 Channel
 */
@PersistenceCapable(detachable="true")
public class MsoChannel implements Serializable {
	private static final long serialVersionUID = 6138621615980949044L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long userId;
		
	@Persistent
	private String name; 
		
	@Persistent
	private String intro;
	
	@Persistent
	private String imageUrl; 
			
	@Persistent
	private boolean isPublic;
	
	@Persistent
	private String langCode;
	
	@Persistent
	private short rating;
	
	@Persistent
	private short license;
	
	@Persistent
	private short advertsing;

	@Persistent
	private int programCount;
	
	@Persistent
	private String sourceUrl;
			
	@NotPersistent
	private short type; //Use with MsoIpg and Subscription, to define attributes such as MsoIpg.TYPE_READONLY

	public static short CONTENTTYPE_SYSTEM = 1;
	public static short CONTENTTYPE_PODCAST = 2;
	public static short CONTENTTYPE_YOUTUBE = 3;
	@Persistent
	public short contentType;
	
	public static short STATUS_SUCCESS = 0;
	public static short STATUS_ERROR = 1;
	public static short STATUS_PROCESSING = 2;
	@Persistent
	private short status;
		
	@Persistent
	private String errorReason;
	
	@NotPersistent
	private int seq; //use with subscription, to specify sequence in IPG. 
		
	@NotPersistent
	private int subscriptionCount;
	
	@Persistent
	private Date createDate;
		
	@Persistent
	private Date updateDate;
			
	public MsoChannel(String name, String intro, String imageUrl, long userId) {
		this.name = name;
		this.intro = intro;
		this.imageUrl = imageUrl;
		this.userId = userId;
	}
	
	public MsoChannel(String sourceUrl, long userId) {
		if (sourceUrl != null) {sourceUrl.trim();}
		this.sourceUrl = sourceUrl;
		this.userId = userId;
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

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
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

	public String getLangCode() {
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public int getProgramCount() {
		return programCount;
	}

	public void setProgramCount(int count) {
		this.programCount = count;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(short status) {
		this.status = status;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public short getContentType() {
		return contentType;
	}

	public void setContentType(short contentType) {
		this.contentType = contentType;
	}

	public String getErrorReason() {
		return errorReason;
	}

	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
	}

	public short getRating() {
		return rating;
	}

	public void setRating(short rating) {
		this.rating = rating;
	}

	public short getLicense() {
		return license;
	}

	public void setLicense(short license) {
		this.license = license;
	}

	public short getAdvertsing() {
		return advertsing;
	}

	public void setAdvertsing(short advertsing) {
		this.advertsing = advertsing;
	}

	public int getSubscriptionCount() {
		return subscriptionCount;
	}

	public void setSubscriptionCount(int subscriptionCount) {
		this.subscriptionCount = subscriptionCount;
	}	
}