package com.nncloudtv.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnStatusCode;
import com.nncloudtv.service.NnStatusMsg;
import com.nncloudtv.service.DepotService;
import com.nncloudtv.web.json.transcodingservice.Channel;
import com.nncloudtv.web.json.transcodingservice.ChannelInfo;
import com.nncloudtv.web.json.transcodingservice.PostResponse;
import com.nncloudtv.web.json.transcodingservice.RtnChannel;
import com.nncloudtv.web.json.transcodingservice.RtnProgram;


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
public class DepotController {

	protected static final Logger log = Logger.getLogger(DepotController.class.getName());
	
	private DepotService transcodingService;
	
	@Autowired
	public DepotController(DepotService transcodingService) {
		this.transcodingService = transcodingService;
	}
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/blank";
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
	public ResponseEntity<String> itemUpdate(@RequestBody RtnProgram rtnProgram, HttpServletRequest req) {
		log.info(rtnProgram.toString());
		PostResponse resp = new PostResponse(
				String.valueOf(NnStatusCode.ERROR), NnStatusMsg.getPlayerMsg(NnStatusCode.ERROR, Locale.ENGLISH));		
		try {
			resp = transcodingService.updateProgram(rtnProgram);
		} catch (Exception e) {
			resp = transcodingService.handleException(e);
		}
		log.info(resp.getErrorCode());
		return NnNetUtil.textReturn("OK");
	}
	
	
	/** 
	 * @param page
	 * @param msoName * indicates to retrieve all the channels
	 *                msoName indicates to retrieve a mso's Ipg
	 * @return channel list
	 */
	@RequestMapping("getChannelList")
	public @ResponseBody ChannelInfo getChannelList(
			  @RequestParam(value="page", required=false)String page, 
			                                    @RequestParam(value="msoName", required=false)String msoName,
			                                    @RequestParam(value="type", required=false)String type,
			                                    HttpServletRequest req) {
		ChannelInfo info = new ChannelInfo();
		List<NnChannel> channels = new ArrayList<NnChannel>();
		NnChannelManager channelMngr = new NnChannelManager();
		short srtType = 0;
		if (type== null)
			srtType = 0;//place holder
		else
			srtType = Short.parseShort(type);
		try {
			if (srtType == NnChannel.CONTENTTYPE_YOUTUBE_SPECIAL_SORTING) {
				channels = channelMngr.findByType(NnChannel.CONTENTTYPE_YOUTUBE_SPECIAL_SORTING);
			} else {
				channels = channelMngr.findMaples();
			}
			String[] transcodingEnv = transcodingService.getTranscodingEnv(req);		
			String callbackUrl = transcodingEnv[1];		
			List<Channel> cs = new ArrayList<Channel>();
			for (NnChannel c : channels) {
				cs.add(new Channel(String.valueOf(c.getId()), 
						           c.getSourceUrl(), 
						           c.getTranscodingUpdateDate(), 
						           "0",
						           String.valueOf(c.getSubscriptionCnt())));
			}
			log.info("maple channels:" + channels.size());
			info.setErrorCode(String.valueOf(NnStatusCode.SUCCESS));
			info.setErrorReason("Success");
			info.setChannels(cs);
			info.setCallBack(callbackUrl);
		} catch (Exception e) {
			PostResponse resp = transcodingService.handleException(e);
			info.setErrorCode(resp.getErrorCode());
			info.setErrorReason(resp.getErrorReason());
		}
		return info;
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
	public  @ResponseBody PostResponse channelUpdate(@RequestBody RtnChannel podcast) {
		log.info(podcast.toString());
		PostResponse resp = new PostResponse(
				String.valueOf(NnStatusCode.ERROR), NnStatusMsg.getPlayerMsg(NnStatusCode.ERROR, Locale.ENGLISH));
		try {
			resp = transcodingService.updateChannel(podcast);
		} catch (Exception e) {
			resp = transcodingService.handleException(e);
		}
		return resp;
	}

	
}
