package com.nnvmso.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.nnvmso.dao.MsoProgramDao;
import com.nnvmso.lib.CacheFactory;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.model.ChannelAutosharing;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.SnsAuth;
import com.nnvmso.model.ViewLog;
import com.nnvmso.web.json.facebook.FBPost;

@Service
public class MsoProgramManager {
	
	protected static final Logger log = Logger.getLogger(MsoProgramManager.class.getName());
	private static MessageSource messageSource = new ClassPathXmlApplicationContext("locale.xml");
	
	private MsoProgramDao msoProgramDao = new MsoProgramDao();
	
	/**
	 * Create program skeleton only, not relate to any channel
	 * 
	 * @param program
	 */
	public void create(MsoProgram program) {
		
		program.setChannelId(0);
		msoProgramDao.save(program);
	}
	
	public void create(MsoChannel channel, MsoProgram program) {		
		Date now = new Date();
		program.setCreateDate(now);
		program.setUpdateDate(now);
		if (program.getPubDate() == null) {
			program.setPubDate(now);
		}
		program.setChannelId(channel.getKey().getId());
		msoProgramDao.save(program);

		//set channel count
		int count = channel.getProgramCount() + 1;
		channel.setProgramCount(count);
		MsoChannelManager channelMngr = new MsoChannelManager();
		channelMngr.save(channel);

		//if the channel's original programCount is zero, its count will not be in the category, adding it now.
		if (count == 1) {
			CategoryManager categoryMngr = new CategoryManager();
			System.out.println("mso program manager, channel create, addChannelCount");
			categoryMngr.addChannelCounter(channel);
		}		

		//store in cache
		Cache cache = CacheFactory.get();
		if (cache != null) {
			List<MsoProgram> programs = new ArrayList<MsoProgram>();
			programs.add(program);
			this.storeInCache(cache, programs, program.getChannelId());
		}				
		
		// hook, auto share to facebook
		AutosharingService sharingService = new AutosharingService();
		SnsAuthManager snsMngr = new SnsAuthManager();
		List<ChannelAutosharing> channelAutosharings = sharingService.findAllByChannelIdAndType(channel.getKey().getId(), SnsAuth.TYPE_FACEBOOK);
		log.info("autosharing count = " + channelAutosharings.size());
		try {
			FBPost fbPost = new FBPost(program.getName(), program.getIntro(), program.getImageUrl());
			MsoManager msoMngr = new MsoManager();
			InetAddress local = InetAddress.getLocalHost();
			String url = "http://" + local.getHostName() + "/view?channel=" + channel.getKey().getId() + "&episode=" + program.getKey().getId();
			fbPost.setLink(url);
			if (program.getComment() != null) {
				fbPost.setMessage(program.getComment());
			}
			for (ChannelAutosharing autosharing : channelAutosharings) {
				SnsAuth snsAuth = snsMngr.findFacebookAuthByMsoId(autosharing.getMsoId());
				Mso mso = msoMngr.findById(autosharing.getMsoId());
				if (mso.getPreferredLangCode() != null && mso.getPreferredLangCode().equals("en")) {
					fbPost.setCaption(messageSource.getMessage("cms.autosharing.episode_added", null, Locale.ENGLISH));
				} else {
					fbPost.setCaption(messageSource.getMessage("cms.autosharing.episode_added", null, Locale.TRADITIONAL_CHINESE));
				}
				if (snsAuth != null && snsAuth.isEnabled()) {
					if (autosharing.getTarget() != null && autosharing.getParameter() != null) {
						fbPost.setFacebookId(autosharing.getTarget());
						fbPost.setAccessToken(autosharing.getParameter());
					} else {
						fbPost.setFacebookId(snsAuth.getToken());
						fbPost.setAccessToken(snsAuth.getSecrete());
					}
					QueueFactory.getDefaultQueue().add(TaskOptions.Builder
                            .withUrl("/CMSAPI/postToFacebook")
                            .payload(new ObjectMapper().writeValueAsBytes(fbPost), "application/json"));
				}
			}
		} catch (UnknownHostException e) {
			NnLogUtil.logException(e);
		} catch (JsonGenerationException e) {
			NnLogUtil.logException(e);
		} catch (JsonMappingException e) {
			NnLogUtil.logException(e);
		} catch (IOException e) {
			NnLogUtil.logException(e);
		}
	} 
	
