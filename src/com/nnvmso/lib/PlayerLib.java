package com.nnvmso.lib;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class PlayerLib {
	public static String getTabDelimitedStr(String[] ori) {
		String delimiter = "\t";
		StringBuilder result = new StringBuilder();
		if (ori.length > 0) {
			result.append(ori[0]);
		    for (int i=1; i<ori.length; i++) {
		       result.append(delimiter);
		       result.append(ori[i]);
		    }
		}
		return result.toString();
	}

	public static ResponseEntity<String> outputReturn(String output) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}	
}
