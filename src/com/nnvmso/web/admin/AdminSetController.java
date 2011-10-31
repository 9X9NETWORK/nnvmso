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
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ChannelSetChannel;
import com.nnvmso.model.ContentOwnership;
import com.nnvmso.model.LangTable;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
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
			output += cs.getKey().getId() + "\t" + cs.getName();
		}
		return NnNetUtil.textReturn("OK");
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
			                             @RequestParam(required=false) String seqs) {
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
		channelSetMngr.create(channelSet, channels);						
		
		//channelSet ownership
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		ownershipMngr.create(new ContentOwnership(), mso, channelSet);
		
		return NnNetUtil.textReturn("OK");
	}	
	
	
}
