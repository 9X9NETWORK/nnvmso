package com.nnvmso.web.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.lib.YouTubeLib;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannelSet;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ChannelSetChannel;
import com.nnvmso.model.ContentOwnership;
import com.nnvmso.model.LangTable;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.CategoryChannelSetManager;
import com.nnvmso.service.CategoryManager;
import com.nnvmso.service.ChannelSetChannelManager;
import com.nnvmso.service.ChannelSetManager;
import com.nnvmso.service.ContentOwnershipManager;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;

@Controller
@RequestMapping("admin/set")
public class AdminSetController {
	protected static final Logger logger = Logger.getLogger(AdminSetController.class.getName());
	
	public final ChannelSetManager setMngr;
	
	@Autowired
	public AdminSetController(ChannelSetManager setMngr) {
		this.setMngr = setMngr;		
	}

	@RequestMapping("deleteMso")
	public ResponseEntity<String> deleteMso(@RequestParam(required=false) long id) {					
		ChannelSetManager csMngr = new ChannelSetManager();
		List<ChannelSet> sets = csMngr.findMsoSets(id);
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		for (ChannelSet cs : sets) {
			List<ChannelSetChannel> list = cscMngr.findByChannelSetId(cs.getKey().getId());
			cscMngr.deleteAll(list);
			csMngr.delete(cs);		
		}
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping("changeSet")
	public ResponseEntity<String> changeSet() {
		String[] urls = {
				"http://www.youtube.com/user/achun5",
				"http://www.youtube.com/user/luckyboommini",
				"http://www.youtube.com/user/wondergirls",
				"http://www.youtube.com/user/beyonceVEVO",
				"http://www.youtube.com/user/linkinparktv",
				"http://www.youtube.com/user/ladygagavevo",
				"http://www.youtube.com/user/KatyPerryVEVO",
				"http://www.youtube.com/user/CAguileraVevo",
				"http://www.youtube.com/user/RihannaVevo",
		};
		ChannelSetManager setMngr = new ChannelSetManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		ChannelSet set = setMngr.findByName("本周主打星");
		if (set != null) {
			List<ChannelSetChannel> list = cscMngr.findByChannelSetId(set.getKey().getId());
			cscMngr.deleteAll(list);
			List<MsoChannel> channels = new ArrayList<MsoChannel>();
			for (int i=0; i<urls.length; i++) {
				String checkedUrl = YouTubeLib.formatCheck(urls[i]);
				MsoChannel c = channelMngr.findBySourceUrlSearch(checkedUrl);
				channels.add(c);
			}
			int i=1;
			for (MsoChannel c : channels) {			
				ChannelSetChannel csc = new ChannelSetChannel(set.getKey().getId(), c.getKey().getId(), i);
				i++;
				cscMngr.save(csc);
			}
		}
		return NnNetUtil.textReturn("OK");
		
	}
	
	@RequestMapping("createBatch")
	public ResponseEntity<String> createBatch() {
		String name="Good TV 好消息";
		String lang="zh";
		String desc="Good TV";
		String cname="非營利組織";
		
		String[] urls = {
				"http://www.youtube.com/user/goodtv#grid/user/FBE16B28C166951F",
				"http://www.youtube.com/user/goodtv#grid/user/A95700BA527EA728",
				"http://www.youtube.com/user/goodtv#grid/user/E9CF038F35D6EEBC",
				"http://www.youtube.com/user/goodtv#grid/user/6B8141F28B843D88",
				"http://www.youtube.com/user/goodtv#grid/user/4554E7A23F1530BF",
				"http://www.youtube.com/user/goodtv#grid/user/CF53AFA4B1499986",
				"http://www.youtube.com/user/goodtv#grid/user/F23F18DCC8C8A819",
				"http://www.youtube.com/user/goodtv#grid/user/0F10FA0EAD8B0875",
		};
		MsoChannelManager channelMngr = new MsoChannelManager();
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		for (int i=0; i<urls.length; i++) {
			String checkedUrl = YouTubeLib.formatCheck(urls[i]);
			MsoChannel c = channelMngr.findBySourceUrlSearch(checkedUrl);
			c.setSeq(i+1);
			channels.add(c);
		}
		String output = "";
		for (MsoChannel c : channels) {
			output += c.getKey().getId() + "\t" + c.getSourceUrl() + "\n";
		}
		Mso mso = new MsoManager().findNNMso();
		ChannelSetManager channelSetMngr = new ChannelSetManager();
		ChannelSet channelSet = new ChannelSet(mso.getKey().getId(), name, desc, true);
		channelSet.setDefaultUrl(name); 
		channelSet.setBeautifulUrl(name);
		//related channels
		channelSet.setLang(lang);
		channelSetMngr.create(channelSet, channels);						
		
		//category and set		
		CategoryChannelSetManager cscMngr = new CategoryChannelSetManager();
		CategoryManager cMngr = new CategoryManager();
		Category c = cMngr.findByName(cname);
		CategoryChannelSet csc = new CategoryChannelSet(c.getKey().getId(), channelSet.getKey().getId());
		cscMngr.save(csc);		
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping("recommendedChange")
	public ResponseEntity<String> recommended(
			@RequestParam(required=false) String lang,
			@RequestParam(required=false) String ids,
			@RequestParam(required=false) String seqs) {

		String[] id = ids.split(",");
		String[] seq = seqs.split(",");
		
		//either turn off a recommended
		//if it's on, then re-arrange the sequence of everything
		ChannelSetManager channelSetMngr = new ChannelSetManager();
		List<ChannelSet> list = channelSetMngr.findFeaturedSets(lang);
		for (ChannelSet cs : list) {
			cs.setFeatured(false);
		}
		channelSetMngr.saveAll(list);		
		list = new ArrayList<ChannelSet>();
		for (int i=0; i<id.length; i++) {
			ChannelSet cs = channelSetMngr.findById(Long.parseLong(id[i]));
			cs.setSeq(Short.parseShort(seq[i]));
			cs.setFeatured(true);
			list.add(cs);
		}
		channelSetMngr.saveAll(list);
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping("recommendedList")
	public ResponseEntity<String> recommendedList() {							
		ChannelSetManager channelSetMngr = new ChannelSetManager();
		List<ChannelSet> list = new ArrayList<ChannelSet>();
		list.addAll(channelSetMngr.findFeaturedSets(LangTable.LANG_EN));
		list.addAll(channelSetMngr.findFeaturedSets(LangTable.LANG_ZH));
		String output = "";
		for (ChannelSet cs : list) {
			output += cs.getKey().getId() + "\t" + cs.getName() + "\n";
		}
		return NnNetUtil.textReturn(output);
	}

	@RequestMapping("delete")
	public ResponseEntity<String> delete(
			@RequestParam(required=false) long id) {		
		ChannelSetManager csMngr = new ChannelSetManager();
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		ChannelSet cs = csMngr.findById(id);		
		List<ChannelSetChannel> list = cscMngr.findByChannelSetId(cs.getKey().getId());
		cscMngr.deleteAll(list);		
		csMngr.delete(cs);		
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping("edit")
	public ResponseEntity<String> edit(
			@RequestParam(required=false) long id,
			@RequestParam(required=false) String name,
            @RequestParam(required=false) String desc,
            @RequestParam(required=false) String channelIds,
            @RequestParam(required=false) String seqs) {
		ChannelSetManager csMngr = new ChannelSetManager();
		MsoChannelManager cMngr = new MsoChannelManager();
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		ChannelSet cs = csMngr.findById(id);
		if (name != null) cs.setName(name);
		if (desc != null) cs.setIntro(desc);
		csMngr.save(cs);		
		if (channelIds == null) 
			return NnNetUtil.textReturn("OK");
		
		String[] chId = channelIds.split(",");
		String[] chSeq = seqs.split(",");		
					
		List<ChannelSetChannel> list = cscMngr.findByChannelSetId(cs.getKey().getId());
		cscMngr.deleteAll(list);
		list = new ArrayList<ChannelSetChannel>();
		for (int i=0; i<chId.length; i++) {
			MsoChannel c = cMngr.findById(Long.parseLong(chId[i]));			
			ChannelSetChannel csc = new ChannelSetChannel(cs.getKey().getId(), c.getKey().getId(), Short.parseShort(chSeq[i]));
			csc.setCreateDate(new Date());
			list.add(csc);
		}
		cscMngr.saveAll(list);
		return NnNetUtil.textReturn("OK");
	}
	
	@RequestMapping("create")
	public ResponseEntity<String> create(@RequestParam(required=false) String name, 			                             
			                             @RequestParam(required=false) String desc,
			                             @RequestParam(required=false) String channelIds,
			                             @RequestParam(required=false) String seqs,
			                             @RequestParam(required=false) String lang,
			                             @RequestParam(required=false) String categoryIds) {
		//set info
		Mso mso = new MsoManager().findNNMso();
		ChannelSetManager channelSetMngr = new ChannelSetManager();
		ChannelSet channelSet = new ChannelSet(mso.getKey().getId(), name, desc, true);
		channelSet.setDefaultUrl(name); 
		channelSet.setBeautifulUrl(name);
		//related channels
		MsoChannelManager channelMngr = new MsoChannelManager();
		String[] chArr = channelIds.split(",");
		String[] seqArr = seqs.split(",");
		List<Long> list = new ArrayList<Long>();
		for (int i=0; i<chArr.length; i++) { list.add(Long.valueOf(chArr[i]));}
		List<MsoChannel> channels = channelMngr.findAllByChannelIds(list);
		for (int i=0; i<channels.size(); i++) {
			channels.get(i).setSeq(Short.valueOf(seqArr[i]));
		}
		channelSet.setLang(lang);
		channelSetMngr.create(channelSet, channels);						
		
		//channelSet ownership
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		ownershipMngr.create(new ContentOwnership(), mso, channelSet);
		
		//category and set
		String[] categoryArr = categoryIds.split(",");
		CategoryChannelSetManager cscMngr = new CategoryChannelSetManager();
		CategoryManager cMngr = new CategoryManager();
		List<Category> categories = new ArrayList<Category>();
		for (int i=0; i<categoryArr.length; i++) {
			Category c = cMngr.findById(Long.parseLong(categoryArr[i]));
			categories.add(c);
		}
		for (Category c : categories) {
			CategoryChannelSet csc = new CategoryChannelSet(c.getKey().getId(), channelSet.getKey().getId());			
			cscMngr.save(csc);		
		}
		return NnNetUtil.textReturn("OK");
	}	

	@RequestMapping("createTest")
	public ResponseEntity<String> createTest(@RequestParam(required=false) String name, 			                             
			                             @RequestParam(required=false) String desc,
			                             @RequestParam(required=false) String channelIds,
			                             @RequestParam(required=false) String seqs,
			                             @RequestParam(required=false) String lang,
			                             @RequestParam(required=false) String categoryIds) {
		//set info
		name="Most Popular this Week";
		desc = "All your favorites in one place!";
		
		Mso mso = new MsoManager().findNNMso();
		ChannelSetManager channelSetMngr = new ChannelSetManager();
		ChannelSet channelSet = new ChannelSet(mso.getKey().getId(), name, desc, true);
		channelSet.setDefaultUrl(name); 
		channelSet.setBeautifulUrl(name);
		//related channels
		MsoChannelManager channelMngr = new MsoChannelManager();
		String[] urls = {
				"http://www.youtube.com/user/TEDtalksDirector",
				"http://www.youtube.com/user/AtGoogleTalks",
				"http://www.youtube.com/user/ReutersVideo",
				"http://www.youtube.com/user/AssociatedPress",
				"http://www.youtube.com/user/JimmyKimmelLive",
				"http://www.youtube.com/user/journeymanpictures",
				"http://www.youtube.com/user/NASAtelevision",
				"http://www.youtube.com/user/NewYorkerDotCom",
				"http://www.youtube.com/user/ComputerHistory",
				"http://www.youtube.com/user/animalplanetTV",                                              
				"http://www.youtube.com/user/NBA",
				"http://www.youtube.com/user/bigthink",
				"http://www.youtube.com/user/ResearchChannel",
				"http://www.youtube.com/user/boyceavenue",
				"http://www.youtube.com/user/growingyourgreens",
				"http://www.youtube.com/user/ThirteenWNET",
				"http://www.youtube.com/user/Autoexpress",
				"http://www.youtube.com/user/PulitzerCenter",
				"http://www.youtube.com/user/richarddawkinsdotnet",
				"http://www.youtube.com/user/FunnyorDie",
				"http://www.youtube.com/reelzchannel",
				"http://www.youtube.com/user/realannoyingorange",
				"http://www.youtube.com/user/FoodNetworkTV",
				"http://www.youtube.com/user/japanesepod101",
				"http://www.youtube.com/user/NationalGeographic",
				"http://www.youtube.com/user/CorridorDigital",
				"http://www.youtube.com/user/trendhuntertv"				
		};
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		int j=1;
		for (String url : urls) {
			String checkedUrl = YouTubeLib.formatCheck(url);
			MsoChannel c = channelMngr.findBySourceUrlSearch(checkedUrl);
			c.setSeq(j);
			channels.add(c);
			j++;
		}
		channelSet.setLang(lang);
		channelSetMngr.create(channelSet, channels);						
		
		//channelSet ownership
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		ownershipMngr.create(new ContentOwnership(), mso, channelSet);
		
		//category and set
		String[] categoryArr = categoryIds.split(",");
		CategoryChannelSetManager cscMngr = new CategoryChannelSetManager();
		CategoryManager cMngr = new CategoryManager();
		List<Category> categories = new ArrayList<Category>();
		for (int i=0; i<categoryArr.length; i++) {
			Category c = cMngr.findById(Long.parseLong(categoryArr[i]));
			categories.add(c);
		}
		for (Category c : categories) {
			CategoryChannelSet csc = new CategoryChannelSet(c.getKey().getId(), channelSet.getKey().getId());			
			cscMngr.save(csc);		
		}
		return NnNetUtil.textReturn("OK");
	}	
	
	
}
