package com.nnvmso.task.mapper;

import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Counter;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

public class ReCategoryMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable>{

	//1. create new categories (admin/category/newStuff)
	//2. ReCategoryMapper
 	//	 a. remove duplicate categories but remove only one
	//   b. remap
	//3. task/calibration/categoryChannelCount
	protected static final Logger log = Logger.getLogger(ReCategoryMapper.class.getName());
		
	@Override
	public void map(Key key, Entity entity, Context context) {
		//old id
		String foodAndWineStr = context.getConfiguration().get("FoodAndWine");
		long foodAndWineId = Long.valueOf(foodAndWineStr);
		String hfStr = context.getConfiguration().get("HealthAndfitness");
		long hfId = Long.valueOf(hfStr);
		String lifestyleStr = context.getConfiguration().get("Lifestyle");
		long lifestyleId = Long.valueOf(lifestyleStr);
		String glStr = context.getConfiguration().get("GayAndlesbian");
		long glId = Long.valueOf(glStr);
		String sportsStr = context.getConfiguration().get("Sports");
		long sportsId = Long.valueOf(sportsStr);
		String comedyStr = context.getConfiguration().get("Comedy");
		long comedyId = Long.valueOf(comedyStr);
		
		//new id
		String tlStr = context.getConfiguration().get("TravelAndLiving");
		long tlId = Long.valueOf(tlStr);
		String lhStr = context.getConfiguration().get("LifestyleAndHobbies");
		long lhId = Long.valueOf(lhStr);
		String soStr = context.getConfiguration().get("SportsAndOutdoors");
		long soId = Long.valueOf(soStr);
		String entertainmentStr = context.getConfiguration().get("Entertainment");
		long entertainmentId = Long.valueOf(entertainmentStr);
		
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();		
		long categoryId = (Long) entity.getProperty("categoryId");
		long channelId = (Long) entity.getProperty("channelId");
		
		context.getCounter("category", String.valueOf(channelId)).increment(1);
		Counter c = context.getCounter("category", String.valueOf(channelId)); //group name, counter name
		if (c.getValue() > 1) {
			mutationPool.delete(entity.getKey());
			log.info("entity removed:(category id, channel id)" + categoryId + ";" + channelId);
		} else {
			long newCategoryId = 0;
			if (categoryId == foodAndWineId) {				
				entity.setProperty("categoryId", tlId);
				newCategoryId = tlId;
			} else if (categoryId == hfId) {
				entity.setProperty("categoryId", lhId);
				newCategoryId = hfId;
			} else if (categoryId == lifestyleId) {			
				entity.setProperty("categoryId", lhId);
				newCategoryId = lifestyleId;
			} else if (categoryId == glId) {				
				entity.setProperty("categoryId", lhId);
				newCategoryId = lhId; 
			} else if (categoryId == sportsId) {			
				entity.setProperty("categoryId", soId);
				newCategoryId = soId;
			} else if (categoryId == comedyId) {
				System.out.println("original comedy, new entertainment:" + entertainmentId);
				entity.setProperty("categoryId", entertainmentId);
				newCategoryId = entertainmentId;				
			}
			if (newCategoryId != 0) {
				log.info("entity changed category:(old)" + categoryId + ";(new)" + newCategoryId + ";(channelId)" + channelId);
				mutationPool.put(entity);
			}
		}
	}
	
}
	