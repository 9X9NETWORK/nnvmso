package com.nnvmso.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Mapper;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tools.mapreduce.ConfigurationXmlUtil;
import com.google.appengine.tools.mapreduce.DatastoreInputFormat;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoProgramManager;
import com.nnvmso.service.NnStatusCode;
import com.nnvmso.service.NnStatusMsg;
import com.nnvmso.service.TranscodingService;
import com.nnvmso.task.mapper.DeleteAllMapper;
import com.nnvmso.web.json.transcodingservice.Channel;
import com.nnvmso.web.json.transcodingservice.ChannelInfo;
import com.nnvmso.web.json.transcodingservice.MapelChannel;
import com.nnvmso.web.json.transcodingservice.PostResponse;
import com.nnvmso.web.json.transcodingservice.Program;
import com.nnvmso.web.json.transcodingservice.ProgramInfo;
import com.nnvmso.web.json.transcodingservice.RtnChannel;
import com.nnvmso.web.json.transcodingservice.RtnProgram;

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
public class TranscodingServiceController {

	protected static final Logger log = Logger.getLogger(TranscodingServiceController.class.getName());
	
	private TranscodingService transcodingService;
	
	@Autowired
	public TranscodingServiceController(TranscodingService transcodingService) {
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
	public @ResponseBody PostResponse itemUpdate(@RequestBody RtnProgram rtnProgram, HttpServletRequest req) {
		log.info(rtnProgram.toString());
		PostResponse resp = new PostResponse(String.valueOf(NnStatusCode.SUCCESS), NnStatusMsg.successStr(Locale.ENGLISH));		
		try {
			ObjectMapper mapper = new ObjectMapper();
			byte[] data = mapper.writeValueAsBytes(rtnProgram);						
			QueueFactory.getQueue("podcastAPI").add(
					TaskOptions.Builder.withUrl("/podcastAPI/itemUpdateTask")
			        .payload(data, "application/json")
		    );
		} catch (Exception e) {
			e.printStackTrace();
		}		 
		return resp;
	}
	
	@RequestMapping("itemUpdateTask")
	public ResponseEntity<String> itemUpdateTask(@RequestBody RtnProgram rtnProgram, HttpServletRequest req) {
		log.info(rtnProgram.toString());
		PostResponse resp = new PostResponse(String.valueOf(NnStatusCode.SUCCESS), NnStatusMsg.successStr(Locale.ENGLISH));		
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
	public @ResponseBody ChannelInfo getChannelList(@RequestParam(value="page", required=false)String page, 
			                                    @RequestParam(value="msoName", required=false)String msoName,
			                                    HttpServletRequest req) {
		ChannelInfo info = new ChannelInfo();
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		MsoChannelManager channelMngr = new MsoChannelManager();		
		try {
			channels = channelMngr.findMaples();
			String[] transcodingEnv = transcodingService.getTranscodingEnv(req);		
			String callbackUrl = transcodingEnv[1];		
			List<Channel> cs = new ArrayList<Channel>();
			for (MsoChannel c : channels) {
				cs.add(new Channel(String.valueOf(c.getKey().getId()), 
						           c.getSourceUrl(), 
						           c.getTranscodingUpdateDate(), 
						           "0",
						           String.valueOf(c.getSubscriptionCount())));
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
	
	@RequestMapping("getEpisodes")
	public @ResponseBody ProgramInfo getEpisodes(@RequestParam(value="channel", required=false)String channel, HttpServletRequest req) {
		if (channel == null)
			return null;
		ProgramInfo info = new ProgramInfo(channel);
		try {		
			MsoProgramManager programMngr = new MsoProgramManager();
			List<MsoProgram> programs = programMngr.findAllByChannelId(Long.parseLong(info.getChannelId()));	
			String[] transcodingEnv = transcodingService.getTranscodingEnv(req);		
			String callback = transcodingEnv[1];		
			info.setCallback(callback);
			List<Program> ps = new ArrayList<Program>();
			for (MsoProgram p : programs) {
				ps.add(new Program(p.getAudioFileUrl(), p.getMpeg4FileUrl(), p.getOtherFileUrl(), p.getWebMFileUrl()));				
			}
			info.setErrorCode(String.valueOf(NnStatusCode.SUCCESS));
			info.setErrorReason("Success");
			info.setPrograms(ps);
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
		PostResponse resp = new PostResponse(String.valueOf(NnStatusCode.ERROR), NnStatusMsg.errorStr(Locale.ENGLISH));
		try {
			resp = transcodingService.updateChannel(podcast);
		} catch (Exception e) {
			resp = transcodingService.handleException(e);
		}
		return resp;
	}

	@RequestMapping("itemDelete")
	public  @ResponseBody PostResponse itemDelete(@RequestBody RtnChannel podcast) {
		log.info(podcast.toString());
		PostResponse resp = new PostResponse(String.valueOf(NnStatusCode.ERROR), NnStatusMsg.errorStr(Locale.ENGLISH));
		try {
			resp = transcodingService.deletePrograms(podcast);
		} catch (Exception e) {
			resp = transcodingService.handleException(e);
		}
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
	@RequestMapping("channelCreate")
	public  @ResponseBody MapelChannel channelCreate(@RequestBody MapelChannel mapel) {
		log.info(mapel.toString());
		MapelChannel resp = new MapelChannel();
		try {
			resp = transcodingService.createChannel(mapel);
		} catch (Exception e) {
			PostResponse post = transcodingService.handleException(e);
			mapel.setErrorCode(post.getErrorCode());
			mapel.setErrorReason(post.getErrorReason());
			NnLogUtil.logException((Exception) e);
			return resp;
		}
		return resp;
	}
	
	/**
	 * Need security checking 
	 */
	@RequestMapping("cleanPdr")
	public ResponseEntity<String> cleanPdr() throws IOException {
	    Configuration conf = new Configuration(false);
	    try {
	    	conf.setClass("mapreduce.map.class", DeleteAllMapper.class, Mapper.class);
	        conf.setClass("mapreduce.inputformat.class", DatastoreInputFormat.class, InputFormat.class);
	    	conf.set(DatastoreInputFormat.ENTITY_KIND_KEY, "PdrRaw");	    		    	
	    	String configXml = ConfigurationXmlUtil.convertConfigurationToXml(conf);
			QueueFactory.getDefaultQueue().add(					                              
					TaskOptions.Builder.withUrl("/mapreduce/start")
					.param("configuration", configXml)
			); 	
	    } catch (Exception e) {	
	    	log.info(e.getMessage());
	    }
		return NnNetUtil.textReturn("OK");
	}
	
	@RequestMapping("updateFbToken")
	public ResponseEntity<String> updateFbToken(@RequestParam(required=false) String access_token,
	                                            @RequestParam(required=false) Boolean real) throws IOException {
		try {
			if (access_token == null || access_token.length() == 0) {
				return NnNetUtil.textReturn("not empty");
			}
			transcodingService.updateFbToken(access_token, (real != null));
		} catch (Exception e) {
			transcodingService.handleException(e);
		}
		return NnNetUtil.textReturn("OK");
	}
	
}
