package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.*;
import com.google.appengine.api.datastore.Key;

/**
 * 9x9 ChannelSet
 */
@PersistenceCapable(detachable="true")
public class ChannelSet implements Serializable {
	
	private static final long serialVersionUID = 6138621615980949044L;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long msoId; // don't use this, please refer to class ContentOwnership
	
	@Persistent
	private String name;
	
	@Persistent
	private String nameSearch;
	
	@Persistent
	private String intro;
			
	@Persistent
	private String imageUrl;
	
	@Persistent
	private boolean isPublic;
	
	@Persistent
	private String defaultUrl;
	
	@Persistent
	private String beautifulUrl;
	
	@Persistent
	private String tag;
	
	@Persistent
	private int channelCount;
	
	@Persistent
	private boolean featured;
	
	@Persistent
	private Date createDate;
		
	@Persistent
	private Date updateDate;
	
	@Persistent
	private String lang;

	@Persistent
	private short seq; //it's used for recommendation

	@Persistent
	private String piwik;
	
	@NotPersistent
	private int subscriptionCount;
	
	public ChannelSet(long msoId, String name, String intro, boolean isPublic) {
		this.msoId = msoId;
		this.name = name;
		this.intro = intro;
		this.isPublic = isPublic;
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
	
	public String getDefaultUrl() {
		return defaultUrl;
	}
	
	public void setDefaultUrl(String defaultUrl) {
		this.defaultUrl = defaultUrl;
	}
	
	public void setBeautifulUrl(String beautifulUrl) {
		this.beautifulUrl = beautifulUrl;
	}
	
	public String getBeautifulUrl() {
		return beautifulUrl;
	}
	
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public long getMsoId() {
		return msoId;
	}
	
	public void setMsoId(long msoId) {
		this.msoId = msoId;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String getNameSearch() {
		return nameSearch;
	}
	
	public void setNameSearch(String nameSearch) {
		this.nameSearch = nameSearch;
	}

	public boolean isFeatured() {
		return featured;
	}

	public void setFeatured(boolean featured) {
		this.featured = featured;
	}

	public void setSubscriptionCount(int subscriptionCount) {
		this.subscriptionCount = subscriptionCount;
	}

	public int getSubscriptionCount() {
		return subscriptionCount;
	}

	public int getChannelCount() {
		return channelCount;
	}

	public void setChannelCount(int channelCount) {
		this.channelCount = channelCount;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
	}

	public String getPiwik() {
		return piwik;
	}

	public void setPiwik(String piwik) {
		this.piwik = piwik;
	}
	
}
