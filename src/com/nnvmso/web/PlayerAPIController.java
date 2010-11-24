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

import com.nnvmso.lib.*;
import com.nnvmso.model.*;
import com.nnvmso.service.*;

/**
 * <p>Serves for Player.</p>
 * <p>Url examples: (notice method name is used at the end of URL) <br/> 
 * http://hostname:port/playerAPI/channelBrowse<br/>
 * http://hostname:port/podcastAPI/login<br/>
 * <p/>
 */

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
	 *         Example: "0	aghubmUydm1zb3IMCxIGTm5Vc2VyGDkM", "100	Login Failed".
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
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
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
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		String name = req.getParameter("name");
		System.out.println("player signup email=" + email + ";pwd=" + password + ";name=" + name);		
		if (email == null || password == null || name == null ||
			email.length() == 0 || password.length() == 0 || name.length() == 0 ||
			email.equals("undefined")) {
				output = PlayerAPI.CODE_MISSING_PARAMS + "\t" + PlayerAPI.PLAYER_CODE_MISSING_PARAMS;
		} else { 
			NnUserManager userMngr = new NnUserManager();
			NnUser user = userMngr.findByEmail(email);
			if (user != null) {
				output = PlayerAPI.CODE_ERROR + "\t" + PlayerAPI.PLAYER_EMAIL_TAKEN;
			} else {
				user = new NnUser(req.getParameter("email"));
				user.setPassword(req.getParameter("password"));
				user.setName(req.getParameter("name"));		
				user = userMngr.createViaPlayer(user);
				userMngr.setUserCookie(resp, NnLib.getKeyStr(user.getKey()));
				output = PlayerAPI.CODE_SUCCESS + "\t" + NnLib.getKeyStr(user.getKey());				
			}
		}
		//return
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
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
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}
	
	/**
	 * Verify a user token <br/>
	 * Example: http://localhost/playerAPI/userTokenVerify?token=aghubmUzdm1zb3INCxIGTm5Vc2VyGKsEDA
	 * 
	 * @param token user key 
	 * @return return code and return message, tab delimited. <br/>
	 * 	       Will delete the user cookie if token is invalid.<br/>
	 *         Example: "0	Success", "4	Invalid user token". 
	 */
	@RequestMapping(value="userTokenVerify")	
	public ResponseEntity<String> userTokenVerify(@RequestParam(value="token") String token, HttpServletResponse resp) {
		NnUser found = new NnUserManager().findByKey(token);
		String output = "";
		if (found == null) {
			output = PlayerAPI.CODE_ERROR + "\t" + PlayerAPI.PLAYER_USER_TOKEN_INVALID;
			CookieHelper.deleteCookie(resp, "user");
		} else {
			output = PlayerAPI.CODE_SUCCESS + "\t" + PlayerAPI.PLAYER_CODE_SUCCESS;			
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);				
	}
		
	/* ==========  CATEGORY: CHANNEL BROWSING ========== */		
	/**
	 * Browse all the on-air channels.
	 * 
	 * @return A string of all the channels' information. <br/>
	 * 	       Each channel is \n delimited. Each channel's information is tab delimited.<br/>
	 *         Channel info has channel id, channel name, channel image url. <br/>
	 *         Example: 1	Channel1	http://hostname/images/img.jpg 
	 *         
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
		System.out.println("channelBrowse:" + output);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
	}
		
	/**
	 * Get all of a user's subscriptions. 
	 * 
	 * @param user user's unique identifier
	 * @return A string of all of the user's subscribed channels' information.<br/>
	 * 	       Each channel is \n delimited. Each channel's information is tab delimited.<br/>  
	 *         Channel info has grid id, channel id, channel name, channel image url.
	 *         Example: 1	1	Channel1	http://hostname/images/img.jpg
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
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}
		
	/**
	 * Get a user's subscribed channels. 
	 * 
	 * Example: http://localhost:8888/playerAPI/channelLineup?user=aghubmUzdm1zb3INCxIGTm5Vc2VyGKsEDA
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
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
	}
	*/
		
	/**
	 * Get program script based on program id.
	 * 
	 * <p>Example: http://localhost:8888/playerAPI/nnscript?program=566</p>  
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
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(script, headers, HttpStatus.OK);
	}
	
	/**
	 * Get program information based on query criteria.
	 * 
	 * <p>
	 * Examples: <br/>
	 *  http://localhost:8888/playerAPI/programInfo?channel=*&user=aghubmUzdm1zb3INCxIGTm5Vc2VyGKsEDA <br/>
	 *  http://localhost:8888/playerAPI/programInfo?channel=153,158 <br/>
	 *  http://localhost:8888/playerAPI/programInfo?channel=153 <br/>
	 * </p> 
	 *  
	 * @param  channel (1)Could be *, all the programs, e.g. channel=* (user is required for wildcard query). 
	 * 		           (2)Could be a channel Id, e.g. channel=1 <br/>
	 * 		           (3)Could be list of channels, e.g. channels = 34,35,36.
	 * @param  user user's unique identifier, it is required for wildcard query 
	 * @return A string of all the programs' info that met the query criteria. <br/>
	 * 		   Each program is separate by carriage return. Each program's information is tab delimited.<br/>
	 * 		   Program info has: channelId, programId, programName, programType, programThumbnailUrl, url1(mpeg4/slideshow), url2(webm)
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
			String url1 = p.getMpeg4FileUrl();
			String url2 = p.getWebMFileUrl();
			System.out.println("hostname=" + req.getLocalAddr() + ";" + req.getLocalPort() + ";" + req.getRequestURI());
			
			if (p.getType().equals(MsoProgram.TYPE_SLIDESHOW)) {
				url1 = "/player/nnscript?program=" + p.getId();
			}
			String[] ori = {String.valueOf(p.getChannelId()), String.valueOf(p.getKey().getId()), p.getName(), p.getType(), p.getImageUrl(), url1, url2};
			output = output + PlayerLib.getTabDelimitedStr(ori);
			output = output + "\n";
		}
		System.out.println("programInfo:" + output);
		//return output;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
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
	@RequestMapping(value="podcastSubmit", method=RequestMethod.POST)
	public ResponseEntity<String> podcastSubmit(HttpServletRequest req) {
		PodcastService podcastService = new PodcastService();
		String output = "";
		boolean valid = true;
		String rssStr = req.getParameter("podcastRSS");
		String userStr = req.getParameter("user");
		String gridStr = req.getParameter("grid");
		if (rssStr == null || userStr == null || gridStr == null ||
			rssStr.length() == 0 || userStr.length() == 0 || gridStr.length() == 0) {
			output = PlayerAPI.CODE_MISSING_PARAMS + "\t" + PlayerAPI.PLAYER_CODE_MISSING_PARAMS;
			valid = false;
		}
		if (valid) {
			String podcastInfo[] = podcastService.getPodcastInfo(rssStr);			
			if (!podcastInfo[0].equals("200") || !podcastInfo[1].contains("xml")) {
				output = PlayerAPI.CODE_ERROR + "\t" + PlayerAPI.PLAYER_RSS_NOT_VALID;
				valid = false;
			} else {
				rssStr = podcastInfo[2];
			}
		}
		if (valid) {
			Mso mso = new MsoManager().findByEmail("default_mso@9x9.com");
			MsoChannel channel = podcastService.findByPodcast(rssStr);
			if (channel == null) {
				//create channel
				channel = podcastService.getDefaultPodcastChannel(rssStr);
				MsoChannel saved = new ChannelManager().create(channel, mso);
				podcastService.submitToTranscodingService(NnLib.getKeyStr(saved.getKey()), rssStr);
				System.out.println("submit to transcoding service=" + rssStr);
				//automatically subscribe to this channel
				SubscriptionManager sMngr = new SubscriptionManager();
				NnUserManager uMngr = new NnUserManager();
				NnUser user = uMngr.findByKey(req.getParameter("user"));
				sMngr.channelSubscribe(user, channel, Short.parseShort(req.getParameter("grid")));
			}
			String[] ori = {Integer.toString(PlayerAPI.CODE_SUCCESS), Long.toString(channel.getId()), channel.getName(), channel.getImageUrl()};
			output = PlayerLib.getTabDelimitedStr(ori);						
		}
		System.out.println("player podcast return:" + output);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
	}
		
	/* ==========  CATEGORY: CHANNEL SUBSCRIPTION ========== */	
	/**
	 * User subscribes a channel on a designated grid location.
	 * 
	 * <p>Example: http://localhost:8888/playerAPI/subscribe?user=aghubmUydm1zb3IMCxIGTm5Vc2VyGDkM&channel=51&grid=2</p>
	 * 
	 * @param user user's unique identifier
	 * @param channel channelId
	 * @param grid grid location, from 1 to 81
	 * @return A string of return code and return message, tab delimited.
	 */	
	@RequestMapping(value="subscribe")
	public ResponseEntity<String> subscribe(@RequestParam(value="user") String user, @RequestParam(value="channel") long channel, @RequestParam(value="grid") short grid ) {			
		//subscribe	
		NnUserManager userMngr = new NnUserManager();
		ChannelManager channelMngr = new ChannelManager();
		SubscriptionManager sMngr = new SubscriptionManager();
		String output = "";

		NnUser foundUser = userMngr.findByKey(user);
		MsoChannel foundChannel = channelMngr.findById(channel);
		if (foundUser != null && foundChannel != null) {
			sMngr.channelSubscribe(foundUser, foundChannel, grid);
			output = PlayerAPI.CODE_SUCCESS + "\t" + PlayerAPI.PLAYER_CODE_SUCCESS;
		} else {
			output = PlayerAPI.CODE_ERROR + "\t" + PlayerAPI.PLAYER_CHANNEL_OR_USER_UNEXISTED;
		}
		//return
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}

	/**
	 * User unsubscribes a channel
	 * 
	 * <p>Example: http://localhost:8888/playerAPI/unsubscribe?user=aghubmUydm1zb3IMCxIGTm5Vc2VyGDkM&channel=51</p>
	 * 
	 * @param user user's unique identifier
	 * @param channel channelId
	 * @return A string of return code and return message, tab delimited.
	 */		
	@RequestMapping(value="unsubscribe")
	public ResponseEntity<String> unsubscribe(@RequestParam(value="user") String user, @RequestParam(value="channel") long channel) {
		SubscriptionManager sMngr = new SubscriptionManager();
		ChannelManager cMngr = new ChannelManager();
		NnUserManager uMngr = new NnUserManager();
		String output = "";
		
		NnUser u = uMngr.findByKey(user);
		MsoChannel c = cMngr.findById(channel);
		if (u != null && c != null) {
			sMngr.channelUnsubscribe(u, c);
			output = PlayerAPI.CODE_SUCCESS + "\t" + PlayerAPI.PLAYER_CODE_SUCCESS;
		} else {
			output = PlayerAPI.CODE_ERROR + "\t" + PlayerAPI.PLAYER_CHANNEL_OR_USER_UNEXISTED;
		} 		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
	}
	
	/* ==========  CATEGORY: CURATOR RELATED ========== */
	/**
	 * NOT IN USE: Get curator information
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
