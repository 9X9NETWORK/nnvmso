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
	private String fauxName; 
	
	@Persistent
	private String nameSearch;

	@Persistent
	private String intro;
	
	@Persistent
	private String fauxIntro;
	
	@Persistent
	private String fauxImageUrl;
	
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
	private String tags;
	
	@Persistent
	private short advertsing;
	
	@Persistent
	private boolean featured;

	@Persistent
	private int programCount;
	
	@Persistent
	private String sourceUrl;
			
	@Persistent
	private String sourceUrlSearch;
	
	@NotPersistent
	private short type; //Use with MsoIpg and Subscription, to define attributes such as MsoIpg.TYPE_READONLY

	@Persistent
	public short contentType;
	public static final short CONTENTTYPE_SYSTEM = 1;
	public static final short CONTENTTYPE_PODCAST = 2;
	public static final short CONTENTTYPE_YOUTUBE_CHANNEL = 3;
	public static final short CONTENTTYPE_YOUTUBE_PLAYLIST = 4;
	public static final short CONTENTTYPE_FACEBOOK = 5;
	public static final short CONTENTTYPE_MIXED = 6;
	public static final short CONTENTTYPE_SLIDE = 7;	
	
	@Persistent
	private short status;
	//general
	public static final short STATUS_SUCCESS = 0;
	public static final short STATUS_ERROR = 1;
	public static final short STATUS_PROCESSING = 2;
	public static final short STATUS_WAIT_FOR_APPROVAL = 3;
	//invalid
	public static final short STATUS_INVALID_FORMAT = 51;
	public static final short STATUS_URL_NOT_FOUND = 53;
	//quality
	public static final short STATUS_NO_VALID_EPISODE = 100;
	public static final short STATUS_BAD_QUALITY = 101;
	//internal
	public static final short STATUS_TRANSCODING_DB_ERROR = 1000;
	public static final short STATUS_NNVMSO_JSON_ERROR = 1001;
			
	//enforce transcoding, could be used to assign special formats or bit rates
	//currently 0 is no, 1 is yes
	@Persistent
	private short enforceTranscoding; 
		
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
			
	@Persistent
	private String transcodingUpdateDate; //timestamps from transcoding server
		
	public MsoChannel(String name, String intro, String imageUrl, long userId) {
		this.name = name;
		this.intro = intro;
		this.imageUrl = imageUrl;
		this.userId = userId;
	}
	
	public MsoChannel(String sourceUrl, long userId) {
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

	public String getNameSearch() {
		return nameSearch;
	}

	public void setNameSearch(String nameSearch) {
		this.nameSearch = nameSearch;
	}

	public String getSourceUrlSearch() {
		return sourceUrlSearch;
	}

	public void setSourceUrlSearch(String sourceUrlSearch) {
		this.sourceUrlSearch = sourceUrlSearch;
	}

	public void setTranscodingUpdateDate(String transcodingUpdateDate) {
		this.transcodingUpdateDate = transcodingUpdateDate;
	}

	public short getEnforceTranscoding() {
		return enforceTranscoding;
	}

	public void setEnforceTranscoding(short enforceTranscoding) {
		this.enforceTranscoding = enforceTranscoding;
	}

	public String getTranscodingUpdateDate() {
		return transcodingUpdateDate;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public boolean isFeatured() {
		return featured;
	}

	public void setFeatured(boolean featured) {
		this.featured = featured;
	}

	public String getFauxName() {
		return fauxName;
	}

	public void setFauxName(String fauxName) {
		this.fauxName = fauxName;
	}

	public String getFauxIntro() {
		return fauxIntro;
	}

	public void setFauxIntro(String fauxIntro) {
		this.fauxIntro = fauxIntro;
	}

	public String getFauxImageUrl() {
		return fauxImageUrl;
	}

	public void setFauxImageUrl(String fauxImageUrl) {
		this.fauxImageUrl = fauxImageUrl;
	}	
}
