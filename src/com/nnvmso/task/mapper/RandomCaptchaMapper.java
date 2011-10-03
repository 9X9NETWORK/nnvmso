package com.nnvmso.task.mapper;

import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

public class RandomCaptchaMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	protected static final Logger log = Logger.getLogger(RandomCaptchaMapper.class.getName());
	
	@Override
	public void map(Key key, Entity entity, Context context) {		
		entity.setProperty("random", Math.random());
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(entity);
	}
	
}
