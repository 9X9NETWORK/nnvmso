package com.nnvmso.task.mapper;

import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

public class CalibrateChannelProgramMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	private static final Logger log = Logger.getLogger(SearchPropertyMapper.class.getName());
	
	@Override
	public void map(Key key, Entity value, Context context) {
		String property = context.getConfiguration().get("propertyName");		
		String search = context.getConfiguration().get("search");
		String toBeSearched = (String) value.getProperty(property);
		if (toBeSearched != null) {
			if (toBeSearched.toLowerCase().contains(search.toLowerCase())) {
				log.info("<< found >> match in " + KeyFactory.keyToString(value.getKey()));
				context.getCounter("SearchMatch", "count").increment(1);
			}
		}
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(value);
	}
	
}
