package com.nnvmso.lib;

import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Service;

public class JqgridHelper {
	
	protected static final Logger log = Logger.getLogger(JqgridHelper.class.getName());
	
	public static Map<String, Object> composeJqgridResponse(int page, int total, int records, List<Map> rows) {
		
		Map<String, Object> response = new HashMap<String, Object>();
		
		response.put("page", page);
		response.put("total", total);
		response.put("records", records);
		response.put("rows", rows);
		
		return response;
	}
	
}
