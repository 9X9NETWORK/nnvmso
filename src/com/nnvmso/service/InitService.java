package com.nnvmso.service;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Text;
import com.nnvmso.lib.NnLib;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.Player;

@Service
public class InitService {
	public void init() {		
		MsoManager msoMngr = new MsoManager();
		NnUserManager userMngr = new NnUserManager();
		//two msos
		Mso a = new Mso("a@a.com", "a");
		a.setPassword("foobie");
		msoMngr.create(a);
		Mso aws = new Mso("aws@9x9.com", "aws");
		aws.setPassword("foobie");		
		msoMngr.create(aws);
		//a user
		NnUser user = new NnUser("u@u.com");
		user.setPassword("foobie");
		userMngr.create(user, a);
		//a player
		Player player = new Player();
		player.setCode(new Text("<h>hello</h>"));
		PlayerManager playerService = new PlayerManager();
		playerService.create(player, a);
		//a aws channel for testing
		MsoChannel channel = new MsoChannel("AWS");
		channel.setImageUrl("/WEB-INF/../images/thumb_noImage.jpg");
		ChannelManager channelMngr = new ChannelManager();
		channelMngr.create(channel, aws);
	}
}
