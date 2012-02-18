package com.nnvmso.task.mapper;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Counter;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

public class DMSubscriptionCounterMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	
	@Override
	public void map(Key key, Entity value, Context context) {		
		
		long userId = (Long) value.getProperty("userId");
		String userIdStr = String.valueOf(userId);
		context.getCounter("SubCount", userIdStr).increment(1);
		
		Counter counter = context.getCounter("SubCount", userIdStr);
		long cnt = counter.getValue();
		long minus = cnt - 1;
		context.getCounter("SubCount", String.valueOf(cnt)).increment(1);
		context.getCounter("SubCount", String.valueOf(minus)).increment(-1);
			
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(value);
	}
	
}
