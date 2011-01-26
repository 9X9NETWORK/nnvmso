package com.nnvmso.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreNeedIndexException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Key;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoIpg;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.PdrRaw;
import com.nnvmso.model.Watched;
import com.nnvmso.lib.*;

@Service
public class PlayerApiService {
	
	private static MessageSource messageSource = new ClassPathXmlApplicationContext("locale.xml");
	
	private Locale myLocale;
	
	public void setLocale(Locale myLocale) {
		this.myLocale = myLocale;
	}
	 	
	protected static final Logger log = Logger.getLogger(PlayerApiService.class.getName());	
	
	private NnUserManager userMngr = new NnUserManager();	
	private MsoManager msoMngr = new MsoManager();
	
	public String hello() {
		return messageSource.getMessage("nnstatus.input_missing", new Object[] {NnStatusCode.INPUT_ERROR}, myLocale);
		
	}

	public String processPdr(String userKey, String pdr, Locale locale) {
		if (userKey == null || userKey.length() == 0 || userKey.equals("undefined")) {
			return NnStatusMsg.inputMissing(locale);
		}
		if (pdr == null || pdr.length() == 0) {return NnStatusMsg.successStr(locale);};
		NnUser user = userMngr.findByKeyStr(userKey);
		if (user == null) {return NnStatusMsg.userInvalid(locale);}
		
		String output = NnStatusMsg.errorStr(locale);
		log.info("original pdr: \n" + pdr);
		String[] lines = pdr.split("\n");
		MsoChannelManager channelMngr = new MsoChannelManager();		
		WatchedManager watchedMngr= new WatchedManager();
		PdrRawManager pdrMngr = new PdrRawManager();
		for (String line : lines) {			
			String[] data = line.split("\t");	
			String verb = data[0]; 
			if (verb.equals("watched")) {
				long channelId = Long.parseLong(data[1]);
				MsoChannel c = channelMngr.findById(channelId);
				Watched watched = watchedMngr.findByUserKeyAndChannelKey(user.getKey(), c.getKey());
				if (c != null) {
					for (int i=2; i< data.length; i++) {
						long programId = Long.parseLong(data[i]);
						if (watched == null) {
							watched = new Watched();
							watched.setChannelKey(c.getKey());
							watched.setUserKey(user.getKey());
							watched.getPrograms().add(programId);
							watchedMngr.create(watched);
						} else {
							System.out.println("detach problem occurs:" + watched.getPrograms().toString() );							
						    if (!watched.getPrograms().contains(programId)) {
						    	watched.getPrograms().add(programId);
						    	watchedMngr.save(watched);
						    }
						}
					}
				}
			} else {
				PdrRaw raw = new PdrRaw(user.getKey(), verb, line.replaceFirst(data[0]+"\t", ""));
				pdrMngr.create(raw);
			}
		}
		output = NnStatusMsg.successStr(locale);
		return output;
	}
	
