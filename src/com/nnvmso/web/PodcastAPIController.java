package com.nnvmso.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nnvmso.json.PodcastChannel;
import com.nnvmso.json.PodcastKeys;
import com.nnvmso.json.PodcastProgram;
import com.nnvmso.lib.NnLib;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.ChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.ProgramManager;

@Controller
@RequestMapping("podcastAPI")
public class PodcastAPIController {

	public static String TRANSCODING_SERVER = "http://awsapi.9x9cloud.tv/api/podpares.php";
	private ChannelManager channelMngr;
	private ProgramManager programMngr;
	private MsoManager msoMngr;
	
	@Autowired
	public PodcastAPIController(ChannelManager channelMngr, MsoManager msoMngr, ProgramManager programMngr) {
		this.channelMngr = channelMngr;
		this.programMngr = programMngr;
		this.msoMngr = msoMngr;
	}
	
	@RequestMapping("itemUpdate")
	public String itemUpdate(@RequestBody PodcastProgram podcastProgram) {
		if (podcastProgram.getAction().equals(PodcastProgram.ACTION_UPDATE_ITEM)) {
			System.out.println("update item:" + podcastProgram.getKey());
			PodcastKeys keys = new PodcastKeys();
			MsoProgram p = programMngr.createViaPodcast(podcastProgram);
			keys.setKey(podcastProgram.getKey());
			keys.setItemKey(NnLib.getKeyStr(p.getKey()));
			NnLib.urlFetch(TRANSCODING_SERVER, keys);
		} else {
			System.out.println("update enclosure:" + podcastProgram.getKey() + ";" + podcastProgram.getItem().getItemKey());
			programMngr.saveViaPodcast(podcastProgram);
		}
		System.out.println("back to itemUpdate");
		return "";
	}
		
	@RequestMapping("channelUpdate")
	public void channelUpdate(@RequestBody PodcastChannel podcast) {
		System.out.println("update:" + podcast.getKey());
		MsoChannel channel = channelMngr.findByKey(podcast.getKey());
		channelMngr.saveViaPodcast(channel, podcast);
		System.out.println("back to channelupdate");
	}

	//dysfunction
	@RequestMapping("batch_create")
	public String batchCreate(@RequestBody PodcastChannel podcast) {
		Mso mso = msoMngr.findByEmail("default_mso@9x9.com");
		MsoChannel channel = channelMngr.createViaPodcast(podcast, mso);		
		//programMngr.saveAllViaPodcast(podcast.getItems(), channel);		
		return "";
	}
	

}
