package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.*;
import com.google.appengine.api.datastore.Key;

/**
 * Programs(shows) under a MsoChannel.
 */
@PersistenceCapable(detachable="true")
public class MsoProgram implements Serializable {
	private static final long serialVersionUID = 5553891672235566066L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private long channelId;
	
	@Persistent
	private String name;
	
	@Persistent
	private String intro;
	
	@Persistent
	private String imageUrl;
	
	@Persistent
	private String imageLargeUrl;
	
	@Persistent
	private String mpeg4FileUrl;
	
	@Persistent
	private String webMFileUrl;				
	
	@Persistent
	private String otherFileUrl;
	
	@Persistent
	private String audioFileUrl;
	
	@Persistent
	private String storageId; // id of where the file physically stores, from transcoding service
	
	@Persistent
	private String errorCode;

	@NotPersistent
	private short status;
	public static short STATUS_OK = 0;
	public static short STATUS_ERROR = 1;	
	public static short STATUS_PROCESSING = 2;
		
	@Persistent
	private String duration;
		
	@Persistent
	private boolean isPublic; 
	
	@Persistent
	private short type;
	public static short TYPE_VIDEO = 1;
	public static short TYPE_AUDIO = 2;	
		
	@Persistent
	private Date createDate;
		
	@Persistent
	private Date updateDate;
		
	public MsoProgram(String name, String intro, String imageUrl, short type) {
		this.name = name;
		this.intro = intro;
		this.imageUrl = imageUrl;
		this.type = type;
	}
	
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public long getChannelId() {
		return channelId;
	}

	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getWebMFileUrl() {
		return webMFileUrl;
	}

	public void setWebMFileUrl(String webMFileUrl) {
		this.webMFileUrl = webMFileUrl;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
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

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public String getMpeg4FileUrl() {
		return mpeg4FileUrl;
	}

	public void setMpeg4FileUrl(String mpeg4FileUrl) {
		this.mpeg4FileUrl = mpeg4FileUrl;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {		
		this.status = status;
	}

	public String getOtherFileUrl() {
		return otherFileUrl;
	}

	public void setOtherFileUrl(String otherFileUrl) {
		this.otherFileUrl = otherFileUrl;
	}

	public String getAudioFileUrl() {
		return audioFileUrl;
	}

	public void setAudioFileUrl(String audioFileUrl) {
		this.audioFileUrl = audioFileUrl;
	}

	public String getImageLargeUrl() {
		return imageLargeUrl;
	}

	public void setImageLargeUrl(String imageLargeUrl) {
		this.imageLargeUrl = imageLargeUrl;
	}

	public String getStorageId() {
		return storageId;
	}

	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}

}
