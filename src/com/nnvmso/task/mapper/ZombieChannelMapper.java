package com.nnvmso.task.mapper;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.ChannelSetChannelManager;
import com.nnvmso.service.ContentOwnershipManager;
import com.nnvmso.service.MsoProgramManager;
import com.nnvmso.service.SubscriptionManager;

/**
 * Zombie Channel Mapper
 * 
 * A channel so called zombie is satisfied following conditions
 *  - type is MsoChannel.CONTENTTYPE_MIXED
 *  - is not owned by any curator
 *  - has no subscribers
 *  - has not episodes in it
 *  - not in any set
 * 
 * @author Louis
 *
 */
public class ZombieChannelMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable> {
	
	protected static final Logger logger = Logger.getLogger(ZombieChannelMapper.class.getName());
	
	@Override
	public void map(Key key, Entity entity, Context context) throws IOException, InterruptedException {
		
		MsoProgramManager programMngr = new MsoProgramManager();
		SubscriptionManager subMngr = new SubscriptionManager();
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		
		Long channelId = key.getId();
		Long contentType = (Long)entity.getProperty("contentType");
		
		if (contentType == MsoChannel.CONTENTTYPE_MIXED) {
			if (ownershipMngr.findAllByChannelId(channelId).size() == 0) {
				if (subMngr.total("channelId == " + channelId) == 0) {
					if (programMngr.total("channelId == " + channelId) == 0) {
						if (cscMngr.findAllByChannelId(channelId).size() == 0) {
							logger.warning("channel " + channelId + " is zombie");
						}
					}
				}
			}
		}
	}
}