	public String handleException (Exception e, Locale locale) {
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
	
	public String saveIpg(String userKey, Locale locale) {
		if (userKey == null || userKey.length() == 0 || userKey.equals("undefined")) {
			return NnStatusMsg.inputMissing(locale);
		}				
		NnUser foundUser = userMngr.findByKeyStr(userKey);				
		if (foundUser == null) { return NnStatusMsg.userInvalid(locale);}				
		Ipg ipg = new Ipg(foundUser);
		IpgManager ipgMngr = new IpgManager();
		ipgMngr.save(ipg);				
		return NnStatusMsg.successStr(locale) + "\n" + Long.toString(ipg.getId());				
	}
	
	public String loadIpg(long ipgId, Locale locale) {
		IpgManager ipgMngr = new IpgManager();
		Ipg ipg = ipgMngr.findById(ipgId);
		if (ipg == null) { return messageSource.getMessage("nnstatus.ipg_invalid", new Object[] {NnStatusCode.IPG_INVALID} , locale);} 
		List<MsoChannel> channels = ipgMngr.findIpgChannels(ipg);
		String output = NnStatusMsg.successStr(locale);
		for (MsoChannel c : channels) {
			output = output + this.composeChannelLineupStr(c);
			output = output + "\n";			
		}
		return output;		
	}
		
	public Locale getLocaleByMso(short msoType) {
		Locale locale = Locale.ENGLISH;
		if (msoType != Mso.TYPE_NN) { locale = Locale.TRADITIONAL_CHINESE; }
		return locale;
	}
	
	public String moveChannel(String userKey, String grid1, String grid2, Locale locale) {		
		//verify input
		if (userKey == null || userKey.length() == 0 || userKey.equals("undefined") || grid1 == null || grid2 == null) {
			return NnStatusMsg.inputMissing(locale);
		}
		if (!Pattern.matches("^\\d*$", grid1) || !Pattern.matches("^\\d*$", grid2)) {
			return messageSource.getMessage("nnstatus.input_error", new Object[] {NnStatusCode.SUCCESS} , locale);
		}		
		NnUser user = userMngr.findByKeyStr(userKey);
		if (user == null) { return messageSource.getMessage("nnstatus.input_error", new Object[] {NnStatusCode.SUCCESS} , locale); }
		
		SubscriptionManager subMngr = new SubscriptionManager();
		boolean success = subMngr.changeSeq(user.getKey(), Integer.parseInt(grid1), Integer.parseInt(grid2));
		if (success) { return NnStatusMsg.successStr(locale); }
		return NnStatusMsg.successStr(locale);
	}
	
	public String findMsoInfo(HttpServletRequest req, Locale locale) {
		Mso mso = msoMngr.findMsoViaHttpReq(req);
		if (mso == null) {return NnStatusMsg.msoInvalid(locale);}
		String results = NnStatusMsg.successStr(locale);
		results = results + "key" + "\t" +NnStringUtil.getKeyStr(mso.getKey()) + "\n";
		results = results + "name"  + "\t" + mso.getName() + "\n";
		results = results + "loglUrl"  + "\t" + mso.getLogoUrl() + "\n";
		results = results + "jingleUrl" + "\t"+ mso.getJingleUrl() + "\n";
		results = results + "preferredLangCode" + "\t" + mso.getPreferredLangCode() + "\n"; 
		return results;
	}

	public String findLocaleByHttpRequest(HttpServletRequest req, Locale locale) {
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
		return NnStatusMsg.successStr(locale) + localeCode;
	}
	 
	public String findCategoriesByUser(String userKey, HttpServletRequest req) {
		Locale locale = this.getLocaleByMso(msoMngr.findMsoTypeViaHttpReq(req));
		//verify input
		if (userKey == null || userKey.length() == 0 || userKey.equals("undefined")) {
			return NnStatusMsg.inputMissing(locale);
		}		
		//verify user		
		NnUser user = userMngr.findByKeyStr(userKey);
		if (user == null) {
			return NnStatusMsg.userInvalid(locale);
		}		
		//find categories
		return NnStatusMsg.successStr(locale) + this.getCategoriesByMsoKey(user.getMsoKey(), locale);
	}
			
	public String findPublicChannelsByCategory(String categoryId, Locale locale) {		
		//verify input
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		if (categoryId == null || categoryId.length() < 1) { return NnStatusMsg.inputMissing(locale); }
		if (!Pattern.matches("^\\d*$", categoryId)) { return NnStatusMsg.inputError(locale); }
		
		//find public channels by categoryId
		MsoChannelManager channelMngr = new MsoChannelManager();
		channels = channelMngr.findPublicChannelsByCategoryId(Long.parseLong(categoryId));
		if (channels == null) {
			return NnStatusMsg.successStr(locale); 
		}
		log.info("find " + channels.size() + " of channels in category, category id:" + categoryId);
		String result = NnStatusMsg.successStr(locale);
		result = result + categoryId + "\n";
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
	
	public String findCategoriesByMsoName(String msoName, HttpServletRequest req, Locale locale) {
		//give a default MSO if msoName does not exist		
		Mso mso = msoMngr.findMsoViaHttpReq(req);
		return this.getCategoriesByMsoKey(mso.getKey(), locale);
	}
	
	public String guestCreate(String ipgId, HttpServletRequest req, HttpServletResponse resp, Locale locale) {				
		// find mso 
		Mso mso = msoMngr.findMsoViaHttpReq(req);
		if (mso == null) { return NnStatusMsg.errorStr(locale); }

		if (ipgId != null) {
			IpgManager ipgMngr = new IpgManager();
			Ipg ipg = ipgMngr.findById(Long.decode(ipgId));
			if (ipg == null) { 
				return messageSource.getMessage("nnstatus.ipg_invalid", new Object[] {NnStatusCode.IPG_INVALID} , locale);
			}
		}		
				
		//create guest
		String password = String.valueOf(("token" + Math.random() + new Date().getTime()).hashCode());
		NnUser guest = new NnUser(NnUser.GUEST_EMAIL, password, NnUser.GUEST_NAME, NnUser.TYPE_USER, mso.getKey());		
		userMngr.create(guest);
		
		//subscribe default channels		
		userMngr.subscibeDefaultChannels(guest);
		String output = this.prepareUserInfo(guest, req, resp);
		return output;
	}
	
	public String prepareUserInfo(NnUser user, HttpServletRequest req, HttpServletResponse resp) {
		String[] results = {"0", 
							String.valueOf(NnStringUtil.getKeyStr(user.getKey())),
							user.getName(),
							String.valueOf(user.getMsoKey().getId())};
		String output = NnStringUtil.getDelimitedStr(results);
		this.setUserCookie(resp, NnStringUtil.getKeyStr(user.getKey()));
		Locale locale = this.getLocaleByMso(msoMngr.findMsoTypeViaHttpReq(req));
		return NnStatusMsg.successStr(locale) + output;
	}
	
	public String channelSubscribe(String userKey, String channelId, String grid, Locale locale) {
		if (userKey == null || channelId == null || grid == null ||
			userKey.equals("undefined")) {
			return NnStatusMsg.inputMissing(locale);
		}
		if (!Pattern.matches("^\\d*$", channelId) || Pattern.matches("^\\d*$", grid)) {
			return NnStatusMsg.inputError(locale);
		}
		
		String output = messageSource.getMessage("nnstatus.channel_or_user_invalid", new Object[] {NnStatusCode.CHANNEL_OR_USER_INVALID} , locale);
		
		NnUser user = new NnUserManager().findByKeyStr(userKey);
		if (user == null) {return output;}		
		MsoChannel channel = new MsoChannelManager().findById(Long.parseLong(channelId));
		if (channel == null || channel.getStatus() == MsoChannel.STATUS_ERROR) { return output;}			
				
		SubscriptionManager subMngr = new SubscriptionManager();
		boolean status = subMngr.channelSubscribe(user, channel, Integer.valueOf(grid), MsoIpg.TYPE_GENERAL);
		if (status) {
			output = NnStatusMsg.successStr(locale);
		} else {
			output = messageSource.getMessage("nnstatus.subscription_duplicate_channel", new Object[] {NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL} , locale);
		}
		return output;		
	}

	public String userCreate(String email, String password, String name, String userToken, HttpServletRequest req, HttpServletResponse resp, Locale locale) {
		//verify input		
		if (email == null || password == null || name == null ||
			email.length() == 0 || password.length() == 0 || name.length() == 0 ||
			email.equals("undefined")) {
			return NnStatusMsg.inputMissing(locale);
		}
		
		//find mso
		Mso mso = msoMngr.findMsoViaHttpReq(req);
		if (mso == null) { return NnStatusMsg.msoInvalid(locale);}
		
		//verify email
		NnUser user = userMngr.findByEmailAndMso(email, mso);
		if (user != null) {
			log.info("user email taken:" + user.getEmail() + ";user msokey=" + user.getMsoKey() + ";msokey:" + mso.getKey());
			return messageSource.getMessage("nnstatus.user_email_taken", new Object[] {NnStatusCode.USER_EMAIL_TAKEN} , locale);
		}
		
		//create user
		if (userToken != null) { user = userMngr.findByKeyStr(userToken);}
		if (user == null ) {
			log.info("User signup userToken NOT FOUND. Token=" + userToken);
			user = new NnUser(email, password, name, NnUser.TYPE_USER, mso.getKey());
			userMngr.create(user); 
			userMngr.subscibeDefaultChannels(user);
		} else {
			log.info("User signup with token=" + userToken + "; email=" + user.getEmail() + "; name=" + user.getName());					 		
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
		String output = this.prepareUserInfo(user, req, resp);
		return output;
	}					
	
	public void setUserCookie(HttpServletResponse resp, String userId) {
		CookieHelper.setCookie(resp, CookieHelper.USER, userId);
	}	
    
	public String findUserByToken(String token, HttpServletRequest req, HttpServletResponse resp, Locale locale) {
		if (token == null) {return NnStatusMsg.inputMissing(locale);}
		
		NnUser found = userMngr.findByKeyStr(token);			
		Mso mso = new MsoManager().findMsoViaHttpReq(req);
		if (found == null || (found != null && !found.getMsoKey().equals(mso.getKey()))) {
			CookieHelper.deleteCookie(resp, CookieHelper.USER);
			return NnStatusMsg.userInvalid(locale);
		}
			
		return this.prepareUserInfo(found, req, resp);
	}
	
	public String findAuthenticatedUser(String email, String password, HttpServletRequest req, HttpServletResponse resp, Locale locale) {		
		Mso mso= new MsoManager().findMsoViaHttpReq(req);		
		String output = messageSource.getMessage("nnstatus.user_login_failed", new Object[] {NnStatusCode.USER_LOGIN_FAILED} , locale);
		NnUser user = userMngr.findAuthenticatedUser(email, password, mso);
		if (user != null) {
			output = this.prepareUserInfo(user, req, resp);
		}
		return output;
	}

	public String createChannel(String categoryIds, String userKey, String url, String grid, HttpServletRequest req, Locale locale) {
		//verify input
		if (url == null || url.length() == 0 ||  grid == null || 
			categoryIds == null || categoryIds.equals("undefined") ||
			userKey == null || userKey.length() == 0 || grid.length() == 0) {
			return NnStatusMsg.inputMissing(locale);
		}
		
		//verify user
		NnUser user = userMngr.findByKeyStr(userKey);
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
			List<CategoryChannel> ccs = ccMngr.findByChannelKey(channel.getKey()); 			
			HashMap<Long, String> existing = new HashMap<Long, String>();
			for (CategoryChannel cc : ccs) {
				existing.put(cc.getCategoryKey().getId(), "");
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
					ccMngr.create(new CategoryChannel(c.getKey(), channel.getKey()));
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
		subMngr.channelSubscribe(user, channel, Integer.parseInt(grid), MsoIpg.TYPE_GENERAL);		
		String result[]= {String.valueOf(channel.getKey().getId()),				  	 	  
				  	 	  channel.getName(),
				  	 	  channel.getImageUrl()};
		return NnStatusMsg.successStr(locale) + NnStringUtil.getDelimitedStr(result);
	}
				
	public String findSubscribedChannels(String userKey, Locale locale) {
		if (userKey == null) {return NnStatusMsg.inputMissing(locale);}
		SubscriptionManager subMngr = new SubscriptionManager();
		List<MsoChannel> channels = subMngr.findSubscribedChannels(userKey);
		String result = NnStatusMsg.successStr(locale);
		for (MsoChannel c : channels) {
			result = result + this.composeChannelLineupStr(c);
			result = result + "\n";
		}
		return result;
	}
	
	public String programInfo(String channelIds, String userKeyStr, String ipgId, Locale locale) {
		if (channelIds == null || (channelIds.equals("*") && userKeyStr == null)) {
			return NnStatusMsg.inputMissing(locale);
		}
		MsoProgramManager programMngr = new MsoProgramManager();		
		String[] chArr = channelIds.split(",");
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		if (channelIds.equals("*") && ipgId != null) {
			IpgManager ipgMngr = new IpgManager();
			Ipg ipg = ipgMngr.findById(Long.parseLong(ipgId));
			if (ipg == null) { return messageSource.getMessage("nnstatus.ipg_invalid", new Object[] {NnStatusCode.IPG_INVALID} , locale);}
			programs = ipgMngr.findIpgPrograms(ipg);
			log.info("ipg program count: " + programs.size());
		} else if (channelIds.equals("*")) {
			NnUser user = userMngr.findByKeyStr(userKeyStr);
			if (user == null) { return NnStatusMsg.userInvalid(locale); }
			programs = programMngr.findAllByUser(user);
		} else if (chArr.length > 1) {
			programs = programMngr.findAllByChannelIdsAndIsPublic(channelIds, true);
		} else {
			programs = programMngr.findAllByChannelId(Long.parseLong(channelIds));
		}		
		return NnStatusMsg.successStr(locale) + this.composeProgramInfoStr(programs);
	}

	private String getCategoriesByMsoKey(Key msoKey, Locale locale) {
		CategoryManager categoryMngr = new CategoryManager();	
		List<Category> categories = categoryMngr.findAllByMsoKey(msoKey);
				
		String output = NnStatusMsg.successStr(locale);
		for (Category c : categories) {
			String[] str = {String.valueOf(c.getKey().getId()), c.getName(), String.valueOf(c.getChannelCount())};
			output = output + NnStringUtil.getDelimitedStr(str) + "\n";			
		}		
		if (categories.size() < 1) { return messageSource.getMessage("nnstatus.category_invalid", new Object[] {NnStatusCode.CATEGORY_INVALID} , locale);}
		
		
		return output;
	}
		
	private String composeChannelLineupStr(MsoChannel c) {
		String intro = c.getIntro();
		if (intro != null) {
			int introLenth = (intro.length() > 256 ? 256 : intro.length()); 
			intro = intro.substring(0, introLenth);
		} else {
			intro = "";
		}
		String imageUrl = c.getImageUrl();
		if (c.getStatus() == MsoChannel.STATUS_ERROR) {
			imageUrl = "/WEB-INF/../images/error.jpg";
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
	
	private String composeProgramInfoStr(List<MsoProgram> programs) {		
		String output = "";
		for (MsoProgram p : programs) {
			String url1 = p.getMpeg4FileUrl();
			String url2 = p.getWebMFileUrl();
			String url3 = p.getOtherFileUrl();
			String url4 = p.getAudioFileUrl();
			String intro = p.getIntro();			
			if (intro != null) {
				int introLenth = (intro.length() > 256 ? 256 : intro.length()); 
				intro = intro.replaceAll("\\s", " ");				
				intro = intro.substring(0, introLenth);
			}

			String[] ori = {String.valueOf(p.getChannelKey().getId()), 
					        String.valueOf(p.getKey().getId()), 
					        p.getName(), 
					        intro,
					        String.valueOf(p.getType()), 
					        p.getDuration(),
					        p.getImageUrl(),
					        p.getImageLargeUrl(),
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
	
	public String findNewPrograms(String userKey, Locale locale) {
		MsoProgramManager programMngr = new MsoProgramManager();
		List<MsoProgram> programs = programMngr.findNew(userKey);
		String output = NnStatusMsg.successStr(locale);
		for (MsoProgram p : programs) {
			output = output + p.getKey().getId() + "\n";			
		}
		return output;		
	}
	
}