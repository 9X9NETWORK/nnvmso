package com.nnvmso.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.json.PodcastChannel;
import com.nnvmso.json.PodcastFeed;
import com.nnvmso.json.PodcastItem;
import com.nnvmso.json.PodcastProgram;
import com.nnvmso.lib.AwsLib;
import com.nnvmso.lib.NnLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;

@Service
public class PodcastService {
	protected static final Logger log = Logger.getLogger(PodcastService.class.getName());	
	
	public Properties getTranscodingServer() {
		Properties pro = new Properties();
		try {
			pro.load(AwsLib.class.getClassLoader().getResourceAsStream("podcast.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pro;
	}
	
	public MsoChannel findByPodcast(String podcastRss) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		    	
		Query q = pm.newQuery(MsoChannel.class);
		q.setFilter("podcast == podcastParam");
		q.declareParameters(Key.class.getName() + " podcastParam");
		@SuppressWarnings("unchecked")
		List<MsoChannel> channels = (List<MsoChannel>) q.execute(podcastRss);
		System.out.println(channels.size());
		pm.close();
		if (channels.size() > 0) {
			return channels.get(0);
		}		
		return null;		
	}
	
	public String[] getPodcastInfo(String urlStr) {
        URL url;
        String podcastInfo[] = new String[3];
        boolean retry = true;
        int counter = 1;
        while (retry) {
			try {
				//http GET
				url = new URL(urlStr);
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setDoOutput(true);
		        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
		        	System.out.println("podcast GET response not ok!" + connection.getResponseCode());	        	
		        }
		        String returnUrl = connection.getURL().toString();
		        if (returnUrl.equals(urlStr)) {
		        	retry = false;
		        } else {
		        	counter++;
		        	retry = counter > 2 ? false : true;
		        }
		        podcastInfo[0] = Integer.toString(connection.getResponseCode());
		        podcastInfo[1] = connection.getHeaderField("content-type");
		        podcastInfo[2] = connection.getURL().toString();
			} catch (Exception e) {
				retry = false;
				podcastInfo[0] = "400";
			}
        }
		return podcastInfo;
	}
	
	
	public MsoChannel saveChannelViaPodcast(MsoChannel channel, PodcastChannel podcast) {
		System.out.println("save via podcast");
		channel.setName(podcast.getTitle());
		if (podcast.getDescription()!= null && podcast.getDescription().length() > 500) {
			podcast.setDescription(podcast.getDescription().substring(0, 500));
		}
		String intro = podcast.getDescription();
		if (intro != null) {
			intro = intro.replaceAll("\t", " ");
			intro = intro.replaceAll("\r", " ");
			intro = intro.replaceAll("\n", " ");
		}
		channel.setIntro(intro);
		channel.setImageUrl(podcast.getImage());
		channel.setPublic(true);
		channel.setUpdateDate(podcast.getPubDate());
		new ChannelManager().save(channel);
		return channel;
	}

	public MsoChannel createChannelViaPodcast(PodcastChannel podcast, Mso mso) {
		MsoChannel channel = new MsoChannel();
		channel.setName(podcast.getTitle());
		if (podcast.getDescription()!= null && podcast.getDescription().length() > 500) {
			podcast.setDescription(podcast.getDescription().substring(0, 500));
		}
		channel.setIntro(podcast.getDescription());
		channel.setImageUrl(podcast.getImage());
		channel.setUpdateDate(podcast.getPubDate());
		channel.setType(MsoChannel.TYPE_PODCAST);
		new ChannelManager().create(channel, mso);
		return channel;
	}	
			
	public MsoProgram updateProgramViaPodcast(PodcastProgram podcastProgram) {
		ProgramManager programMngr = new ProgramManager();
		ChannelManager channelMngr = new ChannelManager();
		PodcastItem item = podcastProgram.getItems()[0]; //!!! for now there's only one item
		MsoProgram program = programMngr.findByStorageId(item.getItemId());
		MsoChannel channel = new ChannelManager().findByKey(podcastProgram.getKey());
		boolean isNew = false;
		if (program == null) {
			isNew = true;
			program = new MsoProgram();			
			program.setChannelKey(channel.getKey());
			program.setChannelId(channel.getId());
		} else {
			Cache cache = null;		
	        try {
	            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
	            cache = cacheFactory.createCache(Collections.emptyMap());
	        } catch (CacheException e) {
	            // ...
	        }        
	        if (cache.get(program.getChannelKey().getId()) != null) {
	        	cache.remove(program.getChannelKey().getId());
	        }
		}
		int status = Integer.parseInt(podcastProgram.getErrorCode());
		System.out.println("status=" + status);
		channel.setStatus(status);
		channelMngr.save(channel);
		
		if (status != 0) {
			return program;
		}
		
		if (item.getTitle() != null) {
			program.setName(item.getTitle());
		}
		String intro = item.getDescription();
		if (intro != null && intro.length() > 500) {
			intro = intro.replaceAll("\t", " ");
			intro = intro.replaceAll("\n", " ");
			intro = intro.replaceAll("\r", " ");
			item.setDescription(item.getDescription().substring(0, 500));
		}
		program.setIntro(intro);
		
		System.out.println("ori intro=" + item.getDescription() + "intro string=" + intro + ";program intro=" + program.getIntro());
		
		if (item.getThumbnail()!= null) {
			program.setImageUrl(item.getThumbnail());
		} else {
			program.setImageUrl(channel.getImageUrl());
		}
		if (item.getThumbnail()!= null) {
			program.setImageLargeUrl(item.getThumbnailLarge());
		} else {
			program.setImageUrl(channel.getImageUrl());
		}		
		program.setStorageId(item.getItemId());
		if (item.getMp4() != null) {			
			program.setMpeg4FileUrl(item.getMp4());
		}
		if (item.getWebm() != null) {
			program.setWebMFileUrl(item.getWebm());
		}
		if (item.getOther() != null) {
			program.setOtherFileUrl(item.getOther());
		}
		if (item.getAudio() != null) {
			program.setAudioFileUrl(item.getAudio());			
		}
		if (item.getDuration() != null) {
			program.setDuration(item.getDuration());
		}
		if (item.getAudio() != null) {
			program.setType(MsoProgram.TYPE_AUDIO);
		} else {
			program.setType(MsoProgram.TYPE_VIDEO);
		}
		if (item.getPubDate() != null) {
			System.out.println(item.getPubDate());
			Date theDate = new Date(Long.parseLong(item.getPubDate())*1000);						
			program.setUpdateDate(theDate);
		}
		program.setPublic(true);
		if (isNew) {
			new ProgramManager().create(program);
		} else {
			new ProgramManager().save(program);
		}
		return program;
	}
		
	public MsoChannel getDefaultPodcastChannel(String rssStr) {
		MsoChannel channel = new MsoChannel("podcast");
		channel.setPodcast(rssStr);
		channel.setImageUrl("/WEB-INF/../images/podcastDefault.jpg");
		channel.setName("Podcast Processing");
		channel.setPublic(false);
		return channel;
	}

	public void submitToTranscodingService(String channelKey, String rss, HttpServletRequest req) {		
		PodcastFeed feed = new PodcastFeed();
		feed.setKey(channelKey);
		feed.setRss(rss);		
		Properties pro = this.getTranscodingServer();
		String url = NnLib.getUrlRoot(req);
		String env = "";
		if (url.contains("9x9tvalpha")) {
			env = "alpha";
		} else if (url.contains("9x9tvbeta")){
			env = "beta";
		} else {
			env = "dev";
		}
		feed.setCallback(url);
		if (env.equals("dev")) {
			log.info("LOGGING: set callback url to dev_callback");
			feed.setCallback(pro.getProperty("dev_callback"));
		}
		String transcodingServer = pro.getProperty(env);
		log.info("LOGGING: podcast callbackUrl = " + feed.getCallback() + "; transcoding server = " + transcodingServer);
		NnLib.urlPostWithJson(transcodingServer, feed);
	}
	
}
