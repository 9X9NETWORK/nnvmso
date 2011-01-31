package com.nnvmso.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreNeedIndexException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoConfig;
import com.nnvmso.model.MsoIpg;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;

@Service
public class PlayerApiService {
	
	private static MessageSource messageSource = new ClassPathXmlApplicationContext("locale.xml");
	protected static final Logger log = Logger.getLogger(PlayerApiService.class.getName());	
	
	private NnUserManager userMngr = new NnUserManager();	
	private MsoManager msoMngr = new MsoManager();
	private Locale locale;
	private Mso mso;
	private String separatorStr = "--\n";
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	public void setMso(Mso mso) {
		this.mso = mso;
	}	
	
	public String findMsoInfo(HttpServletRequest req) {
		Mso theMso = msoMngr.findMsoViaHttpReq(req);
		if (theMso == null) {return NnStatusMsg.msoInvalid(locale);}
		String results = NnStatusMsg.successStr(locale) + separatorStr;
		results = results + this.assembleKeyValue("key", String.valueOf(mso.getKey().getId()));
		results = results + this.assembleKeyValue("name", mso.getName());
		results = results + this.assembleKeyValue("logoUrl", mso.getLogoUrl());
		results = results + this.assembleKeyValue("jingleUrl", mso.getJingleUrl());
		results = results + this.assembleKeyValue("logoClickUrl", mso.getJingleUrl());
		results = results + this.assembleKeyValue("preferredLangCode", mso.getPreferredLangCode()); 
		return results;
	}

	public String handleException (Exception e) {
		String output = NnStatusMsg.errorStr(locale);
		if (e.getClass().equals(DatastoreTimeoutException.class)) {
			output = NnStatusCode.DATABASE_TIMEOUT + "\t" + "database timeout";			
		} else if (e.getClass().equals(NoSuchMessageException.class)) {			
			output = NnStatusCode.OUTPUT_NO_MSG_DEFINED + "\t" + "oops, system does not define this error msg.";
		} else if (e.getClass().equals(DatastoreFailureException.class)) {
			output = NnStatusCode.DATABASE_ERROR + "\t" + "database internal error";
		} else if (e.getClass().equals(DatastoreNeedIndexException.class)) {
			output = NnStatusCode.DATABASE_NEED_INDEX + "\t" + "index is still building, fatal.";
		}
		NnLogUtil.logException((Exception) e);
		return output;
	}	
	
	public String processPdr(String userToken, String pdr, String session) {
		//verify input
		if (userToken == null || userToken.length() == 0 || userToken.equals("undefined")) {
			return NnStatusMsg.inputMissing(locale);
		}
		if (pdr == null || pdr.length() == 0) {return NnStatusMsg.successStr(locale);};
		
		//verify user
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) {return NnStatusMsg.userInvalid(locale);}
		
