package com.nnvmso.model;


import static org.junit.Assert.*;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.MsoProgram;

public class ProgramTest {
	private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Before
	public void setUp() throws Exception {
	    helper.setUp();
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}

	@Test
	public void testStatus() {
		//prepare data
		PersistenceManager pm = PMF.get().getPersistenceManager();
    	MsoProgram video = new MsoProgram("video");
    	video.setType(MsoProgram.TYPE_VIDEO);
    	MsoProgram slideshow = new MsoProgram("slideshow");
    	slideshow.setType(MsoProgram.TYPE_SLIDESHOW);    	
    	pm.makePersistentAll(video, slideshow);
    	pm.close();
    	
    	//verify video    	
    	pm = PMF.get().getPersistenceManager();
    	MsoProgram vVideo = pm.getObjectById(MsoProgram.class, video.getKey());    	    	    	
    	assertEquals(MsoProgram.STATUS_PROCESSING, vVideo.getStatus());
    	vVideo.setMpeg4FileUrl("http:X//mpeg4");
    	assertEquals(MsoProgram.STATUS_PROCESSING, vVideo.getStatus());
    	vVideo.setWebMFileUrl("http://webm");
    	assertEquals(MsoProgram.STATUS_OK, vVideo.getStatus());
    	vVideo.setErrorCode("TRANSCODING-ERR");
    	assertEquals(MsoProgram.STATUS_ERROR, vVideo.getStatus());
    	
    	//verify slideshow    	
    	MsoProgram vSlideshow = pm.getObjectById(MsoProgram.class, slideshow.getKey());
    	assertEquals(MsoProgram.STATUS_PROCESSING, vSlideshow.getStatus());
    	ProgramScript s = new ProgramScript();
    	vSlideshow.setNnScript(s);
    	s.setProgram(vSlideshow);
    	assertEquals(MsoProgram.STATUS_OK, vSlideshow.getStatus());
    	pm.close();
	}
	
}
