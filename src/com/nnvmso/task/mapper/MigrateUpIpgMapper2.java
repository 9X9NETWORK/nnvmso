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
public class MigrateUpIpgMapper2 extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	
	@Override
	public void map(Key key, Entity value, Context context) {
		value.setProperty("programIdStr", null);		
		DatastoreMutationPool mutationPool = this.getAppEngineContext(
				context).getMutationPool();
		mutationPool.put(value);
	}	
}
