package com.nnvmso.task.mapper;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.nnvmso.model.SubscriptionLog;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.SubscriptionLogManager;
import com.nnvmso.service.SubscriptionManager;

public class CalibrateSubscriptionMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable> {
	
	protected static final Logger logger = Logger.getLogger(CalibrateSubscriptionMapper.class.getName());
	
	@Override
	public void map(Key key, Entity entity, Context context) throws IOException, InterruptedException {
		SubscriptionManager subMngr = new SubscriptionManager();
		SubscriptionLogManager subLogMngr = new SubscriptionLogManager();
		
		long nnId = (new MsoManager()).findNNMso().getKey().getId();
		long channelId = key.getId();
		
		SubscriptionLog subLog = subLogMngr.findByMsoIdAndChannelId(nnId, channelId);
		
		if (subLog == null) {
			logger.info("new a subscription log: " + channelId);
			subLog = new SubscriptionLog(nnId, channelId);
			int count = subMngr.total("channelId == " + channelId);
			logger.info("count = " + count);
			subLog.setCount(count);
			subLogMngr.create(subLog);
		} else {
			logger.info("calibrate subscription count: " + channelId);
			int count = subMngr.total("channelId == " + channelId);
			logger.info("count = " + count);
			subLog.setCount(count);
			subLogMngr.save(subLog);
		}
	}
	
}
