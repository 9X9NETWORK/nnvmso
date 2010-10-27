package com.nnvmso.web;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.DebugLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.lib.PlayerLib;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.MsoService;
import com.nnvmso.service.NnUserService;
import com.nnvmso.service.ProgramService;
import com.nnvmso.service.SubscriptionService;

@Controller
@RequestMapping("player")
public class PlayerController {

	@RequestMapping("embed")
	public String embeded() {
		return ("/player/embed");
	}
	
	/**
	 * Get a user's subscribed channels
	 * 
	 * Example: http://localhost:8888/player/channelLineup?user=aghubmUzdm1zb3INCxIGTm5Vc2VyGKsEDA
	 *  
	 * @param  user: NnUserKey
	 * @return Channel info. 
	 * 		   Fields are tab delimited.           
	 * 		   Fields sequence: sequence, ChannelKey, ChannelName, ChannelThumbnailUrl 
	 */				
	@RequestMapping(value="channelLineup")
	public ResponseEntity<String> channelLineup(@RequestParam(value="user") String key) {
		SubscriptionService sService = new SubscriptionService();
		NnUserService userService = new NnUserService();
		NnUser user = userService.findByKey(key);
		sService.subscribe(user); 
		List<MsoChannel> channels = sService.findSubscribedChannels(user);
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
	public @ResponseBody String curatorInfo(@RequestParam(value="curator") String key, 
										    @RequestParam(value="delimited", required=false) String delimited) {		
		MsoService msoService = new MsoService();
		Mso mso = msoService.findByKey(key);
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
	public @ResponseBody String nnScript(@RequestParam(value="program") long programId)
	{
		//!!!!!! fix detached child in programService
		PersistenceManager pm = PMF.get().getPersistenceManager();
		MsoProgram program = pm.getObjectById(MsoProgram.class, programId);		
		String script = program.getScript().getScript().getValue();
		System.out.println(DebugLib.OUT + script);
		return script;
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
	 * 	       id: userId, it is required for wildcard query 
	 * @return Program info.
	 * 		   Each program is separate by carriage return.
	 *         Fields of data in each program is tab delimited.
	 * 		   Fields sequence: programKey, programName, programType, programThumbnailUrl, contentFileUrl
	 */		
	@RequestMapping("programInfo")	
	public ResponseEntity<String> programInfo(@RequestParam(value="channel") String channelIds,
									        @RequestParam(value="user", required = false) String userKey) {
		ProgramService programService = new ProgramService();
		String[] chStrSplit = channelIds.split(",");
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		//http://localhost:8888/player/programInfo?channel=*&id=aghubmUzdm1zb3INCxIGTm5Vc2VyGKsEDA
		if (channelIds.equals("*")) {
			NnUserService userService = new NnUserService();
			SubscriptionService sService = new SubscriptionService();
			NnUser user = userService.findByKey(userKey);
			programs = sService.findSubscribedPrograms(user); 			
		} else if (chStrSplit.length > 1) {			
			//http://localhost:8888/player/programInfo?channel=153,158
			programs = programService.findByChannelIdsAndIsPublic(channelIds, true);
		} else {
			//http://localhost:8888/player/programInfo?channel=153
			long chId = Integer.parseInt(channelIds);
			programs = programService.findByChannelIdAndIsPublic(chId, true);
		}	
		String output = "";		
		for (MsoProgram p : programs) {
			String[] ori = {String.valueOf(p.getChannelId()), String.valueOf(p.getKey().getId()), p.getName(), p.getType(), p.getImageUrl(), p.getWebMFileUrl()};				
			output = output + PlayerLib.getTabDelimitedStr(ori);
			output = output + "\n";
		}		
		//return output;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
		
	}
}