		//pdr process
		String output = NnStatusMsg.errorStr(locale);
		PdrRawManager pdrMngr = new PdrRawManager();
		pdrMngr.processPdr(pdr, user.getKey().getId(), session);
		output = NnStatusMsg.successStr(locale);
		return output;
	}
	
	public String saveIpg(String userToken) {
		if (userToken == null || userToken.length() == 0 || userToken.equals("undefined")) {
			return NnStatusMsg.inputMissing(locale);
		}				
		NnUser foundUser = userMngr.findByToken(userToken);				
		if (foundUser == null) { return NnStatusMsg.userInvalid(locale);}
		
		Ipg ipg = new Ipg();
		IpgManager ipgMngr = new IpgManager();
		ipgMngr.create(ipg, foundUser.getKey().getId());				
		return NnStatusMsg.successStr(locale) + separatorStr + Long.toString(ipg.getId());				
	}
	
	public String loadIpg(long ipgId) {
		IpgManager ipgMngr = new IpgManager();
		Ipg ipg = ipgMngr.findById(ipgId);
		if (ipg == null) { return messageSource.getMessage("nnstatus.ipg_invalid", new Object[] {NnStatusCode.IPG_INVALID} , locale);} 
		List<MsoChannel> channels = ipgMngr.findIpgChannels(ipg);
		String output = NnStatusMsg.successStr(locale);
		for (MsoChannel c : channels) {
			output = output + this.composeChannelLineupStr(c, mso);
			output = output + "\n";			
		}
		return output;		
	}
			
	public String moveChannel(String userToken, String grid1, String grid2) {		
		//verify input
		if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") || grid1 == null || grid2 == null) {
			return NnStatusMsg.inputMissing(locale);
		}
		if (!Pattern.matches("^\\d*$", grid1) || !Pattern.matches("^\\d*$", grid2)) {
			return messageSource.getMessage("nnstatus.input_error", new Object[] {NnStatusCode.SUCCESS} , locale);
		}		
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) { return messageSource.getMessage("nnstatus.input_error", new Object[] {NnStatusCode.SUCCESS} , locale); }
		
		SubscriptionManager subMngr = new SubscriptionManager();
		boolean success = subMngr.changeSeq(user.getKey().getId(), Integer.parseInt(grid1), Integer.parseInt(grid2));
		if (success) { return NnStatusMsg.successStr(locale); }
		return NnStatusMsg.successStr(locale);
	}
	
	public String findLocaleByHttpRequest(HttpServletRequest req) {
		String ip = req.getRemoteAddr();
		log.info("findLocaleByHttpRequest() ip is " + ip);
        String country = "";
		try {
			URL url = new URL("http://brussels.teltel.com/geoip/?ip=" + ip);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
	        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
	        	log.info("findLocaleByHttpRequest() IP service returns error:" + connection.getResponseCode());	        	
	        }
	        BufferedReader rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));;
	        if (rd.readLine()!=null) {country = rd.readLine().toLowerCase();} //assuming one line	        
		} catch (Exception e) {
			NnLogUtil.logException(e);
		}
        String localeCode = "en";
		if (country.equals("tw") || country.equals("cn") || country.equals("hk")) {
			localeCode = "zh";
		} else if (country.equals("")) {
			localeCode = "default";
		}
		return NnStatusMsg.successStr(locale) + separatorStr + localeCode;
	}
	 
