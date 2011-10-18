package com.nnvmso.task.mapper;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

public class ThumbnailMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	
	@Override
	public void map(Key key, Entity entity, Context context) {
		String imageUrl = (String)entity.getProperty("imageUrl");
		if (imageUrl != null) {
			imageUrl = imageUrl.replace("http://www.maplestage.com", " http://9x9ms.s3.amazonaws.com");
			entity.setProperty("imageUrl", imageUrl);
			DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
			mutationPool.put(entity);
		}
	}
	
}
