package com.nnvmso.task.mapper;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.nnvmso.model.MsoProgram;

public class ProgramTypeMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{
	
	@Override
	public void map(Key key, Entity entity, Context context) {
		long dbType = (Long)entity.getProperty("type");
		short type = Short.parseShort(String.valueOf(dbType));
		if (type == MsoProgram.TYPE_AUDIO) {
			entity.setProperty("type", MsoProgram.TYPE_VIDEO);
			DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
			mutationPool.put(entity);			
		}
	}
	
}
