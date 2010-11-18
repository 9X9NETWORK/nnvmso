package com.nnvmso.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.json.PodcastChannel;
import com.nnvmso.json.PodcastKeys;
import com.nnvmso.json.PodcastProgram;
import com.nnvmso.lib.NnLib;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.ChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.ProgramManager;

/**
 * <p>Serves for Transcoding Service.</p>
 * <p>Url examples: (notice method name is used at the end of URL) <br/> 
 * http://hostname:port/podcastAPI/itemUpdate<br/>
 * http://hostname:port/podcastAPI/channelUpdate<br/>
 * <p/>
 *
 */
@Controller
@RequestMapping("podcastAPI")
public class PodcastAPIController {

	public static String TRANSCODING_SERVER = "http://awsapi.9x9cloud.tv/api/podpares.php";
	private ChannelManager channelMngr;
	private ProgramManager programMngr;
	private MsoManager msoMngr;
	
	@Autowired
	public PodcastAPIController(ChannelManager channelMngr, MsoManager msoMngr, ProgramManager programMngr) {
		this.channelMngr = channelMngr;
		this.programMngr = programMngr;
		this.msoMngr = msoMngr;
	}
	
	/**
	 * Transcoding Service update Podcast Program information
	 * 
	 * @param podcastProgram podcastProgram returns in Json format
	 * @return keys keys include channel key and item key
	 */
	
	@RequestMapping("itemUpdate")
	public @ResponseBody PodcastKeys itemUpdate(@RequestBody PodcastProgram podcastProgram) {
		PodcastKeys keys = new PodcastKeys();
		if (podcastProgram.getAction().equals(PodcastProgram.ACTION_UPDATE_ITEM)) {
			System.out.println("update item:" + podcastProgram.getKey());
			MsoProgram p = programMngr.createViaPodcast(podcastProgram);
			keys.setKey(podcastProgram.getKey());
			keys.setItemKey(NnLib.getKeyStr(p.getKey()));			
		} else {
			System.out.println("update enclosure:" + podcastProgram.getKey() + ";" + podcastProgram.getItem().getItemKey());
			programMngr.saveViaPodcast(podcastProgram);
		}
		System.out.println("Finish itemUpdate");
		return keys;
	}
	
	/**
	 * Transcoding service update Podcast Channel Information
	 * 
	 * @param podcast podcast in json type <br/>
	 * {  <br/>
	 *    "action":"updateChannel", <br/>
	 *    "key":"channel_key_id", <br/>
	 *    "title":"channel_title", <br/>
	 *    "description":"channel_description", <br/>    
	 *    "pubDate":"channel_pubDate", <br/>
	 *    "image":"channel_thumbnail",<br/>   
	 *    "errorCode":0, <br/>
	 *    "errorReason":"error description" <br/>     
	 * } 
	 */
	@RequestMapping("channelUpdate")
	public ResponseEntity<String> channelUpdate(@RequestBody PodcastChannel podcast) {
		System.out.println("update:" + podcast.getKey());
		MsoChannel channel = channelMngr.findByKey(podcast.getKey());
		channelMngr.saveViaPodcast(channel, podcast);
		System.out.println("finish channelUpdate");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>("OK", headers, HttpStatus.OK);
		
	}

	/*
	//dysfunction
	@RequestMapping("batch_create")
	public String batchCreate(@RequestBody PodcastChannel podcast) {
		Mso mso = msoMngr.findByEmail("default_mso@9x9.com");
		MsoChannel channel = channelMngr.createViaPodcast(podcast, mso);		
		//programMngr.saveAllViaPodcast(podcast.getItems(), channel);		
		return "";
	}
	*/

}
