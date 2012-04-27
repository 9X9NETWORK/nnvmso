package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.DataStoreCache;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnSetDao;
import com.nncloudtv.dao.NnSetToNnChannelDao;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.PMF;
import com.nncloudtv.lib.QueueMessage;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnSet;
import com.nncloudtv.model.NnSetToNnChannel;

@Service
public class NnSetManager {
	
	protected static final Logger log = Logger.getLogger(NnSetManager.class.getName());
	
	private NnSetDao setDao = new NnSetDao();
	
	public NnSet save(NnSet set) {	
		Date now = new Date();
		if (set.getCreateDate() == null)			
			set.setCreateDate(now);
		set.setUpdateDate(now);
		setDao.save(set);
		
		return set;
	}
	
	public void saveAll(List<NnSet> sets) {
		for(NnSet set : sets) {
			save(set);
		}
	}
	
	public void delete(NnSet cs) {
		setDao.delete(cs);
	}
	
	public NnSet create(NnSet set, List<NnChannel> channels) {		
		if (this.findByName(set.getName()) != null) {
			log.warning("set already exists, name: " + set.getName());
			return null;
		}
		Date now = new Date();
		set.setCreateDate(now);
		set.setUpdateDate(now);		
		setDao.save(set);
		this.addChannels(set, channels);
		return set;
	}

	public void deleteChannel(NnSet set, NnChannel channel) {
		NnSetToNnChannelDao dao = new NnSetToNnChannelDao();
		NnSetToNnChannel sToC = dao.findBySetAndChannel(set.getId(), channel.getId());
		if (dao.findBySetAndChannel(set.getId(), channel.getId()) != null) {
			dao.delete(sToC);
		}
		if (channel.getStatus() == NnChannel.STATUS_SUCCESS && 
			channel.isPublic() && 
			MsoConfigManager.isQueueEnabled(false)) {
			this.processChannelRelatedCounter(set, -1);
		}
	}
	
	public void processChannelRelatedCounter(NnSet set, int cnt) {
		//setDao.evictAll();
		PersistenceManagerFactory pmf = PMF.getContent(); 
		DataStoreCache cache = pmf.getDataStoreCache();
		cache.evictAll();
		
		log.info("<<<<<<<<< original:" + set.getChannelCnt());
		log.info("<<<<<<<<< cnt:" + cnt);
		int count = set.getChannelCnt() + cnt;
		log.info("<<<<<<<<< add:" + count);
		if (count > 0) {
			//change set counter			
			set.setChannelCnt(count);
			setDao.save(set);
			log.info("<<<<<<<<< save:" + set.getId());
			//change category counter
			CategoryManager catMngr = new CategoryManager();
			catMngr.changeChannelCntBySet(set, cnt);
		}
	}
	
	public void addChannels(NnSet set, List<NnChannel> channels) {		
		NnSetToNnChannelDao dao = new NnSetToNnChannelDao();
		int newChCnt = 0;
		for (NnChannel channel : channels) {
			if (dao.findBySetAndChannel(set.getId(), channel.getId()) == null) {
				NnSetToNnChannel sToC = new NnSetToNnChannel(set.getId(), channel.getId(), channel.getSeq()); 
				dao.save(sToC);
				if (channel.getStatus() == NnChannel.STATUS_SUCCESS && channel.isPublic()) {
					newChCnt++;
				}				
			}
		}
		PersistenceManagerFactory pmf = PMF.getContent(); 
		DataStoreCache cache = pmf.getDataStoreCache();
		cache.evictAll();

		Object[] obj = new Object[2];
		if (newChCnt > 0) {
			if (MsoConfigManager.isQueueEnabled(false)) {
				setDao.evict(set);
				log.info("throw to queue <<<<<<<" + set.getChannelCnt());
				obj[0] = set;
				obj[1] = newChCnt;
		        new QueueMessage().fanout("localhost",QueueMessage.SET_CUD_RELATED, obj);
			} else {
				this.processChannelRelatedCounter(set, newChCnt);
				log.info("process channel related");
			}
		}
	}

	public List<NnSet> findSetsByChannel(long channelId) {
		NnSetToNnChannelDao dao = new NnSetToNnChannelDao();
		List<NnSetToNnChannel> list = dao.findByChannel(channelId);
		List<NnSet> result = new ArrayList<NnSet>();
		for (NnSetToNnChannel sToC : list) {
			NnSet set = setDao.findById(sToC.getSetId());
			if (set != null)
				result.add(set);
		}
		return result;
	}
	
	public List<NnSetToNnChannel> findNnSetToNnChannelsBySet(long setId) {
		NnSetToNnChannelDao dao = new NnSetToNnChannelDao();
		return dao.findBySet(setId);
	}
	
