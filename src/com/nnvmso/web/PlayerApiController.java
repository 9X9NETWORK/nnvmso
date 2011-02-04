package com.nnvmso.web;

import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Mso;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnStatusMsg;
import com.nnvmso.service.PlayerApiService;

/**
 * <p>Serves for Player.</p>
 * <p>Url examples: (notice method name is used at the end of URL) <br/> 
 * http://<hostname:port>/playerAPI/channelBrowse<br/>
 * http://<hostname:port>/podcastAPI/login<br/>
 * <p/>
 * <p>General rules:<br/>
 *    API always returns a string. <br/>
 *    First line is status code and status message, separated by tab. <br/>
 *    If there is another set of data, it will be separated from the status line by "--\n" <br/>
 *    Follows data listing if any, data is tab separated, different record is \n separated. <br/>    
 * </p>
 * <p>Example: <br/>
 * 0	success  <br/>
 * -- <br/>
 * token	a466D491UaaU245P412a <br/>
 * name	a
 * </p>8
 */
@Controller
@RequestMapping("playerAPI")
public class PlayerApiController {
	protected static final Logger log = Logger.getLogger(PlayerApiController.class.getName());
	
	private final PlayerApiService playerApiService;	
	private Locale locale;
	
	@Autowired
	public PlayerApiController(PlayerApiService playerApiService) {
		this.playerApiService= playerApiService;
	}	
	
	//!!! if can't find any mso, all dead!
	private void prepService(HttpServletRequest req) {
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findMsoViaHttpReq(req);
		Locale locale = Locale.ENGLISH;
		if (mso.getPreferredLangCode().equals(Mso.LANG_ZH_TW)){
			locale = Locale.TRADITIONAL_CHINESE;
		}		
		playerApiService.setLocale(locale);
		playerApiService.setMso(mso);
		this.locale = locale;
	}		
	
