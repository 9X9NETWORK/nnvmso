package com.nnvmso.task.mapper;

import java.util.Set;
import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.SearchJanitor;

public class UpdateFtsMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	protected static final Logger log = Logger.getLogger(UpdateFtsMapper.class.getName());
	
	@Override
	public void map(Key key, Entity entity, Context context) {
		String name = (String) entity.getProperty("name"); 
		String intro = (String) entity.getProperty("intro");
		String sourceUrl = (String) entity.getProperty("sourceUrl");
		long dbStatus = (Long)entity.getProperty("status");
		short status = Short.parseShort(String.valueOf(dbStatus));
		if (status == MsoChannel.STATUS_SUCCESS) {
			log.info("This channel(" + name + ") is updated fts:" + sourceUrl);
			Set<String> fts = SearchJanitor.getFtsTokens(name, intro);
			entity.setProperty("fts", fts);		
			DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
			mutationPool.put(entity);
		}
	}
	
}
