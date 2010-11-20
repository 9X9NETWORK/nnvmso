package com.nnvmso.service;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.json.PodcastChannel;
import com.nnvmso.json.PodcastFeed;
import com.nnvmso.json.PodcastItem;
import com.nnvmso.json.PodcastProgram;
import com.nnvmso.lib.NnLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;

@Service
public class PodcastService {
	public MsoChannel findByPodcast(String podcastRss) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		    	
		Query q = pm.newQuery(MsoChannel.class);
		q.setFilter("podcast == podcastParam");
		q.declareParameters(Key.class.getName() + " podcastParam");
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
		channel.setIntro(podcast.getDescription());
		channel.setImageUrl(podcast.getImage());
		channel.setPublic(true);
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
		new ChannelManager().create(channel, mso);
		return channel;
	}	
	
	public void saveProgramViaPodcast(PodcastProgram podcastProgram) {
		PodcastItem item = podcastProgram.getItem();
		ProgramManager pMngr = new ProgramManager();
		MsoProgram p = pMngr.findByKey(podcastProgram.getItemKey());
		if (item.getType().equals(MsoProgram.VIDEO_MPEG4)) {
			p.setMpeg4FileUrl(item.getEnclosure());
		}
		if (item.getType().equals(MsoProgram.VIDEO_WEBM)) {
			p.setWebMFileUrl(item.getEnclosure());
		}		
		p.setPublic(true);
		pMngr.save(p);
	}
	
	public MsoProgram createProgramViaPodcast(PodcastProgram podcastProgram) {
		MsoChannel channel = new ChannelManager().findByKey(podcastProgram.getKey());
		MsoProgram p = new MsoProgram();
		p.setChannelKey(channel.getKey());
		p.setChannelId(channel.getId());			
		PodcastItem item = podcastProgram.getItem();
		p.setName(item.getTitle());
		if (item.getDescription()!= null && item.getDescription().length() > 500) {
			item.setDescription(item.getDescription().substring(0, 500));
		}
		p.setImageUrl(channel.getImageUrl());
		p.setIntro(item.getDescription());
		p.setType(MsoProgram.TYPE_VIDEO);
		if (item.getType().equals(MsoProgram.VIDEO_MPEG4)) {
			p.setMpeg4FileUrl(item.getEnclosure());
		}
		if (item.getType().equals(MsoProgram.VIDEO_WEBM)) {
			p.setWebMFileUrl(item.getEnclosure());
		}						
		p.setChannelKey(channel.getKey());
		p.setChannelId(channel.getKey().getId());
		p.setPublic(true);
		new ProgramManager().create(p);
		return p;
	}
		
	public MsoChannel getDefaultPodcastChannel(String rssStr) {
		MsoChannel channel = new MsoChannel("podcast");
		channel.setPodcast(rssStr);
		channel.setImageUrl("/WEB-INF/../images/podcastDefault.jpg");
		channel.setName("Podcast Processing");
		channel.setPublic(false);
		return channel;
	}
	
	public void submitToTranscodingService(String key, String rss) { 
		PodcastFeed feed = new PodcastFeed();
		feed.setKey(key);
		feed.setRss(rss); 
		System.out.println("Podcast post from player:" + feed.getRss());
		String urlStr = "http://awsapi.9x9cloud.tv/dev/podcatcher.php";
		NnLib.urlPostWithJson(urlStr, feed);
	}
	
}
