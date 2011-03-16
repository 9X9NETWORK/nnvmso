package com.nnvmso.task.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.nnvmso.model.NnUser;

public class DMUserCounterMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	
	@Override
	public void map(Key key, Entity value, Context context) {			
		String since = context.getConfiguration().get("since");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date sinceDate = null;
		try {
			if (since!= null) {
				sinceDate = sdf.parse(since);
			}
		} catch (ParseException e) {
			return;
		}
		
		String email = (String) value.getProperty("email");
		Date createDate = (Date)value.getProperty("createDate");
		context.getCounter("AccountCount", "totalAccountCount").increment(1);
		if (email != null) {
			if (email.equals(NnUser.GUEST_EMAIL)) {
				context.getCounter("AccountCount", "totalGuestCount").increment(1);
				if (createDate.after(sinceDate)) {
					context.getCounter("AccountCount", "newGuestCount").increment(1);
				}
			} else {
				context.getCounter("AccountCount", "totalUserCount").increment(1);
				if (createDate.after(sinceDate)) {
					context.getCounter("AccountCount", "newUserCount").increment(1);
				}
			}			
		}
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(value);
	}
	
}
