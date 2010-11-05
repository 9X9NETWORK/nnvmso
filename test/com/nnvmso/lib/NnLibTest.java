package com.nnvmso.lib;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class NnLibTest {

	@Test
	public void testUrlRoot() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/main.app");
		request.setLocalAddr("localhost");
		request.setLocalPort(8888);
		assertEquals("http://localhost:8888", NnLib.getUrlRoot(request));
		request.setLocalAddr("localhost");
		request.setLocalPort(80);
		assertEquals("http://localhost", NnLib.getUrlRoot(request));
	}
}
