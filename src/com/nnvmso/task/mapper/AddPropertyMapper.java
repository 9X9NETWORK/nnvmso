package com.nnvmso.task.mapper;

import java.util.Date;
import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

public class AddPropertyMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	private static final Logger log = Logger.getLogger(AddPropertyMapper.class.getName());

	@Override
	public void map(Key key, Entity value, Context context) {		
		String propertyName = context.getConfiguration().get("propertyName");
		String propertyValue = context.getConfiguration().get("propertyValue");
		String type = context.getConfiguration().get("propertyType");
		
		log.info("Adding property -- property name=" + propertyName + ";value=" + propertyValue + ";type=" + type);
		
		if (type.equals("int")) {
			value.setProperty(propertyName, Integer.parseInt(propertyValue));
		} else if (type.equals("long")) {
			value.setProperty(propertyName, Long.parseLong(propertyValue));
		} else if (type.equals("short")) {
			value.setProperty(propertyName, Short.parseShort(propertyValue));
		} else if (type.equals("boolean")) {
			value.setProperty(propertyName, Boolean.parseBoolean(propertyValue));
		} else if (type.equals("date")) {
			value.setProperty(propertyName, new Date());
		} else if (type.equals("string")) {
			value.setProperty(propertyName, propertyValue);
		} 
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(value);
	}
	
}
