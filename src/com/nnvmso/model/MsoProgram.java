package com.nnvmso.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable="true")
public class MsoProgram implements Serializable {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@NotPersistent
	private long id;
	
	@Persistent
	private Key channelKey;
	
	@Persistent
	private Set<Key> contentKeys;
	
	@Persistent
	private String name;
	
	@Persistent
	private String intro;
	
	@Persistent
	private String imageUrl;
	
	@Persistent
	private String mpeg4FileUrl;
	
	@Persistent
	private String errorCode;

	public static short STATUS_ERROR = 0;	
	public static short STATUS_OK = 1;
	public static short STATUS_PROCESSING = 2;
	
	@NotPersistent
	private short status;
	
	@Persistent
	private ProgramScript script; 	

	@Persistent
	private long duration;
	
	@Persistent
	private boolean isPublic; 
	
	public static String TYPE_VIDEO = "video";
	public static String TYPE_SLIDESHOW = "slideshow";
	public static String TYPE_AUDIO = "audio";	

	public static String VIDEO_WEBM = "webm";
	public static String VIDEO_MPEG4 = "mp4";	
	
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
	
	@Persistent
	private String webMFileUrl;				
	
	public MsoProgram() {
		this.createDate = new Date();
		this.updateDate = new Date();		
	}
	
	public MsoProgram(String name) {
		this.name = name;
		this.type = TYPE_VIDEO; //!!!!
		this.isPublic = false;
		this.contentKeys = new HashSet();
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

	public Set<Key> getContentKeys() {
		return contentKeys;
	}

	public void setContentKeys(Set<Key> contentKeys) {
		this.contentKeys = contentKeys;
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

	public ProgramScript getScript() {
		return script;
	}

	public void setScript(ProgramScript script) {
		this.script = script;
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
		return key.getId();
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public short getStatus() {
		status = STATUS_OK;		
		if (!type.equals(TYPE_SLIDESHOW)) {
			if (mpeg4FileUrl == null || webMFileUrl == null) {
				status = STATUS_PROCESSING;
			}
		} else {
			if (this.getScript() == null) {
				System.out.println("getscript == null");
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
	
}