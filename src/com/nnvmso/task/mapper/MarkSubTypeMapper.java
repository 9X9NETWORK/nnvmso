package com.nnvmso.task.mapper;

import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.nnvmso.model.MsoIpg;

public class MarkSubTypeMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{

	protected static final Logger logger = Logger.getLogger(DeleteGuestMapper.class.getName());
		
	@Override
	public void map(Key key, Entity entity, Context context) {		
		entity.setProperty("type", MsoIpg.TYPE_GENERAL);
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(entity);					
	}
	
}
	