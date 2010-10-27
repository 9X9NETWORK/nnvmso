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

public class ChannelTest {
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
	public void testProgramSeq() {
		//prepare data
		PersistenceManager pm = PMF.get().getPersistenceManager();
    	MsoChannel channel = new MsoChannel("c1");
    	pm.makePersistent(channel);
    	MsoProgram p0 = new MsoProgram("p0");
    	MsoProgram p1 = new MsoProgram("p1");
    	MsoProgram p2 = new MsoProgram("p2");
    	p0.setChannelKey(channel.getKey());
    	p1.setChannelKey(channel.getKey());
    	p2.setChannelKey(channel.getKey());
    	pm.makePersistentAll(p0, p1, p2);
    	List<Key> seq = new ArrayList<Key>();
    	seq.add(p0.getKey());
    	seq.add(p1.getKey());
    	seq.add(p2.getKey());
    	channel.setProgramSeq(seq);
    	pm.makePersistent(channel);
    	pm.close();		
    	
    	//1 exchange with 2
    	pm = PMF.get().getPersistenceManager();
    	MsoChannel c1 = pm.getObjectById(MsoChannel.class, channel.getKey());
    	List<Key> seq1 = (List<Key>)c1.getProgramSeq();
    	System.out.println("orignial = " + seq1.get(0) + ";" + seq1.get(1) + ";" + seq1.get(2) + ";");
    	Key k1 = seq1.get(1);    	
    	Key k2 = seq1.set(2, k1);
    	seq1.set(1, k2);
    	c1.setProgramSeq(seq1);
    	pm.makePersistent(c1);
    	pm.close();
    	
    	//verify
    	pm = PMF.get().getPersistenceManager();
    	MsoChannel c2 = pm.getObjectById(MsoChannel.class, channel.getKey());
    	List<Key> seq2 = (List<Key>)c2.getProgramSeq();
    	assertEquals(seq2.get(2), p1.getKey());
    	assertEquals(seq2.get(1), p2.getKey());    	
    	pm.close();    	
	}
	
}
