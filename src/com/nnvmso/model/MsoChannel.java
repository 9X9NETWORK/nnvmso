package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.YouTubeLib;
import com.nnvmso.service.MsoChannelManager;

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
	private String oriName; 
	
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
	public static final short CONTENTTYPE_MAPLE_VARIETY = 8;
	public static final short CONTENTTYPE_MAPLE_SOAP = 9;
	
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
					
	@Persistent
	private String errorReason;
		
	@NotPersistent
	private int seq; //use with subscription, to specify sequence in IPG. 

	public static final short SORT_NEWEST_TO_OLDEST = 1; //default
	public static final short SORT_OLDEST_TO_NEWEST = 2;
	public static final short SORT_MAPEL = 3;
	@NotPersistent
	private int sorting;

	@NotPersistent
	private String recentlyWatchedProgram;  
	
	@NotPersistent
	private int subscriptionCount;
	
	@Persistent
	private Date createDate;
		
	@Persistent
	private Date updateDate;
			
	@Persistent
	private String transcodingUpdateDate; //timestamps from transcoding server
		
    @Persistent
    private Set<String> fts;

    @Persistent
    private String piwik;
    
	public MsoChannel(String name, String intro, String imageUrl, long userId) {
		this.name = name;
		this.intro = intro;
		this.imageUrl = imageUrl;
		this.userId = userId;
		this.fts = new HashSet<String>();
		MsoChannelManager.updateFTSStuffForMsoChannel(this);		
	}
	
	public MsoChannel(String sourceUrl, long userId) {
		this.sourceUrl = sourceUrl;
		this.userId = userId;
		this.fts = new HashSet<String>();
		MsoChannelManager.updateFTSStuffForMsoChannel(this);		
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
		if (intro != null) {
			intro = intro.replaceAll("\n", " ");
			intro = intro.replaceAll("\t", " ");
			int introLenth = (intro.length() > 256 ? 256 : intro.length()); 
			intro = intro.substring(0, introLenth);
		}
		this.intro = intro;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getPlayerPrefImageUrl() {
		String imageUrl = getImageUrl();
		if ((getStatus() == MsoChannel.STATUS_ERROR) || 
		    (getStatus() != MsoChannel.STATUS_WAIT_FOR_APPROVAL &&
			 getStatus() != MsoChannel.STATUS_SUCCESS && 
			 getStatus() != MsoChannel.STATUS_PROCESSING)) {	
			imageUrl = "http://9x9ui.s3.amazonaws.com/9x9playerV65/images/error.png";
		} 
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
	
	public String getPlayerPrefSource() {
		if (getSourceUrl() != null && getSourceUrl().contains("http://www.youtube.com"))
			return YouTubeLib.getYouTubeChannelName(getSourceUrl());		
		if (getContentType() == MsoChannel.CONTENTTYPE_FACEBOOK)
			return getSourceUrl();
		return "";
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

	public String getSourceUrlSearch() {
		return sourceUrlSearch;
	}

	public void setSourceUrlSearch(String sourceUrlSearch) {
		this.sourceUrlSearch = sourceUrlSearch;
	}

	public void setTranscodingUpdateDate(String transcodingUpdateDate) {
		this.transcodingUpdateDate = transcodingUpdateDate;
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

	public Set<String> getFts() {
		return fts;
	}

	public void setFts(Set<String> fts) {
		this.fts = fts;
	}

	public int getSorting() {
		return sorting;
	}

	public void setSorting(int sorting) {
		this.sorting = sorting;
	}

	public String getPiwik() {
		return piwik;
	}

	public void setPiwik(String piwik) {
		this.piwik = piwik;
	}

	public String getRecentlyWatchedProgram() {
		return recentlyWatchedProgram;
	}

	public void setRecentlyWatchedProgram(String recentlyWatchedProgram) {
		this.recentlyWatchedProgram = recentlyWatchedProgram;
	}

	public String getOriName() {
		return oriName;
	}

	public void setOriName(String oriName) {
		this.oriName = oriName;
	}
	
}
