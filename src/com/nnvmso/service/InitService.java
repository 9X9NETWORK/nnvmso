package com.nnvmso.service;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.nnvmso.lib.NnLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.*;

@Service
public class InitService {
	public void createPodcastChannels(HttpServletRequest req) {
		PodcastService service = new PodcastService();
		Mso mso = new MsoManager().findByEmail("default_mso@9x9.com");		

		String rss1 = "http://www.mevio.com/feeds/geekbrief.xml";
		MsoChannel channel1 = service.getDefaultPodcastChannel(rss1);
		MsoChannel saved1 = new ChannelManager().create(channel1, mso);
		service.submitToTranscodingService(NnLib.getKeyStr(saved1.getKey()), rss1, req);
		
		String rss2 = "http://feeds.feedburner.com/TheBigD";
		MsoChannel channel2 = service.getDefaultPodcastChannel(rss2);
		MsoChannel saved2 = new ChannelManager().create(channel2, mso);
		service.submitToTranscodingService(NnLib.getKeyStr(saved2.getKey()), rss2, req);				

		String rss3 = "http://shortfilms.libsyn.com/rss";
		MsoChannel channel3 = service.getDefaultPodcastChannel(rss3);
		MsoChannel saved3 = new ChannelManager().create(channel3, mso);
		service.submitToTranscodingService(NnLib.getKeyStr(saved3.getKey()), rss3, req);				

		String rss4 = "http://soreallife.libsyn.com/rss";
		MsoChannel channel4 = service.getDefaultPodcastChannel(rss4);
		MsoChannel saved4 = new ChannelManager().create(channel4, mso);
		service.submitToTranscodingService(NnLib.getKeyStr(saved4.getKey()), rss4, req);				

		String rss5 = "http://feeds.pbs.org/pbs/frontlineworld";
		MsoChannel channel5 = service.getDefaultPodcastChannel(rss5);
		MsoChannel saved5 = new ChannelManager().create(channel5, mso);
		service.submitToTranscodingService(NnLib.getKeyStr(saved5.getKey()), rss5, req);				
		
		
	}
	public void createChannels() {
		MsoManager msoMngr = new MsoManager();
		ChannelManager channelMngr = new ChannelManager();
		Mso mso = msoMngr.findByEmail("default_mso@9x9.com");		
		MsoChannel c1 = new MsoChannel("Fox News");
		c1.setImageUrl("http://zoo.atomics.org/video/thumbnails/bee-gees-stayin-alive.jpg");
		c1.setType(MsoChannel.TYPE_MSO);
		c1.setPublic(true);
		channelMngr.create(c1, mso);
		ProgramManager pMngr = new ProgramManager();
		MsoProgram p = new MsoProgram("news1");
		p.setChannelId(c1.getId());
		p.setImageUrl("http://zoo.atomics.org/video/thumbnails/bee-gees-stayin-alive.jpg");
		p.setMpeg4FileUrl("http://zoo.atomics.org/video/jukebox/bee-gees-stayin-alive.webm");
		p.setWebMFileUrl("http://zoo.atomics.org/video/jukebox/bee-gees-stayin-alive.webm");	
		p.setPublic(true);
		pMngr.create(p);

		MsoChannel c2 = new MsoChannel("ABc News");
		c2.setImageUrl("http://zoo.atomics.org/video/thumbnails/bee-gees-stayin-alive.jpg");
		c2.setType(MsoChannel.TYPE_MSO);
		c2.setPublic(true);
		channelMngr.create(c2, mso);
		MsoProgram p1 = new MsoProgram("news1");
		p1.setChannelId(c2.getId());
		p1.setImageUrl("http://zoo.atomics.org/video/thumbnails/bee-gees-stayin-alive.jpg");
		p1.setMpeg4FileUrl("http://zoo.atomics.org/video/jukebox/bee-gees-stayin-alive.webm");
		p1.setWebMFileUrl("http://zoo.atomics.org/video/jukebox/bee-gees-stayin-alive.webm");	
		p1.setPublic(true);
		
		c1.setProgramCount(2);
		c2.setProgramCount(3);
		
		pMngr.create(p1);
		
	}
	
	public void deleteAll() {
		DbDumper dumper = new DbDumper();
		//delete player before mso
		List list = dumper.findAll(Player.class, "createDate");
		dumper.deleteAll(Player.class, list);
		//the rest independent entities
		list = dumper.findAll(Mso.class, "createDate");
		dumper.deleteAll(Mso.class, list);
		list = dumper.findAll(NnUser.class, "createDate");
		dumper.deleteAll(NnUser.class, list);	
		list = dumper.findAll(MsoChannel.class, "createDate");
		dumper.deleteAll(MsoChannel.class, list);
		list = dumper.findAll(MsoProgram.class, "createDate");
		dumper.deleteAll(MsoProgram.class, list);
		list = dumper.findAll(Subscription.class, "createDate");
		dumper.deleteAll(Subscription.class, list);
	}
	
	private void createInitialData() {
		MsoManager msoMngr = new MsoManager();
		NnUserManager userMngr = new NnUserManager();
		//two msos
		Mso mso = new Mso("default_mso@9x9.com", "a");
		mso.setPassword("9x99x9");
		msoMngr.create(mso);
		Mso aws = new Mso("aws@9x9.com", "aws");
		aws.setPassword("9x99x9");		
		msoMngr.create(aws);
		//a user
		NnUser user = new NnUser("default_user@9x9.com");
		user.setPassword("9x99x9");
		userMngr.save(user, mso);
		//a player
		Player player = new Player();
		player.setCode(new Text("<h>hello</h>"));
		PlayerManager playerService = new PlayerManager();
		playerService.create(player, mso);
		//a aws channel for testing
		MsoChannel channel = new MsoChannel("AWS");
		channel.setImageUrl("/WEB-INF/../images/thumb_noImage.jpg");
		ChannelManager channelMngr = new ChannelManager();
		channelMngr.create(channel, aws);
		//a system channel with one program
		MsoChannel system = new MsoChannel("System Channel");
		system.setImageUrl("/WEB-INF/../images/logo_9x9.png");
		system.setType(MsoChannel.TYPE_SYSTEM);
		system.setPublic(true);
		channelMngr.create(system, mso);
		ProgramManager pMngr = new ProgramManager();
		MsoProgram p = new MsoProgram("System Program");
		p.setChannelId(system.getId());
		p.setIntro("Welcome to 9x9");
		p.setImageUrl("/WEB-INF/../images/logo_9x9.png");		
		p.setMpeg4FileUrl("http://s3.amazonaws.com/mp4_9x9/default.mp4");
		p.setWebMFileUrl("http://s3.amazonaws.com/webm9x9/default.webm");	
		p.setPublic(true);
		pMngr.create(p);		
	}
	
	public void alter() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		//get all the keys
		PersistenceManager pm = PMF.get().getPersistenceManager();		    	
		Query q = pm.newQuery(MsoChannel.class);
		List<MsoChannel> channels = (List<MsoChannel>) q.execute();		
		List<Key> keys = new ArrayList<Key>();
		for (MsoChannel c : channels) {
			keys.add(c.getKey());
		}
		pm.close();
		datastore.delete(keys);		
		
		System.out.println("channel size=" + channels.size());
		deleteAll();
	}
	
	public void alterInit() {
		alter();
		createInitialData();
	}
	
	public void init() {
		deleteAll();
		createInitialData();
	}
}