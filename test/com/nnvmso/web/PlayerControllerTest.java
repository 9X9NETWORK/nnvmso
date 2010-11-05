package com.nnvmso.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.nnvmso.lib.NnLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.lib.PlayerLib;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.ChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.NnUserManager;


public class PlayerControllerTest {
	private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	
    private MsoManager msoMngr = new MsoManager();
    private ChannelManager channelMngr = new ChannelManager();
    private NnUserManager userMngr = new NnUserManager();
    private Mso mso = new Mso("a@a.com", "a");
    private MsoChannel c1 = new MsoChannel("channel1");
    private MsoChannel c2 = new MsoChannel("channel2");
    private MsoChannel c3 = new MsoChannel("channel3");
    private NnUser user = new NnUser("u@u.com");
    
    
	@Before
	public void setUp() throws Exception {
        helper.setUp();	
        //mso
		mso.setName("name");
		mso.setIntro("intro");
		mso.setImageUrl("imageUrl");
		mso.setPassword("password");
		msoMngr.create(mso);
		//channel
		c1.setSeq((short)1);
		c1.setImageUrl("image1");
		c2.setSeq((short)2);
		c2.setImageUrl("image2");	
		c3.setSeq((short)3);
		c3.setImageUrl("image3");	
		channelMngr.create(mso, c1);
		channelMngr.create(mso, c2);		
		channelMngr.create(mso, c3);
		//!!! allowing isPublic setting outside of the create
		c1.setPublic(true);
		c2.setPublic(true);
		channelMngr.save(c1);
		channelMngr.save(c2);
		
		//user
		user.setPassword("password");		
		//!!!!!!not passing the mso key
		user.setMsoKeyStr(NnLib.getKeyStr(mso.getKey()));
		userMngr.create(user);
		
		//program
	}

	@After
	public void tearDown() throws Exception {
        helper.setUp();		
	}

	@Test
	public void testCuratorInfo() {
		PlayerController controller = new PlayerController();
		String result = controller.curatorInfo(NnLib.getKeyStr(mso.getKey()), "");
		assertEquals(result, "name\tintro\timageUrl");
	}
	
	@Test
	public void testChannelLineup() {
		//should not retrieve offair channel
		PlayerController controller = new PlayerController();
		ResponseEntity<String> result = controller.channelLineup(user.getEmail());
		String expected1 = "1\t" + c1.getId() + "\t" + c1.getName() + "\t" + "image1\n";
		expected1 = expected1 + "2\t" + c2.getId() + "\t" + c2.getName() + "\t" + "image2\n";
		assertEquals(expected1, result.getBody());
		
		//after c3 is public, channelLineup should retrieve all the channels
		c3.setPublic(true);
		channelMngr.save(c3);
		String expected2 = "1\t" + c1.getId() + "\t" + c1.getName() + "\t" + "image1\n";
		expected2 = expected2 + "2\t" + c2.getId() + "\t" + c2.getName() + "\t" + "image2\n";		
		expected2 = expected2 + "3\t" + c3.getId() + "\t" + c3.getName() + "\t" + "image3\n";
		result = controller.channelLineup(user.getEmail());
		assertEquals(expected2, result.getBody());
		
		//if there is no programs under a channel, then not returning it?
	}
	
//	 *  http://localhost:8888/player/programInfo?channel=*&user=aghubmUzdm1zb3INCxIGTm5Vc2VyGKsEDA
//	 *  http://localhost:8888/player/programInfo?channel=153,158
//	 *  http://localhost:8888/player/programInfo?channel=153	
	@Test
	public void testProgramInfo() {
		
		
	}
	
}
