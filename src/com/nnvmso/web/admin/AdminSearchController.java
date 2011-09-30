package com.nnvmso.web.admin;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.DbDumper;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.NnUserManager;

@Controller
@RequestMapping("admin/search")
public class AdminSearchController {

	protected static final Logger log = Logger.getLogger(AdminSearchController.class.getName());		
	
	@RequestMapping(value="create")
	public ResponseEntity<String> create(HttpServletRequest req) {
		
		DbDumper dumper = new DbDumper();
		@SuppressWarnings("rawtypes")
		
		List list = dumper.findAll(MsoChannel.class, "createDate");
		dumper.deleteAll(MsoChannel.class, list);
		
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findByEmail("mso@9x9.tv");		
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel c1 = new MsoChannel("hello", "this is hello", "", user.getKey().getId());				
		channelMngr.create(c1);

		MsoChannel c2 = new MsoChannel("break", "this is hello break", "", user.getKey().getId());				
		channelMngr.create(c2);

		MsoChannel c3 = new MsoChannel("friday", "tgi friday", "", user.getKey().getId());				
		channelMngr.create(c3);

		MsoChannel c4 = new MsoChannel("running", "marathon series", "", user.getKey().getId());				
		channelMngr.create(c4);
		
		String c5Name = "Finding america hd 720p video podcast";
		String c5Intro = "What if you had the chance to travel every major road in the USA? Ever wondered what the real face of America looks like that's not from a glossy and fake manufactured perspective? Can't afford or don't have time to travel as much as you would lik... ";
		MsoChannel c5 = new MsoChannel(c5Name, c5Intro, "", user.getKey().getId());
		channelMngr.create(c5);				

		String c6Name = "大華嚴寺";
		String c6Intro = "本頻道以大華嚴寺之海雲繼夢和上之講經開示影音為主；並輔以大華嚴寺之法會、活動動態紀實與相關影音。";
		MsoChannel c6 = new MsoChannel(c6Name, c6Intro, "", user.getKey().getId());
		channelMngr.create(c6);
		
		return NnNetUtil.textReturn("create");
	}

	@RequestMapping(value="test")
	public ResponseEntity<String> search(@RequestParam(value="text", required=false) String search) {
		/*
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		List<MsoChannel> searchResults = SearchJanitor.searchChannelEntries(search, pm);
		String text = "";
		for (MsoChannel c : searchResults) {
			text += "c name:" + c.getName() + ";" + c.getIntro() + "\n";
		}
		*/
		return NnNetUtil.textReturn("hello");
	}
	
}
