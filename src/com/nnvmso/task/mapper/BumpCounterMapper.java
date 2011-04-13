package com.nnvmso.task.mapper;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

public class BumpCounterMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	
	@Override
	public void map(Key key, Entity log, Context context) {			
		String targetIdStr = context.getConfiguration().get("msoId");
		long targetId = Long.valueOf(targetIdStr);		
		long msoId = (Long) log.getProperty("msoId");
		long count = (Long) log.getProperty("count");
		
		if (msoId == targetId) {
			count = count * 100;
		}
		log.setProperty("count", count);		
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(log);
	}
	
}
