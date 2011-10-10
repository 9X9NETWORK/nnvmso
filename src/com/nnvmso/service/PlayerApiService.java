package com.nnvmso.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
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
import com.google.apphosting.api.DeadlineExceededException;
import com.nnvmso.dao.ShardedCounter;
import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.lib.PiwikLib;
import com.nnvmso.lib.YouTubeLib;
import com.nnvmso.model.AreaOwnership;
import com.nnvmso.model.Captcha;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannelSet;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.LangTable;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoConfig;
import com.nnvmso.model.MsoIpg;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnContent;
import com.nnvmso.model.NnEmail;
import com.nnvmso.model.NnGuest;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.NnUserChannelSorting;
import com.nnvmso.model.NnUserPref;
import com.nnvmso.model.NnUserShare;
import com.nnvmso.model.NnUserWatched;
import com.nnvmso.model.Subscription;
import com.nnvmso.model.SubscriptionLog;
import com.nnvmso.validation.NnUserValidator;

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

	//create a set
	//do findsetinfo
	public String findSetInfo(String id, String beautifulUrl) {
		//verify input
		if (id == null && beautifulUrl == null) {
			return NnStatusMsg.inputMissing(locale);
		}		
		if (id.startsWith("s")) id = id.replace("s", ""); 			
		if (id != null && !Pattern.matches("^\\d*$", id)) {
			return NnStatusMsg.inputError(locale);
		}
		ChannelSetManager csMngr = new ChannelSetManager();
		ChannelSet cs = null;
		if (id != null) {
			long setId = Long.parseLong(id);
			cs = csMngr.findById(setId);
		} else {
			cs = csMngr.findBybeautifulUrl(beautifulUrl);
		}
		if (cs == null)
			return messageSource.getMessage("nnstatus.set_invalid", new Object[] {NnStatusCode.SET_INVALID} , locale);
		
		List<MsoChannel> channels = csMngr.findChannelsById(cs.getKey().getId());
		String[] result = {"", "", ""};
		//first block: status
		Mso csMso = msoMngr.findById(cs.getMsoId());
		//2nd block, set's mso info	 
		result[0] += assembleKeyValue("name", csMso.getName());
		result[0] += assembleKeyValue("imageUrl", csMso.getLogoUrl()); 
		result[0] += assembleKeyValue("intro", csMso.getIntro());		 
		//3rd block: set info
		result[1] += assembleKeyValue("id", String.valueOf(cs.getKey().getId()));
		result[1] += assembleKeyValue("name", cs.getName());		
		result[1] += assembleKeyValue("imageUrl", cs.getImageUrl());
		result[1] += assembleKeyValue("piwik", cs.getPiwik());
		//4rd block, channel info		
		for (MsoChannel c : channels) {
			result[2] += this.composeChannelLineupStr(c, csMso) + "\n";													
		}		
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}	
	
	public int checkRO() {
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig config = configMngr.findByItem(MsoConfig.RO);
		if (config != null && config.getValue().equals("1"))			
			return NnStatusCode.DATABASE_READONLY;
		return NnStatusCode.SUCCESS;
	}
	
	public String search(String text) {		
		List<MsoChannel> searchResults = MsoChannelManager.searchChannelEntries(text);
		String[] result = {""};
		for (MsoChannel c : searchResults) {
			result[0] += this.composeChannelLineupStr(c, mso) + "\n";
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
	
	public String setUserProfile(String userToken, String items, String values) {
		//verify input
		if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") ||
			items == null || values == null)
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		//verify user
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) 
			return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
		if (user.getEmail().equals(NnUser.GUEST_EMAIL))
			return this.assembleMsgs(NnStatusCode.USER_PERMISSION_ERROR, null);
		String[] key = items.split(",");
		String[] value = values.split(",");
		String password = "";
		String oldPassword = "";
		if (key.length != value.length)
			return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
		for (int i=0; i<key.length; i++) {
			if (key[i].equals("name"))
				user.setName(value[i]);
			if (key[i].equals("year"))
				user.setDob(value[i]);
			if (key[i].equals("password"))
				password = value[i];				
			if (key[i].equals("oldPassword"))
				oldPassword = value[i];				
			if (key[i].equals("sphere"))
				if ((value[i] == null) || (this.checkLang(value[i]) == null))
					return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
				user.setSphere(value[i]);
			if (key[i].equals("gender"))
				user.setGender(Short.parseShort(value[i]));						
			if (key[i].equals("ui-lang"))
				if ((value[i] == null) || (this.checkLang(value[i]) == null))
					return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
				user.setLang(value[i]);
		}
		int status = NnUserValidator.validateProfile(user);
		if (status != NnStatusCode.SUCCESS) {
			log.info("profile fail");
			return this.assembleMsgs(status, null);
		}
		if (password.length() > 0 && oldPassword.length() > 0) {
			NnUser authenticated = userMngr.findAuthenticatedUser(user.getEmail(), oldPassword);
			if (authenticated == null)
				return this.assembleMsgs(NnStatusCode.USER_LOGIN_FAILED, null);
			status = NnUserValidator.validatePassword(password);
			if (status != NnStatusCode.SUCCESS)
				return this.assembleMsgs(status, null);
			user.setPassword(password);
		}
		
		userMngr.save(user);
		return this.assembleMsgs(NnStatusCode.SUCCESS, null);
	}

	public String getUserProfile(String userToken) {
		//verify input
		if (userToken == null || userToken.length() == 0 || userToken.equals("undefined"))
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		//verify user
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) 
			return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
		String[] result = {""};	 
		result[0] += assembleKeyValue("name", user.getName());
		result[0] += assembleKeyValue("email", user.getEmail());
		String gender = "";
		if (user.getGender() != 2)
			gender = String.valueOf(user.getGender());
		result[0] += assembleKeyValue("gender", gender);
		result[0] += assembleKeyValue("year", String.valueOf(user.getDob()));
		result[0] += assembleKeyValue("sphere", user.getSphere());
		result[0] += assembleKeyValue("ui-lang", user.getLang());
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
	
	public String setUserPref(String userToken, String item, String value) {
		//verify input
		if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") ||
			item == null || value == null || item.length() == 0 || value.length() == 0) {
			return NnStatusMsg.inputMissing(locale);
		}		
		//verify user
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) {return NnStatusMsg.userInvalid(locale);}		
		//get preference
		NnUserPrefManager prefMngr = new NnUserPrefManager();
		NnUserPref pref = prefMngr.findByUserIdAndItem(user.getKey().getId(), item);
		if (pref != null) {
			pref.setValue(value);			
			prefMngr.save(pref);
		} else {
			pref = new NnUserPref();
			pref.setValue(value);
			pref.setItem(item);			
			pref.setUserId(user.getKey().getId());
			prefMngr.create(pref);
		}
		return NnStatusMsg.successStr(locale);
	}

	public int addMsoInfoVisitCounter(String msoName, boolean readOnly) {		
		String counterName = msoName + "BrandInfo";
		CounterFactory factory = new CounterFactory();
		ShardedCounter counter = factory.getOrCreateCounter(counterName);
		if (!readOnly)
			counter.increment();			
		return counter.getCount(); 								
	}
	
	public String findMsoInfo(HttpServletRequest req) {
		Mso theMso = msoMngr.findMsoViaHttpReq(req);
		if (theMso == null) {return NnStatusMsg.msoInvalid(locale);}

		MsoConfigManager configMngr = new MsoConfigManager();
		String[] result = {""};
		result[0] += this.assembleKeyValue("key", String.valueOf(mso.getKey().getId()));
		result[0] += this.assembleKeyValue("name", mso.getName());
		result[0] += this.assembleKeyValue("title", mso.getTitle());		
		result[0] += this.assembleKeyValue("logoUrl", mso.getLogoUrl());
		result[0] += this.assembleKeyValue("jingleUrl", mso.getJingleUrl());
		result[0] += this.assembleKeyValue("logoClickUrl", mso.getLogoClickUrl());
		result[0] += this.assembleKeyValue("preferredLangCode", mso.getPreferredLangCode());
		result[0] += this.assembleKeyValue("jingleUrl", mso.getJingleUrl());

		List<MsoConfig> list = configMngr.findAllByMsoId(mso.getKey().getId());
		boolean readOnly = false;
		for (MsoConfig config : list) {
			if (config.getItem().equals(MsoConfig.DEBUG))
				result[0] += this.assembleKeyValue(MsoConfig.DEBUG, config.getValue());
			if (config.getItem().equals(MsoConfig.FBTOKEN))
				result[0] += this.assembleKeyValue(MsoConfig.FBTOKEN, config.getValue());
			if (config.getItem().equals(MsoConfig.RO)) {
				result[0] += this.assembleKeyValue(MsoConfig.RO, config.getValue());
				readOnly = Boolean.parseBoolean(config.getValue());
			}			
		}	
		String locale = this.findLocaleByHttpRequest(req);
		result[0] += this.assembleKeyValue("locale", locale);
		int counter = 0;
		if (!readOnly)
			counter = this.addMsoInfoVisitCounter(theMso.getName(), readOnly);		
		result[0] += this.assembleKeyValue("brandInfoCounter", String.valueOf(counter));		
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}

	public String handleException (Exception e) {
		try {
			
		} catch (com.google.apphosting.api.ApiProxy.CapabilityDisabledException a) {
			System.out.println("handle CapabilityDisabledException");
		}
		
		String output = NnStatusMsg.errorStr(locale);
    	if (e.getClass().equals(NumberFormatException.class)) {
			output = NnStatusCode.INPUT_BAD + "\t" + "INPUT BAD";			
    	} else if (e.getClass().equals(DatastoreTimeoutException.class)) {
			output = NnStatusCode.DATABASE_TIMEOUT + "\t" + "database timeout";			
		} else if (e.getClass().equals(NoSuchMessageException.class)) {			
			output = NnStatusCode.OUTPUT_NO_MSG_DEFINED + "\t" + "oops, system does not define this error msg.";
		} else if (e.getClass().equals(DatastoreFailureException.class)) {
			output = NnStatusCode.DATABASE_ERROR + "\t" + "database internal error";
		} else if (e.getClass().equals(DatastoreNeedIndexException.class)) {
			output = NnStatusCode.DATABASE_NEED_INDEX + "\t" + "index is still building, fatal.";
		} else if (e.getClass().equals(DeadlineExceededException.class)) {
			output = NnStatusCode.GAE_TIMEOUT + "\t" + "GAE timeout";		
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
		pdrMngr.processPdr(pdr, user, session);
		output = NnStatusMsg.successStr(locale);
		return output;
	}
	
	public String markBadProgram(String programId, String userToken) {		
		if (programId == null || userToken == null) {
			return NnStatusMsg.inputMissing(locale);
		}
		try {
			MsoProgramManager programMngr = new MsoProgramManager();
			MsoProgram program = programMngr.findById(Long.parseLong(programId));
			program.setStatus(MsoProgram.STATUS_ERROR);
			programMngr.save(program);
		} catch (NumberFormatException e) {
			log.info("pass invalid program id:" + programId);
		} catch (NullPointerException e) {
			log.info("program does not exist: " + programId);
		}
		return NnStatusMsg.successStr(locale);
	}
	
	public String saveShare(String userToken, String channelId, String programId, String setId) {
		if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") ||
			channelId == null || programId == null || channelId.length() == 0 || programId.length() == 0) {
			return NnStatusMsg.inputMissing(locale);
		}
		if (!Pattern.matches("^\\d*$", channelId)) {
			return NnStatusMsg.inputError(locale);
		}
				
		NnUser foundUser = userMngr.findByToken(userToken);				
		if (foundUser == null) { return NnStatusMsg.userInvalid(locale);}

		NnUserShare share = new NnUserShare();
		share.setChannelId(Long.parseLong(channelId));
		if (Pattern.matches("^\\d*$", programId)) {
			share.setProgramId(Long.parseLong(programId));
		} else {
			share.setProgramIdStr(programId);
		}
		share.setUserId(foundUser.getKey().getId());
		NnUserShareManager shareMngr = new NnUserShareManager();
		shareMngr.create(share, foundUser.getKey().getId());				
		return NnStatusMsg.successStr(locale) + separatorStr + Long.toString(share.getId());				
	}
	
	public String loadShare(long id) {
		NnUserShareManager shareMngr = new NnUserShareManager();
		NnUserShare share= shareMngr.findById(id);
		if (share== null) { return messageSource.getMessage("nnstatus.ipg_invalid", new Object[] {NnStatusCode.IPG_INVALID} , locale);} 
		//first block: status
		String status = NnStatusMsg.successStr(locale);
		//second block: episode information
		String toPlay = separatorStr;
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoProgram program = programMngr.findById(share.getProgramId());
		if (program != null) {
			List<MsoProgram> programs = new ArrayList<MsoProgram>();
			programs.add(program);
			MsoConfig config = new MsoConfigManager().findByMsoIdAndItem(mso.getKey().getId(), MsoConfig.CDN);
			toPlay = toPlay + this.composeProgramInfoStr(programs, config);
		} else {			
			toPlay = toPlay + share.getChannelId() + "\t" + share.getProgramIdStr() + "\n";			
		}
		String channelLineup = separatorStr;
		MsoChannel channel = new MsoChannelManager().findById(share.getChannelId());
		if (channel != null) {
			channelLineup = channelLineup + this.composeChannelLineupStr(channel, mso);
		}
		System.out.println("status:" + status);		
		System.out.println("to play:" + toPlay);
		System.out.println("channelLineupe:" + channelLineup);
		return status + toPlay + channelLineup;
	}
		
	public String moveChannel(String userToken, String grid1, String grid2) {		
		//verify input
		if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") || grid1 == null || grid2 == null) {
			return NnStatusMsg.inputMissing(locale);
		}
		if (!Pattern.matches("^\\d*$", grid1) || !Pattern.matches("^\\d*$", grid2) ||
			Integer.parseInt(grid1) < 0 || Integer.parseInt(grid1) > 81 ||
			Integer.parseInt(grid2) < 0 || Integer.parseInt(grid2) > 81) {
			return NnStatusMsg.inputError(locale);
		}		
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) { return messageSource.getMessage("nnstatus.user_invalid", new Object[] {NnStatusCode.USER_INVALID} , locale); }
		
		SubscriptionManager subMngr = new SubscriptionManager();
		boolean success = subMngr.moveSeq(user.getKey().getId(), Integer.parseInt(grid1), Integer.parseInt(grid2));
		String result = NnStatusMsg.successStr(locale);
		if (!success) 
			result = messageSource.getMessage("nnstatus.subscription_error", new Object[] {NnStatusCode.SUBSCRIPTION_ERROR} , locale);
		return result;
	}

	public String copyChannel(String userToken, String channelId, String grid) {		
		//verify input
		if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") || grid == null) {
			return NnStatusMsg.inputMissing(locale);
		}
		if (!Pattern.matches("^\\d*$", grid) ||
			Integer.parseInt(grid) < 0 || Integer.parseInt(grid) > 81)
			return NnStatusMsg.inputError(locale);		
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) { return messageSource.getMessage("nnstatus.user_invalid", new Object[] {NnStatusCode.USER_INVALID} , locale); }
		
		SubscriptionManager subMngr = new SubscriptionManager();
		boolean success = false;
		success = subMngr.copyChannel(user.getKey().getId(), Long.parseLong(channelId), Short.parseShort(grid));
		String result = NnStatusMsg.successStr(locale);
		if (!success) 
			result = messageSource.getMessage("nnstatus.subscription_error", new Object[] {NnStatusCode.SUBSCRIPTION_ERROR} , locale);
		return result;
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
	        String line = rd.readLine(); 
	        if (line != null) {
	        	country = line.toLowerCase();
	        } //assuming one line	        
		} catch (Exception e) {
			NnLogUtil.logException(e);
		}
		log.info("country from query:" + country + ";with ip:" + ip);
        String locale = "en";
		if (country.equals("tw")) {
			locale = "zh";
		}
		return locale;
	}
	 	
	private String composeChannelByCategoroy(List<MsoChannel> channels) {
		String result = "";
		for (int i=0; i< channels.size(); i++) {	
			if (channels.get(i).getProgramCount() > 0 ) {
				MsoChannel c = channels.get(i);
				String[] ori = {String.valueOf(c.getSeq()),
						        String.valueOf(c.getKey().getId()), 
						        c.getName(), 
						        c.getImageUrl(), 
						        Integer.toString(c.getProgramCount()),
						        String.valueOf(c.getSubscriptionCount()),
						        String.valueOf(c.getContentType()),
						        this.convertEpochToTime(c.getTranscodingUpdateDate(), c.getUpdateDate())
						        };
				result = result + NnStringUtil.getDelimitedStr(ori);
				result = result + "\n";
			}
		}
		return result;		
	}
	
	public String findPublicChannelsByCategoryAndLang(String categoryId, String lang) {		
		//verify input
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		if (categoryId == null || categoryId.length() < 1) { return NnStatusMsg.inputMissing(locale); }
		if (!Pattern.matches("^\\d*$", categoryId)) { return NnStatusMsg.inputError(locale); }
		
		if (lang != null && !lang.equals(Mso.LANG_EN) && !lang.equals(Mso.LANG_ZH))
			return NnStatusMsg.inputError(locale);		
		
		//find public channels by categoryId
		MsoChannelManager channelMngr = new MsoChannelManager();
		channels = channelMngr.findPublicChannelsByCategoryIdAndLang(Long.parseLong(categoryId), lang);
		if (channels == null) { return NnStatusMsg.successStr(locale);}
		
		//assemble output
		log.info("find " + channels.size() + " of channels in category, category id:" + categoryId);
		String result = NnStatusMsg.successStr(locale) + separatorStr;
		result = result + categoryId + "\n" + separatorStr;
		for (int i=0; i< channels.size(); i++) {	
			if (channels.get(i).getProgramCount() > 0 ) {
				MsoChannel c = channels.get(i);
				String[] ori = {String.valueOf(c.getSeq()),
						        String.valueOf(c.getKey().getId()), 
						        c.getName(), 
						        c.getImageUrl(), 
						        Integer.toString(c.getProgramCount()),
						        String.valueOf(c.getSubscriptionCount()),
						        String.valueOf(c.getContentType()),
						        this.convertEpochToTime(c.getTranscodingUpdateDate(), c.getUpdateDate())
						        };
				result = result + NnStringUtil.getDelimitedStr(ori);
				result = result + "\n";
			}
		}
		return result;
	}	
		
	public String createGuest(HttpServletRequest req, HttpServletResponse resp) {
		//verify input
		if (mso == null) { return NnStatusMsg.errorStr(locale); }
		NnGuestManager mngr = new NnGuestManager();
		NnGuest guest = new NnGuest(NnGuest.TYPE_GUEST);
		mngr.create(guest);
		
		String output = NnStatusMsg.successStr(locale) + separatorStr;		
		output = output + assembleKeyValue("token", guest.getToken());
		output = output + assembleKeyValue("name", NnUser.GUEST_NAME);
		output = output + assembleKeyValue("lastLogin", "");

		//prepare cookie and output
		this.setUserCookie(resp, CookieHelper.USER, guest.getToken());
		return output;
	}

	//assemble key and value string
	private String assembleKeyValue(String key, String value) {
		return key + "\t" + value + "\n";
	}
	
	//Prepare user info, it is used by login, guest register, userTokenVerify
	public String prepareUserInfo(NnUser user, NnGuest guest) {
		String output = "";
		if (user != null) {
			output += assembleKeyValue("token", user.getToken());
			output += assembleKeyValue("name", user.getName());
			output += assembleKeyValue("lastLogin", String.valueOf(user.getUpdateDate().getTime()));
			output += assembleKeyValue("sphere", user.getSphere());
			output += assembleKeyValue("ui-lang", user.getLang());			
			NnUserPrefManager prefMngr = new NnUserPrefManager();
			List<NnUserPref> list = prefMngr.findByUserId(user.getKey().getId());		
			for (NnUserPref pref : list) {
				output += this.assembleKeyValue(pref.getItem(), pref.getValue());
			}			
		} else {		
			output += assembleKeyValue("token", guest.getToken());
			output += assembleKeyValue("name", NnUser.GUEST_NAME);
			output += assembleKeyValue("lastLogin", "");			
		}
			
		return output;
	}
	
    private String checkLang(String lang) {
        if (lang != null && !lang.equals(LangTable.LANG_EN) && !lang.equals(LangTable.LANG_ZH))
            return null;
        if (lang == null)
            return LangTable.LANG_EN;
        return lang;
    }
	
	public String findStaticContent(String key, String lang) {
		NnContentManager contentMngr = new NnContentManager();
		NnContent content = contentMngr.findByItemAndLang(key, lang);		
		if (content == null)
			return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        lang = this.checkLang(lang);
        if (lang == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);		
		String[] result = {content.getContent().getValue()};
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
	
	public void setUserCookie(HttpServletResponse resp, String cookieName, String userId) {		
		CookieHelper.setCookie(resp, cookieName, userId);
	}	
		
	public String unsubscribe(String userToken, String channelId, String setId, String grid) {
		//verify input
		if (userToken == null || userToken.equals("undefined"))			
			return NnStatusMsg.inputMissing(locale);		
		if (channelId == null && setId == null) {
			return NnStatusMsg.inputMissing(locale);
		}
		//verify user
		NnUser user = new NnUserManager().findByToken(userToken);
		if (user == null) {return NnStatusMsg.userInvalid(locale);}
		SubscriptionManager subMngr = new SubscriptionManager();
		
		String result = NnStatusMsg.successStr(locale);		
		if (channelId != null) {
			String[] chArr = channelId.split(",");
			if (chArr.length == 1) {
				log.info("unsubscribe single channel");
				Subscription s = null;
				if (grid == null) {
					s = subMngr.findByUserIdAndChannelId(user.getKey().getId(), Long.parseLong(channelId));
				} else {
					s = subMngr.findChannelSubscription(user.getKey().getId(), Long.parseLong(channelId), Integer.parseInt(grid));
				}			
				if (s == null || (s != null && s.getType() == MsoIpg.TYPE_READONLY)) {
					return messageSource.getMessage("nnstatus.subscription_ro_channel", new Object[] {NnStatusCode.SUBSCRIPTION_ERROR} , locale);			
				}
				subMngr.unsubscribeChannel(s);
				result += channelId + "\t" + messageSource.getMessage("nnstatus.success", new Object[] {NnStatusCode.SUCCESS} , locale); 
			} else {
				log.info("unsubscribe multiple channels");
				if (grid == null) return NnStatusMsg.inputMissing(locale);
				String[] gridArr = grid.split(",");
				if (gridArr.length != chArr.length) return NnStatusMsg.inputMissing(locale);
				List<Long> chlist = new ArrayList<Long>();
				List<Integer> gridlist = new ArrayList<Integer>();
				for (int i=0; i<chArr.length; i++) { chlist.add(Long.valueOf(chArr[i]));}
				for (int i=0; i<gridArr.length; i++) { gridlist.add(Integer.valueOf(gridArr[i]));}
				result = result + separatorStr;
				for (int i=0; i<chlist.size(); i++) {
					Subscription s = subMngr.findChannelSubscription(user.getKey().getId(), chlist.get(i), gridlist.get(i));
					if (s == null || (s != null && s.getType() == MsoIpg.TYPE_READONLY)) {
						 result = result + chlist.get(i) + "\t" + messageSource.getMessage("nnstatus.subscription_ro_channel", new Object[] {NnStatusCode.SUBSCRIPTION_ERROR} , locale);
					} else {
						subMngr.unsubscribeChannel(s);
						result = result + chlist.get(i) + "\t" + messageSource.getMessage("nnstatus.success", new Object[] {NnStatusCode.SUCCESS} , locale);
					}
				}
			}
		}
		if (setId != null) {
			AreaOwnershipManager areaMngr = new AreaOwnershipManager();
			AreaOwnership area = areaMngr.findByUserIdAndSetId(user.getKey().getId(), Long.parseLong(setId));
			/*
			if (area == null) 
				return messageSource.getMessage("nnstatus.set_invalid", new Object[] {NnStatusCode.SUBSCRIPTION_ERROR} , locale);
			*/
			areaMngr.delete(area); 
		}
		return result;
	}
	
	public String changeSetInfo(String userToken, String name, String areaNo) {
		//verify input
		if (name == null || areaNo == null)  {			
			return NnStatusMsg.inputMissing(locale);
		}
		if (!Pattern.matches("^\\d*$", areaNo) || Integer.parseInt(areaNo) < 0 || Integer.parseInt(areaNo) > 9) {			
			return NnStatusMsg.inputError(locale);
		}

		NnUser user = new NnUserManager().findByToken(userToken);
		if (user == null) {
			return NnStatusMsg.userInvalid(locale);
		}	
		
		AreaOwnershipManager areaMngr = new AreaOwnershipManager();
		short position = Short.valueOf(areaNo);
		AreaOwnership area = areaMngr.findByUserIdAndAreaNo(user.getKey().getId(), Short.valueOf(position));
		if (area != null) {
			if (area.getType() == AreaOwnership.TYPE_RO)
				return messageSource.getMessage("nnstatus.subscription_ro_set", new Object[] {NnStatusCode.SUBSCRIPTION_RO_SET} , locale); 
			area.setSetName(name);
			areaMngr.save(area);			
		} else {
			area = new AreaOwnership();
			area.setUserId(user.getKey().getId());
			area.setSetName(name);				
			area.setAreaNo(position);
			areaMngr.create(area);
		}
		
		String result = NnStatusMsg.successStr(locale);
		return result;
	}
	
	public String subscribe(String userToken, String channelId, String gridId) {
		//verify input
		@SuppressWarnings("rawtypes")
		HashMap map = this.checkUser(userToken, false);
		if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
			return this.assembleMsgs((Integer)map.get("s"), null);
		}
		if (channelId == null && gridId == null) 
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);		
		NnUser user = (NnUser) map.get("u");				
		SubscriptionManager subMngr = new SubscriptionManager();
		//verify channel and grid
		if (channelId == null)
			return this.assembleMsgs(NnStatusCode.CHANNEL_INVALID, null);
			
		long cId = Long.parseLong(channelId);			
		MsoChannel channel = new MsoChannelManager().findById(cId);			
		if (channel == null || channel.getStatus() == MsoChannel.STATUS_ERROR)
			return this.assembleMsgs(NnStatusCode.CHANNEL_ERROR, null);
		boolean status = subMngr.subscribeChannel(user.getKey().getId(), cId, Integer.parseInt(gridId), MsoIpg.TYPE_GENERAL, mso.getKey().getId());
		if (!status) {
			//the general status shows error even there's only one error
			return this.assembleMsgs(NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL, null);
		}				
		return this.assembleMsgs(NnStatusCode.SUCCESS, null);
	}

	private int checkCaptcha(NnGuest guest, String fileName, String name) {
		NnGuestManager guestMngr = new NnGuestManager();
		if (guest == null)
			return NnStatusCode.CAPTCHA_INVALID;
		if (guest.getCaptchaId() == 0)
			return NnStatusCode.CAPTCHA_INVALID;
		Captcha c = new CaptchaManager().findById(guest.getCaptchaId());
		System.out.println(guest.getGuessTimes() + ";" + NnGuest.GUESS_MAXTIMES);
		if (guest.getGuessTimes() >= NnGuest.GUESS_MAXTIMES)
			return NnStatusCode.CAPTCHA_TOOMANY_TRIES;
		if (!c.getFileName().equals(fileName) || 
			!c.getName().equals(name)) {
			guest.setGuessTimes(guest.getGuessTimes()+1);
			guestMngr.save(guest);
			return NnStatusCode.CAPTCHA_FAILED;
		}
		Date now = new Date();
		if (now.after(guest.getExpiredAt()))
			return NnStatusCode.CAPTCHA_FAILED;
		return NnStatusCode.SUCCESS;
	}
	
	public String createUser(String email, String password, String name, String token,
				             String captchaFilename, String captchaText,
				             String sphere, String lang,
			                 HttpServletRequest req, HttpServletResponse resp) {		
		int status = NnUserValidator.validate(email, password, name, req);
		if (status != NnStatusCode.SUCCESS) 
			return this.assembleMsgs(status, null);
		lang = this.checkLang(lang);	
		sphere = this.checkLang(sphere);
        if (lang == null || sphere == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);				
		
		//find mso
		if (mso == null) { return NnStatusMsg.msoInvalid(locale);}
		
		NnGuestManager guestMngr = new NnGuestManager();
		NnGuest guest = guestMngr.findByToken(token);
		if (guest == null && captchaFilename != null) {
			log.info("such guest does not exist, where does this token from");
			return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
		}
		if (guest != null) {
			if (captchaFilename != null || captchaText != null) {
				status = this.checkCaptcha(guest, captchaFilename, captchaText);
				if (status != NnStatusCode.SUCCESS) 
					return this.assembleMsgs(status, null);
			}
		}
		//verify email
		NnUser user = userMngr.findByEmail(email);
		if (user != null) {
			log.info("user email taken:" + user.getEmail() + "; mso=" + mso.getName() + ";user token=" + user.getToken());
			return messageSource.getMessage("nnstatus.user_email_taken", new Object[] {NnStatusCode.USER_EMAIL_TAKEN} , locale);
		}

		user = new NnUser(email, password, name, NnUser.TYPE_USER, mso.getKey().getId());
		user.setSphere(sphere);
		user.setLang(lang);
		userMngr.create(user, token);
		userMngr.subscibeDefaultChannels(user);							
		String[] result = {this.prepareUserInfo(user, null)};		
		this.setUserCookie(resp, CookieHelper.USER, user.getToken());
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
	
	//!!! can remove all the user check since going to wipe out all the guest account
	public String findUserByToken(String token, HttpServletRequest req, HttpServletResponse resp) {
		if (token == null) {return NnStatusMsg.inputMissing(locale);}
		
		NnGuestManager guestMngr = new NnGuestManager();
		NnUser user = userMngr.findByToken(token);
		NnGuest guest = guestMngr.findByToken(token);
		if (user == null && guest == null) {
			CookieHelper.deleteCookie(resp, CookieHelper.USER);
			return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
		}
		if (user != null) {
			if (user.getEmail().equals(NnUser.GUEST_EMAIL))
				return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
			userMngr.save(user); //change last login time (ie updateTime)
		}
		String[] result = {this.prepareUserInfo(user, guest)};
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
	
	public String findAuthenticatedUser(String email, String password, HttpServletRequest req, HttpServletResponse resp) {		
		log.info("login: email=" + email + "; mso=" + mso.getKey().getId());
		if (email == null || email.length() == 0 ||  password == null || password.length() == 0) {
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		}		
		String[] result = {""};
		NnUser user = userMngr.findAuthenticatedUser(email, password);
		if (user != null) {
			result[0] = this.prepareUserInfo(user, null);
			userMngr.save(user); //change last login time (ie updateTime)
			this.setUserCookie(resp, CookieHelper.USER, user.getToken());
		} else {
			return this.assembleMsgs(NnStatusCode.USER_LOGIN_FAILED, null);
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}

	public String createChannel(String categoryIds, String userToken, String url, String grid, 
			                    String tags, String lang, HttpServletRequest req) {
		//verify input
		if (url == null || url.length() == 0 ||  grid == null || grid.length() == 0 ||
			userToken== null || userToken.length() == 0) {
			return NnStatusMsg.inputMissing(locale);
		}
		if (!Pattern.matches("^\\d*$", grid) || Integer.parseInt(grid) < 0 || Integer.parseInt(grid) > 81) {			
			return NnStatusMsg.inputError(locale);
		}
		if (lang == null || lang.length() == 0) 
			lang = Mso.LANG_EN;
		if (!lang.equals(Mso.LANG_EN) && !lang.equals(Mso.LANG_ZH)) {
			return NnStatusMsg.inputError(locale);
		}
				
		url = url.trim();	
		//verify user
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) { return NnStatusMsg.userInvalid(locale);}		
		if (user.getEmail().equals(NnUser.GUEST_EMAIL)) {
			return messageSource.getMessage("nnstatus.user_permission_error", new Object[] {NnStatusCode.USER_PERMISSION_ERROR} , locale);
		}
				
		CategoryManager categoryMngr = new CategoryManager();
		List<Category> categories = new ArrayList<Category>();
		//verify category
		if (categoryIds == null || categoryIds.equals("undefined") || categoryIds.length() == 0) {
			categories.add(categoryMngr.findByName(Category.UNCATEGORIZED));
		} else {
			categories.addAll(categoryMngr.findCategoriesByIdStr(categoryIds));
		}
		if (categories.size() == 0) { return messageSource.getMessage("nnstatus.category_invalid", new Object[] {NnStatusCode.CATEGORY_INVALID} , locale); }
		
		MsoChannelManager channelMngr = new MsoChannelManager();		
		//verify url, also converge youtube url
		url = channelMngr.verifyUrl(url); 		
		if (url == null) {
			return messageSource.getMessage("nnstatus.channel_url_invalid", new Object[] {NnStatusCode.CHANNEL_URL_INVALID} , locale);			
		}
		
		//verify channel status for existing channel
		MsoChannel channel = channelMngr.findBySourceUrlSearch(url);										
		if (channel != null && (channel.getStatus() == MsoChannel.STATUS_ERROR)) {
			log.info("channel key and status :" + channel.getKey()+ ";" + channel.getStatus());
			return messageSource.getMessage("nnstatus.channel_status_error", new Object[] {NnStatusCode.CHANNEL_STATUS_ERROR} , locale);
		}
		
		if (channel != null) {
			//add categories if necessary
			log.info("User submits a duplicate url:" + url);
			categoryMngr.changeCategory(channel.getKey().getId(), categories);
		} else {				
			//create a new channel
			channel = channelMngr.initChannelSubmittedFromPlayer(url, user);
			channel.setTags(tags);
			channel.setLangCode(lang);
			log.info("User throws a new url:" + url);
			channelMngr.create(channel, categories);
			if (channel.getKey() != null && channel.getContentType() != MsoChannel.CONTENTTYPE_FACEBOOK) { //!!!
				TranscodingService tranService = new TranscodingService();
				tranService.submitToTranscodingService(channel.getKey().getId(), url, req);
			}
		}
		
		//!!!!!!!!!!!! BROADCAST BRODCAST
		//subscribe
		SubscriptionManager subMngr = new SubscriptionManager();
		boolean success = subMngr.subscribeChannel(user.getKey().getId(), channel.getKey().getId(), Integer.parseInt(grid), MsoIpg.TYPE_GENERAL, mso.getKey().getId());
		String output = "";
		if (!success) {
			output = messageSource.getMessage("nnstatus.subscription_duplicate_channel", new Object[] {NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL} , locale);
			return output;
		} else {
			String channelName = "";
			//!!!!! make it function
			if (channel.getSourceUrl() != null && channel.getSourceUrl().contains("http://www.youtube.com"))
				channelName = YouTubeLib.getYouTubeChannelName(channel.getSourceUrl());
			if (channel.getContentType() == MsoChannel.CONTENTTYPE_FACEBOOK) 
				channelName = channel.getSourceUrl();
			
			String result[]= {String.valueOf(channel.getKey().getId()),				  	 	  
			  	 	  channel.getName(),
			  	 	  channel.getImageUrl(),
			  	 	  String.valueOf(channel.getContentType()),
			  	      channelName};
			output = NnStringUtil.getDelimitedStr(result);
		}
		return NnStatusMsg.successStr(locale) + separatorStr + output;
	}
				
	private int convertSetPosToIPGSeq(int seq, int pos) {
		log.info("seq:" + seq + ";pos=" + pos);
		if (seq == 0) {
			return 0;
		}
		try {
			switch(pos) {
			  case 1:
				Integer[] map1 = {1,2,3,10,11,12,19,20,21};
				return map1[seq-1];
			  case 2:
				Integer[] map2 = {4,5,6,13,14,15,22,23,24};
				return map2[seq-1];
			  case 3:
				Integer[] map3 = {7,8,9,16,17,18,25,26,27};
				return map3[seq-1];
			  case 4:
				Integer[] map4 = {28,29,30,37,38,39,46,47,48};
				return map4[seq-1];
			  case 5:
				Integer[] map5 = {31,32,33,40,41,42,49,50,51};
				return map5[seq-1];
			  case 6:
				Integer[] map6 = {34,35,36,43,44,45,52,53,54};
				return map6[seq-1];
			  case 7:
				Integer[] map7 = {55,56,57,64,65,66,74,75,75};
				return map7[seq-1];
			  case 8:
				Integer[] map8 = {58,59,60,67,68,69,76,77,78};
				return map8[seq-1];
			  case 9:
				Integer[] map9 = {61,62,63,70,71,72,79,80,81};
				return map9[seq-1];
			  default:
				return 0;
			}
		} catch (IndexOutOfBoundsException e){
			log.info("pass wrong data");
			return 0;
		}		
	}
	
	private String convertEpochToTime(String transcodingUpdateDate, Date updateDate) {
		String output = "";
		try {
			if (transcodingUpdateDate != null) {
				long epoch = Long.parseLong(transcodingUpdateDate);
				Date myDate = new Date (epoch*1000);
				output = String.valueOf(myDate.getTime());
			} else if (updateDate != null){
				output = String.valueOf(updateDate.getTime());
			}
		} catch (NumberFormatException e) {			
		}
		return output;
	}
	
	public String findChannelInfo(String userToken, boolean userInfo, 
			                      String channelIds, boolean setInfo, boolean isRequired) {
		//verify input
		if ((userToken == null && userInfo == true) || (userToken == null && channelIds == null) || (userToken == null && setInfo == true))
			return NnStatusMsg.inputMissing(locale);

		List<String> result = new ArrayList<String>();
		NnUser user = null;
		if (userToken != null) {
			//verify user
			user = userMngr.findByToken(userToken);
			if (user == null) {
				NnGuest guest = new NnGuestManager().findByToken(userToken);
				if (guest == null)
					return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
				else
					return this.assembleMsgs(NnStatusCode.SUCCESS, null);
			}
		}
		
		if (userInfo) {			
			result.add(this.prepareUserInfo(user, null) + separatorStr);	
		}
		AreaOwnershipManager areaMngr = new AreaOwnershipManager();
		NnUserChannelSortingManager sortingMngr = new NnUserChannelSortingManager();
		List<NnUserChannelSorting> sorts = new ArrayList<NnUserChannelSorting>();
		HashMap<Long, Short> sortMap = new HashMap<Long, Short>();
		HashMap<Long, String> watchedMap = new HashMap<Long, String>();
		if (user != null) {
			List<AreaOwnership> sets = areaMngr.findByUserId(user.getKey().getId());	
			sorts = sortingMngr.findByUser(user.getKey().getId());
		    //set info
			if (setInfo) {
				String setOutput = "";
				for (AreaOwnership s : sets) {
					String[] obj = {
							String.valueOf(s.getAreaNo()),
							String.valueOf(s.getSetId()),
							s.getSetName(),						
							s.getSetImageUrl(),
							String.valueOf(s.getType()),
					};
					setOutput += NnStringUtil.getDelimitedStr(obj) + "\n";
				}
				result.add(setOutput);
			}
			for (NnUserChannelSorting s : sorts) {
				sortMap.put(s.getChannelId(), s.getSort());
			}
			NnUserWatchedManager watchedMngr = new NnUserWatchedManager();
			List<NnUserWatched> watched = watchedMngr.findAllByUserToken(user.getToken());
			for (NnUserWatched w : watched) {
				watchedMap.put(w.getChannelId(), w.getProgram());
			}			
		}
		
		//find channels
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		boolean channelPos = true;
		if (channelIds == null) {
			//find subscribed channels 
			SubscriptionManager subMngr = new SubscriptionManager();
			channels = subMngr.findSubscribedChannels(user.getKey().getId(), mso.getKey().getId());
		} else {
			//find specific channels
			MsoChannelManager channelMngr = new MsoChannelManager();
			channelPos = false;
			String[] chArr = channelIds.split(",");
			if (chArr.length > 1) {
				List<Long> list = new ArrayList<Long>();
				for (int i=0; i<chArr.length; i++) { list.add(Long.valueOf(chArr[i]));}
				channels = channelMngr.findAllByChannelIds(list);
			} else {
				MsoChannel channel = channelMngr.findById(Long.parseLong(channelIds));
				if (channel != null) channels.add(channel);					
			}
		}
		if (isRequired && channels.size() == 0)
			return this.assembleMsgs(NnStatusCode.CHANNEL_INVALID, null);
		//sort by seq
		if (channelPos) {
			TreeMap<Integer, MsoChannel> channelMap = new TreeMap<Integer, MsoChannel>();
			for (MsoChannel c : channels) {
				channelMap.put(c.getSeq(), c);				
			}
			Iterator<Entry<Integer, MsoChannel>> it = channelMap.entrySet().iterator();
	    	channels.clear();
		    while (it.hasNext()) {
		        Map.Entry<Integer, MsoChannel> pairs = (Map.Entry<Integer, MsoChannel>)it.next();
		    	channels.add((MsoChannel)pairs.getValue());
		    }
		}
		String channelOutput = "";
		for (MsoChannel c : channels) {
			if (user != null && sortMap.containsKey(c.getKey().getId()))
		        c.setSorting(sortMap.get(c.getKey().getId()));
		    else 
		    	c.setSorting(MsoChannelManager.getDefaultSorting(c));
			if (user != null && watchedMap.containsKey(c.getKey().getId()))
				c.setRecentlyWatchedProgram(Long.parseLong(watchedMap.get(c.getKey().getId())));
			channelOutput += this.composeChannelLineupStr(c, mso) + "\n";
		}		
		result.add(channelOutput);
		String size[] = new String[result.size()];
		return this.assembleMsgs(NnStatusCode.SUCCESS, result.toArray(size));
	}
	
	//http://localhost:8888/playerAPI/programInfo?ipg=27852&channel=*
	public String findProgramInfo(String channelIds, String userToken, String ipgId, boolean userInfo) {
		if (channelIds == null || (channelIds.equals("*") && userToken == null && ipgId == null)) {		   
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
			if (user == null) {
				NnGuest guest = new NnGuestManager().findByToken(userToken);
				if (guest == null)
					return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
				else
					return this.assembleMsgs(NnStatusCode.SUCCESS, null);
			}
			programs = programMngr.findSubscribedPrograms(user.getKey().getId());
		} else if (chArr.length > 1) {			
			List<Long> list = new ArrayList<Long>();
			for (int i=0; i<chArr.length; i++) { list.add(Long.valueOf(chArr[i]));}
			for (Long l : list) {
				programs.addAll(programMngr.findGoodProgramsByChannelId(l));
			}
		} else {
			programs = programMngr.findGoodProgramsByChannelId(Long.parseLong(channelIds));
		}		
				
		MsoConfig config = new MsoConfigManager().findByMsoIdAndItem(mso.getKey().getId(), MsoConfig.CDN);
		if (config == null) {
			config = new MsoConfig(mso.getKey().getId(), MsoConfig.CDN, MsoConfig.CDN_AMAZON);
			log.severe("mso config does not exist! mso: " + mso.getKey());
		}		
		String result = NnStatusMsg.successStr(locale) + separatorStr;
		if (userInfo) {
			if (user == null && userToken != null) {user = userMngr.findByToken(userToken);}
			result = this.prepareUserInfo(user, null) + separatorStr; 
		}
		return result + this.composeProgramInfoStr(programs, config);
	}

	public String findCategoriesByLang(String lang) {
		if (lang != null && !lang.equals(Mso.LANG_EN) && !lang.equals(Mso.LANG_ZH))
			return NnStatusMsg.inputError(locale);
		if (lang == null)
			lang = Mso.LANG_EN;
		CategoryManager categoryMngr = new CategoryManager();	
		List<Category> categories = categoryMngr.findAllByMsoId(mso.getKey().getId());
				
		String output = NnStatusMsg.successStr(locale) + separatorStr;
		for (Category c : categories) {
			String name =  c.getName();
			int cnt = c.getChannelCount();
			/*
			if (lang.equals(Mso.LANG_ZH)) {
				name = categoryMngr.translate(name);
				cnt = c.getChnChannelCount();
			}
			*/
			String[] str = {String.valueOf(c.getKey().getId()), name, String.valueOf(cnt)};				
			output = output + NnStringUtil.getDelimitedStr(str) + "\n";
		}
		
		if (categories.size() < 1) { return messageSource.getMessage("nnstatus.category_invalid", new Object[] {NnStatusCode.CATEGORY_INVALID} , locale);}
			
		return output;
	}		 
	
	private String composeChannelLineupStr(MsoChannel c, Mso mso) {
		String intro = c.getIntro();
		String imageUrl = c.getPlayerPrefImageUrl();	
		String channelName = "";
		if (c.getSourceUrl() != null && c.getSourceUrl().contains("http://www.youtube.com"))
			channelName = YouTubeLib.getYouTubeChannelName(c.getSourceUrl());
		if (c.getContentType() == MsoChannel.CONTENTTYPE_FACEBOOK) 
			channelName = c.getSourceUrl();
		String[] ori = {Integer.toString(c.getSeq()), 
					    String.valueOf(c.getKey().getId()),
					    c.getName(),
					    intro,
					    imageUrl,
					    String.valueOf(c.getProgramCount()),
					    String.valueOf(c.getType()),
					    String.valueOf(c.getStatus()),
					    String.valueOf(c.getContentType()),
					    channelName,
					    this.convertEpochToTime(c.getTranscodingUpdateDate(), c.getUpdateDate()),
					    String.valueOf(c.getSorting()),
					    c.getPiwik(),
					    String.valueOf(c.getRecentlyWatchedProgram()),
					    };
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
					        String.valueOf(p.getPubDate().getTime()),
					        p.getComment()};
			output = output + NnStringUtil.getDelimitedStr(ori);
			output = output.replaceAll("null", "");
			output = output + "\n";
		}
		return output;		
	}
		
	public String findFeaturedSets(String lang) {
		if (lang == null)
			lang = Mso.LANG_EN;
		ChannelSetManager setMngr = new ChannelSetManager();
		List<ChannelSet> sets = setMngr.findFeaturedSets(lang);
		String[] result = {""};
		for (ChannelSet set : sets) {
			String[] obj = {
				String.valueOf(set.getKey().getId()),
				set.getName(),
				set.getIntro(),
				set.getImageUrl(),
				String.valueOf(set.getChannelCount()),
			};
			result[0] += NnStringUtil.getDelimitedStr(obj) + "\n";			
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);		
	}
	
	public String findFeaturedChannels() {
		MsoChannelManager channelMngr = new MsoChannelManager();
		List<MsoChannel> channels = channelMngr.findFeaturedChannels();		
		String output = NnStatusMsg.successStr(locale) + separatorStr;
		for (MsoChannel c : channels) {
			output += this.composeChannelLineupStr(c, mso) + "\n";
		}				
		return output;		
	}

    public String assembleMsgs(int status, String[] raw) {
        String result = NnStatusMsg.getMsg(status, locale);
        String separatorStr = "--\n";
        if (raw != null && raw.length > 0) {
            result = result + separatorStr;
            for (String s : raw) {
                s = s.replaceAll("null", "");
                result += s + separatorStr;
            }
        }
        if (result.substring(result.length()-3, result.length()).equals(separatorStr)) {
            result = result.substring(0, result.length()-3);
        }                 
        return result;
    }
	
	public String findCategoryInfo(String id, String lang) {
		lang = this.checkLang(lang);	
        if (lang == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);				
		if (id == null)
			id = "0";
		
		String[] result = {"", "", ""};
		CategoryManager categoryMngr = new CategoryManager();		
		CategoryChannelSetManager ccsMngr = new CategoryChannelSetManager();
		ChannelSetManager csMngr = new ChannelSetManager();
		//it's a set, find channel info
		result[0] = id + "\n";
		if (id.startsWith("s")) {
			List<MsoChannel> channels = csMngr.findChannelsById(Long.parseLong(id.substring(1, id.length())));			
			for (MsoChannel c : channels) {
				result[2] += this.composeChannelLineupStr(c, mso) + "\n";
			}
			return this.assembleMsgs(NnStatusCode.SUCCESS, result);
		}
		
		List<Category> categories = categoryMngr.findPlayerCategories(Long.parseLong(id), lang);
		//it's the end of category leaf, find set info
		if (categories.size() == 0) {
			List<CategoryChannelSet> list = ccsMngr.findAllByCategoryId(Long.parseLong(id));
			List<Long> channelSetIdList = new ArrayList<Long>();
			for (CategoryChannelSet l : list) {
				channelSetIdList.add(l.getChannelSetId());
			}
			List<ChannelSet> csList = csMngr.findAllByChannelSetIds(channelSetIdList);
			for (ChannelSet cs : csList) {
				String name =  cs.getName();
				int cnt = cs.getChannelCount();
				String[] str = {"s" + String.valueOf(cs.getKey().getId()), name, String.valueOf(cnt)};				
				result[1] += NnStringUtil.getDelimitedStr(str) + "\n";				
			}
			return this.assembleMsgs(NnStatusCode.SUCCESS, result);			
		}

		//find categories
		for (Category c : categories) {
			String name =  c.getName();
			int cnt = c.getChannelCount();
			String[] str = {String.valueOf(c.getKey().getId()), 
					                       name, 
					                       String.valueOf(cnt), 
					                       String.valueOf(c.getSubCategoryCnt())};				
			result[1] += NnStringUtil.getDelimitedStr(str) + "\n";
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private HashMap checkUser(String userToken, boolean guestOK) {
		HashMap map = new HashMap();    	
		//verify input
		if (userToken == null || userToken.length() == 0 || userToken.equals("undefined"))
			map.put("s", NnStatusCode.INPUT_MISSING);
		if (guestOK) {
			map.put("s", NnStatusCode.SUCCESS);
			return map;
		}
		//verify user
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) {
			map.put("s", NnStatusCode.USER_INVALID);
			return map;
		}
		if (!guestOK && user.getEmail().equals(NnUser.GUEST_EMAIL) ) {
			map.put("s", NnStatusCode.USER_PERMISSION_ERROR);
			return map;
		}
		map.put("s", NnStatusCode.SUCCESS);
		map.put("u", user);
		return map;
    }
	
	public String saveSorting(String userToken, String channelId, String sort) {
		@SuppressWarnings("rawtypes")
		HashMap map = this.checkUser(userToken, false);
		if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
			return this.assembleMsgs((Integer)map.get("s"), null);
		}
		NnUser user = (NnUser) map.get("u");
		NnUserChannelSorting sorting = new NnUserChannelSorting(user.getKey().getId(), 
				                           Long.parseLong(channelId), Short.parseShort(sort));
		NnUserChannelSortingManager sortingMngr = new NnUserChannelSortingManager();
		sortingMngr.save(sorting);		
		return this.assembleMsgs(NnStatusCode.SUCCESS, null);
	}
	
	public String createCaptcha(String token, String action) {
		if (token == null || action == null)
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		CaptchaManager mngr = new CaptchaManager();
		Captcha c = mngr.getRandom();
		if (c == null)
			return this.assembleMsgs(NnStatusCode.CAPTCHA_ERROR, null);
		short a = Short.valueOf(action);   
		Calendar cal = Calendar.getInstance();		
		cal.add(Calendar.MINUTE, 5);		
		NnGuestManager guestMngr = new NnGuestManager();
		NnGuest guest = guestMngr.findByToken(token);				
		if (a == Captcha.ACTION_SIGNUP) {
			if (guest == null)
				return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
		} else if (a == Captcha.ACTION_EMAIL) {
			if (guest == null) {
				guest = new NnGuest(NnGuest.TYPE_USER);
				guest.setToken(token);
			}
		}
		guest.setCaptchaId(c.getKey().getId());
		guest.setExpiredAt(cal.getTime());
		guest.setGuessTimes(0);
		guestMngr.save(guest);		
		return this.assembleMsgs(NnStatusCode.SUCCESS, new String[] {c.getFileName()});
	}	
	
	public String shareByEmail(String userToken, String toEmail, String toName, 
			                   String subject, String content, 
			                   String captcha, String text) {		
		@SuppressWarnings("rawtypes")
		HashMap map = this.checkUser(userToken, false);
		if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
			return this.assembleMsgs((Integer)map.get("s"), null);
		}
		if (captcha == null || text == null || toEmail == null || content == null)
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		NnUser user = (NnUser) map.get("u");
		if (captcha != null) {
			NnGuestManager guestMngr = new NnGuestManager();
			NnGuest guest = guestMngr.findByToken(userToken);
			int status = this.checkCaptcha(guest, captcha, text);
			if (status != NnStatusCode.SUCCESS)
				return this.assembleMsgs(status, null);
			guestMngr.delete(guest);
		}
		EmailService service = new EmailService();
		NnEmail mail = new NnEmail(toEmail, toName, NnEmail.SEND_EMAIL_SHARE, user.getName(), user.getEmail(), subject, content);		
		service.sendEmail(mail);
		return this.assembleMsgs(NnStatusCode.SUCCESS, null);
	}
	
	public String findUserWatched(String userToken, String count, boolean channelInfo) {
		@SuppressWarnings("rawtypes")
		HashMap map = this.checkUser(userToken, false);
		if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
			return this.assembleMsgs((Integer)map.get("s"), null);
		}
		if (count == null) 
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);		

		String[] result = {"", ""};
		NnUserWatchedManager watchedMngr = new NnUserWatchedManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		List<NnUserWatched> watched = watchedMngr.findAllByUserToken(userToken);
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		int cnt = Integer.parseInt(count);
		int i = 1;
		for (NnUserWatched w : watched) {
			if (i > cnt)
				break;
			result[0] += w.getChannelId() + "\t" + w.getProgram() + "\n";
			MsoChannel c = channelMngr.findById(w.getChannelId());
			if (c != null) { 
				channels.add(c);
			}
			i++;
		}
		if (channelInfo) {
			for (MsoChannel c : channels) {
				result[1] += this.composeChannelLineupStr(c, mso) + "\n";
			}
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
	
	public String createPiwikSite(String set, String channel, HttpServletRequest req) {
		String idsite = PiwikLib.createPiwikSite(Integer.parseInt(set), Integer.parseInt(channel), req);
		if (idsite == null)
			return this.assembleMsgs(NnStatusCode.PIWIK_ERROR, null);
		String[] result = {String.valueOf(idsite)};
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}	
	  
}