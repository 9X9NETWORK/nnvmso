package com.nncloudtv.lib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.OperationTimeoutException;

public class CacheFactory {
	
	protected static final Logger log = Logger.getLogger(CacheFactory.class.getName());
	
	private static MemcachedClient cache = null;
	public static int EXP_DEFAULT = 2592000;
	public static int PORT_DEFAULT = 11211;
	public static String ERROR = "ERROR";
	public static boolean isRunning = true;
	
	public static MemcachedClient getClient() {		
		try {
			//TODO load it from memcache.properties, but make sure the property input stream is closed
			cache = new MemcachedClient(new InetSocketAddress("localhost", CacheFactory.PORT_DEFAULT));			
		} catch (IOException e) {
		   log.severe("memcache io exception");
		   cache = null;
		} catch (Exception e) {
		   log.severe("memcache exception");
		   cache = null;
		}		
		return cache;
	}	

	public static Object get(String key) {		
		MemcachedClient cache = CacheFactory.getClient();
		CacheFactory.isRunning = false;
		Object obj = null;
		try {
			obj = cache.get(key);
			CacheFactory.isRunning = true;
		} catch (OperationTimeoutException e) {
			log.severe("get OperationTimeoutException");
		} catch (Exception e) {
			log.severe("get Exception");
			e.printStackTrace();
		} finally {
			cache.shutdown();			
		}
		return obj;
	}	

	public static Object set(String key, Object obj) {		
		MemcachedClient cache = CacheFactory.getClient();
		CacheFactory.isRunning = false;
		Object myObj = null;
		try {
			cache.set(key, CacheFactory.EXP_DEFAULT, obj);
			myObj = cache.get(key);
			CacheFactory.isRunning = true;
		} catch (OperationTimeoutException e) {
			log.severe("set OperationTimeoutException");
		} catch (Exception e) {
			log.severe("set Exception");
			e.printStackTrace();
		} finally {
			cache.shutdown();
		}
		return myObj;
	}	

	public static Object delete(String key) {		
		MemcachedClient cache = CacheFactory.getClient();
		CacheFactory.isRunning = false;
		Object obj = null;
		try {
			cache.delete(key).get();
			CacheFactory.isRunning = true;
		} catch (OperationTimeoutException e) {
			log.severe("get OperationTimeoutException");
		} catch (Exception e) {
			log.severe("get Exception");
			e.printStackTrace();
		} finally {
			cache.shutdown();			
		}
		return obj;
	}	
	
}
