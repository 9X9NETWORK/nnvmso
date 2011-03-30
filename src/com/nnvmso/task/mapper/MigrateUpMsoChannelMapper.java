package com.nnvmso.task.mapper;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.nnvmso.model.MsoChannel;

/**
 * one time deal, add new fields 
 *
 */
public class MigrateUpMsoChannelMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	
	@Override
	public void map(Key key, Entity value, Context context) {
		String name = (String)value.getProperty("name");
		String sourceUrl = (String)value.getProperty("sourceUrl");
		short type = MsoChannel.CONTENTTYPE_PODCAST;
		if (sourceUrl != null) {
			sourceUrl = sourceUrl.toLowerCase();
			if (sourceUrl.contains("http://www.youtube.com") || sourceUrl.contains("https://www.youtube.com") || 
					sourceUrl.contains("http://youtube.com") || sourceUrl.contains("https://youtube.com")) {			
				type = MsoChannel.CONTENTTYPE_YOUTUBE_CHANNEL;
			} else if (sourceUrl.equals("http://9x9pod.s3.amazonaws.com/default.mp4")) {
				type = MsoChannel.CONTENTTYPE_SYSTEM;
			}
		} 
		if (name != null) {name = name.toLowerCase();}
		value.setProperty("contentType", type);
		value.setProperty("sourceUrlSearch", sourceUrl);
		value.setProperty("nameSearch", name);
		value.setProperty("enforceTranscoding", 0);
		value.setProperty("transcodingUpdateDate", "");

		DatastoreMutationPool mutationPool = this.getAppEngineContext(
				context).getMutationPool();
		mutationPool.put(value);
	}	
}
