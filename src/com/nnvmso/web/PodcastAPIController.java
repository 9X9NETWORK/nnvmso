package com.nnvmso.web;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.json.*;
import com.nnvmso.lib.NnLib;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.ChannelManager;
import com.nnvmso.service.PodcastService;

/**
 * <p>Serves for Transcoding Service.</p>
 * <p>Url examples: (method name is used at the end of URL) <br/> 
 * http://hostname:port/podcastAPI/itemUpdate<br/>
 * http://hostname:port/podcastAPI/channelUpdate<br/>
 * <p/>
 * <p>Flow: <br/>
 * (1) nnsmvo notify transcoding service a new podcast channel <br/>
 * (2) transcoding service returns channel metadata via channelUpdate <br/>
 * (3) transcoding service returns program metadata via itemUpdate. (the episode is ready with MP4 format and basic metadata)<br/>
 * (4) transcoding service returns additional program metadata via itemUpdate. (webm is supported) <br/> 
 * </p>
 */
@Controller
@RequestMapping("podcastAPI")
public class PodcastAPIController {

	private ChannelManager channelMngr;
	private PodcastService podcastService;
	
	@Autowired
	public PodcastAPIController(ChannelManager channelMngr, PodcastService podcastService) {
		this.channelMngr = channelMngr;
		this.podcastService = podcastService;
	}
	
	/**
	 * Transcoding Service update Podcast Program information
	 * 
	 * @param podcastProgram podcastProgram returns in Json format <br/>
	 * {<br/>
	 * "action":"updateItem",<br/>
	 * "key":"channel_key_id",<br/>
	 * "errorCode":0, <br/>
	 * "errorReason":"error description", <br/>		
	 * "item": [ <br/>		
	 *   {<br/>
	 *     "title":"title", <br/>
	 *     "description":"description", <br/>
	 *     "pubDate","pubDate", <br/>
	 *     "enclosure":"video_url",	<br/>	
	 *     "type":"mp4",<br/>
	 *   }<br/>
	 *} 
	 * 
	 * @return keys keys include channel key and item key <br/>
	 *  {<br/>
 	 *     "key":"channel_key_id",<br/>
 	 *      "itemkey":"item_key_id",<br/>
     *  } 
	 */
	@RequestMapping("itemUpdate")
	public @ResponseBody PodcastResponse itemUpdate(@RequestBody PodcastProgram podcastProgram) {
		PodcastResponse resp = new PodcastResponse();				
		MsoProgram p = podcastService.createProgramViaPodcast(podcastProgram);
		if (p == null) {
			resp.setErrorCode(PodcastResponse.ERROR_CODE_FAIL);
			resp.setErrorCode(PodcastResponse.ERROR_MSG_FAIL);
		}
		System.out.println("Finish itemUpdate");
		return resp;
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
		//@todo check error code for status
		podcastService.saveChannelViaPodcast(channel, podcast);
		System.out.println("finish channelUpdate");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		//@todo return processing error code back to AWS
		return new ResponseEntity<String>("OK", headers, HttpStatus.OK);		
	}

	@RequestMapping("channelDelete")
	public ResponseEntity<String> channelDelete(@RequestParam(value="channelKey") String channelKey) {
		ChannelManager cMngr = new ChannelManager();
		cMngr.findByKey(channelKey);
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
