package com.nnvmso.model;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.MsoChannel;
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
    	MsoProgram p0 = new MsoProgram("p0");
    	pm.makePersistent(p0);
    	pm.close();
    	
    	//verify
    	pm = PMF.get().getPersistenceManager();
    	MsoProgram p1 = pm.getObjectById(MsoProgram.class, p0.getKey());
    	assertEquals(MsoProgram.STATUS_PROCESSING, p1.getStatus());
    	p1.setMpeg4FileUrl("http://mpeg4");
    	assertEquals(MsoProgram.STATUS_PROCESSING, p1.getStatus());
    	p1.setWebMFileUrl("http://webm");
    	assertEquals(MsoProgram.STATUS_OK, p1.getStatus());
    	p1.setErrorCode("TRANSCODING-ERR");
    	assertEquals(MsoProgram.STATUS_ERROR, p1.getStatus());
    	pm.close();
	}
	
}
