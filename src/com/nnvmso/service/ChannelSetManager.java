package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.ChannelSetDao;
import com.nnvmso.lib.CacheFactory;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannelSet;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ChannelSetChannel;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;

@Service
public class ChannelSetManager {
	
	protected static final Logger logger = Logger.getLogger(ChannelSetManager.class.getName());
	
	private ChannelSetDao channelSetDao = new ChannelSetDao();
		
	public void create(ChannelSet channelSet, List<MsoChannel> channels) {
		channelSet.setChannelCount(channels.size());
		this.create(channelSet);		
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();		
		if (this.findByName(channelSet.getName()) != null) {
			logger.warning("channelSet already exists, name: " + channelSet.getName());
		}
		for (MsoChannel channel : channels) {
			ChannelSetChannel csc = new ChannelSetChannel(channelSet.getKey().getId(), channel.getKey().getId(), channel.getSeq());
			cscMngr.create(csc);
		}
		this.resetRelatedChannelCnt(channelSet, channels);
	}

	public void create(ChannelSet channelSet) {
		channelSet.setNameSearch(channelSet.getName().trim().toLowerCase());
		if (channelSet.getBeautifulUrl() != null)
			channelSet.setBeautifulUrl(channelSet.getBeautifulUrl().toLowerCase());
		Date now = new Date();
		channelSet.setCreateDate(now);
		channelSet.setUpdateDate(now);
		channelSetDao.save(channelSet);
	}

	public List<ChannelSet> findByMso(long msoId) {
		return channelSetDao.findByMso(msoId);
	}
	
	public ChannelSet findByBeautifulUrl(String url) {
		return channelSetDao.findByBeautifulUrl(url.toLowerCase());
	}

	public ChannelSet findByLangAndSeq(String lang, String seq) {
		return channelSetDao.findByLangAndSeq(lang, Short.parseShort(seq));
	}
	
	public int total() {
		return channelSetDao.total();
	}
	
	public int total(String filter) {
		return channelSetDao.total(filter);
	}
	
	public List<ChannelSet> list(int page, int limit, String sidx, String sord) {
		return channelSetDao.list(page, limit, sidx, sord);
	}
	
	public List<ChannelSet> list(int page, int limit, String sidx, String sord, String filter) {
		return channelSetDao.list(page, limit, sidx, sord, filter);
	}
	
	public List<ChannelSet> findFeaturedSetsByMso(Mso mso) {
		return channelSetDao.findFeaturedSetsByMso(mso);
	}
	
	public List<ChannelSet> findFeaturedSets(String lang) {
		return channelSetDao.findFeaturedSets(lang);
	}
	
	public void resetRelatedChannelCnt(ChannelSet channelSet, List<MsoChannel> channels) {
		if (channels == null)
			channels = this.findChannelsBySet(channelSet);
		CategoryManager categoryMngr = new CategoryManager();
		CategoryChannelSetManager ccsMngr = new CategoryChannelSetManager();
		List<CategoryChannelSet> list = ccsMngr.findAllByCategoryId(channelSet.getKey().getId());
		for (CategoryChannelSet ccs : list) {
			Category c = categoryMngr.findById(ccs.getCategoryId());
			if (c != null) {
				c.setChannelCount(c.getChannelCount() + channels.size());
				categoryMngr.save(c);
			}
		}
	}
	
	public void delete(ChannelSet cs) {
		channelSetDao.delete(cs);
	}
	
	public void saveAll(List<ChannelSet> sets) {
		channelSetDao.saveAll(sets);
	}
	
	public ChannelSet save(ChannelSet channelSet) {	
		//NOTE check name existence if needed
		channelSet.setUpdateDate(new Date());
		//shouldn't be too many, should be ok
		List<MsoChannel> channels = this.findChannelsBySet(channelSet);
		channelSet.setChannelCount(channels.size());
		channelSetDao.save(channelSet);
		this.resetRelatedChannelCnt(channelSet, channels);
		return channelSet;
	}
	
	public ChannelSet findByName(String name) {
		return channelSetDao.findByNameSearch(name.trim().toLowerCase());
	}
	
	public List<MsoChannel> findChannelsBySet(ChannelSet set) {
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		MsoChannelManager channelMngr = new MsoChannelManager();		
		List<ChannelSetChannel> cscs = cscMngr.findByChannelSet(set); 
		ArrayList<MsoChannel> results = new ArrayList<MsoChannel>();
		
		int i=1;
		for (ChannelSetChannel csc : cscs) {
			MsoChannel channel = channelMngr.findById(csc.getChannelId());
			if (channel != null) {
				if (set.isFeatured())
					channel.setSeq(csc.getSeq());
				else 
					channel.setSeq(i++);
				results.add(channel);
			}
		}
		return results;
	}

	public List<ChannelSetChannel> findChannelSetChannelsBySet(ChannelSet set) {
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();		
		List<ChannelSetChannel> cscs = cscMngr.findByChannelSet(set); 
		return cscs;		
	}
	
	
	public ChannelSet findById(long channelSetId) {
		return channelSetDao.findById(channelSetId);
	}

	public List<ChannelSet> findAllByLang(String lang) {
		return channelSetDao.findAllByLang(lang);
	}

	public List<ChannelSet> findAll() {
		return channelSetDao.findAll();
	}
	
	public List<ChannelSet> findAllByChannelSetIds(List<Long> channelSetIdList) {
		List<ChannelSet> results = new ArrayList<ChannelSet>();
		for (Long channelSetId : channelSetIdList) {
			ChannelSet channelSet = this.findById(channelSetId);
			if (channelSet != null) results.add(channelSet);
		}
		return results;
	}
	
	public List<ChannelSet> findAllPublicByChannelSetIds(
			List<Long> channelSetIdList) {
		
		List<ChannelSet> results = new ArrayList<ChannelSet>();
		List<ChannelSet> candidates = this.findAllByChannelSetIds(channelSetIdList);
		for (ChannelSet set : candidates) {
			if (set.isPublic()) {
				results.add(set);
			}
		}
		return results;
	}
	
	public List<ChannelSet> findAllSystemChannelSets() {
		
		List<ChannelSet> results = new ArrayList<ChannelSet>();
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		String cacheIdString = "System.ChannelSets(sortby=lang)";
		Cache cache = CacheFactory.get();
		if (cache != null) {
			@SuppressWarnings("unchecked")
			List<ChannelSet> cached = (List<ChannelSet>)cache.get(cacheIdString);
			if (cached != null && cached.size() > 0) {
				logger.info("get system sets from cache");
				return cached;
			}
		}
		
		MsoManager msoMngr = new MsoManager();
		Mso nnMso = msoMngr.findNNMso();
		
		results = ownershipMngr.findOwnedChannelSetsByMsoId(nnMso.getKey().getId());
		
		return results;
	}
	
}
