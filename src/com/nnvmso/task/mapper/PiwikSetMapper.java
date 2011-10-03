package com.nnvmso.task.mapper;

import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.nnvmso.lib.PiwikLib;

public class PiwikSetMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	protected static final Logger log = Logger.getLogger(PiwikSetMapper.class.getName());
	
	@Override
	public void map(Key key, Entity entity, Context context) {	
		String urlRoot = context.getConfiguration().get("urlRoot");
		Key setKey = entity.getKey();
		long setId = setKey.getId();
		String piwik = PiwikLib.createPiwikSite(setId, 0, urlRoot);
		log.info("piwik:" + piwik); 
		entity.setProperty("piwik", piwik);
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(entity);
	}
	
}
