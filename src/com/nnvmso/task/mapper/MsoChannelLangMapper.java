package com.nnvmso.task.mapper;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

public class MsoChannelLangMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	
	@Override
	public void map(Key key, Entity entity, Context context) {			
		String intro = (String) entity.getProperty("intro");
		String langCode = "en";
		System.out.println("intro:" + intro);
		if (intro != null && (!intro.contains("a") && !intro.contains("o")))
			langCode = "zh";
		entity.setProperty("langCode", langCode);				
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(entity);
	}
	
}
