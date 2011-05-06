package test.com.nnvmso.service;

import static org.junit.Assert.*;

import org.junit.Test;

import com.nnvmso.service.InitService;
import com.nnvmso.service.MsoChannelManager;

public class InitServiceTest {

	@Test
	public void testGetMso3OwnedChannels() {
		InitService initService = new InitService();
		MsoChannelManager channelMngr = new MsoChannelManager();
		String[] urls = initService.getMso3OwnedChannels();
		for (String url : urls) {
			assertNotNull(channelMngr.verifyUrl(url));
		}
	}

}
