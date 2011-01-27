package com.nnvmso.task.mapper;

import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

public class RemovePropertyMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	private static final Logger log = Logger.getLogger(AddPropertyMapper.class.getName());

	@Override
	public void map(Key key, Entity value, Context context) {
		log.info("Adding key to deletion pool: " + key);
		//
		value.removeProperty("maxChannel");
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(value);
	}
	
}
