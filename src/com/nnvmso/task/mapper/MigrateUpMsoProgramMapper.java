package com.nnvmso.task.mapper;

import java.util.Date;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
/**
 * one time deal, add new fields
 *
 */
public class MigrateUpMsoProgramMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{

	@Override
	public void map(Key key, Entity value, Context context) {
		Date pubDate = (Date) value.getProperty("updateDate");
		value.setProperty("pubDate", pubDate);
		value.setProperty("status", 0);
		DatastoreMutationPool mutationPool = this.getAppEngineContext(
				context).getMutationPool();
		mutationPool.put(value);
	}	
}
