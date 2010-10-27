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
import com.nnvmso.lib.DebugLib;
import com.nnvmso.lib.NnScriptLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.AwsMessage;
import com.nnvmso.model.Content;
import com.nnvmso.model.ContentScript;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.ProgramScript;
import com.nnvmso.model.Slideshow;

@Service
public class ProgramService {
	// ============================================================
	// find
	// ============================================================	
	public MsoProgram findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoProgram program = pm.getObjectById(MsoProgram.class, id);
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
		MsoChannel c = pm.getObjectById(MsoChannel.class, program.getChannelId()); 
		program.setChannelKey(c.getKey());
		program.setPublic(false);
		List<Key> seq = c.getProgramSeq();		
		pm.makePersistent(program);
		seq.add(program.getKey());
		pm.makePersistent(c);
		pm.close();		
	}
	
	//!! enforce channelKey
	public void save(MsoProgram program) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		System.out.println(DebugLib.OUT + program.getKey());
		program.setUpdateDate(new Date());
		pm.makePersistent(program);
		pm.close();						
	}
	
	public void saveAll(List<MsoProgram> programs) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		for (MsoProgram p : programs) {
			p.setUpdateDate(new Date());
		}
		pm.makePersistentAll(programs);
		pm.close();						
	}
	
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
	   System.out.println(DebugLib.OUT + p.getName());
	   
	   if (msg.getType().equals(MsoProgram.VIDEO_WEBM)) {
		   System.out.println(DebugLib.OUT + MsoProgram.VIDEO_WEBM);
		   p.setWebMFileUrl(msg.getFileUrl());
	   } 
	   if (msg.getType().equals(MsoProgram.VIDEO_MPEG4)) {
		   System.out.println(DebugLib.OUT + MsoProgram.VIDEO_MPEG4);
		   p.setMpeg4FileUrl(msg.getFileUrl());
	   } 
	   if (msg.getType().equals(MsoProgram.TYPE_SLIDESHOW)) {
		   System.out.println(DebugLib.OUT + "enter slideshow!!!!!");
		   Slideshow slide = new Slideshow();
		   slide.setSlides(msg.getSlideshow().getSlides());
		   slide.setAudios(msg.getSlideshow().getAudios());
		   slide.setSlideinfo(msg.getSlideshow().getSlideinfo());
		   String scriptStr = NnScriptLib.generateSlideScript(slide, msg.getFileUrl());
		   ProgramScript oldScript = p.getScript();
		   if (p.getScript() != null) {			   
			   System.out.println(DebugLib.OUT + "old script");
			   oldScript.setScript(new Text(scriptStr));
		   } else {
			   System.out.println(DebugLib.OUT + "new script");
			   ProgramScript script = new ProgramScript();
			   script.setScript(new Text(scriptStr));
			   p.setScript(script);
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