package com.nnvmso.model;

import com.nnvmso.model.NnUser;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;
import java.lang.Long;
import java.lang.Short;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/** 
 * User's IPG. Currently is used for FB.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Ipg implements Serializable {	
	private static final long serialVersionUID = 104915710055479694L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Persistent
	private NnUser user;
	
	@Persistent
	private Date createDate;
	
	@Persistent
	private Date updateDate;
	
	@Persistent(serialized = "true")
	private Hashtable<Short,Key> channels;
	
	// Constructor
	public Ipg(NnUser user) {	
		/*
		SubscriptionManager sMngr = new SubscriptionManager();		
		List<Subscription> subscriptions = sMngr.findAll(user);
		this.channels = new Hashtable<Short,Key>();
		this.createDate = new Date();
		this.user = user;
		
		int size = subscriptions.size();
		for (int i = 0; i < size; i++) {
			Subscription s = subscriptions.get(i);
			if (s.getSeq() > 63) // skip private channels
				continue;
			this.channels.put(new Short(s.getSeq()), s.getChannelKey());
		}
		System.out.println("new IPG size: " + this.channels.size());
		*/
		
	}
	
	public Long getId() {
		return this.id;
	}
	
	public Date getCreateDate() {
		return this.createDate;
	}
	
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public Hashtable<Short,Key> getChannels() {
		return this.channels;
	}
	
	public NnUser getUser() {
		return this.user;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}		
}