	/**
	 * To be ignored  
	 */
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/blank";
	}
	
	/* ==========  CATEGORY: BRAND RELATED ========== */
	/**
	 * Get brand information. 
	 * 	
	 * @param mso mso name, optional 
	 * @return Data returns in key and value pair. Key and value is tab separated. Each pair is \n separated.<br/> 
	 * 		   keys include "key", "name", logoUrl", "jingleUrl", "preferredLangCode" "debug"<br/>
	 */	
	@RequestMapping(value="brandInfo")
	public ResponseEntity<String> brandInfo(@RequestParam(value="mso", required=false)String brandName, HttpServletRequest req) {
		log.info("brandInfo:" + brandName);
		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);
		try {
			output = playerApiService.findMsoInfo(req);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}	

	/* ==========  CATEGORY: ACCOUNT RELATED ========== */
	/**
	 * User login.
	 * 
	 * <p>Only POST operation is supported.</p>
	 * 
	 * @param email email
	 * @param password password
	 * 
	 * @return If signup succeeds, the return message will be         
	 *         <p>preference1 key name (tab) preference1 value (\n)<br/>            
	 *            preference2 key name (tab) preference2 value (\n)<br/>
	 *            preferences.....
	 *         </p> 
	 */	
	@RequestMapping(value="login")
	public ResponseEntity<String> login(HttpServletRequest req, HttpServletResponse resp) {
		String email = req.getParameter("email");
		String password = req.getParameter("password");		
		this.prepService(req);
		log.info("login: email=" + email);		
		String output = NnStatusMsg.errorStr(locale);		
		try {
			output = playerApiService.findAuthenticatedUser(email, password, req, resp);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}	

	/**
	 * Register a guest account. 
	 * If ipg is provided, guest is automatically subscribed to all the channels in the ipg. 
	 * 
	 * @param ipg ipg identifier, it is optional
	 * @return please reference login
	 */	
	@RequestMapping(value="guestRegister")
	public ResponseEntity<String> guestRegister(@RequestParam(value="ipg", required = false) String ipg, HttpServletRequest req, HttpServletResponse resp) {
		log.info("guest register: (ipg)" + ipg);
		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);
		try {
			output = playerApiService.createGuest(ipg, req, resp); 
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}		
		return NnNetUtil.textReturn(output);
	} 
	
	/**
	 *  User signup.
	 *  
	 *  <p>only POST operation is supported.</p>
	 *  
	 *  @param email email
	 *  @param password password
	 *  @param name display name
	 *  @return please reference login
	 */	
	@RequestMapping(value="signup")
    public ResponseEntity<String> signup(HttpServletRequest req, HttpServletResponse resp) {
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		String name = req.getParameter("name");
		String userToken = req.getParameter("user");				
		log.info("signup: email=" + email + ";name=" + name + ";userToken=" + userToken + ";password=" + password);

		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);
		try {
			output = playerApiService.createUser(email, password, name, userToken, req, resp);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}	

	/**
	 * Sign out
	 * 
	 * Clean the cookie
	 * 
	 * @param user user key identifier 
	 */		
	@RequestMapping(value="signout")
    public ResponseEntity<String> signout(@RequestParam(value="user", required=false) String userKey, HttpServletRequest req, HttpServletResponse resp) {
		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);
		try {
			CookieHelper.deleteCookie(resp, CookieHelper.USER);
			output = NnStatusMsg.successStr(locale);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}	
		
	/**
	 * Verify a user token <br/>
	 * Example: http://<host>/playerAPI/userTokenVerify?token=aghubmUzdm1zb3INCxIGTm5Vc2VyGKsEDA
	 * 
	 * @param token user key 
	 * @return Will delete the user cookie if token is invalid.<br/>
	 * 		   Return info please reference login.
	 */	
	@RequestMapping(value="userTokenVerify")
	public ResponseEntity<String> userTokenVerify(@RequestParam(value="token") String token, HttpServletRequest req, HttpServletResponse resp) {
		log.info("userTokenVerify() : userToken=" + token);		

		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);

		try {			
			output = playerApiService.findUserByToken(token, req, resp);
		} catch (Exception e){
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}
	
	/**
	 * Get user's language based on ip
	 * 
	 * @return Language code, currently either zh or en. <br/>          
	 */
	@RequestMapping(value="getLangCode")
	public ResponseEntity<String> getLangCode(HttpServletRequest req) {
		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);
		try {
			output = playerApiService.findLocaleByHttpRequest(req);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);

	}	

	/* ==========  CATEGORY: CHANNEL SUBSCRIPTION ========== */	
	/**
	 * User subscribes a channel on a designated grid location.
	 * 
	 * <p>Example: http://<host>/playerAPI/subscribe?user=aghubmUydm1zb3IMCxIGTm5Vc2VyGDkM&channel=51&grid=2</p>
	 * 
	 * @param user user's unique identifier
	 * @param channel channelId
	 * @param grid grid location, from 1 to 81
	 * @return basic message scheme.
	 */		
	@RequestMapping(value="subscribe")
	public ResponseEntity<String> subscribe(@RequestParam(value="user", required=false) String userToken, 
			                                @RequestParam(value="channel", required=false) String channelId, 
			                                @RequestParam(value="grid", required=false) String grid, 
			                                HttpServletRequest req ) {		
		log.info("subscribe: userToken=" + userToken+ "; channel=" + channelId + "; grid=" + grid);
		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);
		try {
			output = playerApiService.subscribeChannel(userToken, channelId, grid);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}
	
	/**
	 * User unsubscribes a channel
	 * 
	 * <p>Example: http://localhost:8888/playerAPI/unsubscribe?user=aghubmUydm1zb3IMCxIGTm5Vc2VyGDkM&channel=51</p>
	 * 
	 * @param user user's unique identifier
	 * @param channel channelId
	 * @return basic message scheme.
	 */			
	@RequestMapping(value="unsubscribe")
	public ResponseEntity<String> unsubscribe(@RequestParam(value="user") String userToken, 
								              @RequestParam(value="channel") String channelId,
								              HttpServletRequest req) {			
		this.prepService(req);
		log.info("userToken=" + userToken + "; channel=" + channelId);
		String output = NnStatusMsg.errorStr(locale);
		try {
			output = playerApiService.unsubscribeChannel(userToken, channelId);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}	

	/* ==========  CATEGORY: CHANNEL CREATE ========== */		
	/**
	 * Generate a channel based on a podcast RSS feed.
	 * 
	 * <p>Only POST operation is supported.</p>
	 *  
	 * @param url a podcast RSS feed or a YouTube url
	 * @param user user's unique identifier
	 * @param grid grid location
	 * @param category category id
	 * @param langCode language code, en or zh.
	 * 
	 * @return channel id, channel name, image url. <br/>
	 */	
	@RequestMapping(value="channelSubmit")
	public ResponseEntity<String> channelSubmit(HttpServletRequest req) {
		String url = req.getParameter("url") ;
		String userToken= req.getParameter("user");
		String grid = req.getParameter("grid");
		String categoryIds = req.getParameter("category");

		this.prepService(req);		
		log.info("player input - userToken=" + userToken+ "; url=" + url + ";grid=" + grid + ";categoryId=" + categoryIds);				
		String output = NnStatusMsg.errorStr(locale);		
		
		try {
			output = playerApiService.createChannel(categoryIds, userToken, url, grid, req);
		} catch (Exception e){
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);		
	}
	
	/* ==========  CATEGORY: BROWSING ========== */
	/**
	 * Browse all the on-air channels.
	 * 
	 * @param category category id
	 * @return Category info and channels info. <br/>
	 *  	   First line is category info follows channels info. Each channel is \n separated.<br/>    
	 *         Category info includes category id. <br/>
	 *         Channel info includes channel id, channel name, channel image url, program count. <br/>
	 *         Example: 1	Channel1	http://hostname/images/img.jpg	5
	 */	
	@RequestMapping(value="channelBrowse")
	public ResponseEntity<String> channelBrowse(@RequestParam(value="category", required=false) String categoryIds, HttpServletRequest req) {
		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);
		try {
			output = playerApiService.findPublicChannelsByCategory(categoryIds);
		} catch (Exception e){
			output = playerApiService.handleException(e);
		}
		log.info("channelBrowse() return:" + output);
		return NnNetUtil.textReturn(output);
	}	

	/**
	 * Browse categories.
	 *  
	 * @return Categories info. Each category is \n separated.<br/>
	 *         Category info has category id, category name, langCode.<br/>
	 */		
	@RequestMapping(value="categoryBrowse")
	public ResponseEntity<String> categoryBrowse(HttpServletRequest req) {
		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);		
		try {
			output = playerApiService.findCategoriesByMso();
		} catch (Exception e){
			output = playerApiService.handleException(e);
		}		
		return NnNetUtil.textReturn(output);
	}

	/**
	 * Get all of a user's subscriptions. 
	 * 
	 * @param user user's unique identifier
	 * @param userInfo true or false. Whether to return user information as login. If asked, it will be returned after status code. 
	 * @return <p>A string of all of the user's subscribed channels' information.</p>  
	 *         <p>Channel info has following fields: 
	 *         <blockquote> grid id, channel id,  <br/>
	 *         channel name, channel description, channel image url, <br/>
	 *         program count, type(integer, see following), status(integer, see following), <br/>
	 *         subscription count</blockquote>
	 *         <p>type: TYPE_GENERAL = 1; TYPE_READONLY = 2;</p>
	 *         <p>status: STATUS_SUCCESS = 0; STATUS_ERROR = 1; STATUS_INFRINGEMENT = 2; STATUS_RRATED = 3;
	 *         <p> Example: 1	1	Channel1	http://hostname/images/img.jpg	3	1 0</p>
	 *         </p>
	 */		
	@RequestMapping(value="channelLineup")
	public ResponseEntity<String> channelLineup(@RequestParam(value="user", required=false) String userToken,
												@RequestParam(value="userInfo", required=false) String userInfo,
											    HttpServletRequest req) {
		this.prepService(req);
		log.info("userToken=" + userToken + ";isUserInfo:" + userInfo);				
		boolean isUserInfo = Boolean.parseBoolean(userInfo);
		String output = NnStatusMsg.errorStr(locale);
		try {
			output = playerApiService.findSubscribedChannels(userToken, isUserInfo);
		} catch (Exception e){
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}	

	/**
	 * Swap grid location 
	 * 
	 * @param user user's unique identifier
	 * @param grid1
	 * @param grid2
	 * 
	 * @return basic message scheme.
	*/
	@RequestMapping(value="moveChannel")
	public ResponseEntity<String> moveChannel(@RequestParam(value="user", required=false) String userToken, 
											  @RequestParam(value="grid1", required=false) String grid1,
											  @RequestParam(value="grid2", required=false) String grid2,
											  HttpServletRequest req){
		this.prepService(req);
		log.info("userToken=" + userToken + ";grid1=" + grid1 + ";grid2=" + grid2);
		String output = NnStatusMsg.errorStr(locale);
		try {
			output = playerApiService.moveChannel(userToken, grid1, grid2);
		} catch (Exception e){
			output = playerApiService.handleException(e);
		}		
		return NnNetUtil.textReturn(output);		
	}					
	
	//tab = %09
	//\n = %0A
	//http://localhost:8888/playerAPI/pdr?user=2g9p42n5np51913F11gg&pdr=watched%098156%092%093%094%095%0Awatched%092%092%0A
	//http://localhost:8888/playerAPI/pdr?user=2g9p42n5np51913F11gg&session=12345&pdr=1%09clicked%098156%098168%0A100%09clicked%098156%098168
	//http://localhost:8888/playerAPI/pdr?user=2g9p42n5np51913F11gg&session=12345&pdr=1%09watched%09367%09371%0A
	/**
	 * Collecting PDR
	 * 
	 * @param user user token
	 * @param session indicates the session when user starts using the player
	 * @param pdr pdr data
	 * 		  <p> Expecting lines(separated by \n) of the following:<br/>  
	 * 		  delta verb info <br/>
	 * 		  Example: delta watched 1 1 2 3 <br/>
	 * 		  Note: first 1 is channel, the rest are program ids. <br/>  
	 * 		  Note: each field is separated by tab.
	 * 		  </p> 
	 */	
	@RequestMapping(value="pdr")
	public ResponseEntity<String> pdr(@RequestParam(value="user", required=false) String userToken,
									  @RequestParam(value="session", required=false) String session,
									  @RequestParam(value="pdr", required=false) String pdr,
									  HttpServletRequest req) {
		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);
		log.info("user=" + userToken + ";session=" + session + ";pdr=" + pdr);
		try {
			output = playerApiService.processPdr(userToken, pdr, session);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}
	
	/**
	 * Get "new" program list. Current "new" definition: the latest 3 shows in a channel.   
	 * 1. Latest 3 shows users haven't seen 
	 * 2. Latest 3 shows watched by user, if no unseen show in this channel
	 * 
	 * @param user user's unique identifier 
	 * @return A string of new program list.<br/>
	 * 	       Each program is \n delimited.<br/>  
	 *         Example: 1\n2\n3\n
	 */	
	@RequestMapping(value="whatsNew") 
	public ResponseEntity<String> whatsNew(@RequestParam(value="user") String userToken, HttpServletRequest req) {
		this.prepService(req);		
		log.info("userToken=" + userToken);		
		String output = NnStatusMsg.errorStr(locale);
		
		try {
			output = playerApiService.findNewPrograms(userToken);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);		
	}	
	
	/**
	 * Get program information based on query criteria.
	 * 
	 * <p>
	 * Examples: <br/>
	 *  http://<host>/playerAPI/programInfo?channel=*&user=aghubmUzdm1zb3INCxIGTm5Vc2VyGKsEDA <br/>
	 *  http://<host>/playerAPI/programInfo?channel=*&ipg=13671109 <br/>
	 *  http://<host>/playerAPI/programInfo?channel=153,158 <br/>
	 *  http://<host>/playerAPI/programInfo?channel=153 <br/>
	 * </p> 
	 * @param  channel (1)Could be *, all the programs, e.g. channel=* (user is required for wildcard query). 
	 * 		           (2)Could be a channel Id, e.g. channel=1 <br/>
	 * 		           (3)Could be list of channels, e.g. channels = 34,35,36.
	 * @param  user user's unique identifier, it is required for wildcard query
	 * @param  userInfo true or false. Whether to return user information as login. If asked, it will be returned after status code. 
	 * @param  ipg  ipg's unique identifier, it is required for wildcard query
	 * @return <p>Programs info. Each program is separate by \n.</p>
	 *   	   <p>Program info has: <br/>
	 *            channelId, programId, programName, description(max length=256),<br/>
	 *            programType, duration, <br/>
	 *            programThumbnailUrl, programLargeThumbnailUrl, <br/>
	 *            url1(mpeg4/slideshow), url2(webm), url3(flv more likely), url4(audio), <br/> 
	 *            timestamp</p>
	 */
	@RequestMapping("programInfo")
	public ResponseEntity<String> programInfo(@RequestParam(value="channel", required=false) String channelIds,
									          @RequestParam(value="user", required = false) String userToken,
									          @RequestParam(value="userInfo", required=false) String userInfo,
									          @RequestParam(value="ipg", required = false) String ipgId,
									          HttpServletRequest req) {
		this.prepService(req);		
		String output = NnStatusMsg.errorStr(locale);
		boolean isUserInfo = Boolean.parseBoolean(userInfo);
		try {
			output =  playerApiService.findProgramInfo(channelIds, userToken, ipgId, isUserInfo);
		} catch (Exception e){
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}
	
	/* ==========  CATEGORY: IPG RELATED ========== */
	/**
	 * Save User IPG (snapshot)
	 *
	 * @param user user's unique identifier
	 * @return     An unique IPG identifier
	 */
	@RequestMapping(value="saveIpg")
	public ResponseEntity<String> saveIpg(@RequestParam(value="user") String userToken, HttpServletRequest req) {		
		this.prepService(req);
		log.info("saveIpg(" + userToken + ")");
		String output = NnStatusMsg.errorStr(locale);		
		try {
			output = playerApiService.saveIpg(userToken);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}	
	
	/**
	 * Load User IPG (snapshot)
	 *
	 * @param ipg IPG's unique identifier
	 * @return    please refer to channelLineup()
	 */
	@RequestMapping(value="loadIpg")
	public ResponseEntity<String> loadIpg(@RequestParam(value="ipg") Long ipgId, HttpServletRequest req) {		
		log.info("saveIpg(" + ipgId + ")");		
		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);		
		try {
			output = playerApiService.loadIpg(ipgId);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);						
	}
	
}