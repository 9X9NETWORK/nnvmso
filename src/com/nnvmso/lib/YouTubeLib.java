package com.nnvmso.lib;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.PlaylistEntry;
import com.google.gdata.data.youtube.PlaylistFeed;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.nnvmso.service.NnStatusCode;

public class YouTubeLib {
	
	protected static final Logger log = Logger.getLogger(YouTubeLib.class.getName());
	
	private static final String YOUTUBE_CLIENT_ID = "9x9.tv";
	private static final String YOUTUBE_DEVELOPER_KEY = "AI39si5AKUCeyI23OTiunkacaJ_kRzR_VPxVhTJn2Rw7rJYE2mpKKVITytE22jpws7W6L1IBAA6EEf6_B8Pp2y0hG-zM5jtGFw";
	
	/** 
	 * 1. remove those invalid keywords we already know.
	 * 2. merge the following youtube channel formats to one, http://www.youtube.com/user/<userid>
	 *    http://www.youtube.com/<usrid>
	 *    http://www.youtube.com/user/<usrid>
	 *    http://www.youtube.com/profile?user=<usrid>
	 * 3. merge the following youtube playlist formats to one, http://www.youtube.com/view_play_list?p=<pid>
	 *    http://www.youtube.com/view_play_list?p=<pid>
	 *    http://www.youtube.com/user/UCBerkeley#p/c/<pid>
	 *    http://www.youtube.com/user/UCBerkeley#g/c/<pid>
	 *    http://www.youtube.com/user/UCBerkeley#p/c/<pid>/0/-dQltKG3NlI
	 *    http://www.youtube.com/profile?user=UCBerkeley#grid/user/<pid>
	 *    http://www.youtube.com/watch?v=-dQltKG3NlI&p=<pid>
	 *    http://www.youtube.com/watch?v=-dQltKG3NlI&playnext=1&list=<pid>
	 * 4. youtube api call (disabled for now)
	 * Example1: they should all become http://www.youtube.com/user/davidbrucehughes    
	 *    http://www.youtube.com/profile?user=davidbrucehughes#g/u
	 *    http://www.youtube.com/davidbrucehughes#g/a
	 *    http://www.youtube.com/user/davidbrucehughes#g/p
	 * Example2: they should all become http://www.youtube.com/user/view_play_list?p=03D59E2ECDDA66DF
	 *    http://www.youtube.com/view_play_list?p=03D59E2ECDDA66DF
	 *    http://www.youtube.com/user/UCBerkeley#p/c/03D59E2ECDDA66DF
	 *    http://www.youtube.com/user/UCBerkeley#g/c/095393D5B42B2266
	 *    http://www.youtube.com/user/UCBerkeley#p/c/03D59E2ECDDA66DF/0/-dQltKG3NlI
	 *    http://www.youtube.com/profile?user=UCBerkeley#grid/user/03D59E2ECDDA66DF
	 *    http://www.youtube.com/watch?v=-dQltKG3NlI&p=03D59E2ECDDA66DF
	 *    http://www.youtube.com/watch?v=-dQltKG3NlI&playnext=1&list=PL03D59E2ECDDA66DF
	 *    http://www.youtube.com/watch?v=-dQltKG3NlI&playnext=1&list=PL03D59E2ECDDA66DF&feature=list_related
	 */		
	public static String formatCheck(String urlStr) {
		if (urlStr == null) {return null;}
		String[] invalid = {"index", "videos",
		                    "entertainment", "music", "news", "movies",
		                    "comedy", "gaming", "sports", "education",
		                    "shows", 
		                    "store", "channels", "contests_main"};		
		HashSet<String> dic = new HashSet<String>();
		for (int i=0; i<invalid.length; i++) {
			dic.add(invalid[i]);
		}
		String url = null;
		String reg = "^(http|https)://?(www.)?youtube.com/(user/|profile\\?user=)?(\\w+)";		
		Pattern pattern = Pattern.compile(reg);
		Matcher m = pattern.matcher(urlStr);
		while (m.find()) {
			if (dic.contains(m.group(4))) {return null;}
			url = "http://www.youtube.com/user/" + m.group(4);
		}
		reg = "^(http|https)://?(www.)?youtube.com/(user/|profile\\?user=)?(.+)(#(p/c|g/c|grid/user)/(\\w+))";
		pattern = Pattern.compile(reg);
		m = pattern.matcher(urlStr);
		while (m.find()) {
			url = "http://www.youtube.com/view_play_list?p=" + m.group(7);
		}
		
		reg = "^(http|https)://?(www.)?youtube.com/view_play_list\\?p=(\\w+)";
		pattern = Pattern.compile(reg);
		m = pattern.matcher(urlStr);		
		while (m.find()) {
			url = "http://www.youtube.com/view_play_list?p=" + m.group(3);
		}
		
		reg = "^(http|https)://?(www.)?youtube.com/(.+)?(p|list)=(PL)?(\\w+)";
		pattern = Pattern.compile(reg);
		m = pattern.matcher(urlStr);
		while (m.find()) {
			url = "http://www.youtube.com/view_play_list?p=" + m.group(6);
		}
		
		if (url != null) { 
			url = url.toLowerCase();
			if (url.equals("http://www.youtube.com/user/watch")) {
				url = null;
			}
		}
		//log.info("original url:" + urlStr + ";result=" + url);
		
		//if (!youTubeCheck(result)) {return null;} //till the function is fixed		
		return url;		
	}
	
