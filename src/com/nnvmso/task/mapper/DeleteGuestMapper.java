package com.nnvmso.task.mapper;

import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.nnvmso.model.NnUser;

public class DeleteGuestMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{

	protected static final Logger logger = Logger.getLogger(DeleteGuestMapper.class.getName());
		
	//(DeleteGuestMapper)-- mapreduece, remove all the guests
	//(task/account/markSub) find all user's subscription, mark it a type special, like 10 
	//(DeleteSubscriptionMapper) remove everything that's not type 10
	//(MarkSubTypeMapper) subscription back to type 1
	//(task/account/removeSub)find non-youtube channels, put it in hashmap, go through the list and remove those subscriptions
	
	@Override
	public void map(Key key, Entity entity, Context context) {		
		String email= (String) entity.getProperty("email");
		if (email.equals(NnUser.GUEST_EMAIL)) {
			DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
			mutationPool.delete(entity.getKey());
		}
	}
	
}
	