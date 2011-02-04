package com.nnvmso.lib;

import java.util.Collections;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

public class CacheFactory {
	private static Cache cache = null;
	
	public static Cache get() {
	    try {
	        cache = CacheManager.getInstance().getCacheFactory().createCache(
	            Collections.emptyMap());
	    } catch (CacheException e) {}	      		
		
		return cache;
	}	
	
}