	public static Map<String, String> getYouTubePlaylistEntry(String playlistIdStr) {
		
		Map<String, String> results = new HashMap<String, String>();
		YouTubeService youtubeService = new YouTubeService(YOUTUBE_CLIENT_ID, YOUTUBE_DEVELOPER_KEY);
		
		String sourceUrl = "http://gdata.youtube.com/feeds/api/playlists/" + playlistIdStr;
		try {
			PlaylistFeed playlistFeed = youtubeService.getFeed(new URL(sourceUrl), PlaylistFeed.class);
			List<PlaylistEntry> videoEntries = playlistFeed.getEntries();
			log.info("entry count: " + videoEntries.size());
			if (videoEntries.size() > 0) {
				VideoEntry videoEntry = videoEntries.get(0);
				YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
				List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
				if (thumbnails.size() > 0) {
					String thumbnailUrl = thumbnails.get(0).getUrl();
					results.put("thumbnail", thumbnailUrl);
					log.info("thumb: " + thumbnailUrl);
				}
			}
			if (playlistFeed.getTitle() != null) {
				String title = playlistFeed.getTitle().getPlainText();
				results.put("title", title);
				log.info("title: " + title);
			}
			if (playlistFeed.getSubtitle() != null) {
				String subTitle = playlistFeed.getSubtitle().getPlainText();
				results.put("description", subTitle);
				log.info("description: " + subTitle);
			}
		} catch (Exception e) {
			NnLogUtil.logException(e);
		}
		return results;
	}
	
	public static Map<String, String> getYouTubeChannelEntry(String userIdStr) {
		
		Map<String, String> results = new HashMap<String, String>();
		results.put("status", String.valueOf(NnStatusCode.SUCCESS));
		YouTubeService youtubeService = new YouTubeService(YOUTUBE_CLIENT_ID, YOUTUBE_DEVELOPER_KEY);		
		String sourceUrl = "http://gdata.youtube.com/feeds/api/users/" + userIdStr + "/uploads";
		try {
			VideoFeed videoFeed = youtubeService.getFeed(new URL(sourceUrl), VideoFeed.class);
			List<VideoEntry> videoEntries = videoFeed.getEntries();
			log.info("entry count: " + videoEntries.size());
			if (videoEntries.size() > 0) {
				VideoEntry videoEntry = videoEntries.get(0);
				YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
				List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
				if (thumbnails.size() > 0) {
					String thumbnailUrl = thumbnails.get(0).getUrl();
					results.put("thumbnail", thumbnailUrl);
					log.info("thumb: " + thumbnailUrl);
				}
			}
			if (videoFeed.getTitle() != null) {
				String title = videoFeed.getTitle().getPlainText();
				results.put("title", title);
				log.info("title: " + title);
			}
			if (videoFeed.getSubtitle() != null) {
				String subTitle = videoFeed.getSubtitle().getPlainText();
				results.put("description", subTitle);
				log.info("description: " + subTitle);
			}
		} catch (com.google.gdata.util.ServiceForbiddenException e) {
			results.put("status", String.valueOf(NnStatusCode.CHANNEL_YOUTUBE_NOT_AVAILABLE));
		} catch (Exception e) {
			results.put("status", String.valueOf(NnStatusCode.ERROR));
			NnLogUtil.logException(e);
		}
		return results;
	}
	
	public static Map<String, String> getYouTubeVideoEntry(String videoIdStr) {
		
		Map<String, String> results = new HashMap<String, String>();
		YouTubeService youtubeService = new YouTubeService(YOUTUBE_CLIENT_ID, YOUTUBE_DEVELOPER_KEY);
		
		try {
			String videoEntryUrl = "http://gdata.youtube.com/feeds/api/videos/" + videoIdStr;
			VideoEntry videoEntry = youtubeService.getEntry(new URL(videoEntryUrl), VideoEntry.class);
			
			String title = videoEntry.getTitle().getPlainText();
			results.put("title", title);
			
			YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
			String description = mediaGroup.getDescription().getPlainTextContent();
			results.put("description", description);
			
			List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
			if (thumbnails.size() > 0) {
				String thumbnailUrl = thumbnails.get(0).getUrl();
				results.put("thumbnail", thumbnailUrl);
			}
		} catch (Exception e) {
			NnLogUtil.logException(e);
		}
		return results;
	}
	
	public static String getYouTubeChannelName(String urlStr) {
		String channelUrl = "http://www.youtube.com/user/";
		String playListUrl = "http://www.youtube.com/view_play_list?p=";
		String name = urlStr.substring(channelUrl.length(), urlStr.length());		
		if (urlStr.contains("view_play_list")) {
			name = urlStr.substring(playListUrl.length(), urlStr.length()); 
		}
		return name;
	}
		
	
	/**
	 * YouTube API request format, http://gdata.youtube.com/feeds/api/users/androidcentral
	 * This function currently checks only if the query status is not 200.
	 * 
	 * @@@ IMPORTANT: This code will be blocked by YouTube, need to add user's IP, indicating you are on behalf of the user.
	 * 
	 * @param urlStr support only format of http://www.youtube.com/user/android  
	 */
	public static boolean youTubeCheck(String urlStr) {		
		String[] splits = urlStr.split("/");
		String apiReq = "http://gdata.youtube.com/feeds/api/users/" + splits[splits.length-1];

		URL url;
		try {
			//HTTP GET
			url = new URL(apiReq);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
	        int statusCode = connection.getResponseCode();
	        if (statusCode != HttpURLConnection.HTTP_OK) {
	        	log.info("yutube GET response not ok with url:" + urlStr + "; status code = " + connection.getResponseCode());
	        	return false;
	        }
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
}
