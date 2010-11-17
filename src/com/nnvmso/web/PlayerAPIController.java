package com.nnvmso.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.json.PodcastFeed;
import com.nnvmso.lib.*;
import com.nnvmso.model.*;
import com.nnvmso.service.*;

@Controller
@RequestMapping("playerAPI")
public class PlayerAPIController {

	/* ==========  CATEGORY: ACCOUNT RELATED ========== */
	/**
	 * User login.
	 * 
	 * <p>Only POST operation is supported.</p>
	 * 
	 * @param email email
	 * @param password password
	 * 
	 * @return A string of a return code and a return message, tab delimited. <br/>
	 *         If signup succeeds, the return message will be the user key, otherwise it returns error message.<br/>
	 *         Example: "0	Success", "100	Login Failed".
	 */
	@RequestMapping(value="login", method=RequestMethod.POST)
	public ResponseEntity<String> login(HttpServletRequest req, HttpServletResponse resp) {
		String email = req.getParameter("email");
		String pwd = req.getParameter("password");
		NnUser user = new NnUserManager().nnUserAuthenticated(email, pwd);
		String output = "";
		if (user == null) {
			output = PlayerAPI.CODE_LOGIN_FAILED + "\t" + PlayerAPI.PLAYER_CODE_LOGIN_FAILED;
		} else {
			new NnUserManager().setUserCookie(resp, NnLib.getKeyStr(user.getKey()));
			output = PlayerAPI.CODE_SUCCESS + "\t" + NnLib.getKeyStr(user.getKey());
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);				
	}
	
	/**
	 *  User signup.
	 *  
	 *  <p>only POST operation is supported.</p>
	 *  
	 *  @param email email
	 *  @param password password
	 *  @param name display name
	 *  @return A string of a return code and a return message, tab delimited. <br/>
	 *  	    If signup succeeds, the return message will be the user key, otherwise it returns error message.<br/>
	 *          Example: "0	aghubmUydm1zb3IMCxIGTm5Vc2VyGDkM", "200	Missing Params".
	 */
	@RequestMapping(value="signup", method=RequestMethod.POST)
    public ResponseEntity<String> signup(HttpServletRequest req, HttpServletResponse resp) {
		String output = "";
		if (req.getParameter("email") == null || req.getParameter("password") == null || req.getParameter("name") == null ||
			req.getParameter("email").length() == 0 || req.getParameter("password").length() == 0 || req.getParameter("name").length() == 0) {
				output = PlayerAPI.CODE_MISSING_PARAMS + "\t" + PlayerAPI.PLAYER_CODE_MISSING_PARAMS;
		} else { 
			NnUser newUser = new NnUser(req.getParameter("email"));
			newUser.setPassword(req.getParameter("password"));
			newUser.setName(req.getParameter("name"));		
			NnUserManager userMngr = new NnUserManager();
			NnUser user = userMngr.createViaPlayer(newUser);
			userMngr.setUserCookie(resp, NnLib.getKeyStr(user.getKey()));
			output = PlayerAPI.CODE_SUCCESS + "\t" + NnLib.getKeyStr(user.getKey());
		}
		//return
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}
	
	/**
	 * Register a guest account
	 * 
	 * @return A string of return code and a user key, tab delimited.
	 * 	       Failed operation will return return code and error message.<br/>
	 * 	       Example: "0	aghubmUydm1zb3IMCxIGTm5Vc2VyGDkM", "3	Fatal Error".
	 */
	@RequestMapping(value="guestRegister")
	public ResponseEntity<String> guestRegister(HttpServletResponse resp) {
		NnUser guest = new NnUser("guest@9x9.com");
		guest.setPassword("guest");
		guest.setName("guest");
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.createViaPlayer(guest);		
		userMngr.setUserCookie(resp, NnLib.getKeyStr(user.getKey()));
		//return
		String output = PlayerAPI.CODE_SUCCESS + "\t" + NnLib.getKeyStr(user.getKey());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}
	
