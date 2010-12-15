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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.*;
import com.nnvmso.model.*;
import com.nnvmso.service.*;

import java.util.Collections;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

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
	//user info
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
		String output = "";
		try {
			String email = req.getParameter("email");
			String pwd = req.getParameter("password");
			NnUser user = new NnUserManager().nnUserAuthenticated(email, pwd);
			if (user == null) {
				output = PlayerAPI.CODE_LOGIN_FAILED + "\t" + PlayerAPI.PLAYER_CODE_LOGIN_FAILED;
			} else {
				new NnUserManager().setUserCookie(resp, NnLib.getKeyStr(user.getKey()));
				output = PlayerAPI.CODE_SUCCESS + "\t" + NnLib.getKeyStr(user.getKey());
			}
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));			
		} catch (Exception e) {
			output = PlayerAPI.CODE_ERROR + "\t" + PlayerAPI.PLAYER_CODE_ERROR;						
		}
		return PlayerLib.outputReturn(output);
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
		String userToken = req.getParameter("user");
		System.out.println("signup() : userToken=" + userToken + 
				"; email=" + email + "; pwd=" + password + "; name=" + name);		
		if (email == null || password == null || name == null ||
			email.length() == 0 || password.length() == 0 || name.length() == 0 ||
			email.equals("undefined")) {
				output = PlayerAPI.CODE_MISSING_PARAMS + "\t" + PlayerAPI.PLAYER_CODE_MISSING_PARAMS;
		} else { 
			NnUserManager userMngr = new NnUserManager();
			NnUser user = userMngr.findByEmail(email);
			if (user != null) {
				output = PlayerAPI.CODE_ERROR + "\t" + PlayerAPI.PLAYER_EMAIL_TAKEN;
				System.out.println("Signup() : ERROR: " + output);
			} else {
				user = userMngr.findByKey(userToken);
				if (user == null ) {
					System.out.println("signup() find by Token: userToken=" + userToken + " NOT FOUND!!!");
					user = new NnUser(req.getParameter("email"));
					user.setPassword(req.getParameter("password"));
					user.setName(req.getParameter("name"));		
					user = userMngr.createViaPlayer(user);
					userMngr.setUserCookie(resp, NnLib.getKeyStr(user.getKey()));
				} else {
					System.out.println("signup() find by Token: userToken=" + userToken + 
							"; email=" + user.getEmail() + "; pwd=" + user.getPassword() + "; name=" + user.getName());		
					if (user.getEmail().equals("guest@9x9.com") && user.getName().equals("guest")) {
						System.out.println("signup() : 1st time signup after being a guest");
						// 1st time signup, reuse previous cookies user 
						user.setEmail(email);
						user.setPassword(password);
						user.setName(name);
						userMngr.updateUser(user);
					} else {
						System.out.println("signup() : 2nd or more time signup ");
						user.setEmail(email);
						user.setPassword(password);
						user.setName(name);
						user = userMngr.createViaPlayer(user);
						userMngr.setUserCookie(resp, NnLib.getKeyStr(user.getKey()));
					}
				}
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
		System.out.println("userTokenVerify() find by Token: userToken=" + token ); 
		NnUser found = new NnUserManager().findByKey(token);
		String output = "";
		if (found == null) {
			output = PlayerAPI.CODE_ERROR + "\t" + PlayerAPI.PLAYER_USER_TOKEN_INVALID;
			CookieHelper.deleteCookie(resp, "user");
		} else {
			System.out.println("userTokenVerify() user found --" + "email=" + found.getEmail() + "; name=" + found.getName());		
			output = PlayerAPI.CODE_SUCCESS + "\t" + PlayerAPI.PLAYER_CODE_SUCCESS;			
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);				
	}
		
	/* ==========  CATEGORY: CHANNEL BROWSING ========== */
	//!!!!! return those user does not have
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
		ProgramManager programMngr = new ProgramManager();
		
		/*
		Cache cache = null;
		
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());
        } catch (CacheException e) {
            // ...
        }		
        */
		
		for (MsoChannel c:channels) {
			/*
			Object programCountObj = null;
			int programCount = 0;
			if (cache != null ) {
				programCountObj = cache.get( c.getKey() );
				if ( programCountObj == null ) {
					// append program count at the end of the line of each channel
					List<MsoProgram> programs = new ArrayList<MsoProgram>();
					programs = programMngr.findByChannelIdAndIsPublic(c.getKey().getId(), true);
					// don't send the channel if program count is zero
					programCount = programs.size();
					cache.put(c.getKey(), (Object)programCount);
				}
				else {
					programCount = Integer.parseInt( programCountObj.toString() );
				}
			}
			*/
			if ( c.getProgramCount() > 0 ) {
				String[] ori = {Short.toString(c.getSeq()), String.valueOf(c.getKey().getId()), 
						c.getName(), c.getImageUrl(), Integer.toString(c.getProgramCount())};
				output = output + PlayerLib.getTabDelimitedStr(ori);			
				output = output + "\n";
				System.out.println(output);
			}
		}
		System.out.println("channelBrowse() :\n" + output);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
	}
	
	/**
	 * Get "new" program list. Current "new" definition: A channel's latest 3 shows.
	 * 
	 * @param user user's unique identifier 
	 * @return A string of new program list.<br/>
	 * 	       Each program is \n delimited.<br/>  
	 *         Example: 1\n2\n3\n
	 */
	@RequestMapping(value="whatsNew") 
	public ResponseEntity<String> whatsNew(@RequestParam(value="user") String user) {
		NnUserManager userService = new NnUserManager();
		ProgramManager programMngr = new ProgramManager();
		NnUser foundUser = userService.findByKey(user);
		String output = "";
		if (foundUser == null) {
			output = PlayerAPI.CODE_ERROR + "\t" + PlayerAPI.PLAYER_USER_TOKEN_INVALID;			
		} else {
			List<MsoProgram> programs = programMngr.findNew(foundUser);
			for (MsoProgram p : programs) {
				output = output + p.getId() + "\n";			
			}
		}
		return PlayerLib.outputReturn(output);
	}
	
	/**
	 * Get all of a user's subscriptions. 
	 * 
	 * @param user user's unique identifier
	 * @return A string of all of the user's subscribed channels' information.<br/>
	 * 	       Each channel is \n delimited. Each channel's information is tab delimited.<br/>  
	 *         Channel info has grid id, channel id, channel name, channel image url.
	 *         Example: 1	1	Channel1	http://hostname/images/img.jpg (program count) (type) (status)
	 */
	@RequestMapping(value="channelLineup")
	public ResponseEntity<String> channelLineup(@RequestParam(value="user") String user) {
		SubscriptionManager subMngr = new SubscriptionManager();
		NnUserManager userService = new NnUserManager();
		NnUser foundUser = userService.findByKey(user);
		List<MsoChannel> channels = subMngr.findSubscribedChannels(foundUser);
		String output = "";
		for (MsoChannel c:channels) {
			String[] ori = {Short.toString(c.getGrid()), 
						    String.valueOf(c.getKey().getId()),
						    c.getName(),
						    c.getImageUrl()};						    
			output = output + PlayerLib.getTabDelimitedStr(ori);			
			output = output + "\n";
		}				
		//return
		System.out.println(output);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}
				
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
		String output = "";
		try {
			ProgramManager programMngr = new ProgramManager();
			MsoProgram program = programMngr.findById(programId);		
			output = program.getNnScript().getScript().getValue();		
		} catch (Exception e){
			output = PlayerAPI.CODE_ERROR + "\t" + PlayerAPI.PLAYER_CODE_ERROR;						
		}
		return PlayerLib.outputReturn(output);
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
	 * @param  channel (1)Could be *, all the programs, e.g. channel=* (user is required for wildcard query). 
	 * 		           (2)Could be a channel Id, e.g. channel=1 <br/>
	 * 		           (3)Could be list of channels, e.g. channels = 34,35,36.
	 * @param  user user's unique identifier, it is required for wildcard query 
	 * @return A string of all the programs' info that met the query criteria. <br/>
	 * 		   Each program is separate by carriage return. Each program's information is tab delimited.<br/>
	 *   Program info has: <br/>
	 *              channelId, programId, programName, description(max length=256),<br/>
	 *              programType, duration, <br/>
	 *              programThumbnailUrl, programLargeThumbnailUrl, <br/>
	 *              url1(mpeg4/slideshow), url2(webm), url3(flv more likely), url4(audio), <br/> 
	 *              timestamp
	 */
	 //@todo channel equals star and no user token, return missing param 	
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
			String url3 = p.getOtherFileUrl();
			String url4 = p.getAudioFileUrl();
			System.out.println("hostname=" + req.getLocalAddr() + ";" + req.getLocalPort() + ";" + req.getRequestURI());
			
			if (p.getType().equals(MsoProgram.TYPE_SLIDESHOW)) {
				url1 = "/player/nnscript?program=" + p.getId();
			}
			
			String intro = p.getIntro();
			int introLenth = (intro.length() > 256 ? 256 : intro.length()); 
			intro = intro.substring(0, introLenth);
			
			String[] ori = {String.valueOf(p.getChannelId()), 
					        String.valueOf(p.getKey().getId()), 
					        p.getName(), 
					        intro,
					        p.getType(), 
					        p.getDuration(),
					        p.getImageUrl(),
					        p.getImageLargeUrl(),
					        url1, 
					        url2, 
					        url3, 
					        url4, 
					        String.valueOf(p.getUpdateDate().getTime())};
			output = output + PlayerLib.getTabDelimitedStr(ori);
			output = output.replaceAll("null", "");
			output = output + "\n";
		}
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
	 * @param grid grid location //overwrite!!!!
	 * 
	 * @return A string of return code and channel id, tab delimited. <br/> !!!!!!!!
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
		System.out.println("podcastSubmit() : userToken=" + userStr + "; podcastRSS=" + rssStr + "; grid=" + gridStr);
		if (rssStr == null || userStr == null || gridStr == null ||
			rssStr.length() == 0 || userStr.length() == 0 || gridStr.length() == 0) {
			output = PlayerAPI.CODE_MISSING_PARAMS + "\t" + PlayerAPI.PLAYER_CODE_MISSING_PARAMS;
			System.out.println("podcastSubmit() : " + output);
			valid = false;
		}
		if (valid) {
			String podcastInfo[] = podcastService.getPodcastInfo(rssStr);			
			if (!podcastInfo[0].equals("200") || !podcastInfo[1].contains("xml")) {
				output = PlayerAPI.CODE_ERROR + "\t" + PlayerAPI.PLAYER_RSS_NOT_VALID;
				System.out.println("podcastSubmit() : " + output);
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
				podcastService.submitToTranscodingService(NnLib.getKeyStr(saved.getKey()), rssStr, req);
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
		System.out.println("subscribe() : userToken=" + user + "; channel=" + channel + "; grid=" + grid);
		//subscribe	
		NnUserManager userMngr = new NnUserManager();
		ChannelManager channelMngr = new ChannelManager();
		SubscriptionManager sMngr = new SubscriptionManager();
		String output = "";

		NnUser foundUser = userMngr.findByKey(user);
		MsoChannel foundChannel = channelMngr.findById(channel);
		if (foundUser != null && foundChannel != null) {
			System.out.println("subscribe() : user found : userToken=" + user + "; email=" + foundUser.getEmail() + "; ");
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
