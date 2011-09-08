package com.nnvmso.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreNeedIndexException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Category;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoConfig;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.web.json.transcodingservice.MapelChannel;
import com.nnvmso.web.json.transcodingservice.PostResponse;
import com.nnvmso.web.json.transcodingservice.PostUrl;
import com.nnvmso.web.json.transcodingservice.RtnChannel;
import com.nnvmso.web.json.transcodingservice.RtnProgram;
import com.nnvmso.web.json.transcodingservice.RtnProgramItem;

@Service
public class TranscodingService {
	protected static final Logger log = Logger.getLogger(TranscodingService.class.getName());	
	
	public PostResponse handleException (Exception e) {
		PostResponse resp = new PostResponse(String.valueOf(NnStatusCode.ERROR), "Error");		
		if (e.getClass().equals(DatastoreTimeoutException.class)) {
			resp.setErrorCode(String.valueOf(NnStatusCode.DATABASE_TIMEOUT));
			resp.setErrorReason("Database timeout");
		} else if (e.getClass().equals(NoSuchMessageException.class)) {			
			resp.setErrorCode(String.valueOf(NnStatusCode.OUTPUT_NO_MSG_DEFINED));
			resp.setErrorReason("oops, system does not define this error msg");						
		} else if (e.getClass().equals(DatastoreFailureException.class)) {
			resp.setErrorCode(String.valueOf(NnStatusCode.DATABASE_ERROR));
			resp.setErrorReason("Database error");
		} else if (e.getClass().equals(DatastoreNeedIndexException.class)) {
			resp.setErrorCode(String.valueOf(NnStatusCode.DATABASE_NEED_INDEX));
			resp.setErrorReason("index is still building, fatal error");						
		}
		NnLogUtil.logException((Exception) e);
		return resp;
	}	
	
	private short convertStatus(short tranStatus) {
		switch (tranStatus) {
		  case 0: 
			  return MsoChannel.STATUS_SUCCESS;
		  case 1:
			  return MsoChannel.STATUS_NNVMSO_JSON_ERROR;
		  case 2:
			  return MsoChannel.STATUS_INVALID_FORMAT;
		  case 4:
			  return MsoChannel.STATUS_TRANSCODING_DB_ERROR;
		  case 5:
			  return MsoChannel.STATUS_NO_VALID_EPISODE;
		  case 6: 
			  return MsoChannel.STATUS_URL_NOT_FOUND;
		  case 7:
			  return MsoChannel.STATUS_INVALID_FORMAT;
		}
		return MsoChannel.STATUS_SUCCESS;				
	}