	public MsoProgram save(MsoProgram program) {		
		program.setUpdateDate(new Date()); // NOTE: a trying to modify program update time (from admin) will be omitted by this
		program = msoProgramDao.save(program);
		Cache cache = CacheFactory.get();
		if (cache != null) {
			List<MsoProgram> programs = new ArrayList<MsoProgram>();
			programs.add(program);
			this.storeInCache(cache, programs, program.getChannelId());
		}
		//take the chance there's only 50 max per channel, shouldn't take too long, 
		//and the performance of save is not a concern		
		MsoChannelManager channelMngr = new MsoChannelManager();
		channelMngr.calculateAndSaveChannelCount(program.getChannelId());
		return program;
	}

	public void deleteAll(List<MsoProgram> programs) {
		msoProgramDao.deleteAll(programs);
	}
		
	public void delete(MsoProgram program) {
		long id = program.getKey().getId();
		long channelId = program.getChannelId();
		//delete
		msoProgramDao.delete(program);
		log.info("delete program" + id);
		//channel's program count
		this.deleteCacheByChannel(program.getChannelId());
		MsoChannelManager channelMngr = new MsoChannelManager();
		channelMngr.calculateAndSaveChannelCount(program.getChannelId());
		//cache
		Cache cache = CacheFactory.get();		
		@SuppressWarnings("unchecked")
		List<Long> list = (List<Long>)cache.get(this.getCacheProgramListKey(channelId));
		if (list == null) { list = new ArrayList<Long>(); }
		cache.put(this.getCacheKey(program.getKey().getId()), null);
		if (list.contains(id))
			list.remove(id);	
		//!!! category channelCount should be taken care of too when the program count is 0 		
	}
			
	/**	 
	 * @@@ Cached
	 */
	public List<MsoProgram> findGoodProgramsByChannelId(long channelId) {
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		Cache cache = CacheFactory.get();
		//find from cache
		if (cache != null) {
			@SuppressWarnings("unchecked")
			List<Long> list = (ArrayList<Long>)cache.get(this.getCacheProgramListKey(channelId));
			//!!! flaw: bad if cache list is not empty but individual program is not in cache  
			if (list != null) {
				for (Long l : list) {
					MsoProgram p =  this.findById(l);
					if (p!= null) {programs.add(p);}
				}
				return programs;
			}
		}
		//find
		MsoChannelManager channelMngr = new MsoChannelManager();		
		MsoChannel c = channelMngr.findById(channelId);
		if (c == null)
			return null;
		programs = msoProgramDao.findGoodProgramsByChannelId(c);
		//store in cache
		if (cache != null) { this.storeInCache(cache, programs, channelId); }				
		return programs;
	}

	private void storeInCache(Cache cache, List<MsoProgram>programs, long channelId) {
		if (cache == null) {return;}
		//store individual program
		for (MsoProgram p : programs) {
			if (p.getStatus() != MsoProgram.STATUS_OK || p.getType() != MsoProgram.TYPE_VIDEO) {
				cache.put(this.getCacheKey(p.getKey().getId()), null);
			} else {
				cache.put(this.getCacheKey(p.getKey().getId()), p);
			}
		}
		
        //store a channel's program list, sorted by pubDate
		/*
		List<MsoProgram>goodList = msoProgramDao.findGoodProgramsByChannelId(channelId);
		List<Long> list = new ArrayList<Long>();
		for (MsoProgram p : goodList) {
			list.add(p.getKey().getId());
		}
		cache.put(this.getCacheProgramListKey(channelId), list);
		*/
	}
	
	/**
	 * @@@ Cached 
	 */
	/*
	public List<MsoProgram> findGoodProgramsByChannelIds(List<Long>channelIds) {
		log.info("requested channelIds size:" + channelIds.size());
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		List<Long> test = new ArrayList<Long>();
		test.addAll(channelIds); //!!!! test
		Cache cache = CacheFactory.get();
		//find from cache
		if (cache != null) {
			for (Long id : test) {
				@SuppressWarnings("unchecked")
				List<Long> list = (ArrayList<Long>)cache.get(this.getCacheProgramListKey(id));
				if (list != null) {
					for (Long l : list) {
						MsoProgram p =  this.findById(l);
						if (p!= null) {programs.add(p);}
					}
					channelIds.remove(id);					
				}
			}
		}
		log.info("remaining channel size not in the cache:" + channelIds.size());
		if (channelIds.size() > 0) {
			//find
			List<MsoProgram> list = msoProgramDao.findGoodProgramsByChannelIds(channelIds);
			//store in cache
			if (list.size() > 0) {
				programs.addAll(list);
				HashMap<Long, List<MsoProgram>> map = new HashMap<Long, List<MsoProgram>>();
				for (int i=0; i<list.size(); i++) {
					List<MsoProgram> temp = map.get(list.get(i).getChannelId());
					if (temp == null) {temp = new ArrayList<MsoProgram>();}
					temp.add(list.get(i));
					map.put(list.get(i).getChannelId(), temp);
				}				
				for (Long key : map.keySet()) {
					this.storeInCache(cache, map.get(key), key);				
			    }
			}			
		}

		return programs;
	}
	*/