	/* ==========  CATEGORY: CHANNEL BROWSING ========== */		
	/**
	 * Browse all the on-air channels.
	 * 
	 * @return A string of all the channels' information. <br/>
	 * 	       Each channel is \n delimited. Each channel's information is tab delimited.<br/>
	 *         Channel info has channel id, channel name, channel image url. 
	 */
	@RequestMapping(value="channelBrowse")
	public ResponseEntity<String> channelBrowse() {
		ChannelManager channelMngr = new ChannelManager();
		List<MsoChannel> channels = channelMngr.findAllPublic();
		String output ="";
		for (MsoChannel c:channels) {
			String[] ori = {Short.toString(c.getSeq()), String.valueOf(c.getKey().getId()), c.getName(), c.getImageUrl()};
			output = output + PlayerLib.getTabDelimitedStr(ori);			
			output = output + "\n";
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
	}

	/*
	@RequestMapping(value="channelBrowseByUser")
	public ResponseEntity<String> channelBrowseByUser(@RequestParam String userKey) {
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findByKey(userKey);
		
		ChannelManager channelMngr = new ChannelManager();
		List<MsoChannel> channels = channelMngr.findAllPublic();
		String output ="";
		for (MsoChannel c:channels) {
			String[] ori = {Short.toString(c.getSeq()), String.valueOf(c.getKey().getId()), c.getName(), c.getImageUrl()};
			output = output + PlayerLib.getTabDelimitedStr(ori);			
			output = output + "\n";
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
	}
	*/
		
	/**
	 * Get all of a user's subscriptions. 
	 * 
	 * @param user user's unique identifier
	 * @return A string of all of the user's subscribed channels' information.<br/>
	 * 	       Each channel is \n delimited. Each channel's information is tab delimited.<br/>  
	 *         Channel info has grid id, channel id, channel name, channel image url.
	 */
	@RequestMapping(value="channelLineup")
	public ResponseEntity<String> channelLineup(@RequestParam(value="user") String user) {
		SubscriptionManager subMngr = new SubscriptionManager();
		NnUserManager userService = new NnUserManager();
		NnUser foundUser = userService.findByKey(user);
		List<MsoChannel> channels = subMngr.findSubscribedChannels(foundUser);
		String output = "";
		for (MsoChannel c:channels) {
			String[] ori = {Short.toString(c.getGrid()), String.valueOf(c.getKey().getId()), c.getName(), c.getImageUrl()};
			output = output + PlayerLib.getTabDelimitedStr(ori);			
			output = output + "\n";
		}				
		//return
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
		
	}
		
	/**
	 * Get a user's subscribed channels. 
	 * 
	 * Example: http://localhost:8888/player/channelLineup?user=aghubmUzdm1zb3INCxIGTm5Vc2VyGKsEDA
	 *  
	 * @param  user user's unique key
	 * @return Channel info. <br/>
	 * 		   Fields are tab delimited. <br/>
	 * 		   Return sequence are: sequence, ChannelId, ChannelName, ChannelThumbnailUrl. <br/> 
	 */
	/*
	@RequestMapping(value="channelLineupByMso")
	public ResponseEntity<String> channelLineupByMso(@RequestParam(value="user") String user) {
		SubscriptionManager subMngr = new SubscriptionManager();
		NnUserManager userService = new NnUserManager();
		NnUser found = userService.findByKey(user);
		subMngr.subscribe(found); 
		List<MsoChannel> channels = subMngr.findSubscribedChannels(found);
		String output = "";
		for (MsoChannel c:channels) {
			String[] ori = {Short.toString(c.getSeq()), String.valueOf(c.getKey().getId()), c.getName(), c.getImageUrl()};
			output = output + PlayerLib.getTabDelimitedStr(ori);			
			output = output + "\n";
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
	}
	*/
		
	/**
	 * Get program script based on program id.
	 * 
	 * <p>Example: http://localhost:8888/player/nnscript?program=566</p>  
	 *  
	 * @param program program id
	 * @return 9x9script 
	*/
	@RequestMapping("nnscript")
	public ResponseEntity<String> nnScript(@RequestParam(value="program") long programId)
	{
		ProgramManager programMngr = new ProgramManager();
		MsoProgram program = programMngr.findById(programId);		
		String script = program.getNnScript().getScript().getValue();		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(script, headers, HttpStatus.OK);
	}
	
	/**
	 * Get program information based on query criteria.
	 * 
	 * <p>
	 * Examples: <br/>
	 *  http://localhost:8888/player/programInfo?channel=*&user=aghubmUzdm1zb3INCxIGTm5Vc2VyGKsEDA <br/>
	 *  http://localhost:8888/player/programInfo?channel=153,158 <br/>
	 *  http://localhost:8888/player/programInfo?channel=153 <br/>
	 * </p> 
	 *  
	 * @param  channel (1)Could be *, all the programs, e.g. channel=* (user is required for wildcard query). 
	 * 		           (2)Could be a channel Id, e.g. channel=1 <br/>
	 * 		           (3)Could be list of channels, e.g. channels = 34,35,36.
	 * @param  user user's unique identifier, it is required for wildcard query 
	 * @return A string of all the programs' info that met the query criteria. <br/>
	 * 		   Each program is separate by carriage return. Each program's information is tab delimited.<br/>
	 * 		   Program info has: channelId, programId, programName, programType, programThumbnailUrl, contentFileUrl
	 */				
	@RequestMapping("programInfo")	
	public ResponseEntity<String> programInfo(@RequestParam(value="channel") String channel,
									        @RequestParam(value="user", required = false) String user,
									        HttpServletRequest req) {
		ProgramManager programMngr = new ProgramManager();
		String[] chStrSplit = channel.split(",");
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		if (channel.equals("*")) {
			NnUserManager userService = new NnUserManager();
			SubscriptionManager sService = new SubscriptionManager();
			NnUser found = userService.findByKey(user);
			programs = sService.findSubscribedPrograms(found);
		} else if (chStrSplit.length > 1) {			
			programs = programMngr.findByChannelIdsAndIsPublic(channel, true);
		} else {
			long chId = Integer.parseInt(channel);
			programs = programMngr.findByChannelIdAndIsPublic(chId, true);
		}	
		String output = "";		
		for (MsoProgram p : programs) {
			String file = p.getWebMFileUrl();
			System.out.println("hostname=" + req.getLocalAddr() + ";" + req.getLocalPort() + ";" + req.getRequestURI());
			
			if (p.getType().equals(MsoProgram.TYPE_SLIDESHOW)) {
				file = "/player/nnscript?program=" + p.getId();
			}
			String[] ori = {String.valueOf(p.getChannelId()), String.valueOf(p.getKey().getId()), p.getName(), p.getType(), p.getImageUrl(), file};
			output = output + PlayerLib.getTabDelimitedStr(ori);
			output = output + "\n";
		}		
		//return output;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}
	
	/* ==========  CATEGORY: CHANNEL CREATE ========== */	
	/**
	 * Generate a channel based on a podcast RSS feed.
	 * 
	 * <p>Only POST operation is supported.</p>
	 * 
	 * @param podcastRSS a podcast RSS feed
	 * @param user user's unique identifier
	 * @param grid grid location
	 * 
	 * @return A string of return code and channel id, tab delimited. <br/>
	 * 	       Failed operation will return return code and error message.
	 */
	@RequestMapping(value="podcastRSS", method=RequestMethod.POST)
	public ResponseEntity<String> podcastSubmit(HttpServletRequest req) {
		String output = "";
		if (req.getParameter("podcastRSS") == null || req.getParameter("user") == null || req.getParameter("grid") == null ||
			req.getParameter("podcastRSS").length() == 0 || req.getParameter("user").length() == 0 || req.getParameter("grid").length() == 0) {
			output = PlayerAPI.CODE_MISSING_PARAMS + "\t" + PlayerAPI.PLAYER_CODE_MISSING_PARAMS;
		} else {
			Mso mso = new MsoManager().findByEmail("default_mso@9x9.com");
			MsoChannel channel = new MsoChannel("podcast");
			channel.setImageUrl("/WEB-INF/../images/thumb_noImage.jpg");
			channel.setPublic(false);
			MsoChannel saved = new ChannelManager().create(channel, mso);

			PodcastFeed feed = new PodcastFeed();
			feed.setKey(NnLib.getKeyStr(saved.getKey()));
			feed.setRss(req.getParameter("podcastRss")); 
			String urlStr = "http://awsapi.9x9cloud.tv/api/podpares.php";
			NnLib.urlFetch(urlStr, feed);
	
			SubscriptionManager sMngr = new SubscriptionManager();
			NnUserManager uMngr = new NnUserManager();
			NnUser user = uMngr.findByKey(req.getParameter("user"));
			sMngr.channelSubscribe(user, channel, Short.parseShort(req.getParameter("grid")));
			output = PlayerAPI.CODE_SUCCESS + "\t" + Long.toString(channel.getId());
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
	}
		
	/* ==========  CATEGORY: CHANNEL SUBSCRIPTION ========== */	
	/**
	 * User subscribes a channel on a designated grid location.
	 * 
	 * <p>Example: http://localhost:8888/player/subscribe?user=aghubmUydm1zb3IMCxIGTm5Vc2VyGDkM&channel=51&grid=2</p>
	 * 
	 * @param user user's unique identifier
	 * @param channel channelId
	 * @param grid grid location
	 * @return A string of return code and return message, tab delimited.
	 */	
	@RequestMapping(value="subscribe")
	public ResponseEntity<String> subscribe(@RequestParam(value="user") String user, @RequestParam(value="channel") long channel, @RequestParam(value="grid") short grid ) {
		//subscribe	
		NnUserManager userMngr = new NnUserManager();
		NnUser foundUser = userMngr.findByKey(user);
		ChannelManager channelMngr = new ChannelManager();
		MsoChannel foundChannel = channelMngr.findById(channel);
		SubscriptionManager sMngr = new SubscriptionManager();
		sMngr.channelSubscribe(foundUser, foundChannel, grid);
		//return
		String output = PlayerAPI.CODE_SUCCESS + "\t" + PlayerAPI.PLAYER_CODE_SUCCESS;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}

	/* ==========  CATEGORY: CURATOR RELATED ========== */
	/**
	 * Get curator information
	 * 
	 * @param  curator curator key
	 * @return curator info
	 * 		   Fields are tab delimited.           
	 * 		   Fields sequence: msoName, msoIntro, msoThumbnailUrl
	 */			
	public @ResponseBody String curatorInfo(@RequestParam(value="curator") String curator, 
										    @RequestParam(value="delimited", required=false) String delimited) {		
		MsoManager msoMngr = new MsoManager();
		Mso found = msoMngr.findByKey(curator);
		String[] ori = {found.getName(), found.getIntro(), found.getImageUrl()};
		return PlayerLib.getTabDelimitedStr(ori);
	}
	
}
