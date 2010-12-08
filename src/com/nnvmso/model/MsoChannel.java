package com.nnvmso.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable="true")
public class MsoChannel implements Serializable {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@NotPersistent
	private long id;
	
	@Persistent
	private String name; 

	@Persistent
	private Key msoKey;
	
	@Persistent
	private String intro;
	
	@Persistent
	private String imageUrl; 
		
	@Persistent
	private short seq;
	
	@NotPersistent
	private short grid;

	@Persistent
	private boolean isPublic;
		
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
	
	@Persistent
	private String langCode;
	
	@Persistent
	private String tag;
	
	//!!!!yet another temp column.
	@Persistent
	private String podcast;
	
	public static short TYPE_MSO = 0; 
	public static short TYPE_SYSTEM = 1;
	@Persistent
	private short type;
	
	@Persistent 
	private List<Key> programSeq = new ArrayList<Key>();
	
	public MsoChannel() {
		this.createDate = new Date();
		this.updateDate = new Date();		
	}
	
	public MsoChannel(String name) {
		this.name = name;
		this.isPublic = false;
		this.createDate = new Date();
		this.updateDate = new Date();
	}

	public Key getKey() {
		return key;
	}

	public long getId() {
		if (key != null) {
		 id = key.getId();
		}
		return id;
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

	public Key getMsoKey() {
		return msoKey;
	}

	public void setMsoKey(Key msoKey) {
		this.msoKey = msoKey;
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

	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
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

	public void setUpdateDate(String updateDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm a");
        try {
        	this.updateDate = dateFormat.parse(updateDate);
        	System.out.println("UpdateDate parsed = " + dateFormat.format(updateDate));
        } catch (ParseException e) {
        	System.out.println("ERROR: exception while parse updateDate = " + dateFormat.format(updateDate));
        	this.updateDate = new Date();
        }
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

	public List<Key> getProgramSeq() {
		return programSeq;
	}

	public void setProgramSeq(List<Key> programSeq) {
		this.programSeq = programSeq;
	}

	public short getGrid() {
		return grid;
	}

	public void setGrid(short grid) {
		this.grid = grid;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public String getPodcast() {
		return podcast;
	}

	public void setPodcast(String podcast) {
		this.podcast = podcast;
	}
	
}