package com.nncloudtv.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.ShardedCounter;
import com.nncloudtv.lib.AuthLib;
import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.QueueMessage;
import com.nncloudtv.lib.YouTubeLib;
import com.nncloudtv.model.Captcha;
import com.nncloudtv.model.Category;
import com.nncloudtv.model.CntSubscribe;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.MsoIpg;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnContent;
import com.nncloudtv.model.NnDevice;
import com.nncloudtv.model.NnEmail;
import com.nncloudtv.model.NnGuest;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.NnSet;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserChannelSorting;
import com.nncloudtv.model.NnUserPref;
import com.nncloudtv.model.NnUserReport;
import com.nncloudtv.model.NnUserShare;
import com.nncloudtv.model.NnUserSubscribe;
import com.nncloudtv.model.NnUserSubscribeGroup;
import com.nncloudtv.model.NnUserWatched;
import com.nncloudtv.validation.BasicValidator;
import com.nncloudtv.validation.NnUserValidator;

@Service
public class PlayerApiService {
    
    protected static final Logger log = Logger.getLogger(PlayerApiService.class.getName());    
    
    private NnUserManager userMngr = new NnUserManager();    
    private MsoManager msoMngr = new MsoManager();
    private Locale locale;
    private Mso mso;
        
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    public void setMso(Mso mso) {
        this.mso = mso;
    }        

