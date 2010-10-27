package com.nnvmso.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
import com.nnvmso.service.MsoService;


public class PlayerControllerTest {
	private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());		
    private MsoService service;	
    private Mso mso = new Mso("a@a.com", "a");
    
	@Before
	public void setUp() throws Exception {
        helper.setUp();
		service = new MsoService();	
		mso.setName("name");
		mso.setIntro("intro");
		mso.setImageUrl("imageUrl");
		mso.setPassword("password");
		service.create(mso);				
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
	public void testProgramInfo() {
		
	}
	
}
