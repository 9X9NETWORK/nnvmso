package com.nnvmso.service;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Text;
import com.nnvmso.lib.DebugLib;
import com.nnvmso.lib.NnScriptLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.AwsMessage;
import com.nnvmso.model.Content;
import com.nnvmso.model.ContentScript;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.Slideshow;

@Service
public class ContentService {
	public Content findByKey(String keyStr) {
	   PersistenceManager pm = PMF.get().getPersistenceManager();
	   Content c = pm.getObjectById(Content.class, keyStr);
	   pm.close();
	   return c;
	}
	
	public void update(AwsMessage msg) {
	   PersistenceManager pm = PMF.get().getPersistenceManager();
	   Content c = pm.getObjectById(Content.class, msg.getKey());
	   System.out.println(DebugLib.OUT + c.getName());
	   
	   if (!msg.getType().equals(MsoProgram.VIDEO_WEBM)) {
		   c.setWebMFileUrl(msg.getFileUrl());
	   } 
	   if (!msg.getType().equals(MsoProgram.VIDEO_MPEG4)) {
		   c.setMpeg4FileUrl(msg.getFileUrl());
	   } 
	   if (!msg.getToken().equals(MsoProgram.TYPE_SLIDESHOW)) {
		   System.out.println(DebugLib.OUT + "slideshow");
		   Slideshow slide = new Slideshow();
		   slide.setSlides(msg.getSlideshow().getSlides());
		   slide.setAudios(msg.getSlideshow().getAudios());
		   slide.setSlideinfo(msg.getSlideshow().getSlideinfo());
		   String scriptStr = NnScriptLib.generateSlideScript(slide, msg.getFileUrl());
		   ContentScript oldScript = c.getScript();
		   if (c.getScript() != null) {			   
			   System.out.println(DebugLib.OUT + "old script");
			   oldScript.setScript(new Text(scriptStr));
		   } else {
			   System.out.println(DebugLib.OUT + "new script");
			   ContentScript script = new ContentScript();
			   script.setScript(new Text(scriptStr));			   
			   c.setScript(script);
			   script.setContent(c);
		   }
		   System.out.println(DebugLib.OUT + scriptStr);		   
	   }
	   c.setErrorCode(msg.getErrorCode());
	   Transaction tx = pm.currentTransaction();
	   try {
		   tx.begin();
		   pm.makePersistent(c);
		   tx.commit();
	   } finally {
		   if (tx.isActive()) { tx.rollback(); }
	   }
	   pm.close();
	}
	
	public void create(Content content) {
	   PersistenceManager pm = PMF.get().getPersistenceManager();
	   pm.makePersistent(content);
	   pm.close();				   				
	}
}
