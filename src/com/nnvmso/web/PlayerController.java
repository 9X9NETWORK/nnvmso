package com.nnvmso.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.CookieGenerator;

import com.nnvmso.lib.*;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.ChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.ProgramManager;
import com.nnvmso.service.SubscriptionManager;

@Controller
@RequestMapping("player")
public class PlayerController {

	@RequestMapping("zooatomics")
	public String zooatomics(HttpServletRequest req, HttpServletResponse resp) {
		if (CookieHelper.getCookie(req, "platform") == null) {
			CookieHelper.setCookie(resp, "platform", "gae");
		}
		return "player/zooatomics";
	}
		
	@RequestMapping("embed")
	public String embeded(Model model) {
		MsoManager service = new MsoManager();
		Mso mso = service.findByEmail("default_mso@9x9.com");
		model.addAttribute("msoKey", NnLib.getKeyStr(mso.getKey()));
		return ("player/embed");
	}
	
	@RequestMapping(value="channelBrowse")
	public ResponseEntity<String> channelBrowse() {
		ChannelManager channelMngr = new ChannelManager();
		List<MsoChannel> channels = channelMngr.findAllPublic();
		String output ="";
		for (MsoChannel c:channels) {
			String[] ori = {Short.toString(c.getSeq()), String.valueOf(c.getKey().getId()), c.getName(), c.getImageUrl()};
			output = output + PlayerLib.getTabDelimitedStr(ori);			
			output = output + "\n";
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
	}

	@RequestMapping(value="guest_register")
	public ResponseEntity<String> guestRegister(HttpServletResponse resp) {
		//register
		NnUserManager userMngr = new NnUserManager();
		NnUser user = new NnUser("guest@9x9.com");
		user.setPassword("guest");
		MsoManager msoMngr = new MsoManager();
		msoMngr.findByEmail("default_mso@9x9.com");
		Mso mso = new Mso();		
		userMngr.create(user, mso);
		//subscribe
		SubscriptionManager sMngr = new SubscriptionManager();
		ChannelManager cMngr = new ChannelManager();
		MsoChannel system = cMngr.findSystemChannels().get(0);
		sMngr.channelSubscribe(user, system, (short)1);
		//set cookie
		CookieGenerator cookie = new CookieGenerator();		
		cookie.setCookieName("user");
		cookie.addCookie(resp, NnLib.getKeyStr(user.getKey()));
		//return
		String output = NnLib.getKeyStr(user.getKey());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}
	
	//http://localhost:8888/player/subscribe?user=aghubmUydm1zb3IMCxIGTm5Vc2VyGDkM&channel=51&grid=2
	@RequestMapping(value="subscribe")
	public ResponseEntity<String> subscribe(@RequestParam(value="user") String userKey, @RequestParam(value="channel") long channelId, @RequestParam(value="grid") short grid ) {
		//subscribe	
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findByKey(userKey);
		ChannelManager channelMngr = new ChannelManager();
		MsoChannel channel = channelMngr.findById(channelId);
		SubscriptionManager sMngr = new SubscriptionManager();
		sMngr.channelSubscribe(user, channel, grid);
		//return
		String output = "OK";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}

	@RequestMapping(value="channelLineup")
	public ResponseEntity<String> channelLineup(@RequestParam(value="user") String userKey) {
		SubscriptionManager subMngr = new SubscriptionManager();
		NnUserManager userService = new NnUserManager();
		NnUser user = userService.findByKey(userKey);
		List<MsoChannel> channels = subMngr.findSubscribedChannels(user);
		String output = "";
		for (MsoChannel c:channels) {
			String[] ori = {Short.toString(c.getGrid()), String.valueOf(c.getKey().getId()), c.getName(), c.getImageUrl()};
			output = output + PlayerLib.getTabDelimitedStr(ori);			
			output = output + "\n";
		}				
		//return
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
		
	}
	/**
	 * Get a user's subscribed channels
	 * 
	 * Example: http://localhost:8888/player/channelLineup?user=aghubmUzdm1zb3INCxIGTm5Vc2VyGKsEDA
	 *  
	 * @param  user: key
	 * @return Channel info. 
	 * 		   Fields are tab delimited.           
	 * 		   Fields sequence: sequence, ChannelId, ChannelName, ChannelThumbnailUrl 
	 */				
	@RequestMapping(value="channelLineupByMso")
	public ResponseEntity<String> channelLineupByMso(@RequestParam(value="user") String userKey) {
		SubscriptionManager subMngr = new SubscriptionManager();
		NnUserManager userService = new NnUserManager();
		NnUser user = userService.findByKey(userKey);
		subMngr.subscribe(user); 
		List<MsoChannel> channels = subMngr.findSubscribedChannels(user);
		String output = "";
		for (MsoChannel c:channels) {
			String[] ori = {Short.toString(c.getSeq()), String.valueOf(c.getKey().getId()), c.getName(), c.getImageUrl()};
			output = output + PlayerLib.getTabDelimitedStr(ori);			
			output = output + "\n";
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
	}
	
	/**
	 * Get curator information based on curator key
	 * 
	 * @param  curator: MsoKey
	 * @return Curator info.
	 * 		   Fields are tab delimited.           
	 * 		   Fields sequence: msoName, msoIntro, msoThumbnailUrl
	 */			
	public @ResponseBody String curatorInfo(@RequestParam(value="curator") String msoKey, 
										    @RequestParam(value="delimited", required=false) String delimited) {		
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findByKey(msoKey);
		String[] ori = {mso.getName(), mso.getIntro(), mso.getImageUrl()};
		return PlayerLib.getTabDelimitedStr(ori);
	}
	
	/**
	 * Get program script based on program id
	 * 
	 * Examples:
	 *  http://localhost:8888/player/nnscript?program=566
	*/
	@RequestMapping("nnscript")
	public ResponseEntity<String> nnScript(@RequestParam(value="program") long programId)
	{
		ProgramManager programMngr = new ProgramManager();
		MsoProgram program = programMngr.findById(programId);		
		String script = program.getNnScript().getScript().getValue();		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(script, headers, HttpStatus.OK);
	}
	
	/**
	 * Get program information based on query criteria
	 * 
	 * Examples:
	 *  http://localhost:8888/player/programInfo?channel=*&user=aghubmUzdm1zb3INCxIGTm5Vc2VyGKsEDA
	 *  http://localhost:8888/player/programInfo?channel=153,158
	 *  http://localhost:8888/player/programInfo?channel=153
	 *  
	 * @param  channel: could be *, all the programs, e.g. channel=* (user is required for wildcard query)
	 * 			        could be channel Id, e.g. channel=1
	 * 			        could be list of channels, e.g. channels = 34,35,36
	 * 	       user: user account, it is required for wildcard query 
	 * @return Program info.
	 * 		   Each program is separate by carriage return.
	 *         Fields of data in each program is tab delimited.
	 * 		   Fields sequence: channelId, programId, programName, programType, programThumbnailUrl, contentFileUrl
	 */				
	
	@RequestMapping("programInfo")	
	public ResponseEntity<String> programInfo(@RequestParam(value="channel") String channelIds,
									        @RequestParam(value="user", required = false) String userKey,
									        HttpServletRequest req) {
		ProgramManager programMngr = new ProgramManager();
		String[] chStrSplit = channelIds.split(",");
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		if (channelIds.equals("*")) {
			NnUserManager userService = new NnUserManager();
			SubscriptionManager sService = new SubscriptionManager();
			NnUser user = userService.findByKey(userKey);
			programs = sService.findSubscribedPrograms(user); 			
		} else if (chStrSplit.length > 1) {			
			programs = programMngr.findByChannelIdsAndIsPublic(channelIds, true);
		} else {
			long chId = Integer.parseInt(channelIds);
			programs = programMngr.findByChannelIdAndIsPublic(chId, true);
		}	
		String output = "";		
		for (MsoProgram p : programs) {
			String file = p.getWebMFileUrl();
			System.out.println("hostname=" + req.getLocalAddr() + ";" + req.getLocalPort() + ";" + req.getRequestURI());
			
			if (p.getType().equals(MsoProgram.TYPE_SLIDESHOW)) {
				file = "/player/nnscript?program=" + p.getId();
			}
			//String[] ori = {String.valueOf(p.getChannelId()), String.valueOf(p.getKey().getId()), p.getName(), p.getType(), p.getImageUrl(), file};
			
			String[] ori = {String.valueOf(p.getChannelId()), String.valueOf(p.getKey().getId()), p.getName(), p.getType(), p.getImageUrl(), file};
			output = output + PlayerLib.getTabDelimitedStr(ori);
			output = output + "\n";
		}		
		//return output;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}
}
