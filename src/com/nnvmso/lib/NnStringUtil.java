package com.nnvmso.lib;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class NnStringUtil {
	public static String capitalize(String str) {
		str = str.toLowerCase();
		String firstLetter = str.substring(0,1);
        String remainder   = str.substring(1);
        String capitalized = firstLetter.toUpperCase() + remainder.toLowerCase();
        return capitalized;
	}

	public String getDateString(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm a");
        String result = dateFormat.format(date);
        System.out.println("getUpdateDate() : UDate = " + result);
		return result;
	}
		
	public static String getKeyStr(Key key) {
		return KeyFactory.keyToString(key);
	}

	public static String getDelimitedStr(String[] ori) {
		StringBuilder result = new StringBuilder();
		String delimiter = "\t";
		if (ori.length > 0) {
			result.append(ori[0]);
		    for (int i=1; i<ori.length; i++) {
		       result.append(delimiter);
		       result.append(ori[i]);
		    }
		}
		return result.toString();
	}
	
}
