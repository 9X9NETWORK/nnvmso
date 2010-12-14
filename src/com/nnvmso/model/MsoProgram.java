package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.appengine.api.datastore.Key;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@PersistenceCapable(detachable="true")
public class MsoProgram implements Serializable {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent //used as identifier in storage system
	private String storageId;
	
	@NotPersistent
	private long id;
	
	@Persistent
	private Key channelKey;
	
	@Persistent
	private String name;
	
	@Persistent
	private String intro;
	
	@Persistent
	private String imageUrl;
	
	//!!!remove
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
	private String errorCode;

	public static short STATUS_ERROR = 0;	
	public static short STATUS_OK = 1;
	public static short STATUS_PROCESSING = 2;
	
	@NotPersistent
	private short status;
	
	@Persistent//(defaultFetchGroup = "true")
	private ProgramScript nnScript; 	

	@Persistent
	private String duration;
	
	@Persistent
	private boolean isPublic; 
	
	public static String TYPE_VIDEO = "video";
	public static String TYPE_SLIDESHOW = "slideshow";
	public static String TYPE_AUDIO = "audio";	

	public static String VIDEO_WEBM = "webm";
	public static String VIDEO_MPEG4 = "mp4";	
	public static String VIDEO_OTHER = "other";
	
	@Persistent
	private String type;
	
	@Persistent
	private short seq;
	
	@Persistent
	private Date createDate;
		
	@Persistent
	private Date updateDate;
	
	//!!!! need housekeeping
	private long channelId;

	public MsoProgram() {
		this.type = TYPE_VIDEO;
		this.createDate = new Date();
		this.updateDate = new Date();		
	}
	
	public MsoProgram(String name) {
		this.name = name;
		this.type = TYPE_VIDEO;
		this.createDate = new Date();
		this.updateDate = new Date();
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getChannelKey() {
		return channelKey;
	}

	public void setChannelKey(Key channelKey) {
		this.channelKey = channelKey;
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

	public ProgramScript getNnScript() {
		return nnScript;
	}

	public void setNnScript(ProgramScript nnScript) {
		this.nnScript = nnScript;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
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

	public String getUpdateDateString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm a");
        String pubDate = dateFormat.format(updateDate);
        System.out.println("getUpdateDate() : UDate = " + pubDate);
		return pubDate;
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

	public long getChannelId() {
		return channelId;
	}

	public void setChannelId(long channelId) {
		this.channelId = channelId;
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

	public long getId() {
		if (key != null) {
		   id = key.getId();
		}
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public short getStatus() {
		status = STATUS_OK;		
		if (!type.equals(TYPE_SLIDESHOW)) {
			if (mpeg4FileUrl == null || webMFileUrl == null) {
				status = STATUS_PROCESSING;
			}
		} else {
			if (this.getNnScript() == null) {
				status = STATUS_PROCESSING;
			}
		}
		if (errorCode != null) {
			if (!errorCode.equals("0")) {
				status = STATUS_ERROR;
			}
		}
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

	public String getStorageId() {
		return storageId;
	}

	public void setStorageId(String storageId) {
		this.storageId = storageId;
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
	
}