package com.nnvmso.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.PodcastChannel;
import com.nnvmso.service.ChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.ProgramManager;

@Controller
@RequestMapping("podcast")
public class PodcastController {

	private ChannelManager channelMngr;
	private ProgramManager programMngr;
	private MsoManager msoMngr;
	
	@Autowired
	public PodcastController(ChannelManager channelMngr, MsoManager msoMngr, ProgramManager programMngr) {
		this.channelMngr = channelMngr;
		this.programMngr = programMngr;
		this.msoMngr = msoMngr;
	}
	
	@RequestMapping("update")
	public String update(@RequestBody PodcastChannel podcast) {
		System.out.println("update:" + podcast.getKey());
		MsoChannel channel = channelMngr.findByKey(podcast.getKey());
		channel = channelMngr.saveViaPodcast(channel, podcast);
		programMngr.saveAllViaPodcast(podcast.getItems(), channel);
		return ""; 
	}

	@RequestMapping("batch_create")
	public String batchCreate(@RequestBody PodcastChannel podcast) {
		Mso mso = msoMngr.findByEmail("default_mso@9x9.com");
		MsoChannel channel = channelMngr.createViaPodcast(podcast, mso);		
		programMngr.saveAllViaPodcast(podcast.getItems(), channel);		
		return "";
	}
	

}