    public String handleException (Exception e) {
    	if (e.getClass().equals(NumberFormatException.class)) {
    		return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);    		
    	}
        String output = NnStatusMsg.getPlayerMsg(NnStatusCode.ERROR, locale);
        NnLogUtil.logException((Exception) e);
        return output;
    }    

    public int addMsoInfoVisitCounter(String msoName) {
        try {
            new QueueMessage().fanout("localhost",QueueMessage.BRAND_COUNTER, msoName);
        } catch (IOException e) {
            e.printStackTrace();
        }        
        String counterName = msoName + "BrandInfo";
        CounterFactory factory = new CounterFactory();
        ShardedCounter counter = factory.getOrCreateCounter(counterName);            
        return counter.getCount()+1;                                 
    }
    
	public int addMsoInfoVisitCounter(String msoName, boolean readOnly) {		
		String counterName = msoName + "BrandInfo";
		CounterFactory factory = new CounterFactory();
		ShardedCounter counter = factory.getOrCreateCounter(counterName);
		if (!readOnly)
			counter.increment();			
		return counter.getCount(); 								
	}
            
    //assemble key and value string
    public static String assembleKeyValue(String key, String value) {
        return key + "\t" + value + "\n";
    }
    
    /**
     * assemble final output to player 
     * 1. status line in the front
     * 2. raw: for each section needs to be separated by separator string, "--\n"
     */
    public String assembleMsgs(int status, String[] raw) {
        String result = NnStatusMsg.getPlayerMsg(status, locale);
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
			List<NnUserPref> list = prefMngr.findByUser(user);		
			for (NnUserPref pref : list) {
				output += PlayerApiService.assembleKeyValue(pref.getItem(), pref.getValue());
			}			
		} else {		
			output += assembleKeyValue("token", guest.getToken());
			output += assembleKeyValue("name", NnUser.GUEST_NAME);
			output += assembleKeyValue("lastLogin", "");			
		}
			
		return output;
	}

    public void setUserCookie(HttpServletResponse resp, String cookieName, String userId) {        
        CookieHelper.setCookie(resp, cookieName, userId);
    }    

	public String signup(String email, String password, String name, String token,
                         String captchaFilename, String captchaText,
                         String sphere, String lang,
                         String year,
                         HttpServletRequest req, HttpServletResponse resp) {		
		//validate basic inputs
		int status = NnUserValidator.validate(email, password, name, req);
		if (status != NnStatusCode.SUCCESS) 
			return this.assembleMsgs(status, null);
		lang = this.checkLang(lang);	
		sphere = this.checkLang(sphere);
        if (lang == null || sphere == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);				
		
        //convert from guest
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
		NnUser user = new NnUser(email, password, name, NnUser.TYPE_USER, mso.getId());
		user.setSphere(sphere);
		user.setLang(lang);		
		user.setDob(year);
		user.setIp(req.getRemoteAddr());
		status = userMngr.create(user, req, (short)0);
		if (status != NnStatusCode.SUCCESS)
			return this.assembleMsgs(status, null);
		
		userMngr.subscibeDefaultChannels(user);							
		String[] result = {this.prepareUserInfo(user, null)};		
		this.setUserCookie(resp, CookieHelper.USER, user.getToken());
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
	public int checkRO() {
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig config = configMngr.findByItem(MsoConfig.RO);
		if (config != null && config.getValue().equals("1"))			
			return NnStatusCode.DATABASE_READONLY;
		return NnStatusCode.SUCCESS;
	}
    
    public String guestRegister(HttpServletRequest req, HttpServletResponse resp) {
		//verify input		
		NnGuestManager mngr = new NnGuestManager();
		NnGuest guest = new NnGuest(NnGuest.TYPE_GUEST);
		mngr.save(guest, req);		
		
		String[] result = {""};			
		result[0] += assembleKeyValue("token", guest.getToken());
		result[0] += assembleKeyValue("name", NnUser.GUEST_NAME);
		result[0] += assembleKeyValue("lastLogin", "");

		//prepare cookie and output
		this.setUserCookie(resp, CookieHelper.USER, guest.getToken());
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
	public String userTokenVerify(String token, HttpServletRequest req, HttpServletResponse resp) {
		if (token == null) {return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);}
		
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
		String[] result = {""};
		result[0] = this.prepareUserInfo(user, guest);
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
    

	public String category(String id, String lang) {
		lang = this.checkLang(lang);	
        if (lang == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);				        
		if (id == null)
			id = "0";
		
		String[] result = {"", "", ""};
		CategoryManager catMngr = new CategoryManager();
		NnSetManager setMngr = new NnSetManager();
		
		//if it's a set, find channel info
		result[0] = "id" + "\t" + id + "\n";
		if (id.startsWith("s")) {
			long csId = Long.parseLong(id.substring(1, id.length()));
			NnSet set = setMngr.findById(csId);
			if (set != null) {
				result[0] += "piwik" + "\t" + set.getPiwik() + "\n";
			}
			List<NnChannel> channels = setMngr.findPlayerChannelsById(csId);			
			for (NnChannel c : channels) {
				c.setSorting(NnChannelManager.getDefaultSorting(c));
				result[2] += this.composeChannelLineupStr(c, mso) + "\n";
			}
			return this.assembleMsgs(NnStatusCode.SUCCESS, result);
		}
		
		List<Category> categories = catMngr.findPlayerCategories(Long.parseLong(id), lang);
		//if it's the end of category leaf, find set info
		if (categories.size() == 0) {
			List<NnSet> sets = catMngr.findPlayerSetsByCategory(Long.parseLong(id));
			for (NnSet s : sets) {
				String name =  s.getName();
				int cnt = s.getChannelCnt();
				String[] str = {"s" + String.valueOf(s.getId()), 
						        name, 
						        String.valueOf(cnt), "ch"};				
				result[1] += NnStringUtil.getDelimitedStr(str) + "\n";
			}
			return this.assembleMsgs(NnStatusCode.SUCCESS, result);			
		}
		
		//if it's just categories, find categories
		for (Category c : categories) {
			String name =  c.getName();
			int cnt = c.getChannelCnt();
			String subItemHint = "cat"; //what's under this level
			if (c.getSubCatCnt() == 0)
				subItemHint = "set";
			String[] str = {String.valueOf(c.getId()), 
					        name, 
					        String.valueOf(cnt), 
					        subItemHint};				
			result[1] += NnStringUtil.getDelimitedStr(str) + "\n";
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
     
    public String brandInfo(HttpServletRequest req) {    	
    	String[] result = msoMngr.getBrandInfoCache(false);
    	boolean readOnly = MsoConfigManager.isInReadonlyMode(false);
		//locale
		String locale = this.findLocaleByHttpRequest(req);
		result[0] += PlayerApiService.assembleKeyValue("locale", locale);
		//counter
		int counter = 0;
		if (!readOnly)
			counter = this.addMsoInfoVisitCounter(mso.getName(), readOnly);		
		result[0] += PlayerApiService.assembleKeyValue("brandInfoCounter", String.valueOf(counter));
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);        
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
    
	public String pdr(String userToken, String deviceToken,			 
					  String session, String pdr,
			          HttpServletRequest req) {
		if (userToken == null && deviceToken == null)
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		if (pdr == null || pdr.length() == 0) 
			return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);	
		
		NnUser user = null;
		if (userToken != null) { 
			//verify input
			@SuppressWarnings("rawtypes")
			HashMap map = this.checkUser(userToken, false);
			user = (NnUser) map.get("u");
		}
		List<NnDevice> devices = new ArrayList<NnDevice>();
		NnDevice device = null;
		if (deviceToken != null) {
			NnDeviceManager deviceMngr = new NnDeviceManager();
			devices = deviceMngr.findByToken(deviceToken);
			if (devices.size() > 0)
				device = devices.get(0);
		}
		if (device == null && user == null)
			return this.assembleMsgs(NnStatusCode.ACCOUNT_INVALID, null);
		
		//pdr process
		PdrRawManager pdrMngr = new PdrRawManager();
		String ip = req.getRemoteAddr();
		pdrMngr.processPdr(user, device, session, pdr, ip);
		return this.assembleMsgs(NnStatusCode.SUCCESS, null);
	}    
    
    
    public String setInfo(String id, String beautifulUrl) {
        if (id == null && beautifulUrl == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        NnSetManager setMngr = new NnSetManager();
        NnSet set = null;
        if (id != null) {
            set = setMngr.findById(Long.parseLong(id));
        } else {
            set = setMngr.findBybeautifulUrl(beautifulUrl);
        }
        if (set == null)
            return this.assembleMsgs(NnStatusCode.SET_INVALID, null);            
        
        List<NnChannel> channels = setMngr.findPlayerChannelsById(set.getId());
        String result[] = {"", "", ""};

        Mso csMso = msoMngr.findNNMso();
        //mso info        
        result[0] += assembleKeyValue("name", csMso.getName());
        result[0] += assembleKeyValue("imageUrl", csMso.getLogoUrl()); 
        result[0] += assembleKeyValue("intro", csMso.getIntro());            
        //set info
        result[1] += assembleKeyValue("id", String.valueOf(set.getId()));
        result[1] += assembleKeyValue("name", set.getName());
        result[1] += assembleKeyValue("imageUrl", set.getImageUrl());
        result[1] += assembleKeyValue("piwik", set.getPiwik());
        //channel info
        for (NnChannel c : channels) {
			if (c.getStatus() == NnChannel.STATUS_SUCCESS && c.isPublic()) { //!!!!
				c.setSorting(NnChannelManager.getDefaultSorting(c));
			}
            result[2] += this.composeChannelLineupStr(c, csMso) + "\n";                                                    
        }        
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}	
    
    
    private String composeChannelLineupStr(NnChannel c, Mso mso) {
		CntSubscribeManager cntMngr = new CntSubscribeManager();
		CntSubscribe s= cntMngr.findByChannel(c.getId());							
		if (s != null) {c.setSubscriptionCnt(s.getCnt());}		
		
        String[] ori = {Integer.toString(c.getSeq()), 
                        String.valueOf(c.getId()),
                        c.getName(),
                        c.getIntro(),
                        c.getPlayerPrefImageUrl(),
                        String.valueOf(c.getProgramCnt()),
                        String.valueOf(c.getType()),
                        String.valueOf(c.getStatus()),
                        String.valueOf(c.getContentType()),
                        c.getPlayerPrefSource(),
                        this.convertEpochToTime(c.getTranscodingUpdateDate(), c.getUpdateDate()),
					    String.valueOf(c.getSorting()),
					    c.getPiwik(),
					    String.valueOf(c.getRecentlyWatchedProgram()),
					    c.getOriName(),
					    String.valueOf(c.getSubscriptionCnt()),                        
                       };

        String output = NnStringUtil.getDelimitedStr(ori);
        return output;
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
            log.info("convertEpochToTime fails:" + transcodingUpdateDate + ";" + updateDate);
        }
        return output;
    }
    
    public String unsubscribe(String userToken, String channelId, String setId, String grid) {
		//verify input
		if (userToken == null || userToken.equals("undefined"))
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		if (channelId == null && setId == null)
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        //verify user
        NnUser user = new NnUserManager().findByToken(userToken);
        if (user == null)
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);        
        //unsubscribe
        NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
		if (channelId != null) {
			String[] chArr = channelId.split(",");
			if (chArr.length == 1) {
				log.info("unsubscribe single channel");
				NnUserSubscribe s = null;
				if (grid == null) {
					s = subMngr.findByUserAndChannel(user, Long.parseLong(channelId));
				} else {
					s = subMngr.findChannelSubscription(user, Long.parseLong(channelId), Integer.parseInt(grid));
				}
				subMngr.unsubscribeChannel(user, s);				
				NnUserWatchedManager watchedMngr = new NnUserWatchedManager();
				NnUserWatched watched = watchedMngr.findByUserTokenAndChannel(user.getToken(), Long.parseLong(channelId));
				if (watched != null) {
					watchedMngr.delete(user, watched);
				}								
			} else {
				log.info("unsubscribe multiple channels");
			}
		}
		if (setId != null) {
			NnUserSubscribeGroupManager groupMngr = new NnUserSubscribeGroupManager();
			NnUserSubscribeGroup group = groupMngr.findByUserAndSeq(user, Short.parseShort(setId));
			if (group != null)
				groupMngr.delete(user, group);
		}
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
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
		NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
		//verify channel and grid
		if (channelId == null)
			return this.assembleMsgs(NnStatusCode.CHANNEL_INVALID, null);
			
		long cId = Long.parseLong(channelId);			
		NnChannel channel = new NnChannelManager().findById(cId);			
		if (channel == null || channel.getStatus() == NnChannel.STATUS_ERROR)
			return this.assembleMsgs(NnStatusCode.CHANNEL_ERROR, null);
		
		short seq = Short.parseShort(gridId);
		NnUserSubscribe s = subMngr.findByUserAndSeq(user, seq);
		if (s != null)
			return this.assembleMsgs(NnStatusCode.SUBSCRIPTION_POS_OCCUPIED, null);		
		boolean status = subMngr.subscribeChannel(user, cId, seq, MsoIpg.TYPE_GENERAL);
		if (!status) {
			//the general status shows error even there's only one error
			return this.assembleMsgs(NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL, null);
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, null);
	}

    public String channelLineup(String userToken, boolean userInfo, String channelIds, boolean setInfo, boolean isRequired) {
        //verify input    	
        if ((userToken == null && userInfo == true) || 
        	(userToken == null && channelIds == null) || 
        	(userToken == null && setInfo == true)) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
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
		
        //user info
        if (userInfo)
            result.add(this.prepareUserInfo(user, null));
		NnUserSubscribeGroupManager groupMngr = new NnUserSubscribeGroupManager();
		NnUserChannelSortingManager sortingMngr = new NnUserChannelSortingManager();
		List<NnUserChannelSorting> sorts = new ArrayList<NnUserChannelSorting>();
		HashMap<Long, Short> sortMap = new HashMap<Long, Short>();
		HashMap<Long, String> watchedMap = new HashMap<Long, String>();
		if (user != null) {
			List<NnUserSubscribeGroup> groups = groupMngr.findByUser(user);	
			sorts = sortingMngr.findByUser(user);
		    //set info
			if (setInfo) {
				String setOutput = "";
				for (NnUserSubscribeGroup g : groups) {
					String[] obj = {
							String.valueOf(g.getSeq()),
							String.valueOf(g.getId()),
							g.getName(),						
							g.getImageUrl(),
							String.valueOf(g.getType()),
					};
					setOutput += NnStringUtil.getDelimitedStr(obj) + "\n";
				}
				result.add(setOutput);
			}
			for (NnUserChannelSorting s : sorts) {
				sortMap.put(s.getChannelId(), s.getSort());
			}
			NnUserWatchedManager watchedMngr = new NnUserWatchedManager();
			List<NnUserWatched> watched = watchedMngr.findByUserToken(user.getToken());
			for (NnUserWatched w : watched) {
				watchedMap.put(w.getChannelId(), w.getProgram());
			}			
		}
		
		//find channels
		List<NnChannel> channels = new ArrayList<NnChannel>();
		boolean channelPos = true;
		if (channelIds == null) {
			//find subscribed channels 
			NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
			channels = subMngr.findSubscribedChannels(user);
			log.info("user: " + user.getToken() + " find subscribed size:" + channels.size());
		} else {
			//find specific channels
			NnChannelManager channelMngr = new NnChannelManager();
			channelPos = false;
			String[] chArr = channelIds.split(",");
			if (chArr.length > 1) {
				List<Long> list = new ArrayList<Long>();
				for (int i=0; i<chArr.length; i++) { list.add(Long.valueOf(chArr[i]));}
				channels = channelMngr.findAllByChannelIds(list);
			} else {
				NnChannel channel = channelMngr.findById(Long.parseLong(channelIds));
				if (channel != null) channels.add(channel);					
			}
		}
		if (isRequired && channels.size() == 0)
			return this.assembleMsgs(NnStatusCode.CHANNEL_INVALID, null);
		//sort by seq
		if (channelPos) {
			TreeMap<Integer, NnChannel> channelMap = new TreeMap<Integer, NnChannel>();
			for (NnChannel c : channels) {
				channelMap.put(c.getSeq(), c);				
			}
			Iterator<Entry<Integer, NnChannel>> it = channelMap.entrySet().iterator();
	    	channels.clear();
		    while (it.hasNext()) {
		        Map.Entry<Integer, NnChannel> pairs = (Map.Entry<Integer, NnChannel>)it.next();
		    	channels.add((NnChannel)pairs.getValue());
		    }
		}
		String channelOutput = "";
		for (NnChannel c : channels) {
			if (user != null && sortMap.containsKey(c.getId()))
		        c.setSorting(sortMap.get(c.getId()));
		    else 
		    	c.setSorting(NnChannelManager.getDefaultSorting(c));
			if (user != null && watchedMap.containsKey(c.getId())) {
				c.setRecentlyWatchedProgram(watchedMap.get(c.getId()));
			}
			channelOutput += this.composeChannelLineupStr(c, mso) + "\n";
		}		
		result.add(channelOutput);
		String size[] = new String[result.size()];
		return this.assembleMsgs(NnStatusCode.SUCCESS, result.toArray(size));
    }
        
	public String channelSubmit(String categoryIds, String userToken, 
			                    String url, String grid, 
                                String tags, String lang, 
                                HttpServletRequest req) {
		//verify input
		if (url == null || url.length() == 0 ||  grid == null || grid.length() == 0 ||
     		userToken== null || userToken.length() == 0) {
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		}
        if (Integer.parseInt(grid) < 0 || Integer.parseInt(grid) > 81) {            
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        }
		url = url.trim();	
		//verify user
		NnUser user = userMngr.findByToken(userToken);
		if (user == null) 
			return this.assembleMsgs(NnStatusCode.USER_INVALID, null);		
        if (user.getEmail().equals(NnUser.GUEST_EMAIL))
            return this.assembleMsgs(NnStatusCode.USER_PERMISSION_ERROR, null);
				
		NnChannelManager channelMngr = new NnChannelManager();		
		//verify url, also converge youtube url
		url = channelMngr.verifyUrl(url); 		
        if (url == null)
            return this.assembleMsgs(NnStatusCode.CHANNEL_URL_INVALID, null);            
		
		//verify channel status for existing channel
		NnChannel channel = channelMngr.findBySourceUrl(url);										
		if (channel != null && (channel.getStatus() == NnChannel.STATUS_ERROR)) {
            log.info("channel id and status :" + channel.getId()+ ";" + channel.getStatus());
            this.assembleMsgs(NnStatusCode.CHANNEL_STATUS_ERROR, null);
		}
		
		//create a new channel
		if (channel == null) {
			channel = channelMngr.create(url, req);
			if (channel == null) {
				return this.assembleMsgs(NnStatusCode.CHANNEL_URL_INVALID, null);
			}			
			channel.setTag(tags);
			log.info("User throws a new url:" + url);
		}
		
		//subscribe
		NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
		boolean success = subMngr.subscribeChannel(user, channel.getId(), Integer.parseInt(grid), MsoIpg.TYPE_GENERAL);
		String result[] = {""};
		if (!success) {
			return this.assembleMsgs(NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL, null);
		} else {
			String channelName = "";
			//!!!!! make it function
			if (channel.getSourceUrl() != null && channel.getSourceUrl().contains("http://www.youtube.com"))
				channelName = YouTubeLib.getYouTubeChannelName(channel.getSourceUrl());
			if (channel.getContentType() == NnChannel.CONTENTTYPE_FACEBOOK) 
				channelName = channel.getSourceUrl();			
			String output[]= {String.valueOf(channel.getId()),				  	 	  
				              channel.getName(),
				              channel.getImageUrl(),
				              String.valueOf(channel.getContentType()),
			                  channelName};
			result[0] = NnStringUtil.getDelimitedStr(output);
		}		
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
    
    
    public String login(String email, String password, HttpServletRequest req, HttpServletResponse resp) {        
        log.info("login: email=" + email + "; mso=" + mso.getId() + ";password=" + password);
        if (!BasicValidator.validateRequired(new String[] {email, password}))
    	    return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);;    	

    	String result[] = {""};
        NnUser user = userMngr.findAuthenticatedUser(email, password, req);
    	if (user != null) {
    		result[0] = this.prepareUserInfo(user, null);
    		userMngr.save(user); //change last login time (ie updateTime)
    		this.setUserCookie(resp, CookieHelper.USER, user.getToken());
    	} else {
    		return this.assembleMsgs(NnStatusCode.USER_LOGIN_FAILED, null);
    	}
    	return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
       
	public String programInfo(String channelIds, String userToken, 
                                  String ipgId, boolean userInfo,
                                  String sidx, String limit) {
		if (channelIds == null || (channelIds.equals("*") && userToken == null && ipgId == null)) {		   
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		}
		NnProgramManager programMngr = new NnProgramManager();		
		String[] chArr = channelIds.split(",");
		NnUser user = null;
		long sidxL = 0;
		long limitL = 0;
		if (sidx != null) { sidxL = Long.parseLong(sidx); } 
		if (limit != null) {limitL = Long.parseLong(limit);}
		if ((sidx != null && limit == null) || (sidx == null && limit != null))
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
	
		String programInfoStr = "";
		if (channelIds.equals("*")) {
			user = userMngr.findByToken(userToken);
			if (user == null) {
				NnGuest guest = new NnGuestManager().findByToken(userToken);
				if (guest == null)
					return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
				else
					return this.assembleMsgs(NnStatusCode.SUCCESS, null);
			}
		} else if (chArr.length > 1) {			
			List<Long> list = new ArrayList<Long>();
			for (int i=0; i<chArr.length; i++) { list.add(Long.valueOf(chArr[i]));}
			for (Long l : list) {
				programInfoStr = programMngr.findPlayerProgramInfoByChannel(l, sidxL, limitL);				
			}
		} else {
			programInfoStr = programMngr.findPlayerProgramInfoByChannel(Long.parseLong(channelIds), sidxL, limitL);				
		}		
		
        MsoConfig config = new MsoConfigManager().findByMsoAndItem(mso, MsoConfig.CDN);
        if (config == null) {
            config = new MsoConfig(mso.getId(), MsoConfig.CDN, MsoConfig.CDN_AMAZON);
            log.severe("mso config does not exist! mso: " + mso.getId());
        }
        String userInfoStr = "";
        if (userInfo) {
            if (user == null && userToken != null) 
                user = userMngr.findByToken(userToken);
                userInfoStr = this.prepareUserInfo(user, null);
        }
        if (userInfo) {
            String[] result = {userInfoStr, programInfoStr};
            return this.assembleMsgs(NnStatusCode.SUCCESS, result);
        } else {
            String[] result = {programInfoStr};
            return this.assembleMsgs(NnStatusCode.SUCCESS, result);            
        }
	}    

    public String saveIpg(String userToken, String channelId, String programId) {
    	//obsolete
        return this.assembleMsgs(NnStatusCode.ERROR, null);                 
    }    

    public String loadIpg(long ipgId) {
    	//obsolete
        return this.assembleMsgs(NnStatusCode.ERROR, null);                 
    }            

    public String moveChannel(String userToken, String grid1, String grid2) {        
        //verify input
        if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") || grid1 == null || grid2 == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (!Pattern.matches("^\\d*$", grid1) || !Pattern.matches("^\\d*$", grid2) ||
            Integer.parseInt(grid1) < 0 || Integer.parseInt(grid1) > 81 ||
            Integer.parseInt(grid2) < 0 || Integer.parseInt(grid2) > 81) {
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        }        
        NnUser user = userMngr.findByToken(userToken);
        if (user == null) 
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        
        NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
        boolean success = subMngr.moveSeq(user, Integer.parseInt(grid1), Integer.parseInt(grid2));

        if (!success) { return this.assembleMsgs(NnStatusCode.SUBSCRIPTION_ERROR, null); }
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    public String setSetInfo(String userToken, String name, String pos) {
        //verify input
        if (name == null || pos == null)  {            
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (!Pattern.matches("^\\d*$", pos) || Integer.parseInt(pos) < 0 || Integer.parseInt(pos) > 9) {            
            return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
        }

        NnUser user = new NnUserManager().findByToken(userToken);
        if (user == null)
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);    
        
        NnUserSubscribeGroupManager subSetMngr = new NnUserSubscribeGroupManager();
        short position = Short.valueOf(pos);
        NnUserSubscribeGroup subSet = subSetMngr.findByUserAndSeq(user, Short.valueOf(position));
        if (subSet!= null) {
            subSet.setName(name);
            subSetMngr.save(user, subSet);
        } else {
            subSet = new NnUserSubscribeGroup();
            subSet.setUserId(user.getId());
            subSet.setName(name);                
            subSet.setSeq(position);
            subSetMngr.create(user, subSet);            
        }
        
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }
    
    private String checkLang(String lang) {
        if (lang == null || lang.length() == 0)
            return LangTable.LANG_EN;
        if (lang != null && !lang.equals(LangTable.LANG_EN) && !lang.equals(LangTable.LANG_ZH))
            return null;
        return lang;
    }
    
	public String staticContent(String key, String lang) {
		NnContentManager contentMngr = new NnContentManager();
		NnContent content = contentMngr.findByItemAndLang(key, lang);		
		if (content == null)
			return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        lang = this.checkLang(lang);
        if (lang == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);		
		String[] result = {content.getValue()};
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
	
	public String deviceRegister(String userToken, String type, HttpServletRequest req, HttpServletResponse resp) {
		NnUser user = null;
		if (userToken != null) {
			@SuppressWarnings({ "rawtypes"})
			HashMap map = this.checkUser(userToken, false);
			if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
				return this.assembleMsgs((Integer)map.get("s"), null);
			}
			user = (NnUser) map.get("u");
		}
		NnDeviceManager deviceMngr = new NnDeviceManager();
		deviceMngr.setReq(req); //!!!
		NnDevice device = deviceMngr.create(null, user, type);		
		String[] result = {device.getToken()};
		this.setUserCookie(resp, CookieHelper.DEVICE, device.getToken());
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
	  
	public String deviceTokenVerify(String token, HttpServletRequest req) {
		if (token == null)
			return this.assembleMsgs(NnStatusCode.SUCCESS, null);
		NnDeviceManager deviceMngr = new NnDeviceManager();
		deviceMngr.setReq(req); //!!!
		List<NnDevice> devices = deviceMngr.findByToken(token);
		if (devices.size() == 0)
			return this.assembleMsgs(NnStatusCode.DEVICE_INVALID, null);
		List<NnUser> users = new ArrayList<NnUser>(); 
		log.info("<<<<<<<<<<< device size>>>>>>>" + devices.size());
		for (NnDevice d : devices) {
			if (d.getUserId() != 0) {
				NnUser user = userMngr.findById(d.getUserId());
				if (user != null)
					users.add(user);
				else
					log.info("bad data in device:" + d.getToken() + ";userId:" + d.getUserId());
			}	
		}		
		String[] result = {""};
		for (NnUser u : users) {
			result[0] += u.getToken() + "\t" + u.getName() + "\t" + u.getEmail() + "\n";
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}

	public String deviceAddUser(String deviceToken, String userToken, HttpServletRequest req) {
		NnUser user = null;
		if (userToken != null) {
			@SuppressWarnings("rawtypes")
			HashMap map = this.checkUser(userToken, false);
			if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
				return this.assembleMsgs((Integer)map.get("s"), null);
			}
			user = (NnUser) map.get("u");
		}
		if (deviceToken == null)
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		NnDeviceManager deviceMngr = new NnDeviceManager();
		NnDevice device = deviceMngr.addUser(deviceToken, user);
		if (device == null)
			return this.assembleMsgs(NnStatusCode.DEVICE_INVALID, null);
		return this.assembleMsgs(NnStatusCode.SUCCESS, null);
	}

	public String deviceRemoveUser(String deviceToken, String userToken, HttpServletRequest req) {
		NnUser user = null;
		if (userToken != null) {
			@SuppressWarnings("rawtypes")
			HashMap map = this.checkUser(userToken, false);
			if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
				return this.assembleMsgs((Integer)map.get("s"), null);
			}
			user = (NnUser) map.get("u");
		}
		if (deviceToken == null)
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		NnDeviceManager deviceMngr = new NnDeviceManager();
		boolean success = deviceMngr.removeUser(deviceToken, user);
		if (!success) 
			return this.assembleMsgs(NnStatusCode.DEVICE_INVALID, null);
		return this.assembleMsgs(NnStatusCode.SUCCESS, null);
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
	
	public String listRecommended(String lang) {
		lang = this.checkLang(lang);	
        if (lang == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);				
		NnSetManager setMngr = new NnSetManager();
		List<NnSet> sets = setMngr.findFeaturedSets(lang);
		String[] result = {""};
		for (NnSet set : sets) {
			String[] obj = {
				String.valueOf(set.getId()),
				set.getName(),
				set.getIntro(),
				set.getImageUrl(),
				String.valueOf(set.getChannelCnt()),
			};
			result[0] += NnStringUtil.getDelimitedStr(obj) + "\n";			
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);		
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
			guestMngr.save(guest, null);
			return NnStatusCode.CAPTCHA_FAILED;
		}
		Date now = new Date();
		if (now.after(guest.getExpiredAt()))
			return NnStatusCode.CAPTCHA_FAILED;
		return NnStatusCode.SUCCESS;
	}
	 
	public String recentlyWatched(String userToken, String count, boolean channelInfo, boolean episodeIndex) {
		@SuppressWarnings("rawtypes")
		HashMap map = this.checkUser(userToken, false);
		if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
			return this.assembleMsgs((Integer)map.get("s"), null);
		}
		if (count == null) 
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);		
		int cnt = Integer.parseInt(count);
		if (episodeIndex) {
			if (cnt > 5) {
				return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
			}
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, null);
	}

	public String userReport(String userToken, String deviceToken, String session, String comment) {
		if (session == null)
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		if (userToken == null && deviceToken == null)
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		if (comment.length() > 500)
			return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
		
		NnUser user = null;
		if (userToken != null) { 
			//verify input
			@SuppressWarnings("rawtypes")
			HashMap map = this.checkUser(userToken, false);
			user = (NnUser) map.get("u");
		}
		List<NnDevice> devices = new ArrayList<NnDevice>();
		NnDevice device = null;
		if (deviceToken != null) {
			NnDeviceManager deviceMngr = new NnDeviceManager();
			devices = deviceMngr.findByToken(deviceToken);
			if (devices.size() > 0)
				device = devices.get(0);
		}
		if (device == null && user == null)
			return this.assembleMsgs(NnStatusCode.ACCOUNT_INVALID, null);
		
		NnUserReportManager reportMngr = new NnUserReportManager();
		String[] result = {""};
		NnUserReport report = reportMngr.save(user, device, session, comment);
		if (report != null)
			result[0] = PlayerApiService.assembleKeyValue("id", String.valueOf(report.getId()));
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}

	public String setUserProfile(String userToken, String items, String values, HttpServletRequest req) {
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
		
		String[] valid = {"name", "year", "password", 
                "oldPassword", "sphere", "ui-lang", "gender"};		
		HashSet<String> dic = new HashSet<String>();
		for (int i=0; i<valid.length; i++) {
			dic.add(valid[i]);
		}				
		for (int i=0; i<key.length; i++) {
			if (!dic.contains(key[i]))
				return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
			if (key[i].equals("name")) {
				if (value[i].equals(NnUser.GUEST_NAME))
					return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
				user.setName(value[i]);
			}
			if (key[i].equals("year"))
				user.setDob(value[i]);
			if (key[i].equals("password"))
				password = value[i];				
			if (key[i].equals("oldPassword"))
				oldPassword = value[i];				
			if (key[i].equals("sphere")) {
				if ((value[i] == null) || (this.checkLang(value[i]) == null))
					return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
				user.setSphere(value[i]);
			}
			if (key[i].equals("gender"))
				user.setGender(Short.parseShort(value[i]));						
			if (key[i].equals("ui-lang")) {
				if ((value[i] == null) || (this.checkLang(value[i]) == null))
					return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
				user.setLang(value[i]);
			}
		}
		int status = NnUserValidator.validateProfile(user);
		if (status != NnStatusCode.SUCCESS) {
			log.info("profile fail");
			return this.assembleMsgs(status, null);
		}
		if (password.length() > 0 && oldPassword.length() > 0) {
			NnUser authenticated = userMngr.findAuthenticatedUser(user.getEmail(), oldPassword, req);
			if (authenticated == null)
				return this.assembleMsgs(NnStatusCode.USER_LOGIN_FAILED, null);
			status = NnUserValidator.validatePassword(password);
			if (status != NnStatusCode.SUCCESS)
				return this.assembleMsgs(status, null);
			user.setPassword(password);
			user.setSalt(AuthLib.generateSalt());
			user.setCryptedPassword(AuthLib.encryptPassword(user.getPassword(), user.getSalt()));			
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

	public String setUserPref(String token, String item, String value) {
		//verify input
		if (token == null || token.length() == 0 || token.equals("undefined") ||
			item == null || value == null || item.length() == 0 || value.length() == 0) {
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		}		
		//verify user
		NnUser user = userMngr.findByToken(token);
		if (user == null) 
			return this.assembleMsgs(NnStatusCode.USER_INVALID, null);		
		//get preference
		NnUserPrefManager prefMngr = new NnUserPrefManager();
		NnUserPref pref = prefMngr.findByUserAndItem(user, item);
		if (pref != null) {
			pref.setValue(value);			
			prefMngr.save(user, pref);
		} else {
			pref = new NnUserPref();
			pref.setValue(value);
			pref.setItem(item);			
			pref.setUserId(user.getId());
			prefMngr.save(user, pref);
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, null);
	}
	
	public String requestCaptcha(String token, String action, HttpServletRequest req) {
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
		guest.setCaptchaId(c.getId());
		guest.setExpiredAt(cal.getTime());
		guest.setGuessTimes(0);
		guestMngr.save(guest, req);		
		return this.assembleMsgs(NnStatusCode.SUCCESS, new String[] {c.getFileName()});
	}	

	public String saveSorting(String token, String channelId, String sort) {
		@SuppressWarnings("rawtypes")
		HashMap map = this.checkUser(token, false);
		if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
			return this.assembleMsgs((Integer)map.get("s"), null);
		}
		NnUser user = (NnUser) map.get("u");
		NnUserChannelSorting sorting = new NnUserChannelSorting(user.getId(), 
				                           Long.parseLong(channelId), Short.parseShort(sort));
		NnUserChannelSortingManager sortingMngr = new NnUserChannelSortingManager();
		sortingMngr.save(user, sorting);		
		return this.assembleMsgs(NnStatusCode.SUCCESS, null);
	}

	public String saveShare(String userToken, String channelId, String programId, String setId) {
		if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") ||
			channelId == null || programId == null || channelId.length() == 0 || programId.length() == 0) {
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		}
		if (!Pattern.matches("^\\d*$", channelId))
			return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
		
		NnUser user = userMngr.findByToken(userToken);				
		if (user == null) 
			return this.assembleMsgs(NnStatusCode.USER_INVALID, null);

		NnUserShare share = new NnUserShare();
		share.setChannelId(Long.parseLong(channelId));
		if (Pattern.matches("^\\d*$", programId)) {
			share.setProgramId(Long.parseLong(programId));
		} else {
			share.setProgramIdStr(programId);
		}
		share.setUserId(user.getId());
		NnUserShareManager shareMngr = new NnUserShareManager();
		shareMngr.create(share);
		String result[] = {String.valueOf(share.getId())};
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);				
	}

	public String loadShare(long id) {
		NnUserShareManager shareMngr = new NnUserShareManager();
		NnUserShare share= shareMngr.findById(id);
		if (share== null)
			return this.assembleMsgs(NnStatusCode.IPG_INVALID, null);
		
		String[] result = {"", ""};
		NnProgramManager programMngr = new NnProgramManager();
		NnProgram program = programMngr.findById(share.getProgramId());
		if (program != null) {
			List<NnProgram> programs = new ArrayList<NnProgram>();
			programs.add(program);
			result[0] = programMngr.composeProgramInfoStr(programs);
		} else {			
			result[0] = share.getChannelId() + "\t" + share.getProgramIdStr() + "\n";			
		}		
		NnChannel channel = new NnChannelManager().findById(share.getChannelId());		
		if (channel != null) {
			result[1] = this.composeChannelLineupStr(channel, mso);
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
	
	public String userWatched(String userToken, String count, boolean channelInfo, boolean episodeIndex, String channel) {
		@SuppressWarnings("rawtypes")
		HashMap map = this.checkUser(userToken, false);
		if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
			return this.assembleMsgs((Integer)map.get("s"), null);
		}
		if (count == null) 
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);		
		int cnt = Integer.parseInt(count);
		if (episodeIndex) {
			if (cnt > 5) {
				return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
			}
		}
		String[] result = {"", ""};
		NnUserWatchedManager watchedMngr = new NnUserWatchedManager();
		NnChannelManager channelMngr = new NnChannelManager();
		NnProgramManager programMngr = new NnProgramManager();
		List<NnUserWatched> watched = new ArrayList<NnUserWatched>();
		if (channel == null) {
			watched = watchedMngr.findByUserToken(userToken);
		} else {
			NnUserWatched w = watchedMngr.findByUserTokenAndChannel(userToken, Long.parseLong(channel));
			if (w != null) { watched.add(w); }				
		}
		List<NnChannel> channels = new ArrayList<NnChannel>();
		int i = 1;
		for (NnUserWatched w : watched) {
			if (i > cnt)
				break;
			int index = 0;			
			if (episodeIndex && Pattern.matches("^\\d*$", w.getProgram())) {
				String programInfo = programMngr.findPlayerProgramInfoByChannel(w.getChannelId());
				if (programInfo != null && programInfo.length() > 0) {
					index = programMngr.getEpisodeIndex(programInfo, w.getProgram());
				}
			}
			
			result[0] += w.getChannelId() + "\t" + w.getProgram() + "\t" + index + "\n";
			NnChannel c = channelMngr.findById(w.getChannelId());
			if (c != null) { 
				channels.add(c);
			}
			i++;
		}
		if (channelInfo) {
			for (NnChannel c : channels) {
				result[1] += this.composeChannelLineupStr(c, mso) + "\n";
			}
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}

	public String copyChannel(String userToken, String channelId, String grid) {		
		//verify input
		if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") || grid == null) {
			return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
		}
		if (!Pattern.matches("^\\d*$", grid) ||
			Integer.parseInt(grid) < 0 || Integer.parseInt(grid) > 81)
			return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
		NnUser user = userMngr.findByToken(userToken);
		if (user == null)
			return this.assembleMsgs(NnStatusCode.USER_INVALID, null); 
		
		NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
		boolean success = false;
		success = subMngr.copyChannel(user, Long.parseLong(channelId), Short.parseShort(grid));
		if (!success) 
			return this.assembleMsgs(NnStatusCode.SUBSCRIPTION_ERROR, null);
		else
			return this.assembleMsgs(NnStatusCode.SUCCESS, null);
	}

	public String search(String text) {
		if (text == null || text.length() == 0)
			return this.assembleMsgs(NnStatusCode.SUCCESS, null);
		List<NnChannel> searchResults = NnChannelManager.searchChannelEntries(text);
		String[] result = {""};
		for (NnChannel c : searchResults) {
			result[0] += this.composeChannelLineupStr(c, mso) + "\n";
		}
		return this.assembleMsgs(NnStatusCode.SUCCESS, result);
	}
	
}