	public MapelChannel createChannel(MapelChannel mapel) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		CategoryManager categoryMngr = new CategoryManager();
		List<Category> categories = new ArrayList<Category>();
		Category c = categoryMngr.findByName("Entertainment");
		categories.add(c);
		if (c == null) {
			mapel.setErrorCode(String.valueOf(NnStatusCode.CATEGORY_ERROR));
			mapel.setErrorReason("CATEGORY_ERROR");
			return mapel;
		}
		MsoChannel channel = channelMngr.findBySourceUrlSearch(mapel.getSourceUrl());										
		if (channel != null && (channel.getStatus() == MsoChannel.STATUS_ERROR)) {
			log.info("channel key and status :" + channel.getKey()+ ";" + channel.getStatus());
			mapel.setErrorCode(String.valueOf(NnStatusCode.CHANNEL_ERROR));
			mapel.setErrorReason("CHANNEL_ERROR");		
		} else {
			NnUserManager userMngr = new NnUserManager();
			NnUser user = userMngr.findNNUser();			
			channel = new MsoChannel(mapel.getSourceUrl(), user.getKey().getId());
		}
		channel.setImageUrl(mapel.getImage());	
		if (mapel.getTitle() != null) 
			channel.setNameSearch(mapel.getTitle().toLowerCase());
		channel.setName(mapel.getTitle());					
		if (mapel.getDescription() != null) { 
			mapel.setDescription(mapel.getDescription().replaceAll("\\s", " "));
			if (mapel.getDescription().length() > 500)
				mapel.setDescription(mapel.getDescription().substring(0, 499));
			channel.setIntro(mapel.getDescription());
		}
		channel.setStatus(MsoChannel.STATUS_SUCCESS);		
		channel.setLangCode(Mso.LANG_ZH_TW);		
		channelMngr.create(channel, categories);
		mapel.setKey(String.valueOf(channel.getKey().getId()));
		mapel.setErrorCode(String.valueOf(NnStatusCode.SUCCESS));
		mapel.setErrorReason("SUCCESS");
		return mapel;
	}
	
	public PostResponse updateChannel(RtnChannel podcast) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findById(Long.parseLong(podcast.getKey()));
		if (channel == null) {
			return new PostResponse(String.valueOf(NnStatusCode.CHANNEL_INVALID), "CHANNEL_INVALID");
		}
		if (!podcast.getErrorCode().equals(String.valueOf(MsoChannel.STATUS_SUCCESS))) {
			channel.setPublic(false);
			channel.setStatus(this.convertStatus(Short.valueOf(podcast.getErrorCode())));
			channel.setErrorReason(podcast.getErrorReason());
			channelMngr.save(channel);
			return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS"); 
		}
		//change status to WAIT_FOR_APPROVAL at the end
		if (channel.getStatus() == MsoChannel.STATUS_PROCESSING) { 
			String name = podcast.getTitle();
			channel.setName(name);
			if (name != null) { channel.setNameSearch(name.toLowerCase()); };			
			String intro = podcast.getDescription();
			if (intro!= null && intro.length() > 500) {
				intro = intro.substring(0, 499);
			}
			if (intro != null) { intro = intro.replaceAll("\\s", " ");}
			channel.setIntro(intro);
			channel.setImageUrl(podcast.getImage());
			channel.setStatus(MsoChannel.STATUS_WAIT_FOR_APPROVAL);
		}
		
		if (podcast.getLastUpdateTime() != null) {
			channel.setTranscodingUpdateDate(podcast.getLastUpdateTime());
		}
		channel.setPublic(true);
		channel.setErrorReason("");
		channelMngr.save(channel);		
		return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS");
	}

	public String[] getPodcastInfo(String urlStr) {
        URL url;
        String podcastInfo[] = new String[3];
        boolean retry = true; //the "good" podcastUrl oftentimes comes after a redirect, giving it twice chance. 
        int counter = 1;
        while (retry) {
			try {
				//HTTP GET
				url = new URL(urlStr);
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setDoOutput(true);
		        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
		        	log.info("podcast GET response not ok!" + connection.getResponseCode());	        	
		        }
		        String returnUrl = connection.getURL().toString();
		        if (returnUrl.equals(urlStr)) {
		        	retry = false;
		        } else {
		        	counter++;
		        	retry = counter > 2 ? false : true;
		        }
		        podcastInfo[0] = Integer.toString(connection.getResponseCode());
		        podcastInfo[1] = connection.getHeaderField("content-type");
		        podcastInfo[2] = connection.getURL().toString();
			} catch (Exception e) {
				retry = false;
				podcastInfo[0] = "400";
			}
        }
		return podcastInfo;
	}
					
	public PostResponse updateProgram(RtnProgram rtnProgram) {
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoChannelManager channelMngr = new MsoChannelManager();			
		RtnProgramItem item = rtnProgram.getItems()[0]; //for now there's only one item
		log.info("updateProgramViaTranscodingService(): " + item.toString());
		if (!Pattern.matches("^\\d*$", rtnProgram.getKey())) {
			return new PostResponse(String.valueOf(NnStatusCode.CHANNEL_INVALID), "channel invalid");
		}
		MsoChannel channel = channelMngr.findById(Long.parseLong(rtnProgram.getKey()));		
		if (channel == null) {
			return new PostResponse(String.valueOf(NnStatusCode.CHANNEL_INVALID), "channel invalid");
		}
		
		if (channel.getProgramCount() >= MsoChannelManager.MAX_CHANNEL_SIZE) {
			MsoProgram oldest = programMngr.findOldestByChannelId(channel.getKey().getId());
			programMngr.delete(oldest); 			
		}
		
		if (channel.isPublic() != true && channel.getStatus() == MsoChannel.STATUS_SUCCESS) {
			channel.setPublic(true);
			channelMngr.save(channel);
		}
		
		MsoProgram program = programMngr.findByStorageId(item.getItemId());
		boolean isNew = false;
		if (program == null) {
			isNew = true;
			program = new MsoProgram("", "", "", MsoProgram.TYPE_VIDEO);
		}
		if (isNew) {
			if (item.getTitle() != null) { program.setName(item.getTitle()); }					

			String intro = item.getDescription();
			if (intro != null && intro.length() > 500) {
				intro = item.getDescription().substring(0, 499);
			}
			if (intro != null) {intro.replaceAll("\\s", " ");}
			program.setIntro(intro);
			
			if (item.getThumbnail()!= null) {
				program.setImageUrl(item.getThumbnail());
			} else {
				program.setImageUrl(channel.getImageUrl());
			}		
			
			if (item.getThumbnail()!= null) {
				program.setImageLargeUrl(item.getThumbnailLarge());
			} else {
				program.setImageUrl(channel.getImageUrl());
			}		
		}
		program.setStorageId(item.getItemId());	
		
		if (item.getMp4() != null) {			
			program.setMpeg4FileUrl(item.getMp4());
		}		
		if (item.getWebm() != null) {
			program.setWebMFileUrl(item.getWebm());
		}
		if (item.getOther() != null) {
			program.setOtherFileUrl(item.getOther());
		}
		if (item.getAudio() != null) {
			program.setAudioFileUrl(item.getAudio());			
		}
		if (item.getDuration() != null) {
			program.setDuration(item.getDuration());
		}
		if (item.getAudio() != null) {
			program.setType(MsoProgram.TYPE_AUDIO);
		} else {
			program.setType(MsoProgram.TYPE_VIDEO);
		}
		if (item.getPubDate() != null) {
			Date theDate = new Date(Long.parseLong(item.getPubDate())*1000);
			program.setPubDate(theDate);
		} else {
			program.setPubDate(new Date());
		}
		program.setPublic(true);
		if (isNew) {
			programMngr.create(channel, program);
		} else {
			programMngr.save(program);
		}
		return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS");
	}

	public void submitToTranscodingService(long channelId, String sourceUrl, HttpServletRequest req) {
		PostUrl postUrl = new PostUrl();
		postUrl.setKey(String.valueOf(channelId));
		postUrl.setRss(sourceUrl);	
		String[] transcodingEnv = this.getTranscodingEnv(req);
		String transcodingServer = transcodingEnv[0] + "podcatcher.php";
		String callbackUrl = transcodingEnv[1];
		String devel = transcodingEnv[2];		
		postUrl.setCallback(callbackUrl);
		if (!devel.equals("1")) {
			NnNetUtil.urlPostWithJson(transcodingServer, postUrl);			
		}
	}
	
	public Properties getTranscodingServerPro() {
		Properties pro = new Properties();
		try {
			pro.load(MsoChannelManager.class.getClassLoader().getResourceAsStream("transcoding.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pro;
	}
	
	public void updateFbToken(String fbToken) {
		MsoConfigManager configMngr = new MsoConfigManager();		
		MsoConfig config = configMngr.findByItem(MsoConfig.FBTOKEN);
		if (config == null) { config = new MsoConfig();	}
		config.setItem(MsoConfig.FBTOKEN);
		config.setValue(fbToken);
		configMngr.save(config);
		
	}
	
	public String[] getTranscodingEnv(HttpServletRequest req) {
		//get environment
		Properties pro = getTranscodingServerPro();
		String url = NnNetUtil.getUrlRoot(req);
		String env = "office";
		String callback_env = "_callback";
		if (url.contains("9x9tvalpha") || url.contains("alpha.9x9.tv") || url.contains("alpha.5f.tv")) {
			env = "alpha";
		} else if (url.contains("9x9tvbeta") || url.contains("beta.9x9.tv") || url.contains("beta.5f.tv")){
			env = "beta";
		} else if (url.contains("9x9tvdev") || url.contains("dev.9x9.tv") || url.contains("dev.5f.tv")){
			env = "dev";
		} else if (url.contains("9x9tvqa") || url.contains("qa.9x9.tv") || url.contains("qa.5f.tv")) {
			env = "qa";
		} else if (url.contains("9x9tvprod") || url.contains("prod.9x9.tv") || url.contains("prod.5f.tv") ||
				   url.contains("9x9.tv") || url.contains("www.9x9.tv") || 
				   url.contains("5f.tv") || url.contains("www.5f.tv")) {
			env = "prod";
		}
		callback_env = env + callback_env;
		
		//set transcoding server and callback urls
		String transcodingServer = pro.getProperty(env);		
		String callbackServer = url;
		if (pro.getProperty(callback_env).length() > 0) {
			callbackServer = pro.getProperty(callback_env);
		}
		String devel = pro.getProperty("devel");
		return new String[]{transcodingServer, callbackServer, devel};
	}
	
}
