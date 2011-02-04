package com.nnvmso.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreNeedIndexException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.nnvmso.web.json.transcodingservice.*;
import com.nnvmso.model.*;
import com.nnvmso.lib.*;

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
	
	public PostResponse updateChannel(RtnChannel podcast) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel = channelMngr.findById(Long.parseLong(podcast.getKey()));
		if (channel == null) {
			return new PostResponse(String.valueOf(NnStatusCode.CHANNEL_INVALID), "CHANNEL_INVALID");
		}
		if (!podcast.getErrorCode().equals(String.valueOf(MsoChannel.STATUS_SUCCESS))) {
			channel.setPublic(false);
			channel.setStatus(MsoChannel.STATUS_ERROR);
			channel.setErrorReason(podcast.getErrorReason());
			channelMngr.save(channel);
			return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS"); 
		}
		String name = podcast.getTitle(); 
		if (name != null) { name = NnStringUtil.capitalize(name);}
		channel.setName(name);
		
		String intro = podcast.getDescription();
		if (intro!= null && intro.length() > 500) {
			intro = intro.substring(0, 499);
		}
		if (intro != null) { intro = intro.replaceAll("\\s", " ");}
		channel.setIntro(intro);
		
		channel.setImageUrl(podcast.getImage());
		if (podcast.getPubDate() != null) {
			channel.setUpdateDate(new Date(Long.parseLong(podcast.getPubDate())*1000));
		}
		channel.setStatus(MsoChannel.STATUS_SUCCESS);
		channelMngr.save(channel);		
		return new PostResponse(String.valueOf(NnStatusCode.SUCCESS), "SUCCESS");
	}

	public String[] getPodcastInfo(String urlStr) {
        URL url;
        String podcastInfo[] = new String[3];
        boolean retry = true;
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
		MsoChannel channel = channelMngr.findById(Long.parseLong(rtnProgram.getKey()));		
		if (channel == null) {
			return new PostResponse(String.valueOf(NnStatusCode.CHANNEL_INVALID), "channel invalid");
		}
		
		if (channel.getProgramCount() > MsoChannelManager.MAX_CHANNEL_SIZE) {
			return new PostResponse(String.valueOf(NnStatusCode.CHANNEL_MAXSIZE_EXCEEDED), "CHANNEL_MAXSIZE_EXCEEDED");			
		}
		
		if (channel.isPublic() != true) {
			channel.setPublic(true);
			channelMngr.save(channel);
		}
		
		MsoProgram program = programMngr.findByStorageId(item.getItemId());
		boolean isNew = false;
		if (program == null) {
			isNew = true;
			program = new MsoProgram("", "", "", MsoProgram.TYPE_VIDEO);
		}					
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
			program.setUpdateDate(theDate);
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
		TranscodingService tranService = new TranscodingService();
		String[] transcodingEnv = tranService.getTranscodingEnv(req);
		String transcodingServer = transcodingEnv[0];
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
	
	public String[] getTranscodingEnv(HttpServletRequest req) {
		Properties pro = getTranscodingServerPro();
		String url = NnNetUtil.getUrlRoot(req);
		String env = "office";
		if (url.contains("9x9tvalpha") || url.contains("alpha.9x9.tv")) {
			env = "alpha";
		} else if (url.contains("9x9tvbeta") || url.contains("beta.9x9.tv")){
			env = "beta";
		} else if (url.contains("9x9tvdev") || url.contains("dev.9x9.tv")){
			env = "dev";
		}
		String transcodingServer = pro.getProperty(env);
		String callbackServer = url;
		if (env.equals("office")) {
			callbackServer = pro.getProperty("office_callback");
		}		

		log.info("Original requestUrl=" + req.getRequestURL().toString() + 
				 "; Callback server=" + callbackServer +
				 "; Transcoding server=" + transcodingServer); 
		String devel = pro.getProperty("devel");
		return new String[]{transcodingServer, callbackServer, devel};
	}
	
}
