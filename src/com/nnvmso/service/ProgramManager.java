package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.nnvmso.json.AwsMessage;
import com.nnvmso.json.Slideshow;
import com.nnvmso.lib.DebugLib;
import com.nnvmso.lib.NnScriptLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.ProgramScript;

@Service
public class ProgramManager {
	// ============================================================
	// find
	// ============================================================
	public List<MsoProgram> findNew(NnUser user) {
		SubscriptionManager sMngr = new SubscriptionManager();
		List<MsoChannel> channels = sMngr.findSubscribedChannels(user);				
		System.out.println("channel size=" + channels.size());
		List<MsoProgram> newPrograms = new ArrayList<MsoProgram>();
		for (MsoChannel c : channels) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Query query = pm.newQuery(MsoProgram.class);
			query.setFilter("channelKey == channelKeyParam");
			query.declareParameters(Key.class.getName() + " channelKeyParam");
	    	query.setOrdering("updateDate desc");	    				
	    	List<MsoProgram> results = (List<MsoProgram>) query.execute(c.getKey());
			int recentSize = 3;
			for (int i=0; i<results.size(); i++) {
				if (i == recentSize) {break;}
				System.out.println("new program=" + results.get(i).getId() + " in channel " + c.getId() + " and its date is " + c.getUpdateDate());
				if (c.getType() != MsoChannel.TYPE_SYSTEM) {
					newPrograms.add(results.get(i));
				}												
			}
		}
		return newPrograms;
	}
	
	public MsoProgram findByKey(String key) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoProgram program = pm.getObjectById(MsoProgram.class, key);
		MsoProgram detached = pm.detachCopy(program);
		pm.close();
		return detached;
	}
	
	public MsoProgram findByStorageId(String storageId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query query = pm.newQuery(MsoProgram.class);
		query.setFilter("storageId == '" + storageId + "'");	
		List<MsoProgram> results = (List<MsoProgram>) query.execute();
		MsoProgram detached = null;
		if (results.size() > 0) {
			detached = pm.detachCopy(results.get(0));
		}
		pm.close();
		return detached;
	}
	
	public List<MsoProgram> findAll() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoProgram.class);
		List<MsoProgram> programs = (List<MsoProgram>)q.execute();
		List<MsoProgram> detached = (List<MsoProgram>)pm.detachCopyAll(programs);
		pm.close();
		return detached;		
	}
	
	public void findAllAndSetWhatever() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(MsoProgram.class);
		List<MsoProgram> programs = (List<MsoProgram>) query.execute();
		for (MsoProgram p : programs) {
			MsoChannel c = pm.getObjectById(MsoChannel.class, p.getChannelKey());
			p.setChannelId(c.getId());
		}
		pm.makePersistentAll(programs);
		pm.close();		
	}
	
	public MsoProgram findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoProgram program = pm.getObjectById(MsoProgram.class, id);
		MsoProgram detached = pm.detachCopy(program);		
		pm.close();
		return detached;
	}

	//it is used if script is not in defaultFetchGroup
	public MsoProgram findGroupById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoProgram program = pm.getObjectById(MsoProgram.class, id);
		program.getNnScript();
		MsoProgram detached = pm.detachCopy(program);
		pm.close();
		return detached;
	}	
	
	public List<MsoProgram> findByChannel(MsoChannel channel) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoProgram.class);
		q.setFilter("channelKey == channelKeyParam");
		q.declareParameters(Key.class.getName() + " channelKeyParam");
		List<MsoProgram> programs = (List<MsoProgram>) q.execute(channel.getKey());
		programs.size();
		List<MsoProgram> detached = (List<MsoProgram>)pm.detachCopyAll(programs);
		pm.close();
		return detached;
	}	
		
	public List<MsoProgram> findByChannelIdAndIsPublic(long channelId, boolean isPublic) {
		System.out.println(DebugLib.OUT + "channelId=" + channelId);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoChannel c = pm.getObjectById(MsoChannel.class, channelId);
		List<MsoProgram> programs;
		if (c != null) {
			Query q = pm.newQuery(MsoProgram.class);		
			q.setFilter("channelKey == channelKeyParam && isPublic == isPublicParam");
			q.declareParameters(Key.class.getName() + " channelKeyParam, boolean isPublicParam");
			programs = (List<MsoProgram>) q.execute(c.getKey(), isPublic);
			System.out.println(DebugLib.OUT + programs.size());
		} else {
			programs = new ArrayList<MsoProgram>();
		}
		pm.close();
		return programs;
	}

	public List<MsoProgram> findByChannelIdsAndIsPublic(String chIds, boolean isPublic) {
		String[] chStrSplit = chIds.split(","); 			
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Key[] channelKeys = new Key[chIds.length()];
		for (int i=0; i<chStrSplit.length; i++) {
			MsoChannel c = pm.getObjectById(MsoChannel.class, Integer.parseInt(chStrSplit[i]));
			channelKeys[i] = c.getKey();
		}
		Query q = pm.newQuery(MsoProgram.class, ":p.contains(channelKey)");
		List<MsoProgram> programs = new ArrayList((List<MsoProgram>) q.execute(Arrays.asList(channelKeys)));
		Iterator<MsoProgram> iter = programs.iterator();
		while(iter.hasNext()) {
		  MsoProgram p = iter.next();
		  if (!p.isPublic()) {
			  iter.remove();
		  }
		}
		pm.close();
		return programs;
	}
	
	// ============================================================
	// c.u.d
	// ============================================================
	//!!! transaction
	//!!! to force channel id, channel id should be one of the params
	public void create(MsoProgram program) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		System.out.println(DebugLib.OUT + program.getChannelId());
		MsoChannel c = pm.getObjectById(MsoChannel.class, program.getChannelId()); 
		program.setChannelKey(c.getKey());
		List<Key> seq = c.getProgramSeq();		
		pm.makePersistent(program);
		seq.add(program.getKey());
		int count = c.getProgramCount() + 1;
		c.setProgramCount(count);
		pm.makePersistent(c);
		pm.close();		
	}
	
	//!! enforce channelKey
	public void save(MsoProgram program) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		program.setUpdateDate(new Date());
		pm.makePersistent(program);
		pm.close();
	}
	
	public void saveAll(List<MsoProgram> programs, MsoChannel channel) {
		System.out.println("SAVEALL!!!!" + programs.size());
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		for (MsoProgram p : programs) {
			System.out.println(DebugLib.OUT + "saveall:" + p.getName());
			p.setChannelKey(channel.getKey());
			p.setUpdateDate(new Date());
		}
		pm.makePersistentAll(programs);
		pm.close();						
	}
	
	/*
	public void saveAllViaPodcast(PodcastItem podcasts[], MsoChannel channel) {
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		for (PodcastItem pod : podcasts) {
			System.out.println(DebugLib.OUT + pod.getTitle());
			MsoProgram p = new MsoProgram();
			p.setChannelKey(channel.getKey());
			p.setChannelId(channel.getId());			
			p.setName(pod.getTitle());
			if (pod.getDescription().length() > 500) {
				pod.setDescription(pod.getDescription().substring(0, 500));
			}
			p.setIntro(pod.getDescription());
			p.setType(MsoProgram.TYPE_VIDEO);
			p.setWebMFileUrl(pod.getEnclosure());
			p.setMpeg4FileUrl(pod.getEnclosure());
			p.setPublic(true);
			programs.add(p);
		}
		this.saveAll(programs, channel);
	}
	*/

	//!!!! transaction
	public void deleteAll(List<MsoProgram> programs) {
    	PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoChannel c = pm.getObjectById(MsoChannel.class, programs.get(0).getChannelKey());
		List<Key> seq = c.getProgramSeq();
		for (Key k : seq) {
			seq.remove(k);
		}
		pm.makePersistent(c);
    	pm.deletePersistentAll(programs);   		
    	pm.close();
	}
	
	//!!!transaction
	public void setOnOff(List<MsoProgram> programs, boolean isPublic) {
    	PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoChannel c = pm.getObjectById(MsoChannel.class, programs.get(0).getChannelKey());
		List<Key> seq = c.getProgramSeq();
		for (MsoProgram p : programs) {
			p.setPublic(isPublic);
			if (isPublic) {
				seq.add(p.getKey());
			} else {
				seq.remove(p.getKey());
			}
		}
		c.setProgramSeq(seq);
		pm.makePersistentAll(programs);
		pm.makePersistent(c);
    	pm.close();		
	}

	public void setOrders(List<MsoProgram> programs) {
    	PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoChannel c = pm.getObjectById(MsoChannel.class, programs.get(0).getChannelKey());
		List<Key> seq = c.getProgramSeq();
		seq.clear();		
		for (MsoProgram p : programs) {
			seq.add(p.getKey());
		}
		c.setProgramSeq(seq);
		pm.makePersistent(c);
    	pm.close();		
	}
	
	public void saveViaAws(AwsMessage msg) {
	   PersistenceManager pm = PMF.get().getPersistenceManager();
	   MsoProgram p = pm.getObjectById(MsoProgram.class, msg.getKey());	   
	   
	   if (msg.getType().equals(MsoProgram.VIDEO_WEBM)) {
		   System.out.println(DebugLib.OUT + MsoProgram.VIDEO_WEBM);
		   p.setWebMFileUrl(msg.getFileUrl());
	   } 
	   if (msg.getType().equals(MsoProgram.VIDEO_MPEG4)) {
		   System.out.println(DebugLib.OUT + MsoProgram.VIDEO_MPEG4);
		   p.setMpeg4FileUrl(msg.getFileUrl());
	   } 
	   System.out.println(DebugLib.OUT + "type is =" + msg.getType());
	   if (msg.getType().equals(MsoProgram.TYPE_SLIDESHOW)) {
		   System.out.println(DebugLib.OUT + "enter slideshow!!!!!");
		   Slideshow slide = new Slideshow();
		   slide.setSlides(msg.getSlideshow().getSlides());
		   slide.setAudios(msg.getSlideshow().getAudios());
		   slide.setSlideinfo(msg.getSlideshow().getSlideinfo());
		   String scriptStr = NnScriptLib.generateSlideScript(slide, msg.getFileUrl());
		   ProgramScript oldScript = p.getNnScript();
		   if (p.getNnScript() != null) {			   
			   System.out.println(DebugLib.OUT + "old script");
			   oldScript.setScript(new Text(scriptStr));
		   } else {
			   System.out.println(DebugLib.OUT + "new script");
			   ProgramScript script = new ProgramScript();
			   script.setScript(new Text(scriptStr));
			   p.setNnScript(script);
			   script.setProgram(p);			   
		   }
		   System.out.println(DebugLib.OUT + scriptStr);
	   }
	   p.setErrorCode(msg.getErrorCode());
	   Transaction tx = pm.currentTransaction();
	   try {
		   tx.begin();
		   pm.makePersistent(p);
		   tx.commit();
	   } finally {
		   if (tx.isActive()) { tx.rollback(); }
	   }
	   pm.close();		
	}
	
}