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
 * <b>This is API specification for 9x9 Player.</b> Please note although the document is written in JavaDoc form, it is <b>generic Web Service API via HTTP request-response, no Java necessary</b>.
 * <p>
 * <blockquote>
 * Example:
 * <p>
 * Player Request: <br/>
 * http://qa.9x9.tv/playerAPI/brandInfo?mso=9x9
 * <p>
 * Service response:  <br/>
 * 0	success<br/>
 * --<br/>
 * name		9x9<br/>
 * title	9x9.tv<br/>
 * </blockquote>
 * <p>
 * <b>In this document, method name is used as part of the URL</b>, examples:
 * <p>   
 * <blockquote>
 * http://hostname:port/playerAPI/channelBrowse?category=1<br/>
 * http://hostname:port/playerAPI/brandInfo?mso=9x9<br/>
 * </blockquote>
 * 
 * <p>
 * <b>API categories:</b
 * <p>
 * <blockquote>
 * Brand information: brandInfo
 * <p>
 * Account related: guestRegister, signup, login, userTokenVerify, signout
 * <p>
 * Category listing: categoryBrowse
 * <p>
 * Channel and program listing: channelLineup, programInfo
 * <p>
 * IPG action: moveChannel, channelSubmit, subscribe, unsubscribe
 * <p>
 * IPG snapshot: saveIpg, loadIpg
 * <p>
 * Data collection: pdr, programRemove
 * </blockquote>
 * <p>
 * <b>9x9 Player API always returns a string:</b>
 * <p>
 * First line is status code and status message, separated by tab.<br/>
 * <p>
 * Different sets of data are separated by "--\n".
 * <p>
 * Data representation is \t separated of each field, \n separated of each record.
 * <p>
 * <blockquote>
 * Example 1: login 
 * <p>
 * 0	success  <br/>
 * -- <br/>
 * token	a466D491UaaU245P412a <br/>
 * name	a
 * <p>
 * Example 2: categoryBrowse
 * <p>
 * 0	success  <br/>
 * -- <br/>
 * 1201	Movie	5 <br/>
 * 1203	TV	2 <br/>
 * 1204 Sports 2 <br/>
 * </blockquote>
 * <p>     
 * Please note each api's document omits status code and status message.
 * <p>    
 * <b>Basic API flows:</b>
 * <blockquote>
 * The first step is to call brandInfo to retrieve brand information. It returns brand id, brand logo, and any necessary brand information.
 * <p>
 * The next step depends on the UI requirement. Use categoryBrowse to find category listing based on the brand. 
 * Or get an account first. Use userTokenVerify if there's an existing user token. 
 * If there's no token at hand, either sign up for user as a guest(guestRegister) or ask user to signup(signup).
 * <p>
 * Channel and program listing(channelLineup and programInfo) would be ready after an account is registered.
 * <p>
 * </blockquote>
 * 
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
	 * @param mso mso name, optional, server returns default mso 9x9 if omiited
	 * @return <p>Data returns in key and value pair. Key and value is tab separated. Each pair is \n separated.<br/> 
	 * 		   keys include "key", "name", logoUrl", "jingleUrl", "preferredLangCode" "debug"<br/></p>
	 *         <p>Example: <br/>
	 *          0	success <br/>
	 *          --<br/>
	 *          key	1<br/>
	 *          name	9x9<br/>
	 *          title	9x9.tv<br/>
	 *          logoUrl	/WEB-INF/../images/logo_9x9.png<br/>
	 *          jingleUrl	/WEB-INF/../videos/opening.swf<br/>
	 *          logoClickUrl	/<br/>
	 *          preferredLangCode	en<br/>
	 *          debug	1<br/>
	 *         </p>
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
		log.info(output);
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
	 *         <p> Example: <br/>
	 *         0	success <br/>
	 *         --<br/>
	 *         token	QQl0l208W2C4F008980F<br/>
	 *         name	c<br/>
	 *         lastLogin	1300822489194<br/>
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
		log.info(output);
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
		log.info(output);
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
		log.info(output);
		return NnNetUtil.textReturn(output);
	}	

	/**
	 * User cookie will be removed
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
		log.info(output);
		return NnNetUtil.textReturn(output);
	}	
		
	/**
	 * Verify user token <br/>
	 * Example: http://host:port/playerAPI/userTokenVerify?token=QQl0l208W2C4F008980F
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
		log.info(output);
		return NnNetUtil.textReturn(output);
	}
	
	/**
	 * @deprecated Get user's language based on ip
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
		log.info(output);
		return NnNetUtil.textReturn(output);

	}	

	/* ==========  CATEGORY: CHANNEL SUBSCRIPTION ========== */	
	/**
	 * User subscribes a channel on a designated grid location.
	 * 
	 * <p>Example: http://host:port/playerAPI/subscribe?user=QQl0l208W2C4F008980F&channel=51&grid=2</p>
	 * 
	 * @param user user's unique identifier
	 * @param channel channelId
	 * @param grid grid location, from 1 to 81
	 * @return status code and status message 
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
		log.info(output);
		return NnNetUtil.textReturn(output);
	}
	
	/**
	 * User unsubscribes a channel
	 * 
	 * <p>Example: http://host:port/playerAPI/unsubscribe?user=QQl0l208W2C4F008980F&channel=51</p>
	 * 
	 * @param user user's unique identifier
	 * @param channel channelId
	 * @return status code and status message 
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
		log.info(output);
		return NnNetUtil.textReturn(output);
	}	

	/* ==========  CATEGORY: CHANNEL CREATE ========== */		
	/**
	 * Generate a channel based on a podcast RSS feed or a YouTube URL.
	 * 
	 * <p>Only POST operation is supported.</p>
	 *  
	 * @param url a podcast RSS feed or a YouTube url
	 * @param user user's unique identifier
	 * @param grid grid location, 0 - 81
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
		log.info(output);
		return NnNetUtil.textReturn(output);		
	}
	
	/* ==========  CATEGORY: BROWSING ========== */
	/**
	 * Browse all the on-air channels by category.
	 * 
	 * @param category category id
	 * @return Category info and channels info. <br/>
	 *  	   First section is category info, follows channels info. Each channel is \n separated.<br/>    
	 *         Category info has category id. <br/>
	 *         Channel info includes channel id, channel name, channel image url, program count, subscription count <br/>
	 *         Example: 	<br/>
	 *         0	success<br/>
	 *         --<br/>
	 *         1174<br/>
	 *         --<br/>
	 *         0	1207	Etsy	http://s3.amazonaws.com/9x9chthumb/a.gif	2	2 <br/>
	 *         0	1217	System	http://s3.amazonaws.com/9x9chthumb/b.gif	1	2 <br/>        
	 */		
	@RequestMapping(value="channelBrowse")
	public ResponseEntity<String> channelBrowse(@RequestParam(value="category", required=false) String categoryIds, HttpServletRequest req) {
		this.prepService(req);
		log.info(categoryIds);		
		String output = NnStatusMsg.errorStr(locale);
		try {
			output = playerApiService.findPublicChannelsByCategory(categoryIds);
		} catch (Exception e){
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}	

	/**
	 * Browse categories.
	 *  
	 * @return Categories info. Each category is \n separated.<br/>
	 *         Category info has category id, category name, channel count.<br/>
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
	 *         program count, type(integer, see following), status(integer, see following)
	 *         </blockquote>
	 *         <p>type: TYPE_GENERAL = 1; TYPE_READONLY = 2;</p>
	 *         <p>status: STATUS_SUCCESS = 0; STATUS_ERROR = 1; 
	 *         <p> Example: <br/>
	 *         0	success<br/>
	 *         -- <br/>
	 *         1	1207	Channel1	http://hostname/images/img.jpg	3	1 0<br/>
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
	 * Move a channel from grid 1 to grid2
	 * 
	 * @param user user's unique identifier
	 * @param grid1
	 * @param grid2
	 * 
	 * @return status code and status message
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
		log.info(output);
		return NnNetUtil.textReturn(output);		
	}					
		
	/**
	 * @deprecated
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
	 *  http://host:port/playerAPI/programInfo?channel=*&user=QQl0l208W2C4F008980F <br/>
	 *  http://host:port/playerAPI/programInfo?channel=*&ipg=13671109 <br/>
	 *  http://host:port/playerAPI/programInfo?channel=153,158 <br/>
	 *  http://host:port/playerAPI/programInfo?channel=153 <br/>
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
	 *            publish date timestamp</p>
	 */
	@RequestMapping("programInfo")
	public ResponseEntity<String> programInfo(@RequestParam(value="channel", required=false) String channelIds,
									          @RequestParam(value="user", required = false) String userToken,
									          @RequestParam(value="userInfo", required=false) String userInfo,
									          @RequestParam(value="ipg", required = false) String ipgId,
									          HttpServletRequest req) {
		this.prepService(req);		
		log.info("params: channel:" + channelIds + ";user:" + userToken + ";ipg:" + ipgId);
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
	 * @param channel channel id
	 * @param program program id
	 * @return A unique IPG identifier
	 */
	@RequestMapping(value="saveIpg")
	public ResponseEntity<String> saveIpg(@RequestParam(value="user", required=false) String userToken, 
			                              @RequestParam(value="channel", required=false) String channelId, 
			                              @RequestParam(value="program", required=false) String programId, 
			                              HttpServletRequest req) {		
		this.prepService(req);
		log.info("saveIpg(" + userToken + ")");
		String output = NnStatusMsg.errorStr(locale);		
		try {
			output = playerApiService.saveIpg(userToken, channelId, programId);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}	
	
	/**
	 * Load User IPG (snapshot)
	 *
	 * @param ipg IPG's unique identifier
	 * @return  Returns a program to play follows ipg information.
	 * 	        The program to play returns in the 2nd section, format please reference programInfo format.
	 *          3rd section is ipg information, format please reference channelLineup.
	 */
	@RequestMapping(value="loadIpg")
	public ResponseEntity<String> loadIpg(@RequestParam(value="ipg") Long ipgId, 
										  HttpServletRequest req) {		
		log.info("ipgId:" + ipgId);		
		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);		
		try {
			output = playerApiService.loadIpg(ipgId);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);						
	}

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
		log.info("user=" + userToken + ";session=" + session);
		try {
			output = playerApiService.processPdr(userToken, pdr, session);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}

	/**
	 * Mark a program bad when player sees it 
	 * 
	 * @param user user token
	 * @param program programId
	 */	
	@RequestMapping(value="programRemove")
	public ResponseEntity<String> programRemove(@RequestParam(value="program", required=false) String programId,
				                                @RequestParam(value="user", required=false) String userToken,
				                                HttpServletRequest req) {
		this.prepService(req);
		String output = NnStatusMsg.errorStr(locale);
		log.info("bad program:" + programId + ";reported by user:" + userToken);
		try {
			output = playerApiService.markBadProgram(programId, userToken);
		} catch (Exception e) {
			output = playerApiService.handleException(e);
		}
		return NnNetUtil.textReturn(output);
	}
	
}