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
	private Key nnUserKey;
		
	@Persistent
	private String name; 
		
	@Persistent
	private String intro;
	
	@Persistent
	private String imageUrl; 
			
	@Persistent
	private boolean isPublic;
	
	public static String LANG_EN = "en"; 
	public static String LANG_ZH = "zh";	
	public static String LANG_ZH_TW = "zh-tw";	
	@Persistent
	private String langCode;
	
	@Persistent
	private String tag;

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
	public static short STATUS_INFRINGEMENT = 2;
	public static short STATUS_RRATED = 3;
	@Persistent
	private short status;
		
	@Persistent
	private String errorReason;
	
	@NotPersistent
	private int seq; //use with subscription, to specify sequence in IPG. 
	
	@Persistent
	private Date createDate;
		
	@Persistent
	private Date updateDate;
			
	public MsoChannel(String name, String intro, String imageUrl, Key nnUserKey) {
		this.name = name;
		this.intro = intro;
		this.imageUrl = imageUrl;
		this.nnUserKey = nnUserKey;
	}
	
	public MsoChannel(String sourceUrl, Key nnUserKey) {
		this.sourceUrl = sourceUrl;
		this.nnUserKey = nnUserKey;
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

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
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

	public Key getNnUserKey() {
		return nnUserKey;
	}

	public void setNnUserKey(Key nnUserKey) {
		this.nnUserKey = nnUserKey;
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
		
}