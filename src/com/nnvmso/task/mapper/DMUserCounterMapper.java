package com.nnvmso.task.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

public class DMUserCounterMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	
	@Override
	public void map(Key key, Entity value, Context context) {		
		String since = context.getConfiguration().get("since");
		String before = context.getConfiguration().get("before");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date sinceDate = null;
		Date beforeDate = null;
		try {
			sinceDate = sdf.parse(since);
			beforeDate = sdf.parse(before);
		} catch (ParseException e) {
			return;
		}
		
		Date createDate = (Date)value.getProperty("createDate");
		Date updateDate = (Date)value.getProperty("updateDate");
		if (createDate.before(beforeDate)) {
			context.getCounter("AccountCount", "totalUserCount").increment(1);			
		}
		if (createDate.after(sinceDate) && createDate.before(beforeDate)) {
			context.getCounter("AccountCount", "newUserCount").increment(1);
		}
		if (updateDate.after(sinceDate) && createDate.before(beforeDate)) {
			context.getCounter("AccountCount", "activeUserCount").increment(1);
		}

		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(value);
	}
	
}
