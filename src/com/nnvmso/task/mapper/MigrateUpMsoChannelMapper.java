package com.nnvmso.task.mapper;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

/**
 * one time deal, add new fields 
 *
 */
public class MigrateUpMsoChannelMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	
	@Override
	public void map(Key key, Entity value, Context context) {
		String name = (String)value.getProperty("name");
		String sourceUrl = (String)value.getProperty("sourceUrl");

		if (sourceUrl != null) {sourceUrl = sourceUrl.toLowerCase();} 
		if (name != null) {name = name.toLowerCase();}
		value.setProperty("sourceUrlSearch", sourceUrl);
		value.setProperty("nameSearch", name);
		value.setProperty("enforceTranscoding", 0);
		value.setProperty("transcodingUpdateDate", "");

		DatastoreMutationPool mutationPool = this.getAppEngineContext(
				context).getMutationPool();
		mutationPool.put(value);
	}	
}
