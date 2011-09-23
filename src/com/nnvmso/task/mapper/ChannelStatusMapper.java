package com.nnvmso.task.mapper;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.nnvmso.model.MsoChannel;

public class ChannelStatusMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	
	@Override
	public void map(Key key, Entity entity, Context context) {
		long dbStatus = (Long)entity.getProperty("status");
		short status = Short.parseShort(String.valueOf(dbStatus));
		if (status == MsoChannel.STATUS_SUCCESS) {
			entity.setProperty("status", MsoChannel.STATUS_WAIT_FOR_APPROVAL);
			DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
			mutationPool.put(entity);			
		}
	}
	
}
