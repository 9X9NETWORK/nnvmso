package com.nnvmso.task.mapper;

import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Counter;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.nnvmso.model.NnUser;

public class MergeAccountMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{

	protected static final Logger logger = Logger.getLogger(ReCategoryMapper.class.getName());
		
	@Override
	public void map(Key key, Entity entity, Context context) {
		context.getCounter("AccountCount", "totalGuestCount").increment(1);

		System.out.println(context.getCounter("AccountCount", "totalGuestCount").getValue());

		/*
		String toBeRemovedStr = context.getConfiguration().get("msoId");
		long toBeRemovedId = Long.valueOf(toBeRemovedStr);
				
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();		
		String email= (String) entity.getProperty("email");
		long msoId = (Long) entity.getProperty("msoId");
		 */
		
		/*
		if (email.equals("a@a.com")) {
			System.out.println("<<<<<<<< email:" + email + ";msoId:" + msoId);
		}
		
		//works only for 5f, 9x9, but not working for 9x9, 5f
		if (!email.equals(NnUser.GUEST_EMAIL)) {
			context.getCounter("user", email).increment(1);
			Counter c = context.getCounter("user", email);
			
			System.out.println("email:" + email + ";counter=" + c.getValue());
			if (c.getValue() > 1 && msoId == toBeRemovedId) {
				System.out.println("entity removed:" + ";" + email);
				mutationPool.delete(entity.getKey());
			} else if (c.getValue() > 1 && msoId != toBeRemovedId) {
				System.out.println("entity not able to removed:" + ";" + email);
			}
		}
		*/
	}
	
}
	