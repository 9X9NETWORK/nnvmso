package com.nncloudtv.lib;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.text.StrTokenizer;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class NnNetUtil {
	
	protected final static Logger log = Logger.getLogger(NnNetUtil.class.getName());

	public static void logUrl(HttpServletRequest req) {
		String url = req.getRequestURL().toString();		
		String queryStr = req.getQueryString();		
		if (queryStr != null && !queryStr.equals("null"))
			queryStr = "?" + queryStr;
		else 
			queryStr = "";
		url = url + queryStr;
		log.info(url);
	}
	
	public static ResponseEntity<String> textReturn(String output) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/plain;charset=utf-8"));
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);		
	}	

	public static ResponseEntity<String> htmlReturn(String output) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf("text/html;charset=utf-8"));			                                                    
		return new ResponseEntity<String>(output, headers, HttpStatus.OK);
	}	
	
	public static String getUrlRoot(HttpServletRequest req) {
		String url = req.getRequestURL().toString();
	    Pattern p = Pattern.compile("(^http://.*?)/(.*)");	    	    
	    Matcher m = p.matcher(url);
	    String host = "";
	    if (m.find()) {
	    	host = m.group(1);
	    }
		return host;
	}

	public static String getIp(HttpServletRequest req) {
        String ip;
        boolean found = false;
        if ((ip = req.getHeader("x-forwarded-for")) != null) {
          StrTokenizer tokenizer = new StrTokenizer(ip, ",");
          while (tokenizer.hasNext()) {
            ip = tokenizer.nextToken().trim();
            if (isIPv4Valid(ip) && !isIPv4Private(ip)) {
              found = true;
              break;
            }
          }
        }
        if (!found) {
          ip = req.getRemoteAddr();
        }
        return ip;		
	}
	
    public static boolean isIPv4Private(String ip) {
        long longIp = ipV4ToLong(ip);
        return (longIp >= ipV4ToLong("10.0.0.0") && longIp <= ipV4ToLong("10.255.255.255")) ||
            (longIp >= ipV4ToLong("172.16.0.0") && longIp <= ipV4ToLong("172.31.255.255")) ||
            longIp >= ipV4ToLong("192.168.0.0") && longIp <= ipV4ToLong("192.168.255.255");
      }    
    
    public static long ipV4ToLong(String ip) {
        String[] octets = ip.split("\\.");
        return (Long.parseLong(octets[0]) << 24) + (Integer.parseInt(octets[1]) << 16) +
            (Integer.parseInt(octets[2]) << 8) + Integer.parseInt(octets[3]);
    }    
    
    public static boolean isIPv4Valid(String ip) {
    	String _255 = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    	Pattern pattern = Pattern.compile("^(?:" + _255 + "\\.){3}" + _255 + "$");
        return pattern.matcher(ip).matches();
    }
	
    public static void urlGet (String urlStr) {
		URL url;
		try {
			url = new URL(urlStr);
	        HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
		    connection.setDoOutput(true);
			connection.setRequestMethod("GET");
	        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {	        	
	        	log.info("response not ok!" + connection.getResponseCode());
	        }			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}    	
    }
    
	public static void urlPostWithJson(String urlStr, Object obj) {
		log.info("post to " + urlStr);
        URL url;
		try {
			url = new URL(urlStr);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.writeValue(writer, obj);
	        log.info("url fetch-json:" + mapper.writeValueAsString(obj));	        
	        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {	        	
	        	log.info("response not ok!" + connection.getResponseCode());
	        }
	        writer.close();	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
