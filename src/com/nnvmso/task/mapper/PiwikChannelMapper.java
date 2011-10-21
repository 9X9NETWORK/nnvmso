package com.nnvmso.task.mapper;

import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.nnvmso.lib.PiwikLib;
import com.nnvmso.model.MsoChannel;

public class PiwikChannelMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	protected static final Logger log = Logger.getLogger(PiwikChannelMapper.class.getName());
	
	@Override
	public void map(Key key, Entity entity, Context context) {
		
		entity.setProperty("piwik", null);
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(entity);

		/*
		String urlRoot = context.getConfiguration().get("urlRoot");
		//Key channelKey = (Key) entity.getProperty("key");
		Key channelKey = entity.getKey();
		long dbStatus = (Long)entity.getProperty("status");
		short status = Short.parseShort(String.valueOf(dbStatus));
		if (channelKey != null) {
			long channelId = channelKey.getId();
			if (status == MsoChannel.STATUS_SUCCESS) {
				entity.setProperty("piwik", PiwikLib.createPiwikSite(0, channelId, urlRoot));
				DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
				mutationPool.put(entity);
			}
		} else {
			String name = (String) entity.getProperty("name");
			log.info("channel Key is empty:" + name);
		}
		*/
	}
	
}
