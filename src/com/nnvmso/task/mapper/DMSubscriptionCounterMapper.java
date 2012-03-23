package com.nnvmso.task.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Counter;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

public class DMSubscriptionCounterMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	
	protected static final Logger log = Logger.getLogger(DMSubscriptionCounterMapper.class.getName());
	
	@Override
	public void map(Key key, Entity value, Context context) {		
		String before = context.getConfiguration().get("before");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date beforeDate = null;
		try {
			beforeDate = sdf.parse(before);
		} catch (ParseException e) {
			log.info("parse error");
			return;
		}
		
		Date updateDate = (Date)value.getProperty("updateDate");
		if (updateDate.before(beforeDate)) {		
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
	
}
