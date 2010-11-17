package com.nnvmso.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.geronimo.mail.util.Base64Encoder;

import com.nnvmso.json.AwsS3Post;

public class AwsLib {
	public static Properties getAwsCredentials() {
		Properties pro = new Properties();
		try {
			pro.load(AwsLib.class.getClassLoader().getResourceAsStream("AwsCredentials.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return pro; 		
	}

	public static String getPolicy(AwsS3Post s3) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");  
	    df.setTimeZone(TimeZone.getTimeZone("GMT"));    	    	    
	    Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.HOUR, 24); //The policy expires in an hour	    
	    String expirationDate = df.format(cal.getTime());	    
    	String policyDoc =
    	"{\"expiration\": \"" + expirationDate + "\"," + 
		  "\"conditions\": [ " +
		    "{\"bucket\": \"" + s3.getBucket_name() + "\"}, " +  
		    "{\"acl\": \"" + s3.getAcl() + "\"}, " + 
		    "{\"success_action_redirect\": \"" + s3.getSuccess_action_redirect() + "\"}, " +
		    "[\"starts-with\", \"$key\", \"" +  s3.getKey() + "\"]," +
		    "[\"starts-with\", \"$filename\", \""  + "\"], " + //to work with swfupload, has no effect
		    "[\"starts-with\", \"$Content-Type\", \"" + s3.getContent_type() + "\"], " +
		    "[\"starts-with\", \"$x-amz-meta-filename\", \"" + s3.getX_amz_meta_filename() + "\"], " +
		    "[\"starts-with\", \"$x-amz-meta-token\", \"" + s3.getX_amz_meta_token() + "\"], " +
		    "[\"starts-with\", \"$x-amz-meta-creatDate\", \"" + s3.getX_amz_meta_creatDate() + "\"], " +
		    "[\"content-length-range\", 0, 5368709120]" +		    
		  "]\r" + 
		"}";

    	System.out.println(policyDoc);    	
    	Base64Encoder encoder = new Base64Encoder();
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();    	
    	try {
			encoder.encode(policyDoc.getBytes(), 0, policyDoc.getBytes().length, stream);
		} catch (IOException e) {
			e.printStackTrace();
		} 
    	String policy = stream.toString().replaceAll("\n", "").replaceAll("\r", "");
    	return policy;
	}
	
	public static String getSignature(String policy) {
    	Base64Encoder encoder = new Base64Encoder();
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();    	
    	String signature = null;
    	try {
			encoder.encode(policy.getBytes(), 0, policy.getBytes().length, stream);
	    	//String policy = stream.toString().replaceAll("\n", "").replaceAll("\r", "");
	    	String awsSecretKey = AwsLib.getAwsCredentials().getProperty("secretKey");
			Mac hmac = Mac.getInstance("HmacSHA1");			
			hmac.init(new SecretKeySpec(awsSecretKey.getBytes("UTF-8"), "HmacSHA1"));
			byte[] reset = hmac.doFinal(policy.getBytes("UTF-8"));
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		encoder.encode(reset, 0, reset.length, baos);
    		signature = baos.toString().replaceAll("\r", "");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		return signature;
	}
}
