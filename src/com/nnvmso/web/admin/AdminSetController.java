package com.nnvmso.web.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ContentOwnership;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
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
    
	@RequestMapping("create")
	public ResponseEntity<String> create(@RequestParam(required=false) String msoName, 
			                             @RequestParam(required=false) String setName,
			                             @RequestParam(required=false) String setDesc,
			                             @RequestParam(required=false) String channelIds,
			                             @RequestParam(required=false) String seqs) {
		//set info
		Mso mso = new MsoManager().findByName(msoName);
		ChannelSetManager channelSetMngr = new ChannelSetManager();
		ChannelSet channelSet = new ChannelSet(mso.getKey().getId(), setName, setDesc, true);
		channelSet.setDefaultUrl(setName); 
		channelSet.setBeautifulUrl(setName);
		//related channels
		MsoChannelManager channelMngr = new MsoChannelManager();
		String[] chArr = channelIds.split(",");
		String[] seqArr = seqs.split(",");
		List<Long> list = new ArrayList<Long>();
		for (int i=0; i<chArr.length; i++) { list.add(Long.valueOf(chArr[i]));}
		List<MsoChannel> channels = channelMngr.findAllByChannelIds(list);
		System.out.println("channels size found for channel set: " + channels.size());
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