//	public String findCategoriesByUser(String userKey) {
//		//verify input
//		if (userKey == null || userKey.length() == 0 || userKey.equals("undefined")) {
//			return NnStatusMsg.inputMissing(locale);
//		}		
//		//verify user		
//		NnUser user = userMngr.findByKeyStr(userKey);
//		if (user == null) {
//			return NnStatusMsg.userInvalid(locale);
//		}		
//		//find categories
//		return NnStatusMsg.successStr(locale) + this.getCategoriesByMsoKey(user.getMsoKey(), locale);
//	}
			
	public String findPublicChannelsByCategory(String categoryId) {		
		//verify input
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		if (categoryId == null || categoryId.length() < 1) { return NnStatusMsg.inputMissing(locale); }
		if (!Pattern.matches("^\\d*$", categoryId)) { return NnStatusMsg.inputError(locale); }
		
		//find public channels by categoryId
		MsoChannelManager channelMngr = new MsoChannelManager();
		channels = channelMngr.findPublicChannelsByCategoryId(Long.parseLong(categoryId));
		if (channels == null) { return NnStatusMsg.successStr(locale);}
		
		//assemble output
		log.info("find " + channels.size() + " of channels in category, category id:" + categoryId);
		String result = NnStatusMsg.successStr(locale) + separatorStr;
		result = result + categoryId + "\n" + separatorStr;
		for (int i=0; i< channels.size(); i++) {	
			if (channels.get(i).getProgramCount() > 0 ) {
				String[] ori = {String.valueOf(channels.get(i).getSeq()),
						        String.valueOf(channels.get(i).getKey().getId()), 
						        channels.get(i).getName(), 
						        channels.get(i).getImageUrl(), 
						        Integer.toString(channels.get(i).getProgramCount())};
				result = result + NnStringUtil.getDelimitedStr(ori);
				result = result + "\n";
			}
		}
		return result;
	}	
		
	public String createGuest(String ipgId, HttpServletRequest req, HttpServletResponse resp) {
		//verify input
		if (mso == null) { return NnStatusMsg.errorStr(locale); }
		IpgManager ipgMngr = new IpgManager();
		Ipg ipg = null;
		if (ipgId != null) {
			ipg = ipgMngr.findById(Long.decode(ipgId));
			if (ipg == null) 
				return messageSource.getMessage("nnstatus.ipg_invalid", new Object[] {NnStatusCode.IPG_INVALID} , locale);
		}		
		
		//create guest
		String password = String.valueOf(("token" + Math.random() + new Date().getTime()).hashCode());
		NnUser guest = new NnUser(NnUser.GUEST_EMAIL, password, NnUser.GUEST_NAME, NnUser.TYPE_USER, mso.getKey().getId());		
		userMngr.create(guest);
		System.out.println(guest.getToken());
		
		//subscribe default channels
		if (ipg != null) {
			SubscriptionManager sMngr = new SubscriptionManager();
			List<MsoChannel> ipgChannels = ipgMngr.findIpgChannels(ipg);
			for (MsoChannel c : ipgChannels)
				sMngr.subscribeChannel(guest.getKey().getId(), c.getKey().getId(), c.getSeq(), c.getType());			
		} else {
			userMngr.subscibeDefaultChannels(guest);
		}
		
		//prepare cookie and output
		String output = this.prepareUserInfo(guest);
		this.setUserCookie(resp, guest.getToken());
		return output;
	}

	//assemble key and value string
	private String assembleKeyValue(String key, String value) {
		return key + "\t" + value + "\n";
	}
	
	//Prepare user info, it is used by login, guest register, userTokenVerify
	public String prepareUserInfo(NnUser user) {
		String output = NnStatusMsg.successStr(locale) + separatorStr;		
		output = output + assembleKeyValue("token", user.getToken());
		output = output + assembleKeyValue("name", user.getName()); 		
		return output;
	}
	
	public void setUserCookie(HttpServletResponse resp, String userId) {
		CookieHelper.setCookie(resp, CookieHelper.USER, userId);
	}	
		
	public String unsubscribeChannel(String userToken, String channelId) {
		//verify input
		if (userToken == null || channelId == null || userToken.equals("undefined")) {			
			return NnStatusMsg.inputMissing(locale);
		}
		if (!Pattern.matches("^\\d*$", channelId)) {
			return NnStatusMsg.inputError(locale);
		}		
		//verify user
		NnUser user = new NnUserManager().findByToken(userToken);
		if (user == null) {return NnStatusMsg.userInvalid(locale);}		
		SubscriptionManager subMngr = new SubscriptionManager();
		subMngr.unsubscribeChannel(user.getKey().getId(), Long.parseLong(channelId));
		return NnStatusMsg.successStr(locale);		
	}
	
	public String subscribeChannel(String userToken, String channelId, String grid) {
		//verify input
		if (userToken == null || channelId == null || grid == null || userToken.equals("undefined")) {			
			return NnStatusMsg.inputMissing(locale);
		}
		if (!Pattern.matches("^\\d*$", channelId) || !Pattern.matches("^\\d*$", grid)) {
			return NnStatusMsg.inputError(locale);
		}		
		//verify user and channel
		String output = messageSource.getMessage("nnstatus.channel_or_user_invalid", new Object[] {NnStatusCode.CHANNEL_OR_USER_INVALID} , locale);		
		NnUser user = new NnUserManager().findByToken(userToken);
		if (user == null) {return output;}		
		MsoChannel channel = new MsoChannelManager().findById(Long.parseLong(channelId));
		if (channel == null || channel.getStatus() == MsoChannel.STATUS_ERROR) { return output;}			
				
		//subscribe
		SubscriptionManager subMngr = new SubscriptionManager();
		boolean status = subMngr.subscribeChannel(user.getKey().getId(), channel.getKey().getId(), Integer.valueOf(grid), MsoIpg.TYPE_GENERAL);
		if (status) {
			output = NnStatusMsg.successStr(locale);
		} else {
			output = messageSource.getMessage("nnstatus.subscription_duplicate_channel", new Object[] {NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL} , locale);
		}
		return output;		
	}

	public String createUser(String email, String password, String name, String userToken, 
			                 HttpServletRequest req, HttpServletResponse resp) {
		//verify input		
		if (email == null || password == null || name == null ||
			email.length() == 0 || password.length() == 0 || name.length() == 0 ||
			email.equals("undefined")) {
			return NnStatusMsg.inputMissing(locale);
		}
		String regex = "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$";
		if (!Pattern.matches(regex, email) || password.length() < 6) {		
			return messageSource.getMessage("nnstatus.input_error", new Object[] {NnStatusCode.SUCCESS} , locale);
		}
			
		//find mso
		if (mso == null) { return NnStatusMsg.msoInvalid(locale);}
		
		//verify email
		NnUser user = userMngr.findByEmailAndMso(email, mso);
		if (user != null) {
			log.info("user email taken:" + user.getEmail() + "; mso=" + mso.getName() + ";user token=" + user.getToken());
			return messageSource.getMessage("nnstatus.user_email_taken", new Object[] {NnStatusCode.USER_EMAIL_TAKEN} , locale);
		}
		
		//create user
		if (userToken != null) { user = userMngr.findByToken(userToken);}
		if (user == null ) {
			log.info("User signup userToken NOT FOUND. Token=" + userToken);
			user = new NnUser(email, password, name, NnUser.TYPE_USER, mso.getKey().getId());
			userMngr.create(user); 
			userMngr.subscibeDefaultChannels(user);
		} else {
			log.info("User signup with guest token=" + userToken + "; email=" + user.getEmail() + "; name=" + user.getName());					 		
			if (user.getEmail().equals(NnUser.GUEST_EMAIL)) {
				log.info("1st time signup after being a guest");
				user.setEmail(email);
				user.setPassword(password);
				user.setName(name);
				userMngr.save(user);
			} else {
				return messageSource.getMessage("nnstatus.user_token_taken", new Object[] {NnStatusCode.USER_TOKEN_TAKEN} , locale);				
			}
		}
		String output = this.prepareUserInfo(user);
		this.setUserCookie(resp, user.getToken());
		return output;
	}
	
	public String findUserByToken(String token, HttpServletRequest req, HttpServletResponse resp) {
		if (token == null) {return NnStatusMsg.inputMissing(locale);}
		
		NnUser found = userMngr.findByToken(token);	
		
		if (found == null || (found != null && found.getMsoId() != mso.getKey().getId())) {
			CookieHelper.deleteCookie(resp, CookieHelper.USER);
			return NnStatusMsg.userInvalid(locale);
		}
		this.setUserCookie(resp, found.getToken());	
		return this.prepareUserInfo(found);
	}
	
	public String findAuthenticatedUser(String email, String password, HttpServletRequest req, HttpServletResponse resp) {		
		log.info("login: email=" + email + "; mso=" + mso.getName());
		String output = messageSource.getMessage("nnstatus.user_login_failed", new Object[] {NnStatusCode.USER_LOGIN_FAILED} , locale);		
		NnUser user = userMngr.findAuthenticatedUser(email, password, mso.getKey().getId());
		if (user != null) {
			output = this.prepareUserInfo(user);
			this.setUserCookie(resp, user.getToken());
		}
		return output;
	}

	public String createChannel(String categoryIds, String userToken, String url, String grid, HttpServletRequest req) {
		//verify input
		if (url == null || url.length() == 0 ||  grid == null || 
			categoryIds == null || categoryIds.equals("undefined") ||
			userToken== null || userToken.length() == 0 || grid.length() == 0) {
			return NnStatusMsg.inputMissing(locale);
		}
		
		//verify user
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) { return NnStatusMsg.userInvalid(locale);}		
		if (user.getEmail().equals(NnUser.GUEST_EMAIL)) {
			return messageSource.getMessage("nnstatus.user_permission_error", new Object[] {NnStatusCode.USER_PERMISSION_ERROR} , locale);
		}
				
		//verify category
		CategoryManager categoryMngr = new CategoryManager();
		List<Category> categories = categoryMngr.findAllByIds(categoryIds);
		if (categories.size() == 0) { return messageSource.getMessage("nnstatus.category_invalid", new Object[] {NnStatusCode.CATEGORY_INVALID} , locale); }
		

		//verify whether a duplication
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findBySourceUrl(url);
		boolean duplicate = false;
		if (channel != null) {
			duplicate = true;
			log.info("User submits a duplicate url:" + url);			
			//update category if necessary
			CategoryChannelManager ccMngr = new CategoryChannelManager();
			// --find existing categories
			List<CategoryChannel> ccs = ccMngr.findAllByChannelId(channel.getKey().getId()); 			
			HashMap<Long, String> existing = new HashMap<Long, String>();
			for (CategoryChannel cc : ccs) {
				existing.put(cc.getCategoryId(), "");
			}
			// --find new category user defines if there's any 
			String[] ids = categoryIds.split(",");
			List<String> newIds = new ArrayList<String>();
			for (int i=0; i<ids.length; i++) {		
				if (!existing.containsKey(Long.parseLong(ids[i]))) {newIds.add(ids[i]);}
			}
			// --add new category
			if (newIds.size() > 0) {
				HashMap<Long, Category> categoryMap = new HashMap<Long, Category>();
				for (Category c : categories) {
					categoryMap.put(c.getKey().getId(), c);
				}
				for (String id : newIds) {
					Category c = categoryMap.get(Long.valueOf(id));
					log.info("a duplicate url but new category:" + c.getName() + ";" + c.getKey().getId());
					ccMngr.create(new CategoryChannel(c.getKey().getId(), channel.getKey().getId()));
				}
			}
		}
		
		//verify whether it's a bad channel
		if (duplicate && channel.getStatus() != MsoChannel.STATUS_SUCCESS) {
			return messageSource.getMessage("nnstatus.channel_status_error", new Object[] {NnStatusCode.CHANNEL_STATUS_ERROR} , locale);
		}
		
		//verify source url
		if (!duplicate) {
			channel = channelMngr.initChannelSubmittedFromPlayer(url, user);
			if (!channelMngr.verifyPodcastUrl(channel)) {
				return messageSource.getMessage("nnstatus.channel_url_invalid", new Object[] {NnStatusCode.CHANNEL_URL_INVALID} , locale);				
			}
			//create a new channel
			log.info("User throws a new url:" + url);
			System.out.println("channel=" + channel.getName() + ";img=" + channel.getImageUrl());
			channelMngr.create(channel, categories);
			if (channel.getKey() != null) {
				TranscodingService tranService = new TranscodingService();
				tranService.submitToTranscodingService(NnStringUtil.getKeyStr(channel.getKey()), url, req);
			}
		}
		
		//subscribe
		SubscriptionManager subMngr = new SubscriptionManager();
		subMngr.subscribeChannel(user.getKey().getId(), channel.getKey().getId(), Integer.parseInt(grid), MsoIpg.TYPE_GENERAL);		
		String result[]= {String.valueOf(channel.getKey().getId()),				  	 	  
				  	 	  channel.getName(),
				  	 	  channel.getImageUrl()};
		return NnStatusMsg.successStr(locale) + separatorStr + NnStringUtil.getDelimitedStr(result);
	}
				
	public String findSubscribedChannels(String userToken, boolean userInfo) {
		//verify input
		if (userToken == null) {return NnStatusMsg.inputMissing(locale);}
		
		//verify user
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) {return NnStatusMsg.userInvalid(locale);}
		
		String result = NnStatusMsg.successStr(locale) + separatorStr;
		if (userInfo) { result = this.prepareUserInfo(user) + separatorStr;	}
		
		//find subscribed channels 
		SubscriptionManager subMngr = new SubscriptionManager();
		List<MsoChannel> channels = subMngr.findSubscribedChannels(user.getKey().getId());
		for (MsoChannel c : channels) {
			result = result + this.composeChannelLineupStr(c, mso);
			result = result + "\n";
		}
		return result;
	}
	
	public String findProgramInfo(String channelIds, String userToken, String ipgId, boolean userInfo) {
		if (channelIds == null || (channelIds.equals("*") && userToken == null)) {
			return NnStatusMsg.inputMissing(locale);
		}
		MsoProgramManager programMngr = new MsoProgramManager();		
		String[] chArr = channelIds.split(",");
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		NnUser user = null;
		if (channelIds.equals("*") && ipgId != null) {
			IpgManager ipgMngr = new IpgManager();
			Ipg ipg = ipgMngr.findById(Long.parseLong(ipgId));
			if (ipg == null) { return messageSource.getMessage("nnstatus.ipg_invalid", new Object[] {NnStatusCode.IPG_INVALID} , locale);}
			programs = ipgMngr.findIpgPrograms(ipg);
			log.info("ipg program count: " + programs.size());
		} else if (channelIds.equals("*")) {
			user = userMngr.findByToken(userToken);
			if (user == null) { return NnStatusMsg.userInvalid(locale); }
			programs = programMngr.findSubscribedPrograms(user.getKey().getId());
		} else if (chArr.length > 1) {
			List<Long> list = new ArrayList<Long>();
			for (int i=0; i<chArr.length; i++) { list.add(Long.valueOf(chArr[i]));}
			programs = programMngr.findAllByChannelIdsAndIsPublic(list, true);
		} else {
			programs = programMngr.findAllByChannelId(Long.parseLong(channelIds));
		}		
				
		MsoConfig config = new MsoConfigManager().findByMsoIdAndItem(mso.getKey().getId(), MsoConfig.CDN);
		if (config == null) {
			config = new MsoConfig(mso.getKey().getId(), MsoConfig.CDN, MsoConfig.CDN_AMAZON);
			log.severe("mso config does not exist! mso: " + mso.getKey());
		}
		System.out.println("program size:" + programs.size());		
		String result = NnStatusMsg.successStr(locale) + separatorStr;
		if (userInfo) {
			if (user == null && userToken != null) {user = userMngr.findByToken(userToken);}
			result = this.prepareUserInfo(user) + separatorStr; 
		}
		return result + this.composeProgramInfoStr(programs, config);
	}

	public String findCategoriesByMso() {
		CategoryManager categoryMngr = new CategoryManager();	
		List<Category> categories = categoryMngr.findAllByMsoId(mso.getKey().getId());
				
		String output = NnStatusMsg.successStr(locale) + separatorStr;
		for (Category c : categories) {
			String[] str = {String.valueOf(c.getKey().getId()), c.getName(), String.valueOf(c.getChannelCount())};
			output = output + NnStringUtil.getDelimitedStr(str) + "\n";			
		}
		
		if (categories.size() < 1) { return messageSource.getMessage("nnstatus.category_invalid", new Object[] {NnStatusCode.CATEGORY_INVALID} , locale);}
			
		return output;
	}
		
	private String composeChannelLineupStr(MsoChannel c, Mso mso) {
		String intro = c.getIntro();
		if (intro != null) {
			int introLenth = (intro.length() > 256 ? 256 : intro.length()); 
			intro = intro.substring(0, introLenth);
		} else {
			intro = "";
		}
		String imageUrl = c.getImageUrl();
		if (c.getStatus() == MsoChannel.STATUS_ERROR) {
			imageUrl = "/WEB-INF/../images/error.png";
		} else if (c.getStatus() == MsoChannel.STATUS_PROCESSING) {
			if (mso.getPreferredLangCode().equals(Mso.LANG_ZH_TW)) {
				imageUrl = "/WEB-INF/../images/processing_cn.png";
			}
		}

		String[] ori = {Integer.toString(c.getSeq()), 
					    String.valueOf(c.getKey().getId()),
					    c.getName(),
					    intro,
					    imageUrl,
					    String.valueOf(c.getProgramCount()),
					    String.valueOf(c.getType()),
					    String.valueOf(c.getStatus())}; 					    
		String output = NnStringUtil.getDelimitedStr(ori);
		return output;
	}
	
	private String composeProgramInfoStr(List<MsoProgram> programs, MsoConfig config) {		
		String output = "";
		
		String regexCache = "^(http|https)://(9x9cache.s3.amazonaws.com|s3.amazonaws.com/9x9cache)";
		String regexPod = "^(http|https)://(9x9pod.s3.amazonaws.com|s3.amazonaws.com/9x9pod)";
		String cache = "http://cache.9x9.tv";
		String pod = "http://pod.9x9.tv";
		
		for (MsoProgram p : programs) {
			//file urls
			String url1 = p.getMpeg4FileUrl();
			String url2 = p.getWebMFileUrl();
			String url3 = p.getOtherFileUrl();
			String url4 = p.getAudioFileUrl();
			if (url1 == null) {url1 = "";}
			if (url2 == null) {url2 = "";}
			if (url3 == null) {url3 = "";}
			if (url4 == null) {url4 = "";}	
			String imageUrl = p.getImageUrl();
			String imageLargeUrl = p.getImageLargeUrl();
			if (imageUrl == null) {imageUrl = "";}
			if (imageLargeUrl == null) {imageLargeUrl = "";}	
			if (config.getValue().equals(MsoConfig.CDN_AKAMAI)) {
				log.info("akamai replacement");
				url1 = url1.replaceFirst(regexCache, cache);
				url1 = url1.replaceAll(regexPod, pod);
				url2 = url2.replaceFirst(regexCache, cache);
				url2 = url2.replaceAll(regexPod, pod);
				url3 = url3.replaceFirst(regexCache, cache);
				url3 = url3.replaceAll(regexPod, pod);
				url4 = url4.replaceFirst(regexCache, cache);
				url4 = url4.replaceAll(regexPod, pod);
				imageUrl = imageUrl.replaceFirst(regexCache, cache);
				imageUrl = imageUrl.replaceAll(regexPod, pod);
				imageLargeUrl = imageLargeUrl.replaceFirst(regexCache, cache);
				imageLargeUrl = imageLargeUrl.replaceAll(regexPod, pod);				 
			}
					
			//intro
			String intro = p.getIntro();			
			if (intro != null) {
				int introLenth = (intro.length() > 256 ? 256 : intro.length()); 
				intro = intro.replaceAll("\\s", " ");				
				intro = intro.substring(0, introLenth);
			}
					
			//the rest
			String[] ori = {String.valueOf(p.getChannelId()), 
					        String.valueOf(p.getKey().getId()), 
					        p.getName(), 
					        intro,
					        String.valueOf(p.getType()), 
					        p.getDuration(),
					        imageUrl,
					        imageLargeUrl,
					        url1, 
					        url2, 
					        url3, 
					        url4, 
					        String.valueOf(p.getUpdateDate().getTime())};
			output = output + NnStringUtil.getDelimitedStr(ori);
			output = output.replaceAll("null", "");
			output = output + "\n";
		}
		return output;		
	}
	
	public String findNewPrograms(String userToken) {
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) {return NnStatusMsg.userInvalid(locale);}
		MsoProgramManager programMngr = new MsoProgramManager();
		List<MsoProgram> programs = programMngr.findNew(user.getKey().getId());
		String output = NnStatusMsg.successStr(locale) + separatorStr;
		for (MsoProgram p : programs) {
			output = output + p.getKey().getId() + "\n";			
		}
		return output;		
	}
	
}