package com.nnvmso.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.APILib;
import com.nnvmso.lib.NnLib;
import com.nnvmso.model.IPG;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.ChannelManager;
import com.nnvmso.service.IPGManager;
import com.nnvmso.service.PlayerAPI;

@Controller
@RequestMapping("ipg")
public class IPGAPIController {
	private final IPGManager ipgMngr;
	private final ChannelManager channelMngr;
		
	@Autowired
	public IPGAPIController(IPGManager ipgMngr, ChannelManager channelMngr) {
		this.ipgMngr = ipgMngr;
		this.channelMngr = channelMngr;
	}
		
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLib.logException(e);
		return "error/exception";				
	}	
	
	@RequestMapping(value="saveIPG")
	public ResponseEntity<String> saveIPG(@RequestParam(value="user")String user) {
		String output = "";
		try {
			String[] ori = {String.valueOf(PlayerAPI.CODE_ERROR), PlayerAPI.PLAYER_CHANNEL_OR_USER_UNEXISTED};
			IPG ipg = ipgMngr.saveCurrentSnapshot(user);
			if (ipg != null) {
				ori[0] = String.valueOf(PlayerAPI.CODE_SUCCESS);
				ori[1] = String.valueOf(ipg.getKey().getId()); 			       
			}
			output = APILib.getTabDelimitedStr(ori);
		} catch (Exception e){
			String[] ori = {String.valueOf(PlayerAPI.CODE_FATAL), PlayerAPI.PLAYER_CODE_FATAL};
			output = APILib.getTabDelimitedStr(ori);
		}
		return APILib.outputReturn(output);
	}
	
	@RequestMapping(value="loadIPG")
	public ResponseEntity<String> loadIPG(@RequestParam(value="ipg")String id) {
		String output = "";
		try {
			IPG ipg = ipgMngr.findById(id);
			if (ipg != null) {
				List<MsoChannel> channels = channelMngr.findByKeys(ipg.getChannelKeys());
				for (int i=0; i<ipg.getGrids().size(); i++) {
					MsoChannel c = channels.get(i);
					if (c != null) {
						String[] ori = {
							ipg.getGrids().get(i).toString(), Long.toString(c.getKey().getId()),
							c.getName(), c.getImageUrl(), Integer.toString(c.getProgramCount())};
						output = output + APILib.getTabDelimitedStr(ori) + "\n";
					}
				}
			} else {
				String[] ori = {String.valueOf(PlayerAPI.CODE_ERROR), PlayerAPI.PLAYER_CODE_ERROR};
				output = APILib.getTabDelimitedStr(ori);
			}
		} catch (Exception e) {
			String[] ori = {String.valueOf(PlayerAPI.CODE_FATAL), PlayerAPI.PLAYER_CODE_FATAL};
			output = APILib.getTabDelimitedStr(ori);			
		}
		return APILib.outputReturn(output);
	}
		
}
