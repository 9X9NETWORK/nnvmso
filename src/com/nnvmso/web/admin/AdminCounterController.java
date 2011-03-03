package com.nnvmso.web.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.dao.ShardedCounter;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.service.CounterFactory;

@Controller
@RequestMapping("admin/counter")
public class AdminCounterController {
	
	/**
	 * currently there are 9x9BrandInfo and 5fBrandInfo 
	 */
	@RequestMapping(value="create")
	public ResponseEntity<String> create(@RequestParam(required=false)String name) {
	    CounterFactory factory = new CounterFactory();
	    factory.getOrCreateCounter(name);
		return NnNetUtil.textReturn("create");
	}
		
	@RequestMapping(value="addShard")
	public ResponseEntity<String> addShard(@RequestParam(required=false)String name) {
	    CounterFactory factory = new CounterFactory();
	    ShardedCounter counter = factory.getOrCreateCounter(name);
	    counter.addShard();					
		return NnNetUtil.textReturn("addShard");
	}
	
	@RequestMapping(value="increment")
	public ResponseEntity<String> increment(@RequestParam(required=false)String name) {
		CounterFactory factory = new CounterFactory();
		ShardedCounter counter = factory.getOrCreateCounter(name);
		counter.increment();
		return NnNetUtil.textReturn("counterIncrement");
	}
	
	@RequestMapping(value="get")
	public ResponseEntity<String> counter(@RequestParam(required=false)String name) {
	    CounterFactory factory = new CounterFactory();
	    ShardedCounter counter = factory.getCounter(name);
		return NnNetUtil.textReturn(String.valueOf(counter.getCount()));
	}	
	
	
	
	
}
