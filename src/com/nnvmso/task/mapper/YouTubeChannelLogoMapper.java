package com.nnvmso.task.mapper;

import java.util.Map;
import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.nnvmso.lib.YouTubeLib;
import com.nnvmso.model.MsoChannel;

public class YouTubeChannelLogoMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable> {
	
	protected static final Logger log = Logger.getLogger(YouTubeChannelLogoMapper.class.getName());
	
	@Override
	public void map(Key key, Entity entity, Context context) {
		
		long dbContentType = (Long) entity.getProperty("contentType");
		short contentType = Short.parseShort(String.valueOf(dbContentType));
		String badLogoUrl = context.getConfiguration().get("badLogoUrl");		
		if (contentType == MsoChannel.CONTENTTYPE_YOUTUBE_CHANNEL) {
			
			String imageUrl = (String) entity.getProperty("imageUrl");
			if (imageUrl == null || imageUrl.equals(badLogoUrl)) {
				DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
				String name = YouTubeLib.getYouTubeChannelName((String) entity.getProperty("sourceUrl"));
				Map<String, String> youtube = YouTubeLib.getYouTubeChannelEntry(name);
				String thumbnail = youtube.get("thumbnail");
				String description = youtube.get("description");
				if (thumbnail != null) {
					log.info("youtube: " + (String)entity.getProperty("name") + " - " + thumbnail);
					entity.setProperty("imageUrl", thumbnail);
					mutationPool.put(entity);
				}
				if (description != null && description.length() > 0 && entity.getProperty("intro") == null) {
					entity.setProperty("intro", description);
					mutationPool.put(entity);
				}
			}
		} else if (contentType == MsoChannel.CONTENTTYPE_YOUTUBE_PLAYLIST) {
			
			String imageUrl = (String) entity.getProperty("imageUrl");
			if (imageUrl == null || imageUrl.equals(badLogoUrl)) {
				DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
				String name = YouTubeLib.getYouTubeChannelName((String) entity.getProperty("sourceUrl"));
				Map<String, String> playlist = YouTubeLib.getYouTubePlaylistEntry(name);
				String thumbnail = playlist.get("thumbnail");
				String description = playlist.get("description");
				if (thumbnail != null) {
					log.info("playlist: " + (String)entity.getProperty("name") + " - " + thumbnail);
					entity.setProperty("imageUrl", thumbnail);
					mutationPool.put(entity);
				}
				if (description != null && description.length() > 0 && entity.getProperty("intro") == null) {
					entity.setProperty("intro", description);
					mutationPool.put(entity);
				}
			}
		}
	}
	
	
	
}
