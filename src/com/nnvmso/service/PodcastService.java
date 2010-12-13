package com.nnvmso.service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

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

	public static String THIS_HOST = "alpha.9x9.tv";
	
	public static String TRANSCODING_SERVER_DEV = "http://awsapi.9x9cloud.tv/dev/podcatcher.php";
	public static String TRANSCODING_SERVER_ALPHA = "http://awsapi.9x9cloud.tv/alpha/podcatcher.php";
	public static String TRANSCODING_SERVER_BETA = "http://awsapi.9x9cloud.tv/beta/podcatcher.php";
	public static String TRANSCODING_SERVER_TW = "http://awsapi.9x9cloud.tv/tw/podcatcher.php";
	
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
		new ChannelManager().create(channel, mso);
		return channel;
	}	
			
	public MsoProgram createProgramViaPodcast(PodcastProgram podcastProgram) {
		ProgramManager programMngr = new ProgramManager();
		PodcastItem item = podcastProgram.getItems()[0]; //!!! for now there's only one item
		MsoProgram p = programMngr.findByStorageId(item.getItemId());
		if (p != null) { 
			return p;
		}		
		
		MsoChannel channel = new ChannelManager().findByKey(podcastProgram.getKey());
		p = new MsoProgram();
		
		p.setChannelKey(channel.getKey());
		p.setChannelId(channel.getId());		
		p.setName(item.getTitle());
		if (item.getDescription()!= null && item.getDescription().length() > 500) {
			item.setDescription(item.getDescription().substring(0, 500));
		}
		p.setIntro(item.getDescription());
		if (item.getThumbnail()!= null) {
			p.setImageUrl(item.getThumbnail());
		} else {
			p.setImageUrl(channel.getImageUrl());
		}
		if (item.getThumbnail()!= null) {
			p.setImageLargeUrl(item.getThumbnailLarge());
		} else {
			p.setImageUrl(channel.getImageUrl());
		}		
		p.setStorageId(item.getItemId());
		p.setMpeg4FileUrl(item.getMp4());
		p.setWebMFileUrl(item.getWebm());
		p.setOtherFileUrl(item.getOther());
		p.setAudioFileUrl(item.getAudio());
		if (item.getAudio() != null) {
			p.setType(MsoProgram.TYPE_AUDIO);
		} else {
			p.setType(MsoProgram.TYPE_VIDEO);
		}
		if (item.getPubDate() != null) {
			System.out.println(item.getPubDate());
			Date theDate = new Date(Long.parseLong(item.getPubDate())*1000);						
			p.setUpdateDate(theDate);
		}
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
	
	public void submitToTranscodingService(String channelKey, String rss, HttpServletRequest req) { 
		PodcastFeed feed = new PodcastFeed();
		feed.setKey(channelKey);
		feed.setRss(rss);
		String url = NnLib.getUrlRoot(req);
		feed.setCallback(url);
		String urlStr = TRANSCODING_SERVER_DEV;
		NnLib.urlPostWithJson(urlStr, feed);
	}
	
}