	public List<NnSet> findFeaturedSets(String lang) {
		return setDao.findFeaturedSets(lang);
	}
		
	public NnSet findBybeautifulUrl(String url) {
		return setDao.findByBeautifulUrl(url);
	}
	
	public List<NnSet> findByLang(String lang) {
		return setDao.findByLang(lang);
	}
	
	public NnSet findByLangAndSeq(String lang, String seq) {
		return setDao.findByLangAndSeq(lang, Short.parseShort(seq));
	}
	
	public NnSet findByName(String name) {
		return setDao.findByName(name.trim());
	}
	
	public List<NnSet> findAll() {
		return setDao.findAll();
	}

	//!!!
	public List<NnSet> findByIds(List<Long> ids) {
		List<NnSet> results = new ArrayList<NnSet>();
		for (Long id : ids) {
			NnSet s = this.findById(id);
			if (s != null) results.add(s);
		}
		return results;
	}
	
	public List<NnSet> findPublicByIds(List<Long> ids) {
		List<NnSet> results = new ArrayList<NnSet>();
		List<NnSet> candidates = this.findByIds(ids);
		for (NnSet set : candidates) {
			if (set.isPublic()) {
				results.add(set);
			}
		}
		return results;
	}
	
	
	public List<NnChannel> findPlayerChannelsById(long setId) {
		NnSetChannelManager scMngr = new NnSetChannelManager();
		NnChannelManager channelMngr = new NnChannelManager();		
		List<NnSetToNnChannel> relations = scMngr.findBySet(setId);
		ArrayList<NnChannel> results = new ArrayList<NnChannel>();
		
		for (NnSetToNnChannel sToC : relations) {
			NnChannel c = channelMngr.findById(sToC.getChannelId());
			if (c != null) {
				if (c.getStatus() == NnChannel.STATUS_SUCCESS && c.isPublic()) {
					c.setSeq(sToC.getSeq());
					results.add(c);
				}
			}
		}
		return results;
	}

	public NnSet findById(long setId) {
		return setDao.findById(setId);
	}

	//!!! cache
	public List<NnSet> findAllSystemSets() {		
		List<NnSet> results = new ArrayList<NnSet>();
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		String cacheIdString = "System.NnSets(sortby=lang)";
		@SuppressWarnings("unchecked")
		List<NnSet> cached = (List<NnSet>)CacheFactory.get(cacheIdString);		
		if (cached != null && cached.size() > 0) {
			log.info("get system sets from cache");
			return cached;
		}
		MsoManager msoMngr = new MsoManager();
		Mso nnMso = msoMngr.findNNMso();		
		results = ownershipMngr.findOwnedSetsByMso(nnMso.getId());
		return results;
	}

	public List<NnChannel> findChannelsById(long setId) {
		NnSetToNnChannelDao sToCDao = new NnSetToNnChannelDao();
		NnChannelManager channelMngr = new NnChannelManager();		
		List<NnSetToNnChannel> list = sToCDao.findBySet(setId); 
		ArrayList<NnChannel> results = new ArrayList<NnChannel>();
		
		for (NnSetToNnChannel nToC : list) {
			NnChannel channel = channelMngr.findById(nToC.getChannelId());
			if (channel != null) {
				channel.setSeq(nToC.getSeq());
				results.add(channel);
			}
		}
		return results;
	}
	
	public List<NnChannel> findPublicChannelsById(long setId) {
		NnSetToNnChannelDao sToCDao = new NnSetToNnChannelDao();
		NnChannelManager channelMngr = new NnChannelManager();		
		List<NnSetToNnChannel> list = sToCDao.findBySet(setId); 
		ArrayList<NnChannel> results = new ArrayList<NnChannel>();
		
		for (NnSetToNnChannel nToC : list) {
			NnChannel channel = channelMngr.findById(nToC.getChannelId());
			if (channel != null && channel.getStatus() == NnChannel.STATUS_SUCCESS && channel.getProgramCnt() > 0 && channel.isPublic()) {
				channel.setSeq(nToC.getSeq());
				results.add(channel);
			}
		}
		return results;
	}
	
	public int total() {
		return setDao.total();
	}
	
	public int total(String filter) {
		return setDao.total(filter);
	}
	
	public List<NnSet> list(int page, int limit, String sidx, String sord) {
		return setDao.list(page, limit, sidx, sord);
	}
	
	public List<NnSet> list(int page, int limit, String sidx, String sord, String filter) {
		return setDao.list(page, limit, sidx, sord, filter);
	}
	
}
