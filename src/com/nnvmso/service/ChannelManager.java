package com.nnvmso.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.nnvmso.lib.*;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.PodcastChannel;

@Service
public class ChannelManager {

	public final short MAX_CHANNEL_SIZE = 81;
	public final short MAX_MSOCHANNEL_SIZE = 77;
	public final short SYSTEM_CHANNEL_SIZE = 4;

	// ============================================================
	// find
	// ============================================================
	public void findAllAndSetWhatever() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(MsoChannel.class);
		List<MsoChannel> channels = (List<MsoChannel>) query.execute();
		for (MsoChannel c : channels) {
			if (c.getImageUrl().equals("false")) {
				c.setImageUrl("/WEB-INF/../images/thumb_noImage.jpg");
			}
		}
		pm.makePersistentAll(channels);
		pm.close();		
	}

	public List<MsoChannel> findSystemChannels() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoChannel.class);		
		q.setFilter("type == typeParam");
		q.declareParameters("short typeParam");
		List<MsoChannel> channels = (List<MsoChannel>) q.execute(MsoChannel.TYPE_SYSTEM);
		channels.size();
		pm.close();		
		return channels;
	}
	
	public List<MsoChannel> findAllPublic() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoChannel.class);
		q.setFilter("isPublic == isPublicParam");
		q.declareParameters("boolean isPublicParam");
		List<MsoChannel> channels = (List<MsoChannel>) q.execute(true);
		channels.size();
		pm.close();		
		return channels;
	}
	
	public MsoChannel findByKey(String key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoChannel channel = pm.getObjectById(MsoChannel.class, key);
		MsoChannel detached = pm.detachCopy(channel);
		pm.close();
		return detached;
	}
	
	public MsoChannel findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoChannel channel = pm.getObjectById(MsoChannel.class, id);
		MsoChannel detached = pm.detachCopy(channel);
		pm.close();
		return detached;
	}

	public List<MsoChannel> findByMso(Key msoKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		    	
		Query q = pm.newQuery(MsoChannel.class);
		q.setFilter("msoKey == msoKeyParam");
		q.declareParameters(Key.class.getName() + " msoKeyParam");
		List<MsoChannel> channels = (List<MsoChannel>) q.execute(msoKey);
		channels.size();
		pm.close();
		return channels;
	}
		
	public List<MsoChannel> findByIsPublic(Key msoKey, boolean isPublic) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		    	
		Query q = pm.newQuery(MsoChannel.class);
		q.setFilter("msoKey == msoKeyParam && isPublic == isPublicParam");
		q.declareParameters(Key.class.getName() + " msoKeyParam, boolean isPublicParam");
		List<MsoChannel> channels = (List<MsoChannel>) q.execute(msoKey, isPublic);
		System.out.println(DebugLib.OUT + channels.size());
		pm.close();
		return channels;		
	}	
	
	// ============================================================
	// c.u.d
	// ============================================================	
	public MsoChannel create(MsoChannel channel, Mso mso) {
		channel.setMsoKey(mso.getKey());
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoChannel.class);
		q.setFilter("msoKey == msoKeyParam");
		q.declareParameters(Key.class.getName() + " msoKeyParam");
		List<MsoChannel> channels = (List<MsoChannel>)q.execute(mso.getKey());
		short available = 1;
		if (channels.size() != 0) {
			Set grid = new HashSet();
			for (MsoChannel c : channels) {
				grid.add(c.getSeq());
			}
			for (short i=1; i< 82; i++) {
				if (!grid.contains(i)) {
					available = i;
					break;
				}
			}			
		}
		channel.setSeq(available);
		this.save(channel);
		return channel;
	}
	
	public MsoChannel saveViaPodcast(MsoChannel channel, PodcastChannel podcast) {
		channel.setName(podcast.getTitle());
		if (podcast.getDescription().length() > 500) {
			podcast.setDescription(podcast.getDescription().substring(0, 500));
		}
		channel.setIntro(podcast.getDescription());
		channel.setImageUrl(podcast.getImage());
		channel.setPublic(true);
		this.save(channel);
		return channel;
	}

	public MsoChannel createViaPodcast(PodcastChannel podcast, Mso mso) {
		MsoChannel channel = new MsoChannel();
		channel.setName(podcast.getTitle());
		if (podcast.getDescription().length() > 500) {
			podcast.setDescription(podcast.getDescription().substring(0, 500));
		}
		channel.setIntro(podcast.getDescription());
		channel.setImageUrl(podcast.getImage());
		this.create(channel, mso);
		return channel;
	}
	
	public void save(MsoChannel channel) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		channel.setUpdateDate(new Date());
		pm.makePersistent(channel);
		pm.close();
	}
	
	/*
	 * !!!transaction
	 * channel and program are supposed to be in one entity group
	 * in which it means to generate key ourselves
	 * also implies if the original relationship is not in the same entity group, then migration would be somewhat difficult
	 */
	public void delete(MsoChannel channel) {
		ProgramManager pService = new ProgramManager();
		List<MsoProgram> programs = pService.findByChannel(channel);
    	PersistenceManager pm = PMF.get().getPersistenceManager();
    	for (MsoProgram p : programs) {
        	System.out.println(DebugLib.OUT + p.getKey());    		
    	}
    	System.out.println(DebugLib.OUT + channel.getKey());
		pm.deletePersistentAll(programs);
		pm.deletePersistent(channel);   
    	pm.close();    			
	}

}
