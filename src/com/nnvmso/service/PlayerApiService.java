package com.nnvmso.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
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
import com.nnvmso.lib.YouTubeLib;
import com.nnvmso.model.AreaOwnership;
import com.nnvmso.model.Category;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoConfig;
import com.nnvmso.model.MsoIpg;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.NnUserPref;
import com.nnvmso.model.NnUserShare;
import com.nnvmso.model.Subscription;
import com.nnvmso.model.SubscriptionLog;

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
		
		System.out.println("channel set id:" + cs.getKey().getId());
		List<MsoChannel> channels = csMngr.findChannelsById(cs.getKey().getId());
		System.out.println(channels.size());
		//first block: status
		String output = NnStatusMsg.successStr(locale) + separatorStr;
		Mso csMso = msoMngr.findById(cs.getMsoId());
		//2nd block		
		output = output + assembleKeyValue("name", csMso.getName());
		output = output + assembleKeyValue("imageUrl", csMso.getLogoUrl()); 
		output = output + assembleKeyValue("intro", csMso.getIntro());
		output = output + separatorStr;			
		//3rd block: set info
		output += assembleKeyValue("id", String.valueOf(cs.getKey().getId()));
		output += assembleKeyValue("name", cs.getName());		
		output += assembleKeyValue("imageUrl", cs.getImageUrl());
		
		//4rd block, channel info
		String channelLineup = separatorStr;
		for (MsoChannel c : channels) {
			channelLineup = channelLineup + this.composeChannelLineupStr(c, csMso) + "\n";													
		}
		output += channelLineup;
		return output;
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
	
	public int addMsoInfoVisitCounter(String msoName) {
		String counterName = msoName + "BrandInfo";
		CounterFactory factory = new CounterFactory();
		ShardedCounter counter = factory.getOrCreateCounter(counterName);
		counter.increment();			
		return counter.getCount(); 								
	}
	
	public String findMsoInfo(HttpServletRequest req) {
		Mso theMso = msoMngr.findMsoViaHttpReq(req);
		if (theMso == null) {return NnStatusMsg.msoInvalid(locale);}
		int counter = this.addMsoInfoVisitCounter(theMso.getName());
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig config = configMngr.findByMsoIdAndItem(theMso.getKey().getId(), MsoConfig.DEBUG);
		MsoConfig fbConfig = configMngr.findByItem(MsoConfig.FBTOKEN);
		String debug = "1";
		if (config != null) { debug = config.getValue(); }
		
		String results = NnStatusMsg.successStr(locale) + separatorStr;
		results = results + this.assembleKeyValue("key", String.valueOf(mso.getKey().getId()));
		results = results + this.assembleKeyValue("name", mso.getName());
		results = results + this.assembleKeyValue("title", mso.getTitle());		
		results = results + this.assembleKeyValue("logoUrl", mso.getLogoUrl());
		results = results + this.assembleKeyValue("jingleUrl", mso.getJingleUrl());
		results = results + this.assembleKeyValue("logoClickUrl", mso.getLogoClickUrl());
		results = results + this.assembleKeyValue("preferredLangCode", mso.getPreferredLangCode());
		results = results + this.assembleKeyValue("jingleUrl", mso.getJingleUrl());
		results = results + this.assembleKeyValue("brandInfoCounter", String.valueOf(counter));
		results = results + this.assembleKeyValue("debug", debug);
		if (fbConfig!=null)
			results = results + this.assembleKeyValue(MsoConfig.FBTOKEN, fbConfig.getValue());
		
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
		pdrMngr.processPdr(pdr, user.getKey().getId(), session);
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
	
	public String saveIpg(String userToken, String channelId, String programId) {
		return NnStatusCode.API_DEPRECATED + "\t" + "Depreacated";		
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
	
	public String loadIpg(long ipgId) {
		return NnStatusCode.API_DEPRECATED + "\t" + "Depreacated";		
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
		
		//subscribe default channels
		if (ipg != null) {
			SubscriptionManager sMngr = new SubscriptionManager();
			List<MsoChannel> ipgChannels = ipgMngr.findIpgChannels(ipg);
			for (MsoChannel c : ipgChannels)
				sMngr.subscribeChannel(guest.getKey().getId(), c.getKey().getId(), c.getSeq(), c.getType(), mso.getKey().getId());			
		} else {
			userMngr.subscibeDefaultChannels(guest);
		}
		
		//prepare cookie and output
		String output = this.prepareUserInfo(guest);	
		this.setUserCookie(resp, CookieHelper.GUEST, guest.getToken());
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
		output = output + assembleKeyValue("lastLogin", String.valueOf(user.getUpdateDate().getTime()));
		
		NnUserPrefManager prefMngr = new NnUserPrefManager();
		List<NnUserPref> list = prefMngr.findByUserId(user.getKey().getId());		
		for (NnUserPref pref : list) {
			output = output + this.assembleKeyValue(pref.getItem(), pref.getValue());
		}
			
		return output;
	}
	
	public void setUserCookie(HttpServletResponse resp, String cookieName, String userId) {		
		CookieHelper.setCookie(resp, cookieName, userId);
	}	
		
	public String unsubscribe(String userToken, String channelId, String setId, String grid) {
		//verify input
		if (userToken == null || userToken.equals("undefined"))			
			return NnStatusMsg.inputMissing(locale);		
		if (channelId == null && setId == null) 
			return NnStatusMsg.inputMissing(locale);
		if ((channelId != null && !Pattern.matches("^\\d*$", channelId)) || 
			(setId != null && !Pattern.matches("^\\d*$", setId)))
			return NnStatusMsg.inputError(locale);		
		//verify user
		NnUser user = new NnUserManager().findByToken(userToken);
		if (user == null) {return NnStatusMsg.userInvalid(locale);}
		
		//unsubscribe
		if (channelId != null) {
			SubscriptionManager subMngr = new SubscriptionManager();
			Subscription s = null;
			if (grid == null) {
				s = subMngr.findByUserIdAndChannelId(user.getKey().getId(), Long.parseLong(channelId));
			} else {
				s = subMngr.findChannelSubscription(user.getKey().getId(), Long.parseLong(channelId), Integer.parseInt(grid));
				System.out.print("find subscription:" + s);
			}			
			if (s == null || (s != null && s.getType() == MsoIpg.TYPE_READONLY)) {
				return messageSource.getMessage("nnstatus.subscription_ro_channel", new Object[] {NnStatusCode.SUBSCRIPTION_ERROR} , locale);			
			}			
			subMngr.unsubscribeChannel(s);
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
		return NnStatusMsg.successStr(locale);
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
	
	public String subscribe(String userToken, String channelId, String setId, String gridId, String pos) {
		//verify input
		if (userToken == null || userToken.equals("undefined")) return NnStatusMsg.inputMissing(locale);
		if ((setId != null && pos == null) || (setId == null && pos != null)) return NnStatusMsg.inputMissing(locale);
		if ((channelId != null && gridId == null) || (channelId == null && gridId != null)) return NnStatusMsg.inputMissing(locale);
		
		String output = messageSource.getMessage("nnstatus.channel_or_user_invalid", new Object[] {NnStatusCode.CHANNEL_OR_USER_INVALID} , locale);		
		NnUser user = new NnUserManager().findByToken(userToken);
		if (user == null) {return output;}	
		
		SubscriptionManager subMngr = new SubscriptionManager();
		if (setId != null) {			
			//find all channels
			List<Subscription> subscribeList = subMngr.findAllByUser(user.getKey().getId());
			ChannelSetManager csMngr = new ChannelSetManager();
			long sId = Long.parseLong(setId);
			ChannelSet cs = csMngr.findById(sId);
			if (cs == null)
				return messageSource.getMessage("nnstatus.set_invalid", new Object[] {NnStatusCode.SET_INVALID} , locale);			
			
			List<MsoChannel> channels = csMngr.findChannelsById(cs.getKey().getId());
			Short[] startArr = {1,4,7,28,31,34,55,58,61};
			Short start = startArr[Integer.parseInt(pos)-1]; 
			for (MsoChannel c : channels) {					
				short grid= 0;
				if (c.getSeq() < 4)
					grid = (short) (c.getSeq() + start - 1);
				if (c.getSeq() > 3 && c.getSeq() < 7)
					grid = (short) (start + 9 + c.getSeq()-4);
				else if (c.getSeq() > 6)
					grid = (short) (start + 18 + c.getSeq()-7);
				for (Subscription l : subscribeList) {
					if (l.getSeq() == grid) {
						return messageSource.getMessage("nnstatus.subscription_set_occupied", new Object[] {NnStatusCode.SUBSCRIPTION_SET_OCCUPIED} , locale);							
					}
				}
			}
			AreaOwnershipManager areaMngr = new AreaOwnershipManager();
			AreaOwnership area = areaMngr.findByUserIdAndSetId(user.getKey().getId(), cs.getKey().getId());
			if (area != null) {
				return messageSource.getMessage("nnstatus.subscription_duplicate_set", new Object[] {NnStatusCode.SUBSCRIPTION_DUPLICATE_SET} , locale);												
			}
			area = new AreaOwnership();											
			area.setUserId(user.getKey().getId());
			area.setSetId(cs.getKey().getId());
			area.setSetName(cs.getName());
			area.setSetImageUrl(cs.getImageUrl());
			area.setAreaNo(Short.parseShort(pos));
			area.setType(AreaOwnership.TYPE_RO);
			areaMngr.save(area);
			SubscriptionLogManager logMngr = new SubscriptionLogManager();
			SubscriptionLog log = logMngr.findByMsoIdAndSetId(mso.getKey().getId(), cs.getKey().getId());
			if (log == null) {
				log = new SubscriptionLog();
				log.setCount(1);
				log.setSetId(cs.getKey().getId());
				log.setMsoId(mso.getKey().getId());
				logMngr.save(log);
			}			
		}

		//verify channel and grid
		if (channelId != null) {
			long cId = Long.parseLong(channelId);			
			MsoChannel channel = new MsoChannelManager().findById(cId);			
			if (channel == null || channel.getStatus() == MsoChannel.STATUS_ERROR) { 
				return messageSource.getMessage("nnstatus.subscription_duplicate_channel", new Object[] {NnStatusCode.CHANNEL_STATUS_ERROR} , locale);
			}
			boolean status = subMngr.subscribeChannel(user.getKey().getId(), cId, Integer.parseInt(gridId), MsoIpg.TYPE_GENERAL, mso.getKey().getId());
			if (!status) {
				//the general status shows error even there's only one error
				output = messageSource.getMessage("nnstatus.subscription_duplicate_channel", new Object[] {NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL} , locale);
			}			
		}
		
		output = NnStatusMsg.successStr(locale);		
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
		email = email.trim();
		name = name.trim();
		if (!Pattern.matches(regex, email.toLowerCase()) || password.length() < 6) {		
			return NnStatusMsg.inputError(locale);
		}
			
		//find mso
		if (mso == null) { return NnStatusMsg.msoInvalid(locale);}
		
		//verify email
		NnUser user = userMngr.findByEmailAndMso(email, mso);
		if (user != null) {
			log.info("user email taken:" + user.getEmail() + "; mso=" + mso.getName() + ";user token=" + user.getToken());
			return messageSource.getMessage("nnstatus.user_email_taken", new Object[] {NnStatusCode.USER_EMAIL_TAKEN} , locale);
		}
		boolean convertFromGuest = true;
		//create user
		if (userToken != null) { user = userMngr.findByToken(userToken);}
		if (user == null ) {
			convertFromGuest = false;
			log.info("User signup userToken NOT FOUND. Token=" + userToken);
			user = new NnUser(email, password, name, NnUser.TYPE_USER, mso.getKey().getId());
			log.info("user, based on the input:" + user.toString());
			userMngr.create(user);
			userMngr.subscibeDefaultChannels(user);
		} else {
			log.info("User signup with guest token=" + userToken + "; email=" + email + "; name=" + name + ";password=" + password);					 		
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
		log.info("user, after user's created" + user.toString());			
		String output = this.prepareUserInfo(user);
		if (convertFromGuest) {
			CookieHelper.deleteCookie(resp, CookieHelper.GUEST);
		}
		this.setUserCookie(resp, CookieHelper.USER, user.getToken());
		return output;
	}
	
	public String findUserByToken(String token, HttpServletRequest req, HttpServletResponse resp) {
		if (token == null) {return NnStatusMsg.inputMissing(locale);}
		
		NnUser found = userMngr.findByToken(token);			
		if (found == null || (found != null && found.getMsoId() != mso.getKey().getId())) {
			CookieHelper.deleteCookie(resp, CookieHelper.USER);
			return NnStatusMsg.userInvalid(locale);
		}
		
		if (found.getEmail().equals(NnUser.GUEST_EMAIL)) {
			this.setUserCookie(resp, CookieHelper.GUEST, found.getToken());			
		} else {
			this.setUserCookie(resp, CookieHelper.USER, found.getToken());
		}
		userMngr.save(found); //change last login time (ie updateTime)
		return this.prepareUserInfo(found);
	}
	
	public String findAuthenticatedUser(String email, String password, HttpServletRequest req, HttpServletResponse resp) {		
		log.info("login: email=" + email + "; mso=" + mso.getKey().getId());
		if (email == null || email.length() == 0 ||  password == null || password.length() == 0) {
			return NnStatusMsg.inputMissing(locale);
		}		
		String output = messageSource.getMessage("nnstatus.user_login_failed", new Object[] {NnStatusCode.USER_LOGIN_FAILED} , locale);		
		NnUser user = userMngr.findAuthenticatedUser(email, password, mso.getKey().getId());
		if (user != null) {
			output = this.prepareUserInfo(user);
			userMngr.save(user); //change last login time (ie updateTime)
			this.setUserCookie(resp, CookieHelper.USER, user.getToken());
		}
		return output;
	}

	public String createChannel(String categoryIds, String userToken, String url, String grid, String tags, HttpServletRequest req) {
		//verify input
		if (url == null || url.length() == 0 ||  grid == null || grid.length() == 0 ||
			categoryIds == null || categoryIds.equals("undefined") || categoryIds.length() == 0 ||
			userToken== null || userToken.length() == 0) {
			return NnStatusMsg.inputMissing(locale);
		}
		if (!Pattern.matches("^\\d*$", grid) || Integer.parseInt(grid) < 0 || Integer.parseInt(grid) > 81) {			
			return NnStatusMsg.inputError(locale);
		}
		
		url = url.trim();
		
		//verify user
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) { return NnStatusMsg.userInvalid(locale);}		
		if (user.getEmail().equals(NnUser.GUEST_EMAIL)) {
			return messageSource.getMessage("nnstatus.user_permission_error", new Object[] {NnStatusCode.USER_PERMISSION_ERROR} , locale);
		}
				
		//verify category
		CategoryManager categoryMngr = new CategoryManager();
		List<Category> categories = categoryMngr.findCategoriesByIdStr(categoryIds);
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
		System.out.println("seq:" + seq + ";pos=" + pos);
		if (seq == 0) {
			return 0;
		}
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
	
	public String findChannelInfo(String userToken, boolean userInfo, String channelIds, boolean setInfo) {
		//verify input
		if ((userToken == null && userInfo == true) || (userToken == null && channelIds == null) || (userToken == null && setInfo == true)) {
			return NnStatusMsg.inputMissing(locale);
		}		
		NnUser user = null;
		if (userToken != null) {
			//verify user
			user = userMngr.findByToken(userToken);
			if (user == null) {return NnStatusMsg.userInvalid(locale);}
		}
		
		String result = NnStatusMsg.successStr(locale) + separatorStr;
		if (userInfo) {
			result = this.prepareUserInfo(user) + separatorStr;	}
		
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
			//check format
			if (chArr.length > 1) {
				List<Long> list = new ArrayList<Long>();
				for (int i=0; i<chArr.length; i++) { list.add(Long.valueOf(chArr[i]));}
				channels = channelMngr.findAllByChannelIds(list);
			} else {
				MsoChannel channel = channelMngr.findById(Long.parseLong(channelIds));
				channels.add(channel);
			}
		}
		AreaOwnershipManager areaMngr = new AreaOwnershipManager();
		List<MsoChannel> setChannels = new ArrayList<MsoChannel>();
		if (user != null) {
			List<AreaOwnership> sets = areaMngr.findByUserId(user.getKey().getId());		
		    //set info
			if (setInfo) {
				for (AreaOwnership s : sets) {
					String[] obj = {
							String.valueOf(s.getAreaNo()),
							String.valueOf(s.getSetId()),
							s.getSetName(),						
							s.getSetImageUrl(),
							String.valueOf(s.getType()),
					};
					result = result + NnStringUtil.getDelimitedStr(obj);;
					result = result + "\n";
				}
				result = result + separatorStr;					
			}		
			
			//find channels from set
			ChannelSetManager setMngr = new ChannelSetManager();
			for (AreaOwnership area : sets) {
				if (area.getType() == AreaOwnership.TYPE_RO) {
					List<MsoChannel> list = setMngr.findChannelsById(area.getSetId());
					for (MsoChannel c : list) {
						c.setSeq(this.convertSetPosToIPGSeq(c.getSeq(), area.getAreaNo()));
					}
					setChannels.addAll(list);				
				}
			}
		}
		//sort by key
		if (channelPos) {
			TreeMap<Integer, MsoChannel> channelMap = new TreeMap<Integer, MsoChannel>();
			for (MsoChannel c : channels) {
				channelMap.put(c.getSeq(), c);
			}
			if (user != null) {
				//overwrite by set channels
				for (MsoChannel c : setChannels) {
					channelMap.put(c.getSeq(), c);
				}
			}
			Iterator<Entry<Integer, MsoChannel>> it = channelMap.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<Integer, MsoChannel> pairs = (Map.Entry<Integer, MsoChannel>)it.next();
				result = result + this.composeChannelLineupStr((MsoChannel)pairs.getValue(), mso);
				result = result + "\n";	       
		    }
		} else {
			log.info("channelLineup: regardless the pos");
			for (MsoChannel c : channels) {
				result = result + this.composeChannelLineupStr(c, mso);
				result = result + "\n";
			}
		}
		return result;
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
			if (user == null) { return NnStatusMsg.userInvalid(locale); }
			programs = programMngr.findSubscribedPrograms(user.getKey().getId());
		} else if (chArr.length > 1) {
			List<Long> list = new ArrayList<Long>();
			for (int i=0; i<chArr.length; i++) { list.add(Long.valueOf(chArr[i]));}
			programs = programMngr.findGoodProgramsByChannelIds(list);
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
		} else if (c.getStatus() != MsoChannel.STATUS_WAIT_FOR_APPROVAL &&
				   c.getStatus() != MsoChannel.STATUS_SUCCESS && 
				   c.getStatus() != MsoChannel.STATUS_PROCESSING) {	
				imageUrl = "http://9x9ui.s3.amazonaws.com/9x9playerV65/images/error.png";
		}

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
					    this.convertEpochToTime(c.getTranscodingUpdateDate(), c.getUpdateDate())
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
					        String.valueOf(p.getPubDate().getTime())};
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
	
	public String findFeaturedSetsByMso() {
		ChannelSetManager setMngr = new ChannelSetManager();
		SubscriptionLogManager logMngr = new SubscriptionLogManager();		
		
		List<ChannelSet> sets = setMngr.findFeaturedSetsByMso(mso);		
		String output = NnStatusMsg.successStr(locale) + separatorStr;		
		for (ChannelSet set : sets) {
			SubscriptionLog log = logMngr.findByMsoIdAndSetId(mso.getKey().getId(), set.getKey().getId());
			int counter = 0;
			if (log != null)
				counter = log.getCount();
			String[] obj = {
				String.valueOf(set.getKey().getId()),
				set.getName(),
				set.getImageUrl(),
				String.valueOf(set.getChannelCount()),
				String.valueOf(counter),
			};
			output += NnStringUtil.getDelimitedStr(obj) + "\n";			
		}				
		return output;		
	}

	public String findFeaturedChannelsByMso() {
		MsoChannelManager channelMngr = new MsoChannelManager();
		//!!! temp
		NnUser user = null;
		if (mso.getName().equals(Mso.NAME_5F)) {
			user = userMngr.findByEmailAndMso("mso@5f.tv", mso);
		} else  {
			user = userMngr.findByEmailAndMso("mso@9x9.tv", mso);
		}
		
		List<MsoChannel> channels = channelMngr.findFeaturedChannelsByMso(user);		
		String output = NnStatusMsg.successStr(locale) + separatorStr;
		for (MsoChannel c : channels) {
			output += this.composeChannelLineupStr(c, mso) + "\n";							
						
		}				
		return output;		
	}
	
}