	public List<MsoProgram> findSubscribedPrograms(long userId) {
		SubscriptionManager subService = new SubscriptionManager();			
		List<MsoChannel> channels = subService.findSubscribedChannels(userId, 0);
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		//List<Long> channelIds = new ArrayList<Long>();
		for (MsoChannel c : channels) {
			programs.addAll(this.findGoodProgramsByChannelId(c.getKey().getId()));
		}		
		//System.out.println(channelIds.size());
		//programs = this.findGoodProgramsByChannelIds(channelIds);
		return programs;
	}
	
	public MsoProgram findByStorageId(String storageId) {
		return msoProgramDao.findByStorageId(storageId);
	}
	
	/**
	 * @@@ Cached 
	 */
	public MsoProgram findById(long id) {
		//find from cache
		Cache cache = CacheFactory.get();
		String key = this.getCacheKey(id);
		if (cache != null) {
			MsoProgram program = (MsoProgram) cache.get(key);
			if (program != null) {
				return program;
			}
		}
		//find
		MsoProgram program = msoProgramDao.findById(id);
		//save in the cache
		if (cache != null && program != null) { cache.put(key, program);}
		return program;
	}
	
	public MsoProgram findByKey(Key key) {
		return msoProgramDao.findByKey(key);
	}
	
	public List<MsoProgram> list(int page, int limit, String sidx, String sord) {
		return msoProgramDao.list(page, limit, sidx, sord);
	}
	
	public List<MsoProgram> list(int page, int limit, String sidx, String sord, String filter) {
		return msoProgramDao.list(page, limit, sidx, sord, filter);
	}
	
	public int total() {
		return msoProgramDao.total();
	}
	
	public int total(String filter) {
		return msoProgramDao.total(filter);
	}
	
	public List<MsoProgram> findAllByChannelId(long channelId) {
		return msoProgramDao.findAllByChannelId(channelId);
	}

	public int findAndDeleteProgramsOlderThanMax(long channelId) {
		return msoProgramDao.findAndDeleteProgramsOlderThanMax(channelId);
	}
	
	public MsoProgram findOldestByChannelId(long channelId) {
		MsoProgram oldest = msoProgramDao.findOldestByChannelId(channelId); 
		log.info("find the oldest program:" + oldest.getKey().getId() + ";" + oldest.getName() + ";" + oldest.getStorageId() + ";" + oldest.getPubDate());		
		return oldest;
	}
	
	//example, program(1)
	private String getCacheKey(long id) {
		return "program(" + id + ")";		
	}

	//example, channel(1)program(1)
	private String getCacheProgramListKey(long channelId) {
		return "channel-programList(" + channelId + ")";		
	}	
	
	/*
	public String findCacheByChannel(long channelId) {
		Cache cache = CacheFactory.get();
		String listStr = "";
		String programStr = "";
		if (cache != null) {
			@SuppressWarnings("unchecked")
			List<Long> list = (ArrayList<Long>)cache.get(this.getCacheProgramListKey(channelId));
			if (list != null) {
				for (Long l : list) { listStr = listStr + l + "\t"; }
			}
			List<MsoProgram> programs = msoProgramDao.findGoodProgramsByChannelId(channelId);
			for (MsoProgram p : programs) {	
				MsoProgram cacheP = (MsoProgram) cache.get(this.getCacheKey(p.getKey().getId())); 
				if (cacheP != null) {
					programStr = programStr + cacheP.getKey().getId() + "\t" + cacheP.getChannelId() + "\t" + cacheP.getName() + "\n";
				}
			}			
		}	 		
		return "program list:\n" + listStr + "\nprogram cached:\n" + programStr; 		
	}
	*/
	
	public void cacheByChannelId(long channelId) {
		Cache cache = CacheFactory.get();
		if (cache != null) {
			this.findGoodProgramsByChannelId(channelId);
		}
	}
	
	public void deleteCacheByChannel(long channelId) {
		Cache cache = CacheFactory.get();
		List<MsoProgram> programs = this.findGoodProgramsByChannelId(channelId);
		if (cache != null) {
			for (MsoProgram c : programs) {				
				cache.remove(this.getCacheKey(c.getKey().getId()));
			}
			cache.remove(this.getCacheProgramListKey(channelId));
		}
	}
	
}
