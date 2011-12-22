package com.nnvmso.lib;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class NnStringUtil {
	
	protected static final Logger logger = Logger.getLogger(NnStringUtil.class.getName());
	
	public static final int MAX_JDO_STRING_LENGTH = 500;
	
	/**
	 * To to truncate an UTF-8 string to fit proper bytes
	 *
	 * source: http://stackoverflow.com/questions/119328/how-do-i-truncate-a-java-string-to-fit-in-a-given-number-of-bytes-once-utf-8-enc
	 * @throws UnsupportedEncodingException 
	 */
	public static String truncateUTF8(String str, int maxBytes) {
		if (str == null) { return null; }
		Charset utf8 = Charset.forName("UTF-8");
		int totalBytes = str.getBytes(utf8).length;
		if (totalBytes <= maxBytes) { return str; }
		for (int i = 0, b = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			
			// ranges from http://en.wikipedia.org/wiki/UTF-8
			int skip = 0;
			int more;
			if (c <= 0x007f) {
				more = 1;
			}
			else if (c <= 0x07FF) {
				more = 2;
			} else if (c <= 0xd7ff) {
				more = 3;
			} else if (c <= 0xDFFF) {
				// surrogate area, consume next char as well
				more = 4;
				skip = 1;
			} else {
				more = 3;
			}
			
			if (b + more > maxBytes) {
				String truncStr = str.substring(0, i);
				int truncBytes = truncStr.getBytes(utf8).length;
				logger.info("truncate string length " + str.length() + " (" + totalBytes + " bytes) --> " + i + " (" + truncBytes + " bytes)");
				return truncStr;
			}
			b += more;
			i += skip;
		}
		return str;
	}
	
	/**
	 * Truncate string to fit GAE JDO limitation
	 * @throws UnsupportedEncodingException 
	 */
	public static String truncateUTF8(String str) {
		return truncateUTF8(str, MAX_JDO_STRING_LENGTH);
	}
	
	public static String capitalize(String str) {
		if (str == null) {return null;}
		str = str.toLowerCase();
		String firstLetter = str.substring(0,1);
        String remainder   = str.substring(1);
        String capitalized = firstLetter.toUpperCase() + remainder.toLowerCase();
        return capitalized;
	}

	public static String getDateString(Date date) {
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
	
	public static String escapedQuote(String str) {
		
		return "'" + str.replaceAll("'", "''") + "'";
	}
	
	public static String bytesToHex(byte[] src){
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
	
	
	/**
	 * Translate string to html safe characters
	 * 
	 * @param str
	 * @return
	 */
	public static String htmlSafeChars(String str) {
		if (str == null)
			return null;
		return str.replaceAll("\n", " ")
		          .replaceAll("\t", " ")
		          .replaceAll("&", "&amp;")
		          .replaceAll("<", "&lt;")
		          .replaceAll(">", "&gt;")
		          .replaceAll("\"", "&quot;");
	}
	
	public static String revertHtml(String str) {
		if (str == null) return null;
		return str.replaceAll("&quot;", "\"")
		           .replaceAll("&gt;", ">")
		           .replaceAll("&lt;", "<")
		           .replaceAll("&amp;", "&");
	}
	
	public static String htmlSafeAndTrucated(String str) {
		for (int i = MAX_JDO_STRING_LENGTH; i > 0; i--) {
			String truncated = truncateUTF8(str, i);
			String htmlSafe = htmlSafeChars(truncated);
			Integer bytelen = htmlSafe.getBytes(Charset.forName("UTF-8")).length;
			if (bytelen <= MAX_JDO_STRING_LENGTH) {
				logger.info("length in bytes: " + bytelen);
				return htmlSafe;
			}
		}
		return null;
	}
}
